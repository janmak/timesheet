package com.aplana.timesheet.reports;

public class Report02 extends BaseReport {

    public static final String jrName = "report02";

    public static final String jrNameFile = "Отчет №2. Сводный отчет затраченного времени по проекту";

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    private Integer projectId;

    private Integer emplDivisionId;

    private Integer employeeId;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getEmplDivisionId() {
        return emplDivisionId;
    }

    public void setEmplDivisionId(Integer emplDivisionId) {
        this.emplDivisionId = emplDivisionId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
