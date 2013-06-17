package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeePlanDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeePlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeePlanService {

    @Autowired
    private EmployeePlanDAO employeePlanDAO;

    public List<EmployeePlan> find(Employee employee, Integer year, Integer month) {
        return employeePlanDAO.find(employee, year, month);
    }

    @Transactional
    public void mergeProjectPlans(List<EmployeePlan> employeePlans) {
        for (EmployeePlan employeePlan : employeePlans) {
            if (employeePlan.getValue() == null || employeePlan.getValue() == 0)
                employeePlanDAO.remove(employeePlan);
            else
                employeePlanDAO.store(employeePlan);
        }
    }

    public EmployeePlan tryFind(Employee employee, Integer year, Integer month, DictionaryItem dictionaryItem) {
        return employeePlanDAO.tryFind(employee, year, month, dictionaryItem);
    }

    public void remove(List<EmployeePlan> employeePlansToDelete) {
        for (EmployeePlan employeePlan : employeePlansToDelete) {
            employeePlanDAO.remove(employeePlan);
        }
    }
}
