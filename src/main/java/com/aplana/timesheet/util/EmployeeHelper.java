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

import java.util.List;

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

	public String getEmployeeListJson(List<Division> divisions, Boolean filterFired) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final List<Employee> employees = employeeService.getEmployees(division, filterFired);
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
                    employeesBuilder.withElement(
                            anObjectBuilder().
                                    withField(ID, aStringBuilder(employee.getId())).
                                    withField(VALUE, JsonNodeBuilders.aStringBuilder(getValue(employee))).
                                    withField("jobId", aStringBuilder(employee.getJob().getId())).
                                    withField("dateByDefault", JsonNodeBuilders.aStringBuilder(
                                            dateToString(timeSheetService.getLastWorkdayWithoutTimesheet(employee.getId()), DATE_FORMAT))).
                                    withField("firstWorkDate", JsonNodeBuilders.aStringBuilder(
                                            dateToString(timeSheetService.getEmployeeFirstWorkDay(employee.getId()), DATE_FORMAT)))
                    );
                }
            }

            builder.withElement(nodeBuilder.withField("divEmps", employeesBuilder));
        }

		return JsonUtil.format(builder.build());
	}

    private String getValue(Employee employee) {
        final StringBuilder sb = new StringBuilder(employee.getName());

        if( null != employee.getEndDate()) {
            sb.append(" (уволен: ");
            sb.append(dateToString(employee.getEndDate(), DATE_FORMAT));
            sb.append(")");
        }

        return sb.toString();
    }

}
