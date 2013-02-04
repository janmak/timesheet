package com.aplana.timesheet.form;

import com.aplana.timesheet.dao.entity.Employee;

import java.util.List;

/**
 * @author iziyangirov
 */
public class AssignmentLeadersTableRowForm {

    Integer divisionId;
    String division;

    Integer regionId;
    String region;

    Integer leaderId;
    List<Employee> regionDivisionEmployees;

    public AssignmentLeadersTableRowForm(){

    }

    public AssignmentLeadersTableRowForm(Integer divisionId,
                                         String divisionName,
                                         Integer regionId,
                                         String regionName){
        this.setDivisionId(divisionId);
        this.setDivision(divisionName);
        this.setRegionId(regionId);
        this.setRegion(regionName);
    }

    public List<Employee> getRegionDivisionEmployees() {
        return regionDivisionEmployees;
    }

    public void setRegionDivisionEmployees(List<Employee> regionDivisionEmployees) {
        this.regionDivisionEmployees = regionDivisionEmployees;
    }

    public Integer getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(Integer divisionId) {
        this.divisionId = divisionId;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }
}
