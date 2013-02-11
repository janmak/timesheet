package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeProjectPlanDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeProjectPlan;
import com.aplana.timesheet.dao.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeeProjectPlanService {

    @Autowired
    private EmployeeProjectPlanDAO employeeProjectPlanDAO;

    public List<EmployeeProjectPlan> find(Employee employee, Integer year, Integer month) {
        return employeeProjectPlanDAO.find(employee, year, month);
    }

    public void store(List<EmployeeProjectPlan> employeeProjectPlans) {
        for (EmployeeProjectPlan employeeProjectPlan : employeeProjectPlans) {
            employeeProjectPlanDAO.store(employeeProjectPlan);
        }

    }

    public EmployeeProjectPlan tryFind(Employee employee, Integer year, Integer month, Project project) {
        try {
            return employeeProjectPlanDAO.find(employee, year, month, project);
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void remove(Employee employee, Integer year, Integer month) {
        employeeProjectPlanDAO.remove(employee, year, month);
    }
}
