package com.fr.swift.query.info.bean.parser;

import com.fr.swift.query.info.bean.element.CalculatedFieldBean;
import com.fr.swift.query.info.bean.element.MetricBean;
import com.fr.swift.query.info.bean.element.SortBean;
import com.fr.swift.query.info.bean.post.CalculatedFieldQueryInfoBean;
import com.fr.swift.query.info.bean.post.PostQueryInfoBean;
import com.fr.swift.query.info.bean.post.RowSortQueryInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.info.bean.type.PostQueryType;
import com.fr.swift.query.info.element.dimension.Dimension;
import com.fr.swift.query.info.element.target.GroupTarget;
import com.fr.swift.query.info.group.post.CalculatedFieldQueryInfo;
import com.fr.swift.query.info.group.post.PostQueryInfo;
import com.fr.swift.query.info.group.post.RowSortQueryInfo;
import com.fr.swift.query.sort.AscSort;
import com.fr.swift.query.sort.DescSort;
import com.fr.swift.query.sort.Sort;
import com.fr.swift.query.sort.SortType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lyon on 2018/6/7.
 */
class PostQueryInfoParser {

    static List<PostQueryInfo> parse(List<PostQueryInfoBean> postQueryInfoBeans,
                                     List<Dimension> dimensions, List<MetricBean> metrics) {
        Map<String, Integer> fieldIndexMap = getFieldIndexMap(postQueryInfoBeans, metrics);
        List<PostQueryInfo> postQueryInfoList = new ArrayList<PostQueryInfo>();
        for (PostQueryInfoBean bean : postQueryInfoBeans) {
            postQueryInfoList.add(parse(dimensions.size(), bean, fieldIndexMap));
        }
        return postQueryInfoList;
    }

    /**
     * 取出所有指标字段的IndexMap，当前计算指标字段的依赖关系只能是后面依赖前面（客户端写查询信息的时候要注意）
     */
    private static Map<String, Integer> getFieldIndexMap(List<PostQueryInfoBean> postQueryInfoBeans, List<MetricBean> metrics) {
        Map<String, Integer> fieldIndexMap = new HashMap<String, Integer>();
        for (MetricBean metric : metrics) {
            fieldIndexMap.put(metric.getName(), fieldIndexMap.size());
        }
        for (PostQueryInfoBean postQueryInfoBean : postQueryInfoBeans) {
            if (postQueryInfoBean.getType() != PostQueryType.CAL_FIELD) {
                continue;
            }
            List<CalculatedFieldBean> calculatedFieldBeans = ((CalculatedFieldQueryInfoBean) postQueryInfoBean).getCalculatedFieldBeans();
            for (CalculatedFieldBean calculatedFieldBean : calculatedFieldBeans) {
                fieldIndexMap.put(calculatedFieldBean.getName(), fieldIndexMap.size());
            }
        }
        return fieldIndexMap;
    }

    private static PostQueryInfo parse(int dimensionSize, PostQueryInfoBean bean, Map<String, Integer> fieldIndexMap) {
        PostQueryType type = bean.getType();
        switch (type) {
            case CAL_FIELD:
                List<GroupTarget> targets = CalculatedFieldParser.parse(((CalculatedFieldQueryInfoBean) bean).getCalculatedFieldBeans(), fieldIndexMap);
                return new CalculatedFieldQueryInfo(targets);
            case ROW_SORT:
                return parseRowSortQueryInfo(dimensionSize, (RowSortQueryInfoBean) bean, fieldIndexMap);
        }
        return null;
    }

    private static PostQueryInfo parseRowSortQueryInfo(int dimensionSize, RowSortQueryInfoBean bean,
                                                       Map<String, Integer> fieldIndexMap) {
        List<Sort> sorts = new ArrayList<Sort>();
        List<SortBean> sortBeans = bean.getSortBeans();
        for (SortBean sortBean : sortBeans) {
            // TODO: 2018/6/7 这边复用之前排序的代码，所以targetIndex要算上维度
            int targetIndex = dimensionSize + fieldIndexMap.get(sortBean.getColumn());
            sorts.add(sortBean.getType() == SortType.ASC ? new AscSort(targetIndex) : new DescSort(targetIndex));
        }
        return new RowSortQueryInfo(sorts);
    }

    /**
     * 解析resultJoinQuery的PostQueryInfo
     *
     * @param postQueryInfoBeans
     * @param dimensions
     * @param queryInfoBeans
     * @return
     */
    static List<PostQueryInfo> parsePostQueryInfoOfResultJoinQuery(List<PostQueryInfoBean> postQueryInfoBeans,
                                                                   List<Dimension> dimensions, List<QueryInfoBean> queryInfoBeans) {
        Map<String, Integer> fieldIndexMap = getFieldIndexMapOfResultJoinQueryInfo(postQueryInfoBeans, queryInfoBeans);
        List<PostQueryInfo> postQueryInfoList = new ArrayList<PostQueryInfo>();
        for (PostQueryInfoBean bean : postQueryInfoBeans) {
            postQueryInfoList.add(parse(dimensions.size(), bean, fieldIndexMap));
        }
        return postQueryInfoList;
    }

    private static Map<String, Integer> getFieldIndexMapOfResultJoinQueryInfo(List<PostQueryInfoBean> postQueryInfoBeans,
                                                                              List<QueryInfoBean> queryBeans) {
        Map<String, Integer> fieldIndexMap = new HashMap<String, Integer>();
        for (QueryInfoBean queryBean : queryBeans) {
            // TODO: 2018/6/8 这边都是假定groupQuery
            List<MetricBean> metricBeans = ((GroupQueryInfoBean) queryBean).getMetricBeans();
            for (MetricBean metricBean : metricBeans) {
                fieldIndexMap.put(metricBean.getName(), fieldIndexMap.size());
            }
            List<PostQueryInfoBean> queryInfoBeans = ((GroupQueryInfoBean) queryBean).getPostQueryInfoBeans();
            for (PostQueryInfoBean queryInfoBean : queryInfoBeans) {
                if (queryInfoBean.getType() != PostQueryType.CAL_FIELD) {
                    continue;
                }
                List<CalculatedFieldBean> calculatedFieldBeans = ((CalculatedFieldQueryInfoBean) queryInfoBean).getCalculatedFieldBeans();
                for (CalculatedFieldBean calculatedFieldBean : calculatedFieldBeans) {
                    fieldIndexMap.put(calculatedFieldBean.getName(), fieldIndexMap.size());
                }
            }
        }
        for (PostQueryInfoBean postQueryInfoBean : postQueryInfoBeans) {
            if (postQueryInfoBean.getType() != PostQueryType.CAL_FIELD) {
                continue;
            }
            List<CalculatedFieldBean> calculatedFieldBeans = ((CalculatedFieldQueryInfoBean) postQueryInfoBean).getCalculatedFieldBeans();
            for (CalculatedFieldBean calculatedFieldBean : calculatedFieldBeans) {
                fieldIndexMap.put(calculatedFieldBean.getName(), fieldIndexMap.size());
            }
        }
        return fieldIndexMap;
    }
}
