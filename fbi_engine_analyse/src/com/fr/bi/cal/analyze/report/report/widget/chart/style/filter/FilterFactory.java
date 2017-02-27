package com.fr.bi.cal.analyze.report.report.widget.chart.style.filter;

import com.fr.bi.cal.analyze.report.report.widget.chart.style.filter.condition.EmptyFilter;
import com.fr.bi.cal.analyze.report.report.widget.chart.style.filter.condition.GeneralAndFilter;
import com.fr.bi.cal.analyze.report.report.widget.chart.style.filter.condition.GeneralOrFilter;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

/**
 * Created by AstronautOO7 on 2016/9/19.
 */
public class FilterFactory {
    public static IFilter parseFilter(JSONObject filterValue) throws JSONException {
        IFilter filter = null;
        if(filterValue.has("filter_type")){
            switch (filterValue.getInt("filter_type")){
                case BIReportConstant.FILTER_TYPE.AND:
                    filter = new GeneralAndFilter(filterValue);
                    break;
                case BIReportConstant.FILTER_TYPE.OR:
                    filter = new GeneralOrFilter(filterValue);
                    break;
                case BIReportConstant.FILTER_TYPE.EMPTY_CONDITION:
                    filter = new EmptyFilter();
                    break;
                default:
//                    filter = new SingleFilter(filterValue);
                    break;
            }
        }
        return filter;
    }
}
