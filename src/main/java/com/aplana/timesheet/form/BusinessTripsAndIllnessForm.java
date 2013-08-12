package com.aplana.timesheet.form;

import com.aplana.timesheet.enums.QuickReportTypesEnum;

import java.util.Arrays;
import java.util.List;

/**
 * User: vsergeev
 * Date: 18.01.13
 */
public class BusinessTripsAndIllnessForm {

    private Integer divisionId;
    private Integer employeeId;
    private Integer year;
    private Integer month;
    private List<QuickReportTypesEnum> reportTypes = Arrays.asList(QuickReportTypesEnum.values());
    private Integer reportType = QuickReportTypesEnum.ILLNESS.getId();

    public List<QuickReportTypesEnum> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<QuickReportTypesEnum> reportTypes) {
        this.reportTypes = reportTypes;
    }

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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }
}
