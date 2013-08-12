package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 26.07.13
 */
public class FeedbackSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackSenderTest.class);

    @Autowired
    public SendMailService sendMailService;
    @Autowired
    public TSPropertyProvider propertyProvider;
    @Autowired
    public EmployeeDAO employeeDAO;

    public List<String> emails;
    public FeedbackSender feedbackSender;
    public Iterable<String> iterable;
    public FeedbackForm feedbackForm;

    public Employee findRandomEmployeeForVacationWithNotNullLeaders(){
        return employeeDAO.findRandomEmployeeForVacationWithNotNullLeaders();
    }

    public List<Mail> getMailList(FeedbackForm params) {
        return feedbackSender.getMailList(params);
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

        Employee employee = findRandomEmployeeForVacationWithNotNullLeaders();

        Integer employeeId = employee.getId();
        Integer divisionId = employee.getDivision().getId();


        feedbackForm = new FeedbackForm();
        feedbackForm.setEmployeeId(employeeId);
        feedbackForm.setDivisionId(divisionId);
        feedbackForm.setFeedbackDescription("Не смог(ла) отправить отчет");
        feedbackForm.setFeedbackTypeName("cantsendreport");
        feedbackForm.setFeedbackType(5);

        emails = new ArrayList<String>();

        String email = propertyProvider.getMailProblemsAndProposalsCoaddress(feedbackForm.getFeedbackType());
        emails.add(email);

        feedbackSender = new FeedbackSender(sendMailService, propertyProvider);
    }

    @Test
    public void testFeedbackSenderTest () {

        List<Mail> mails = getMailList(feedbackForm);
        Mail mail = mails.get(0);
        iterable = mail.getToEmails();

        List<String> expected = new ArrayList<String>(emails);
        List<String> actual = toList(iterable);
        java.util.Collections.sort(expected);
        java.util.Collections.sort(actual);
        logger.info("lalala = {}", expected);
        logger.info("lalala1 = {}", actual);

        assertEquals(expected, actual);
    }
}