package com.fr.bi.field.filtervalue.date.onevaluefilter;

import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.field.filtervalue.string.onevaluefilter.StringOneValueFilterValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by User on 2016/6/8.
 */
public class YMDLikeFilterValue extends StringOneValueFilterValue {

    @BICoreField
    private String CLASS_TYPE = "YMDLikeFilterValue";

    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    Calendar calendar = Calendar.getInstance();

    @Override
    public boolean isMatchValue(String v) {
        calendar.setTimeInMillis(Long.parseLong(v));
        return formatter.format(calendar.getTime()).indexOf(value) != -1;
    }
}
