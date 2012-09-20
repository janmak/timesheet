package com.aplana.timesheet.reports;

import net.sf.jasperreports.engine.JRDataSource;

public class Report05 extends BaseReport {

    public static final String jrName = "report05";

    public static final String jrNameFile="Отчет №5. Детализация трудозатрат для СК";

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
        return reportDAO.getReport05Data(this);
    }

    private Integer divisionId;

	private Integer employeeId;

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
}
