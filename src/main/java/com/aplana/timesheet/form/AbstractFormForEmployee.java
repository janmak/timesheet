package com.aplana.timesheet.form;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractFormForEmployee {

    private Integer employeeId;
    private Integer divisionId;

    public final Integer getDivisionId() {
        return divisionId;
    }

    public final void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public final Integer getEmployeeId() {
        return employeeId;
    }

    public final void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }
}
