package com.aplana.timesheet.reports;

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

	private Integer employeeId;

	public Integer getEmployeeId() {
		return employeeId;
	}
}
