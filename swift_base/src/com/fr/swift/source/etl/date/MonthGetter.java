package com.fr.swift.source.etl.date;

import java.util.Calendar;

/**
 * @author Daniel
 *
 */
public class MonthGetter implements DateGetter {

    public static final MonthGetter INSTANCE = new MonthGetter();

    @Override
    public Integer get(Long v) {
        if(v == null){
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(v);
        //需要加1Calendar的月份从0开始-11
        return c.get(Calendar.MONTH) + 1;
    }

}
