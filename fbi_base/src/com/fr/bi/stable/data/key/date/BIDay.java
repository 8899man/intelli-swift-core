package com.fr.bi.stable.data.key.date;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by GUY on 2015/2/11.
 */
public class BIDay implements Serializable {
    private static final long serialVersionUID = 6717658315497913730L;
    public static BIDay NULL = new BIDay(-1, -1, -1);

    private int year;

    private int month;

    private int day;

    public long getTime() {
        return time;
    }

    private long time;

    public BIDay() {
    }

    public BIDay(long t){
        this.time = t;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t);
        day =  c.get(Calendar.DAY_OF_MONTH);
        month = c.get(Calendar.MONTH);
        year =  c.get(Calendar.YEAR);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        this.time = c.getTimeInMillis();
    }

    public BIDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        this.time = c.getTimeInMillis();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof BIDay)) {
            return false;
        }

        BIDay date = (BIDay) o;

        if (year != date.year) {
            return false;
        }
        if (month != date.month) {
            return false;
        }
        return day == date.day;

    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + day;
        return result;
    }

    public int compareTo(BIDay day) {
        if (year != day.year){
            return year - day.year;
        }
        if (month != day.month){
            return month - day.month;
        }
        return this.day - day.day;
    }

    @Override
    public String toString() {
        return "BIDay{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}