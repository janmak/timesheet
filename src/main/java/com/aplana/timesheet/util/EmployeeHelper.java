package com.aplana.timesheet.util;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.RegionService;
import com.aplana.timesheet.service.TimeSheetService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.anArrayBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static com.aplana.timesheet.util.DateTimeUtil.dateToString;
import static com.aplana.timesheet.util.JsonUtil.aStringBuilder;

@Service
public class EmployeeHelper {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeHelper.class);

    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String DIVISION_ID = "divId";
    private static final String REGION_ID = "regId";
    private static final String MANAGER_ID = "manId";
    private static final String JOB_ID = "jobId";
    private static final String DIVISION_EMPLOYEES = "divEmps";
    private static final String DATE_BY_DEFAULT = "dateByDefault";
    private static final String FIRST_WORK_DATE = "firstWorkDate";
    private static final String DEFAULT_MANAGER = "-1";
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private RegionService regionService;

    @Transactional(readOnly = true)
    public String getEmployeeListJson(List<Division> divisions, Boolean filterFired) {
        return getEmployeeListJson(divisions, filterFired, false);
    }

    @Transactional(readOnly = true)
    public String getEmployeeListJson(List<Division> divisions, Boolean filterFired, Boolean addDetails) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, filterFired);
            final Map<Integer, Date> lastWorkdays = timeSheetService.getLastWorkdayWithoutTimesheetMap(division);
            final JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            final JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();

            nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));

            if (employees.isEmpty()) {
                employeesBuilder.withElement(
                        anObjectBuilder().
                                withField(ID, aStringBuilder(0)).
                                withField(VALUE, JsonNodeBuilders.aStringBuilder(StringUtils.EMPTY))
                );
            } else {
                for (Employee employee : employees) {
                    JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                            withField(ID, aStringBuilder(employee.getId())).
                            withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee)));
                    if (addDetails) {

                        Date defaultDate = lastWorkdays.get(employee.getId());
                        if (defaultDate == null)
                            defaultDate = employee.getStartDate();

                        Date curDate = java.util.Calendar.getInstance().getTime();
                            if (defaultDate.after(curDate)) {
                                defaultDate = curDate;
                            }

                        objectNodeBuilder.withField(JOB_ID, aStringBuilder(employee.getJob().getId())).
                                withField(DATE_BY_DEFAULT, JsonNodeBuilders.aStringBuilder(
                                        dateToString(defaultDate, DATE_FORMAT))).
                                withField(FIRST_WORK_DATE, JsonNodeBuilders.aStringBuilder(
                                        dateToString(employee.getStartDate(), DATE_FORMAT)));
                    }
                    employeesBuilder.withElement(objectNodeBuilder);
                }
            }

            builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
        }

        return JsonUtil.format(builder.build());
    }

    @Transactional(readOnly = true)
    public String getEmployeeListWithRegAndManJson(List<Division> divisions, Boolean filterFired){
        return getEmployeeListWithRegAndManJson(divisions, filterFired, false);
    }

    @Transactional(readOnly = true)
    public String getEmployeeListWithRegAndManJson(List<Division> divisions, Boolean filterFired, Boolean addDetails){
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, filterFired);
            final Map<Integer, Date> lastWorkdays = timeSheetService.getLastWorkdayWithoutTimesheetMap(division);

            if (!employees.isEmpty()) {
                for (Region region : regionService.getRegions()){
                    List<Employee> managerList = getManagerList(employees);
                    for (Employee manager : managerList){
                        JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
                        JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();
                        nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));
                        nodeBuilder.withField(REGION_ID, aStringBuilder(region.getId()));
                        nodeBuilder.withField(MANAGER_ID, aStringBuilder(manager.getId()));
                        for (Employee employee : employees) {
                            if ((employee.getRegion().getId().equals(region.getId()))
                                    &&(employee.getManager()!=null)
                                    &&(employee.getManager().getId().equals(manager.getId()))){
                                JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                                        withField(ID, aStringBuilder(employee.getId())).
                                        withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee))).
                                        withField(REGION_ID, aStringBuilder(employee.getRegion().getId()));
                                if (addDetails) {

                                    Date defaultDate = lastWorkdays.get(employee.getId());
                                    if (defaultDate == null)
                                        defaultDate = employee.getStartDate();

                                    objectNodeBuilder.withField(JOB_ID, aStringBuilder(employee.getJob().getId())).
                                            withField(DATE_BY_DEFAULT, JsonNodeBuilders.aStringBuilder(
                                                    dateToString(defaultDate, DATE_FORMAT))).
                                            withField(FIRST_WORK_DATE, JsonNodeBuilders.aStringBuilder(
                                                    dateToString(employee.getStartDate(), DATE_FORMAT)));
                                }
                                employeesBuilder.withElement(objectNodeBuilder);
                            }
                        }
                        builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
                    }

                    /** Специальный случай, когда у работника нет начальника*/
                    JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
                    JsonArrayNodeBuilder employeesBuilder = anArrayBuilder();
                    nodeBuilder.withField(DIVISION_ID, aStringBuilder(division.getId()));
                    nodeBuilder.withField(REGION_ID, aStringBuilder(region.getId()));
                    nodeBuilder.withField(MANAGER_ID, JsonNodeBuilders.aStringBuilder(DEFAULT_MANAGER));
                    for (Employee employee : employees) {
                        if ((employee.getRegion().getId().equals(region.getId()))
                                && (employee.getManager() == null)){
                            JsonObjectNodeBuilder objectNodeBuilder = anObjectBuilder().
                                    withField(ID, aStringBuilder(employee.getId())).
                                    withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee)));
                            if (addDetails) {

                                Date defaultDate = lastWorkdays.get(employee.getId());
                                if (defaultDate == null)
                                    defaultDate = employee.getStartDate();

                                objectNodeBuilder.withField(JOB_ID, aStringBuilder(employee.getJob().getId())).
                                        withField(DATE_BY_DEFAULT, JsonNodeBuilders.aStringBuilder(
                                                dateToString(defaultDate, DATE_FORMAT))).
                                        withField(FIRST_WORK_DATE, JsonNodeBuilders.aStringBuilder(
                                                dateToString(employee.getStartDate(), DATE_FORMAT)));
                            }
                            employeesBuilder.withElement(objectNodeBuilder);
                        }
                    }
                    builder.withElement(nodeBuilder.withField(DIVISION_EMPLOYEES, employeesBuilder));
                }
            }
        }

        return JsonUtil.format(builder.build());
    }

    @Transactional(readOnly = true)
    public String getManagerListJson(){
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        List<Employee> managerList = employeeService.getManagerListForAllEmployee();
        for (Employee e : managerList){
            JsonObjectNodeBuilder nodeBuilder = anObjectBuilder();
            nodeBuilder.withField(ID, aStringBuilder(e.getId()));
            nodeBuilder.withField(VALUE, JsonNodeBuilders.aStringBuilder(e.getName()));
            nodeBuilder.withField(DIVISION_ID, aStringBuilder(e.getDivision().getId()));
            builder.withElement(nodeBuilder);
        }
        return JsonUtil.format(builder.build());
    }


    private List<Employee> getManagerList(List<Employee> employees){
        List<Employee> managerList = new ArrayList<Employee>();
        for (Employee emp : employees){
            Boolean isAdded = false;
            for (Employee man : managerList){
                if ((emp.getManager() != null) && (man.getId().equals(emp.getManager().getId()))){
                    isAdded = true;
                }
            }
            if (!isAdded && emp.getManager() != null ){
                managerList.add(emp.getManager());
            }
        }
        return managerList;
    }

    private String getValue(Employee employee) {
        final StringBuilder sb = new StringBuilder(employee.getName());
        Timestamp endDate =  employee.getEndDate();

        if (null != endDate) {
            if (DateUtils.truncatedCompareTo(endDate, new Date(), Calendar.DAY_OF_MONTH) < 0) {
                sb.append(" (уволен: ").append(dateToString(employee.getEndDate(), DATE_FORMAT)).append(")");
            }
        }

        return sb.toString();
    }

}
