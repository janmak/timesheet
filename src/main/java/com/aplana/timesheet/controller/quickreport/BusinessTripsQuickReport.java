package com.aplana.timesheet.controller.quickreport;

import com.aplana.timesheet.dao.entity.Periodical;

import java.util.ArrayList;
import java.util.List;

/**
 * User: vsergeev
 * Date: 21.01.13
 */
public class BusinessTripsQuickReport implements QuickReport {

    private List<Periodical> periodicalsList = new ArrayList<Periodical>();
    private Long mounthWorkDays = 0L;
    private Long mounthCalendarDays = 0L;

    @Override
    public List<Periodical> getPeriodicalsList() {
        return periodicalsList;
    }

    @Override
    public void setPeriodicalsList(List<Periodical> periodicalsList) {
        this.periodicalsList = periodicalsList;
    }

    @Override
    public Long getMounthWorkDays() {
        return mounthWorkDays;
    }

    @Override
    public void setMounthWorkDays(Long mounthWorkDays) {
        this.mounthWorkDays = mounthWorkDays;
    }

    @Override
    public Long getMounthCalendarDays() {
        return mounthCalendarDays;
    }

    @Override
    public void setMounthCalendarDays(Long mounthCalendarDays) {
        this.mounthCalendarDays = mounthCalendarDays;
    }

}
