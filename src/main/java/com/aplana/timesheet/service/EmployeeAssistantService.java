package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeAssistantDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeeAssistantService {

    @Autowired
    private EmployeeAssistantDAO employeeAssistantDAO;

    public EmployeeAssistant find(Employee employee) {
        return employeeAssistantDAO.find(employee);
    }

    public  EmployeeAssistant tryFind(Employee employee) {
        try {
            return find(employee);
        } catch (NoResultException ex) {
            return null;
        }
    }

}
