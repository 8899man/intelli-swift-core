package com.fr.swift.query.info.bean.element.filter.impl;

/**
 * Created by Lyon on 2018/6/28.
 */
public class NFilterBean extends DetailFilterInfoBean<Integer> {

    private static final long serialVersionUID = 5745104332814069864L;

    @Override
    public Integer getFilterValue() {
        return filterValue;
    }

    @Override
    public void setFilterValue(Integer filterValue) {
        this.filterValue = filterValue;
    }
}
