package com.aplana.timesheet.reports;

import com.aplana.timesheet.dao.JasperReportDAO;
import net.sf.jasperreports.engine.JRDataSource;

public interface TSJasperReport {
    JRDataSource prepareDataSource();

    void checkParams();

    String getJRName();

    public void setDivisionOwnerId(Integer divisionOwnerId);

    void setReportDAO(JasperReportDAO reportDAO);

    String getJRNameFile();
}
