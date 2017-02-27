package com.fr.bi.cal.analyze.report.report.widget.chart.style.filter.string;

import com.fr.bi.cal.analyze.report.report.widget.chart.style.filter.IFilter;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.stable.StringUtils;

/**
 * Created by AstronautOO7 on 2016/9/19.
 */
public class StringNotNullFilter implements IFilter{

    private boolean isStringNotNull(String value){
        return value != null && !StringUtils.isEmpty(value);
    }

    @Override
    public JSONArray getFilterResult(JSONArray array) throws JSONException {
        JSONArray result = new JSONArray();
        for(int i = 0; i < array.length(); i++){
            if(isStringNotNull(array.getString(i))){
                result.put(array.getString(i));
            }
        }
        return result;
    }
}
