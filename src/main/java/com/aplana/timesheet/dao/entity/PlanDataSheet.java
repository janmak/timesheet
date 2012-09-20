package com.aplana.timesheet.dao.entity;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class PlanDataSheet {

    private String plan;
    private Date date;

    public PlanDataSheet(String plan, Date date) {
        this.date = date;
        this.plan = plan;
    }

    public Date getDate() {
        return date;
    }

    public String getPlan() {
        if (plan != null)
            return plan.replace("\r\n", "<br>");
        return null;
    }

    public String getDateInString() {
        if (date != null) {
            SimpleDateFormat norm = new SimpleDateFormat("dd.MM.yyyy");
            return norm.format(date);
        } else
            return "Планов нет";
    }
}
