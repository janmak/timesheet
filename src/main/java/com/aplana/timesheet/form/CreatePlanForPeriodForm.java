package com.aplana.timesheet.form;

import java.util.Date;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class CreatePlanForPeriodForm {

    public static final String EMPLOYEE_ID          = "employeeId";
    public static final String FROM_DATE            = "fromDate";
    public static final String TO_DATE              = "toDate";
    public static final String PROJECT_ID           = "projectId";
    public static final String PERCENT_OF_CHARGE    = "percentOfCharge";

    private Integer employeeId;
    private Date fromDate;
    private Date toDate;
    private Integer projectId;
    private Byte percentOfCharge;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Byte getPercentOfCharge() {
        return percentOfCharge;
    }

    public void setPercentOfCharge(Byte percentOfCharge) {
        this.percentOfCharge = percentOfCharge;
    }
}
