package com.aplana.timesheet.util;

import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * User: vsergeev
 * Date: 05.02.13
 */
public class DateNumbers {

    private Integer day;
    private Integer month;
    private Integer year;

    public DateNumbers() {

    }

    public DateNumbers(Integer day, Integer month, Integer year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public DateNumbers(Date date) {
        final Calendar beginCalendar = DateUtils.toCalendar(date);

        this.year = beginCalendar.get(Calendar.YEAR);
        this.month = beginCalendar.get(Calendar.MONTH);
        this.day = beginCalendar.get(Calendar.DATE);
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    /**
     * нумерация месяцев с 0!
     */
    public Integer getMonth() {
        return month;
    }

    /**
     * нумерация месяцев с 1!
     * @return
     */
    public Integer getDatabaseMonth() {
        return month + 1;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "DateNumbers{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
