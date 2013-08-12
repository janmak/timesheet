package com.aplana.timesheet.reports;

public class Report06 extends BaseReport {

    public static final String jrName = "report06";

    public static final String jrNameFile="Отчет №6. Распределение трудозатрат в проекте для СК";

    @Override
    public String getJRName() {
        return jrName;
    }

    @Override
    public String getJRNameFile() {
        return jrNameFile;
    }

    private Integer projectId;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
