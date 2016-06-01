package com.fr.bi.field.target.key.sum;


import com.finebi.cube.conf.field.BusinessField;
import com.fr.bi.conf.report.widget.field.target.filter.TargetFilter;
import com.fr.bi.stable.constant.BIReportConstant;

public class SumKey extends SummaryKey {

    /**
     *
     */
    private static final long serialVersionUID = 3092151610397394316L;

    public SumKey(BusinessField ck, TargetFilter filter) {
        super(ck, filter);
    }

    @Override
    public int getSummaryType() {
        return BIReportConstant.SUMMARY_TYPE.SUM;
    }
}