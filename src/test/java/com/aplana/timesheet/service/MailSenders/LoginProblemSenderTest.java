package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 26.07.13
 */
public class LoginProblemSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackSenderTest.class);

    @Autowired
    public SendMailService sendMailService;
    @Autowired
    public TSPropertyProvider propertyProvider;
    @Autowired
    public EmployeeDAO employeeDAO;

    public AdminMessageForm adminMessageForm;
    public List<String> emails;
    public LoginProblemSender loginProblemSender;
    public Iterable<String> iterable;

    public Employee findRandomEmployeeForVacationWithNotNullLeaders(){
        return employeeDAO.findRandomEmployeeForVacationWithNotNullLeaders();
    }

    public List<Mail> getMailList(AdminMessageForm params) {
        return loginProblemSender.getMailList(params);
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

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date(113, 7, 20);
        String string = df.format(date);
        String mail = "Evgenia.Makedonskaya@aplana.com";
        logger.info("lalala = {}", mail);

        adminMessageForm = new AdminMessageForm();
        adminMessageForm.setEmail(mail);
        adminMessageForm.setDate(string);
        adminMessageForm.setDescription("я пишу письмо админу ля ля ля");
        adminMessageForm.setError("у меня полно ошибок ля ля ля");
        adminMessageForm.setName("Краказябр Иванович");

        emails = new ArrayList<String>();
        emails.add(adminMessageForm.getEmail());

        loginProblemSender = new LoginProblemSender(sendMailService, propertyProvider);
    }

    @Test
    public void testFeedbackSenderTest () {
        List<Mail> mails = getMailList(adminMessageForm);
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