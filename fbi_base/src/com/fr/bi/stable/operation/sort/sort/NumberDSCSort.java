package com.fr.bi.stable.operation.sort.sort;

import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.operation.sort.AbstractSort;
import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;
import com.fr.bi.stable.operation.sort.comp.IComparator;

/**
 * Created by Young's on 2016/3/3.
 */
public class NumberDSCSort extends AbstractSort {
    private static final long serialVersionUID = -3652624067791601680L;
    protected transient IComparator comparator = ComparatorFacotry.getComparator(getSortType());


    @Override
    public int getSortType() {
        return BIReportConstant.SORT.NUMBER_DESC;
    }

    @Override
    public IComparator getComparator() {
        return comparator;
    }
}