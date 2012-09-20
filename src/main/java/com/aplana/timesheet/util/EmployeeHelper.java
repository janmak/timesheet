package com.aplana.timesheet.util;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeHelper {

	@Autowired
	private EmployeeService employeeService;

	public String getEmployeeListJson(List<Division> divisions) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < divisions.size(); i++) {
			List<Employee> employees = employeeService.getEmployees(divisions.get(i));
			sb.append("{divId:'");
			sb.append(divisions.get(i).getId());
			sb.append("', divEmps:[");
			if (employees.size() > 0) {
				for (int j = 0; j < employees.size(); j++) {
					sb.append("{id:'");
					sb.append(employees.get(j).getId());
					sb.append("', value:'");
					sb.append(employees.get(j).getName());
					sb.append("', jobId:'");
					sb.append(employees.get(j).getJob().getId());
					sb.append("'}");
					if (j < (employees.size() - 1)) {
						sb.append(", ");
					}
				}
				sb.append("]}");
			} else {
				sb.append("{id:'0', value:''}]}");
			}

			if (i < (divisions.size() - 1)) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
