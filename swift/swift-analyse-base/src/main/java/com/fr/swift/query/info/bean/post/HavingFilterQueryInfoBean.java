package com.fr.swift.query.info.bean.post;

import com.fr.swift.query.info.bean.element.filter.FilterInfoBean;
import com.fr.swift.query.info.bean.type.PostQueryType;
import com.fr.third.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by Lyon on 2018/6/3.
 */
public class HavingFilterQueryInfoBean extends AbstractPostQueryInfoBean {

    @JsonProperty
    Map<String, FilterInfoBean> filterInfoMap;

    {
        type = PostQueryType.HAVING_FILTER;
    }

    public Map<String, FilterInfoBean> getFilterInfoMap() {
        return filterInfoMap;
    }

    public void setFilterInfoMap(Map<String, FilterInfoBean> filterInfoMap) {
        this.filterInfoMap = filterInfoMap;
    }

    @Override
    public PostQueryType getType() {
        return PostQueryType.HAVING_FILTER;
    }
}
