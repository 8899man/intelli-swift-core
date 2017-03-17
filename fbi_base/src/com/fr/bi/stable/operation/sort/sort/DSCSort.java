package com.fr.bi.stable.operation.sort.sort;

import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.operation.sort.AbstractSort;
import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;
import com.fr.bi.stable.operation.sort.comp.IComparator;

/**
 * Created by GUY on 2015/4/9.
 */
public class DSCSort extends AbstractSort {
    private static final long serialVersionUID = 5081361685264495648L;
    protected transient IComparator comparator = ComparatorFacotry.getComparator(getSortType());


    @Override
    public int getSortType() {
        return BIReportConstant.SORT.DESC;
    }

    @Override
    public IComparator getComparator() {
        return comparator;
    }
}