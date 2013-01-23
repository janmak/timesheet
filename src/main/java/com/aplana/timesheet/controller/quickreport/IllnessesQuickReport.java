package com.aplana.timesheet.controller.quickreport;

import com.aplana.timesheet.dao.entity.Periodical;

import java.util.ArrayList;
import java.util.List;

/**
 * User: vsergeev
 * Date: 21.01.13
 */
public class IllnessesQuickReport implements QuickReport {

    private List<Periodical> periodicalsList = new ArrayList<Periodical>();
    private Long mounthWorkDays = 0L;
    private Long mounthCalendarDays = 0L;
    private Double mounthWorkDaysOnIllnessWorked = 0d;
    private Long yearWorkDaysOnIllness = 0L;
    private Double yearWorkDaysOnIllnessWorked = 0d;

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

    public Double getMounthWorkDaysOnIllnessWorked() {
        return mounthWorkDaysOnIllnessWorked;
    }

    public void setMounthWorkDaysOnIllnessWorked(Double mounthWorkDaysOnIllnessWorked) {
        this.mounthWorkDaysOnIllnessWorked = mounthWorkDaysOnIllnessWorked;
    }

    public Long getYearWorkDaysOnIllness() {
        return yearWorkDaysOnIllness;
    }

    public void setYearWorkDaysOnIllness(Long yearWorkDaysOnIllness) {
        this.yearWorkDaysOnIllness = yearWorkDaysOnIllness;
    }

    public Double getYearWorkDaysOnIllnessWorked() {
        return yearWorkDaysOnIllnessWorked;
    }

    public void setYearWorkDaysOnIllnessWorked(Double yearWorkDaysOnIllnessWorked) {
        this.yearWorkDaysOnIllnessWorked = yearWorkDaysOnIllnessWorked;
    }

}
