package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 24.07.13
 */
public class ExceptionSenderTest  extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionSenderTest.class);

    @Autowired
    public TSPropertyProvider propertyProvider;
    @Autowired
    public SendMailService sendMailService;

    public ExceptionSender exceptionSender;

    public List<String> mailProblemsAndProposalsCoaddress;
    public Iterable<String> iterable;
    public String problem;

    private static Set<String> clearDoubles(Iterable<String> emails) {

        final Set<String> uniqueEmails = Sets.newHashSet();

        for (String email : emails) {
            uniqueEmails.addAll(Arrays.asList(email.split("\\s*,\\s*")));
        }

        return uniqueEmails;
    }

    public List<Mail> getMailList(String problem) {
        return exceptionSender.getMailList(problem);
    }

    public static <String> List<String> toList(Iterable<String> iterable) {
        if(iterable instanceof List) {
            return (List<String>) iterable;
        }
        ArrayList<String> list = new ArrayList<String>();
        if(iterable != null) {
            for(String e: iterable) {
                list.add(e);
            }
        }
        return list;
    }

    @Before
    public void gettingData () {

        propertyProvider.getMailProblemsAndProposalsCoaddress(0);
        List<String> mail = new ArrayList<String>();
        mail.add(propertyProvider.getMailProblemsAndProposalsCoaddress(0));
        Set<String> forDeletingDoubles = clearDoubles(mail);
        mailProblemsAndProposalsCoaddress = new ArrayList<String>(forDeletingDoubles);

        problem = "i have a problem with bla-bla-bla-bla";

        exceptionSender = new ExceptionSender(sendMailService,propertyProvider);
    }

    @Test
    public void testExceptionSenderTest () {

        logger.info("lalala = {}", propertyProvider.getTimesheetMailMarker() + " У одного из пользователей во время работы произошла ошибка. Подробности в письме.");

        List<Mail> mails = getMailList(problem);
        Mail mail = mails.get(0);
        iterable = mail.getToEmails();

        List<String> expected = new ArrayList<String>(mailProblemsAndProposalsCoaddress);
        List<String> actual = toList(iterable);
        java.util.Collections.sort(expected);
        java.util.Collections.sort(actual);

        assertEquals(expected, actual);
    }
}