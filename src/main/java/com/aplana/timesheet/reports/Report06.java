package com.aplana.timesheet.reports;

import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.util.DateTimeUtil;
import net.sf.jasperreports.engine.JRDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    @Override
    public JRDataSource prepareDataSource() {
        return reportDAO.getReport06Data(this);
    }

    private Integer projectId;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
