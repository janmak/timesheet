package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang.StringUtils;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractSenderWithAssistants<T> extends MailSender<T> {

    public AbstractSenderWithAssistants(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    protected final String getAssistantEmail(Employee employee) {
        final EmployeeAssistant employeeAssistant = sendMailService.getEmployeeAssistant(employee);

        return (employeeAssistant == null) ? StringUtils.EMPTY : employeeAssistant.getAssistant().getEmail();
    }
}
