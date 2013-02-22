package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
public abstract class AbstractVacationApprovalSenderWithCopyToAuthor extends AbstractVacationApprovalSender {

    public AbstractVacationApprovalSenderWithCopyToAuthor(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    final protected List<Mail> getMailList(VacationApproval params) {
        List<Mail> mailList = getMainMailList(params);
        Employee author = params.getVacation().getAuthor();
        Employee employee = params.getVacation().getEmployee();
        if (! author.getId().equals(employee.getId())) {
            addAdressToCcEmails(params.getVacation().getAuthor().getEmail(), mailList);
        }

        return mailList;
    }

    protected abstract List<Mail> getMainMailList(VacationApproval params);

    final private List<Mail> addAdressToCcEmails(String email, List<Mail> mailList) {
        for (Mail mail : mailList) {
            ArrayList<String> emails = Lists.newArrayList(mail.getCcEmails());
            emails.add(email);
            mail.setCcEmails(emails);
        }

        return mailList;
    }
}
