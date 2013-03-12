package com.aplana.timesheet.util;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonNodeBuilders;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.TimeSheetService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static argo.jdom.JsonNodeBuilders.anArrayBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static com.aplana.timesheet.util.DateTimeUtil.dateToString;
import static com.aplana.timesheet.util.JsonUtil.aStringBuilder;

@Service
public class EmployeeHelper {

    private static final String ID = "id";
    private static final String VALUE = "value";
    private static final String DATE_FORMAT = "dd.MM.yyyy";

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetService timeSheetService;

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

            nodeBuilder.withField("divId", aStringBuilder(division.getId()));

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

                        objectNodeBuilder.withField("jobId", aStringBuilder(employee.getJob().getId())).
                                withField("dateByDefault", JsonNodeBuilders.aStringBuilder(
                                        dateToString(defaultDate, DATE_FORMAT))).
                                withField("firstWorkDate", JsonNodeBuilders.aStringBuilder(
                                        dateToString(employee.getStartDate(), DATE_FORMAT)));
                    }
                    employeesBuilder.withElement(objectNodeBuilder);
                }
            }

            builder.withElement(nodeBuilder.withField("divEmps", employeesBuilder));
        }

        return JsonUtil.format(builder.build());
    }

    private String getValue(Employee employee) {
        final StringBuilder sb = new StringBuilder(employee.getName());

        if (null != employee.getEndDate()) {
            sb.append(" (уволен: ");
            sb.append(dateToString(employee.getEndDate(), DATE_FORMAT));
            sb.append(")");
        }

        return sb.toString();
    }

}
