package com.aplana.timesheet.reports;

import net.sf.jasperreports.engine.JRDataSource;

public class Report07 extends BaseReport{
    // FIX не использовать константы сделать list для проверки валидных констант
    private static Integer PERIOD_TYPE_MONTH = 1;
    private static Integer PERIOD_TYPE_KVARTAL = 3;
    private static Integer PERIOD_TYPE_HALF_YEAR = 6;
    private static Integer PERIOD_TYPE_YEAR = 12;
    public static final String jrName = "report07";

    public static final String jrNameFile = "Отчёт №7, Относительная активность по проектам";

    @Override
    public JRDataSource prepareDataSource() {
        return reportDAO.getReport07Data(this);
    }

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    private Boolean filterDivisionOwner = true;
    private Integer divisionOwner;
    private Integer divisionEmployee;
    private Integer periodType = PERIOD_TYPE_MONTH;

    public Boolean getFilterDivisionOwner() {
        return filterDivisionOwner;
    }

    public void setFilterDivisionOwner(Boolean filterDivisionOwner) {
        this.filterDivisionOwner = filterDivisionOwner;
    }

    public Integer getDivisionOwner() {
        return divisionOwner;
    }

    public void setDivisionOwner(Integer divisionOwner) {
        this.divisionOwner = divisionOwner;
    }

    public Integer getDivisionEmployee() {
        return divisionEmployee;
    }

    public void setDivisionEmployee(Integer divisionEmployee) {
        this.divisionEmployee = divisionEmployee;
    }

    public Integer getPeriodType() {
        return periodType;
    }

    public void setPeriodType(Integer periodType) {
        this.periodType = periodType;
    }
}
