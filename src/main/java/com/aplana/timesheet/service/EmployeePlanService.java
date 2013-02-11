package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeePlanDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeePlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
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

    public void store(List<EmployeePlan> employeePlans) {
        for (EmployeePlan employeePlan : employeePlans) {
            employeePlanDAO.store(employeePlan);
        }

    }

    public EmployeePlan tryFind(Employee employee, Integer year, Integer month, DictionaryItem dictionaryItem) {
        try {
            return employeePlanDAO.find(employee, year, month, dictionaryItem);
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void remove(List<EmployeePlan> employeePlansToDelete) {
        for (EmployeePlan employeePlan : employeePlansToDelete) {
            employeePlanDAO.remove(employeePlan);
        }
    }
}
