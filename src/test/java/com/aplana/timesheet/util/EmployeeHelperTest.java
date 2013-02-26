package com.aplana.timesheet.util;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class EmployeeHelperTest extends AbstractTest {

    @Autowired
    private DivisionDAO divisionDAO;

    @Autowired
    private EmployeeHelper employeeHelper;

    @Autowired
    private EmployeeService employeeService;

    final Boolean filterFired = Boolean.FALSE;
    private Division division;
    private String json;

    @Before
    public void setUp() throws Exception {
        division = divisionDAO.getActiveDivisions().get(0);
        json = getJson();
    }

    private String getJson() {
        final List<Division> divisions = Arrays.asList(division);

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < divisions.size(); i++) {
            List<Employee> employees = employeeService.getEmployees(divisions.get(i), filterFired);
            sb.append("{\"divId\":\"");
            sb.append(divisions.get(i).getId());
            sb.append("\",\"divEmps\":[");
            if (employees.size() > 0) {
                for (int j = 0; j < employees.size(); j++) {
                    sb.append("{\"id\":\"");
                    sb.append(employees.get(j).getId());
                    sb.append("\",\"value\":\"");
                    sb.append(employees.get(j).getName());
                    if( null != employees.get(j).getEndDate()) {
                        sb.append(" (уволен: ");
                        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                        sb.append(df.format(employees.get(j).getEndDate()));
                        sb.append(")");
                    }
                    sb.append("\",\"jobId\":\"");
                    sb.append(employees.get(j).getJob().getId());
                    sb.append("\"}");
                    if (j < (employees.size() - 1)) {
                        sb.append(",");
                    }
                }
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

    @Test
    public void testGetEmployeeListJson() throws Exception {
        assertEquals(json, employeeHelper.getEmployeeListJson(Arrays.asList(division), filterFired));
    }


}
