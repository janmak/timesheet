package com.aplana.timesheet.controller.quickreport;

import com.aplana.timesheet.dao.entity.Periodical;

import java.util.List;

/**
 * User: vsergeev
 * Date: 21.01.13
 */
public interface QuickReport {

    List<Periodical> getPeriodicalsList();

    void setPeriodicalsList(List<Periodical> listOfPeriodicals);

    void setMounthCalendarDays(Long countOfDays);

    Long getMounthCalendarDays();

    void setMounthWorkDays(Long i);

    Long getMounthWorkDays();
}
