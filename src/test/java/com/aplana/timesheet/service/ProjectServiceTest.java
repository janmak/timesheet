package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.properties.TSPropertyProvider;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class ProjectServiceTest extends AbstractJsonTest {

    @Autowired
    private DivisionDAO divisionDAO;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    public VacationApprovalDAO vacationApprovalDAO;

    @Autowired
    public EmployeeDAO employeeDAO;

    @Autowired
    public VacationDAO vacationDAO;

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Autowired
    private CalendarService calendarService;

    Vacation storedVacation;

    private String getProjectListWithOwnerDivisionJson(List<Division> divisions) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        List<Project> projectList = projectDAO.getProjects();
        for (Project project : projectList) {
            result.append("{\"id\":\"");
            result.append(project.getId());
            result.append("\",\"value\":\"");
            result.append(project.getName());
            result.append("\",\"state\":\"");
            result.append(project.getState().getId());
            result.append("\",\"ownerDivisionId\":\"");
            result.append(project.getManager().getDivision().getId());
            result.append("\"}");
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append("]");
        return result.toString();
    }

    private String getProjectListJson(List<Division> divisions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < divisions.size(); i++) {
            sb.append("{\"divId\":\"");
            sb.append(divisions.get(i).getId());
            Set<Project> projects = divisions.get(i).getProjects();
            sb.append("\",\"divProjs\":[");
            if (projects.size() > 0) {
                int count = 0;
                for (Project project : projects) {
                    if (project.isActive()) {
                        sb.append("{\"id\":\"");
                        sb.append(project.getId());
                        sb.append("\",\"value\":\"");
                        sb.append(project.getName());
                        sb.append("\",\"state\":\"");
                        sb.append(project.getState().getId());
                        sb.append("\"}");
                        sb.append(",");
                    }
                    count++;
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("]}");
            } else {
                sb.append("{\"id\":\"0\",\"value\":\"\"}]}");
            }

            if (i < (divisions.size() - 1)) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String getProjectListJson() {
        return getProjectListAsJson(projectService.getProjects());
    }

    private String getProjectListAsJson(List<Project> projects){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (projects.size() > 0) {
            int count = 0;
            for (Project project : projects) {
                if (project.isActive()) {
                    sb.append("{\"id\":\"");
                    sb.append(project.getId());
                    sb.append("\",\"value\":\"");
                    sb.append(project.getName());
                    sb.append("\",\"state\":\"");
                    sb.append(project.getState().getId());
                    sb.append("\"}");
                    sb.append(",");
                }
                count++;
            }
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append("{\"id\":\"0\",\"value\":\"\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    public Vacation findRandomVacation (){
        return vacationApprovalDAO.findRandomVacation();
    }

    public Employee findRandomEmployee (){
        return employeeDAO.findRandomEmployee();
    }

    public Vacation store (Vacation vacation){
        return vacationDAO.storeVacation(vacation);
    }

    public List<Project> getEmployeeProjectPlanByDates(Date beginDate, Date endDate, Employee employee) {
        return projectService.getEmployeeProjectPlanByDates(beginDate, endDate, employee);
    }

    public List<Project> getEmployeeProjectsFromTimeSheetByDates(Date beginDate, Date endDate, Employee employee) {
        return projectService.getEmployeeProjectsFromTimeSheetByDates(beginDate, endDate, employee);
    }


    public  List<Project> gettingData () {

        Employee employee = findRandomEmployee();

        Date beginDate = new Date(113, 7, 10);
        Date endDate = new Date(11, 7, 16);
        Date today = new Date(113, 6, 10);
        Vacation testVacation = findRandomVacation();
        DictionaryItem dictionaryItem  = testVacation.getType();
        DictionaryItem status = testVacation.getStatus();

        Vacation vacation = new Vacation();
        vacation.setEmployee(employee);
        vacation.setBeginDate(beginDate);
        vacation.setEndDate(endDate);
        vacation.setAuthor(employee);
        vacation.setStatus(status);
        vacation.setType(dictionaryItem);
        vacation.setComment("comment_for_vacation_testVacationApprovalResultDAOTest_store");
        vacation.setCreationDate(today);
        storedVacation = store(vacation);

        Date beginDateTest = storedVacation.getBeginDate();
        Date endDateTest = storedVacation.getEndDate();

        List<Project> employeeProjects = getEmployeeProjectPlanByDates(beginDateTest, endDateTest, employee);

        if (employeeProjects.isEmpty()) {
            Integer beforeVacationDays = propertyProvider.getBeforeVacationDays();
            Date periodBeginDate = DateUtils.addDays(vacation.getCreationDate(), 0 - beforeVacationDays);
            employeeProjects = getEmployeeProjectsFromTimeSheetByDates(periodBeginDate, vacation.getCreationDate(), vacation.getEmployee());
        }

        return employeeProjects;
    }

    @Test
    public void testGetProjectListWithOwnerDivisionJson() throws Exception {
        final List<Division> activeDivisions = divisionDAO.getActiveDivisions();

        assertJsonEquals(getProjectListWithOwnerDivisionJson(activeDivisions), projectService.getProjectListWithOwnerDivisionJson(activeDivisions));
    }

    @Test
    public void testGetProjectListJsonWithDivisions() throws Exception {
        final List<Division> divisions = divisionDAO.getActiveDivisions();

        assertJsonEquals(getProjectListJson(divisions), projectService.getProjectListJson(divisions));
    }

    @Test
    public void testGetProjectListJson() throws Exception {
        assertJsonEquals(getProjectListJson(), projectService.getProjectListJson());
    }

    @Test
    public void testGetProjectListAsJson() throws Exception {
        final List<Project> projects = projectDAO.getAll();

        assertJsonEquals(getProjectListAsJson(projects), projectService.getProjectListAsJson(projects));
    }

    @Test
    public void testGetProjectsForVacation () {

        assertEquals(gettingData(), projectService.getProjectsForVacation(storedVacation));

    }
}