package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.BusinessTripTypes;
import com.aplana.timesheet.enums.IllnessTypes;
import com.aplana.timesheet.enums.QuickReportTypes;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 23.01.13
 */
public class BusinessTripsAndIllnessAddForm {

    private Employee employee;
    private Date beginDate = new Date();
    private Date endDate = new Date();
    private String comment;
    private Integer reason = IllnessTypes.ILLNESS.getId();
    private Integer reportType = QuickReportTypes.ILLNESS.getId();
    private List<QuickReportTypes> reportTypes = Arrays.asList(QuickReportTypes.values());
    private List<IllnessTypes> illnessTypes = Arrays.asList(IllnessTypes.values());
    private Integer businessTripType = BusinessTripTypes.NOT_PROJECT.getId();
    private List<BusinessTripTypes> businessTripTypes = Arrays.asList(BusinessTripTypes.values());
    private Integer projectId;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<BusinessTripTypes> getBusinessTripTypes() {
        return businessTripTypes;
    }

    public void setBusinessTripTypes(List<BusinessTripTypes> businessTripTypes) {
        this.businessTripTypes = businessTripTypes;
    }

    public Integer getBusinessTripType() {
        return businessTripType;
    }

    public void setBusinessTripType(Integer businessTripType) {
        this.businessTripType = businessTripType;
    }

    public List<QuickReportTypes> getReportTypes() {
        return reportTypes;
    }

    public void setReportTypes(List<QuickReportTypes> reportTypes) {
        this.reportTypes = reportTypes;
    }

    public List<IllnessTypes> getIllnessTypes() {
        return illnessTypes;
    }

    public void setIllnessTypes(List<IllnessTypes> illnessTypes) {
        this.illnessTypes = illnessTypes;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public Integer getReportType() {
        return reportType;
    }

    public void setReportType(Integer reportType) {
        this.reportType = reportType;
    }
}
