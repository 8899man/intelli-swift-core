package com.fr.swift.jdbc.adaptor;

import com.fr.swift.jdbc.adaptor.ast.expr.SwiftOrderingExpr;
import com.fr.swift.jdbc.adaptor.parser.SwiftExprParser;
import com.fr.swift.jdbc.druid.sql.ast.SQLExpr;
import com.fr.swift.jdbc.druid.sql.ast.SQLOrderBy;
import com.fr.swift.jdbc.druid.sql.ast.SQLOrderingSpecification;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLAggregateExpr;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLAggregateOption;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLAllColumnExpr;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLIdentifierExpr;
import com.fr.swift.jdbc.druid.sql.ast.expr.SQLPropertyExpr;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLExprTableSource;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSelectItem;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.fr.swift.jdbc.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.fr.swift.jdbc.druid.sql.visitor.SQLASTVisitor;
import com.fr.swift.jdbc.druid.sql.visitor.SQLASTVisitorAdapter;
import com.fr.swift.jdbc.druid.util.FnvHash;
import com.fr.swift.query.aggregator.AggregatorType;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.MetricBean;
import com.fr.swift.query.info.bean.element.SortBean;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.post.PostQueryInfoBean;
import com.fr.swift.query.info.bean.post.RowSortQueryInfoBean;
import com.fr.swift.query.info.bean.query.DetailQueryInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.info.bean.type.DimensionType;
import com.fr.swift.query.sort.SortType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lyon on 2018/12/6.
 */
public class QueryASTVisitorAdapter extends SQLASTVisitorAdapter implements QueryInfoBeanParser {

    private QueryInfoBean queryInfoBean;

    // select clause
    @Override
    public boolean visit(SQLSelectQueryBlock x) {
        if (isGroup(x.getSelectList())) {
            GroupASTVisitorAdapter visitor = new GroupASTVisitorAdapter();
            visitor.visit(x);
            this.queryInfoBean = visitor.getQueryInfoBean();
        } else {
            DetailASTVisitorAdapter visitor = new DetailASTVisitorAdapter();
            visitor.visit(x);
            this.queryInfoBean = visitor.getQueryInfoBean();
        }
        return false;
    }

    private static boolean isGroup(List<SQLSelectItem> items) {
        final boolean[] group = {false};
        // 有聚合函数或者from子查询都是分组查询
        SQLASTVisitor visitor = new SQLASTVisitorAdapter() {
            @Override
            public boolean visit(SQLAggregateExpr x) {
                group[0] = true;
                return false;
            }

            @Override
            public boolean visit(SQLSubqueryTableSource x) {
                group[0] = true;
                return false;
            }
        };
        for (SQLSelectItem item : items) {
            item.accept(visitor);
        }
        return group[0];
    }

    @Override
    public QueryInfoBean getQueryInfoBean() {
        return queryInfoBean;
    }

    private static abstract class AbstractASTVisitorAdapter extends SQLASTVisitorAdapter implements QueryInfoBeanParser {

        String tableName;
        List<SortBean> sortBeans;
        FilterInfoBean filterInfoBean;

        void parseWere(SQLExpr where) {
            if (where == null) {
                return;
            }
            WhereASTVisitorAdapter visitor = new WhereASTVisitorAdapter();
            where.accept(visitor);
            filterInfoBean = visitor.getFilterInfoBean();
        }

        @Override
        public boolean visit(SQLExprTableSource x) {
            SQLExpr name = x.getExpr();
            if (name instanceof SQLIdentifierExpr) {
                tableName = ((SQLIdentifierExpr) name).getName();
            } else if (name instanceof SQLPropertyExpr) {
                tableName = ((SQLPropertyExpr) name).getName();
            }
            return false;
        }

        @Override
        public boolean visit(SQLOrderBy x) {
            if (x == null) {
                return false;
            }
            sortBeans = new ArrayList<SortBean>();
            List<SQLSelectOrderByItem> items = x.getItems();
            for (SQLSelectOrderByItem item : items) {
                SQLExpr sort = item.getExpr();
                if (sort instanceof SQLIdentifierExpr) {
                    SortBean sortBean = new SortBean();
                    sortBean.setName(((SQLIdentifierExpr) sort).getName());
                    sortBean.setType(item.getType() == SQLOrderingSpecification.ASC ? SortType.ASC : SortType.DESC);
                    sortBeans.add(sortBean);
                }
            }
            return false;
        }
    }

    private static class DetailASTVisitorAdapter extends AbstractASTVisitorAdapter implements QueryInfoBeanParser {

        private DetailQueryInfoBean bean;

        public DetailASTVisitorAdapter() {
            this.bean = new DetailQueryInfoBean();
        }

        @Override
        public boolean visit(SQLSelectQueryBlock x) {
            visit((SQLExprTableSource) x.getFrom());
            bean.setTableName(tableName);
            // dimensions
            List<DimensionBean> dimensionBeans = new ArrayList<DimensionBean>();
            List<SQLSelectItem> items = x.getSelectList();
            for (SQLSelectItem item : items) {
                SQLExpr name = item.getExpr();
                if (name instanceof SQLIdentifierExpr) {
                    DimensionBean dimensionBean = new DimensionBean();
                    dimensionBean.setAlias(item.getAlias());
                    dimensionBean.setColumn(((SQLIdentifierExpr) name).getName());
                    dimensionBean.setType(DimensionType.DETAIL);
                    dimensionBeans.add(dimensionBean);
                } else if (name instanceof SQLAllColumnExpr) {
                    DimensionBean dimensionBean = new DimensionBean();
                    dimensionBean.setType(DimensionType.DETAIL_ALL_COLUMN);
                    dimensionBeans.add(dimensionBean);
                    break;
                }
            }
            bean.setDimensions(dimensionBeans);
            // where
            parseWere(x.getWhere());
            bean.setFilter(filterInfoBean);
            // order by
            visit(x.getOrderBy());
            bean.setSorts(sortBeans);
            return false;
        }

        @Override
        public QueryInfoBean getQueryInfoBean() {
            return bean;
        }
    }

    private static class GroupASTVisitorAdapter extends AbstractASTVisitorAdapter implements QueryInfoBeanParser {

        private GroupQueryInfoBean bean;

        public GroupASTVisitorAdapter() {
            this.bean = new GroupQueryInfoBean();
        }

        @Override
        public boolean visit(SQLSelectQueryBlock x) {
            visit((SQLExprTableSource) x.getFrom());
            bean.setTableName(tableName);
            // dimensions and metrics
            List<MetricBean> metricBeans = new ArrayList<MetricBean>();
            List<DimensionBean> dimensionBeans = new ArrayList<DimensionBean>();
            List<Long> dimensionHashCodes = new ArrayList<Long>();
            List<SQLSelectItem> items = x.getSelectList();
            for (SQLSelectItem item : items) {
                SQLExpr column = item.getExpr();
                if (column instanceof SQLAggregateExpr) {
                    MetricBean metricBean = new MetricBean();
                    metricBean.setType(getAggType((SQLAggregateExpr) column));
                    metricBean.setAlias(item.getAlias());
                    metricBean.setColumn(getColumnName((SQLAggregateExpr) column));
                    metricBeans.add(metricBean);
                } else if (column instanceof SQLIdentifierExpr) {
                    DimensionBean dimensionBean = new DimensionBean();
                    dimensionBean.setType(DimensionType.GROUP);
                    dimensionBean.setColumn(((SQLIdentifierExpr) column).getName());
                    dimensionBean.setAlias(item.getAlias());
                    SortBean sortBean = new SortBean();
                    sortBean.setName(((SQLIdentifierExpr) column).getName());
                    // group by desc by default
                    sortBean.setType(SortType.DESC);
                    dimensionBean.setSortBean(sortBean);
                    dimensionHashCodes.add(dimensionBean.getAlias() == null ?
                            FnvHash.fnv1a_64_lower(dimensionBean.getColumn()) : FnvHash.fnv1a_64_lower(dimensionBean.getAlias()));
                    dimensionBeans.add(dimensionBean);
                }
            }
            bean.setAggregations(metricBeans);
            bean.setDimensions(dimensionBeans);
            // where
            parseWere(x.getWhere());
            bean.setFilter(filterInfoBean);
            // group by items
            SQLSelectGroupByClause groupByClause = x.getGroupBy();
            List<SQLExpr> dims = groupByClause.getItems();
            for (SQLExpr dim : dims) {
                int index = -1;
                if (dim instanceof SwiftOrderingExpr) {
                    SQLExpr name = ((SwiftOrderingExpr) dim).getExpr();
                    if (name instanceof SQLIdentifierExpr) {
                        index = dimensionHashCodes.indexOf(FnvHash.fnv1a_64_lower(((SQLIdentifierExpr) name).getName()));
                    }
                }
                if (index != -1) {
                    SQLOrderingSpecification sp = ((SwiftOrderingExpr) dim).getType();
                    dimensionBeans.get(index).getSortBean().setType(
                            sp == SQLOrderingSpecification.DESC ? SortType.DESC : SortType.ASC);
                }
            }
            // sorts
            visit(x.getOrderBy());
            RowSortQueryInfoBean rowSort = new RowSortQueryInfoBean();
            rowSort.setSortBeans(sortBeans);
            List<PostQueryInfoBean> postQueryInfoBeans = new ArrayList<PostQueryInfoBean>();
            postQueryInfoBeans.add(rowSort);
            bean.setPostAggregations(postQueryInfoBeans);
            return false;
        }

        private static String getColumnName(SQLAggregateExpr aggregateExpr) {
            List<SQLExpr> params = aggregateExpr.getArguments();
            if (params != null && !params.isEmpty()) {
                if (params.get(0) instanceof SQLIdentifierExpr) {
                    return ((SQLIdentifierExpr) params.get(0)).getName();
                }
            }
            return null;
        }

        private static AggregatorType getAggType(SQLAggregateExpr aggregateExpr) {
            String funcName = aggregateExpr.getMethodName();
            int index = Arrays.binarySearch(SwiftExprParser.AGGREGATE_FUNCTIONS_CODES, FnvHash.fnv1a_64_lower(funcName));
            if (index < 0) {
                return AggregatorType.DUMMY;
            }
            SwiftExprParser.AggregationFunction fn = SwiftExprParser.FNS[index];
            switch (fn) {
                case COUNT: {
                    if (aggregateExpr.getOption() == SQLAggregateOption.DISTINCT) {
                        return AggregatorType.DISTINCT;
                    } else {
                        return AggregatorType.COUNT;
                    }
                }
                case AVG:
                    return AggregatorType.AVERAGE;
                case MAX:
                    return AggregatorType.MAX;
                case MIN:
                    return AggregatorType.MIN;
                case SUM:
                    return AggregatorType.SUM;
                case STD_DEV:
                    return AggregatorType.STANDARD_DEVIATION;
                case HLL_DISTINCT:
                    return AggregatorType.HLL_DISTINCT;
                default:
                    return AggregatorType.DUMMY;
            }
        }

        @Override
        public QueryInfoBean getQueryInfoBean() {
            return bean;
        }
    }
}
