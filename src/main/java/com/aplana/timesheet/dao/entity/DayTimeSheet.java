package com.aplana.timesheet.dao.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DayTimeSheet implements Comparable<DayTimeSheet> {

    private Timestamp calDate;

    private Boolean workDay;

    private Integer id;

    private Integer act_type;

    private BigDecimal duration;

    public DayTimeSheet(Timestamp calendarDate, Boolean isHoliday, Integer timeSheetId, BigDecimal duration, Integer act_type) {
        this.setCalDate(calendarDate);
        this.setWorkDay(!isHoliday); // APLANATS-266. workday = true - выходной день, а false - рабочий!
        this.setId(timeSheetId);
        this.setDuration(duration);
        this.setAct_type(act_type);
    }

    public Integer getAct_type() {
        return act_type;
    }

    public void setAct_type(Integer act_type) {
        this.act_type = act_type;
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public void setDuration(BigDecimal duration) {
        this.duration = duration;
    }

    public Timestamp getCalDate() {
        return calDate;
    }

    public void setCalDate(Timestamp calDate) {
        this.calDate = calDate;
    }

    public Boolean getWorkDay() {
        return workDay;
    }

    public void setWorkDay(Boolean workDay) {
        this.workDay = workDay;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int compareTo(DayTimeSheet o) {
        if (getCalDate().getTime() < o.getCalDate().getTime())
            return -1;
        else if (getCalDate().getTime() == o.getCalDate().getTime())
            return 0;
        else
            return 1;
    }
}
