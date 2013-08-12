package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.CalendarDAO;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 01.08.13
 */
public class TimeSheetDeletedSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetDeletedSenderTest.class);

    @Autowired
    public SendMailService sendMailService;
    @Autowired
    public TSPropertyProvider propertyProvider;
    @Autowired
    public EmployeeDAO employeeDAO;
    @Autowired
    public TimeSheetDAO timeSheetDAO;
    @Autowired
    public CalendarDAO calendarDAO;

    public List<String> emailList;
    public TimeSheetDeletedSender timeSheetDeletedSender;
    public TimeSheet timeSheet;

    public Employee findRandomEmployee() {
        return employeeDAO.findRandomEmployee();
    }

    public TimeSheet findLastTimeSheetBefore(com.aplana.timesheet.dao.entity.Calendar date, Integer employeeId) {
        return timeSheetDAO.findLastTimeSheetBefore(date,employeeId);
    }

    public com.aplana.timesheet.dao.entity.Calendar find(Timestamp date) {
        return calendarDAO.find(date);
    }

    public List<Mail> getMailList(TimeSheet params) {
        return timeSheetDeletedSender.getMailList(params);
    }

    private static Set<String> clearDoubles(Iterable<String> emails) {

        final Set<String> uniqueEmails = Sets.newHashSet();

        for (String email : emails) {
            uniqueEmails.addAll(Arrays.asList(email.split("\\s*,\\s*")));
        }

        return uniqueEmails;
    }

    protected Collection<String> getToEmails(TimeSheet input) {
        Integer empId = input.getEmployee().getId();

        Set<String> result = new HashSet<String>();

        result.add(input.getEmployee().getEmail());
        result.add(TSPropertyProvider.getMailFromAddress());
        result.add(sendMailService.getEmployeesManagersEmails(empId));
        result.add(sendMailService.getProjectsManagersEmails(input));
        result.add(sendMailService.getProjectParticipantsEmails(input));

        return result;
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

    public List<String> conventingMailToString (List<Mail> mails) {
        List<String> actual = new ArrayList<String>();

        for (Integer n = 0; n < mails.size(); n++) {
            Mail mail = mails.get(n);
            Iterable<String> iterable = mail.getToEmails();
            List<String> converting = toList(iterable);
            actual.addAll(converting);
        }
        return actual;
    }

    @Before
    public void gettingData () throws ParseException {

        Employee employee = findRandomEmployee();
        Integer employeeId = employee.getId();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = dateFormat.parse("24/05/2013");
        long time = date.getTime();
        Timestamp timestamp = new Timestamp(time);
        logger.info("fbgdfsg = {}", timestamp);

        com.aplana.timesheet.dao.entity.Calendar calendar = find(timestamp);
        logger.info("gdg2 = {}", calendar);

        timeSheet = findLastTimeSheetBefore(calendar ,employeeId);

        List<String> email = new ArrayList<String>(getToEmails(timeSheet));
        Set<String> forDeletingDoubles = clearDoubles(email);
        emailList = new ArrayList<String>(forDeletingDoubles);

        timeSheetDeletedSender = new TimeSheetDeletedSender(sendMailService, propertyProvider);
    }

    @Test
    public void testTimeSheetDeletedSenderTest () {
        List<Mail> mails = getMailList(timeSheet);
        List<String> actual = new ArrayList<String>();
        actual = conventingMailToString(mails);
        java.util.Collections.sort(emailList);
        java.util.Collections.sort(actual);
        logger.info("expected = {}", emailList);
        logger.info("actual = {}", actual);

        assertEquals(emailList, actual);
    }
}