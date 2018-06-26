package com.fr.swift.query.info.bean.parser;

import com.fr.swift.query.filter.info.FilterInfo;
import com.fr.swift.query.filter.info.GeneralFilterInfo;
import com.fr.swift.query.filter.info.MatchFilterInfo;
import com.fr.swift.query.filter.info.SwiftDetailFilterInfo;
import com.fr.swift.query.filter.match.ToStringConverter;
import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.DetailFilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.GeneralFilterInfoBean;
import com.fr.swift.query.info.bean.element.filter.impl.MatchFilterInfoBean;
import com.fr.swift.segment.column.ColumnKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyon on 2018/6/7.
 */
class FilterInfoParser {

    static FilterInfo parse(FilterInfoBean bean) {
        if (null != bean) {
            switch (bean.getBeanType()) {
                case DETAIL:
                    DetailFilterInfoBean detailFilterInfoBean = (DetailFilterInfoBean) bean;
                    ColumnKey columnKey = new ColumnKey(detailFilterInfoBean.getColumn());
                    columnKey.setRelation(RelationSourceParser.parse(detailFilterInfoBean.getRelation()));
                    return new SwiftDetailFilterInfo(columnKey, detailFilterInfoBean.getFilterValue(), detailFilterInfoBean.getType());
                case GENERAL:
                    GeneralFilterInfoBean generalFilterInfoBean = (GeneralFilterInfoBean) bean;
                    List<FilterInfoBean> filterInfoBeans = generalFilterInfoBean.getChildren();
                    List<FilterInfo> filterInfos = new ArrayList<FilterInfo>();
                    if (null != filterInfoBeans) {
                        for (FilterInfoBean filterInfoBean : filterInfoBeans) {
                            filterInfos.add(parse(filterInfoBean));
                        }
                    }
                    return new GeneralFilterInfo(filterInfos, generalFilterInfoBean.getType());
                case MATCH:
                    MatchFilterInfoBean matchFilterInfoBean = (MatchFilterInfoBean) bean;
                    // TODO convert还没有做
                    return new MatchFilterInfo(parse(matchFilterInfoBean.getFilterInfoBean()), matchFilterInfoBean.getIndex(), new ToStringConverter());
            }
        }
        return null;
    }
}
