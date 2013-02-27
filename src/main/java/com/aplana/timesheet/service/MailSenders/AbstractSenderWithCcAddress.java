package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
public abstract class AbstractSenderWithCcAddress<T> extends AbstractSenderWithAssistants<T>
        implements MailWithCcAddresses<T>{

    public AbstractSenderWithCcAddress(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    final protected List<Mail> getMailList(T params) {
        List<Mail> mailList = getMainMailList(params);
        String ccEmail = getCcEmail(params);
        addAddressToCcEmails(mailList, ccEmail);

        return mailList;
    }

    final protected List<Mail> addAddressToCcEmails(List<Mail> mailList, String ccEmail) {
        for (Mail mail : mailList) {
            ArrayList<String> emails = Lists.newArrayList(mail.getCcEmails());
            emails.add(ccEmail);
            mail.setCcEmails(emails);
        }

        return mailList;
    }

}
