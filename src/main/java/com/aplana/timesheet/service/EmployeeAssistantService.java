package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeAssistantDAO;
import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.util.Set;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class EmployeeAssistantService {

    @Autowired
    private EmployeeAssistantDAO employeeAssistantDAO;

    public EmployeeAssistant find(Set<String> managersEmails) {
        return employeeAssistantDAO.find(managersEmails);
    }

    public EmployeeAssistant tryFind(Set<String> managersEmails) {
        try {
            return find(managersEmails);
        } catch (NoResultException ex) {
            return null;
        }
    }

}
