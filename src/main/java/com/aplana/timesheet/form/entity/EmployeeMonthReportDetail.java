package com.aplana.timesheet.form.entity;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Project;

/* класс - строка в таблице детализации месячной работы по проектам */
public class EmployeeMonthReportDetail implements Comparable<EmployeeMonthReportDetail> {
    private DictionaryItem act_type;
    private Project project;
    private Double planHours;
    private Double factHours;
    private Double durationPlan;

    public DictionaryItem getAct_type() {
        return act_type;
    }

    public void setAct_type(DictionaryItem act_type) {
        this.act_type = act_type;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Double getPlanHours() {
        return planHours;
    }

    public void setPlanHours(Double planHours) {
        this.planHours = planHours;
    }

    public Double getFactHours() {
        return factHours;
    }

    public void setFactHours(Double factHours) {
        this.factHours = factHours;
    }

    public EmployeeMonthReportDetail(DictionaryItem act_type, Project project, Double planHours, Double factHours, Double durationPlan) {
        this.act_type = act_type;
        this.project = project;
        this.planHours = planHours;
        this.factHours = factHours;
        this.durationPlan = durationPlan;
    }

    public int compareTo(EmployeeMonthReportDetail o) {
        int result;
        result = getAct_type().getValue().compareTo(o.getAct_type().getValue());
        if (result == 0) {
            result = getProject().getName().compareTo(o.getProject().getName());
        }
        return result;
    }

    public Integer getPlanPercent() {
        if (durationPlan != null && durationPlan != 0 && planHours != null) {
            Long l = Math.round(planHours/durationPlan*100);
            return l.intValue();
        } else {
            return 0;
        }
    }

    public Integer getFactPercent() {
        if (durationPlan != null && durationPlan != 0 && factHours != null) {
            Long l = Math.round(factHours/durationPlan*100);
            return l.intValue();
        } else {
            return 0;
        }
    }
}
