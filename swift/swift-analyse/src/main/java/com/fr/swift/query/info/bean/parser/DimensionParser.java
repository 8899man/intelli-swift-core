package com.fr.swift.query.info.bean.parser;

import com.fr.general.ComparatorUtils;
import com.fr.swift.config.ColumnIndexingConf;
import com.fr.swift.config.service.IndexingConfService;
import com.fr.swift.SwiftContext;
import com.fr.swift.query.group.Groups;
import com.fr.swift.query.group.impl.NoGroupRule;
import com.fr.swift.query.group.info.IndexInfoImpl;
import com.fr.swift.query.info.bean.element.DimensionBean;
import com.fr.swift.query.info.bean.element.SortBean;
import com.fr.swift.query.info.element.dimension.DetailDimension;
import com.fr.swift.query.info.element.dimension.Dimension;
import com.fr.swift.query.info.element.dimension.GroupDimension;
import com.fr.swift.query.sort.AscSort;
import com.fr.swift.query.sort.DescSort;
import com.fr.swift.query.sort.Sort;
import com.fr.swift.query.sort.SortType;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.source.SourceKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyon on 2018/6/7.
 */
class DimensionParser {

    // TODO: 2018/6/7 解析各类bean过程中相关参数的合法性校验以及相关异常处理规范
    static List<Dimension> parse(SourceKey table, List<DimensionBean> dimensionBeans, List<SortBean> sortBeans) {
        IndexingConfService service = SwiftContext.get().getBean(IndexingConfService.class);
        List<Dimension> dimensions = new ArrayList<Dimension>();
        for (int i = 0; i < dimensionBeans.size(); i++) {
            DimensionBean dimensionBean = dimensionBeans.get(i);
            ColumnKey columnKey = new ColumnKey(dimensionBean.getColumn());
            SortBean sortBean = getDimensionSort(dimensionBean.getColumn(), sortBeans);
            Sort sort = null;
            if (sortBean != null) {
                sort = sortBean.getType() == SortType.ASC ? new AscSort(i) : new DescSort(i);
            }
            ColumnIndexingConf conf = service.getColumnConf(table, dimensionBean.getColumn());
            switch (dimensionBean.getType()) {
                case GROUP:
                    dimensions.add(new GroupDimension(i, columnKey, Groups.newGroup(new NoGroupRule()), sort,
                            new IndexInfoImpl(conf.requireIndex(), conf.requireGlobalDict())));
                    break;
                case DETAIL:
                    dimensions.add(new DetailDimension(i, columnKey, Groups.newGroup(new NoGroupRule()), sort,
                            new IndexInfoImpl(conf.requireIndex(), conf.requireGlobalDict())));
                    break;
            }
        }
        return dimensions;
    }

    private static SortBean getDimensionSort(String name, List<SortBean> sortBeans) {
        if (null != sortBeans && null != name) {
            for (SortBean sortBean : sortBeans) {
                if (ComparatorUtils.equals(sortBean.getName(), name)) {
                    return sortBean;
                }
            }
        }
        return null;
    }

    static List<Dimension> parse(List<DimensionBean> joinedFields) {
        List<Dimension> dimensions = new ArrayList<Dimension>();
        for (int i = 0; i < joinedFields.size(); i++) {
            DimensionBean dimensionBean = joinedFields.get(i);
            ColumnKey columnKey = new ColumnKey(dimensionBean.getColumn());
            switch (dimensionBean.getType()) {
                case GROUP:
                    dimensions.add(new GroupDimension(i, columnKey, Groups.newGroup(new NoGroupRule()), null, new IndexInfoImpl(false, false)));
                    break;
                case DETAIL:
                    dimensions.add(new DetailDimension(i, columnKey, Groups.newGroup(new NoGroupRule()), null, new IndexInfoImpl(false, false)));
                    break;
            }
        }

        return dimensions;
    }
}
