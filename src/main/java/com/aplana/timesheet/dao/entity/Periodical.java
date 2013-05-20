package com.aplana.timesheet.dao.entity;

import java.util.Date;

/**
 * User: vsergeev
 * Date: 21.01.13
 */
public interface Periodical {

    public Date getBeginDate();

    public void setBeginDate(Date beginDate);

    public Date getEndDate();

    public void setEndDate(Date endDate);

    public Periodical clone() throws CloneNotSupportedException;

    void setWorkingDays(Long workingDays);

    Long getWorkingDays();

    void setCalendarDays(Long calendarDays);

    Long getCalendarDays();

    Employee getEmployee();
}
