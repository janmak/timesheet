package com.aplana.timesheet.reports;

import net.sf.jasperreports.engine.JRDataSource;

public class Report01 extends BaseReport {

    public static final String jrName = "report01";

    public static final String jrNameFile = "Отчет №1. Переработки, работа в выходные и праздничные дни";

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    @Override
    public JRDataSource prepareDataSource() {
        return reportDAO.getReport01Data(this);
    }

    private Integer divisionId;

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }
}
