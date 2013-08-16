package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.ReportCheckDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 01.08.13
 */
public class PersonalAlertSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(PersonalAlertSenderTest.class);

    @Autowired
    public SendMailService sendMailService;
    @Autowired
    public TSPropertyProvider propertyProvider;
    @Autowired
    public EmployeeDAO employeeDAO;
    @Autowired
    public ReportCheckDAO reportCheckDAO;

    public ReportCheck storedReportCheck2;
    public ReportCheck storedReportCheck3;
    public List<String> emailList;
    public List<ReportCheck> reportCheckList;
    public Iterable<String> iterable;
    public ReportCheck storedReportCheck;
    public PersonalAlertSender personalAlertSender;

    public Employee findRandomEmployeeForVacationWithNotNullLeaders (){
        return employeeDAO.findRandomEmployeeForVacationWithNotNullLeaders();
    }

    public ReportCheck store (ReportCheck reportCheck) {
        return reportCheckDAO.store(reportCheck);
    }

    public List<Mail> getMailList(List<ReportCheck> params) {
        return personalAlertSender.getMailList(params);
    }

    public void  delete (ReportCheck reportCheck){
        reportCheckDAO.delete(reportCheck);
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
    public void getData (){

        Employee employee = findRandomEmployeeForVacationWithNotNullLeaders();

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = Calendar.getInstance().getTime();
        String checkDate = df.format(today);
        Date date1 = new Date(113, 7, 20);
        Date date2 = new Date(113, 7, 21);
        Date date3 = new Date(113, 7, 22);
        String string1 = df.format(date1);
        String string2 = df.format(date2);
        String string3 = df.format(date3);
        List<String> passedDays = new ArrayList<String>();
        passedDays.add(string1);
        passedDays.add(string2);
        passedDays.add(string3);

        Employee employee2 = findRandomEmployeeForVacationWithNotNullLeaders();

        Date date4 = new Date(113, 7, 20);
        Date date5 = new Date(113, 7, 21);
        String string4 = df.format(date4);
        String string5 = df.format(date5);
        List<String> passedDays2 = new ArrayList<String>();
        passedDays.add(string4);
        passedDays.add(string5);

        ReportCheck reportCheck = new ReportCheck();
        reportCheck.setEmployee(employee);
        reportCheck.setDivision(employee.getDivision());
        reportCheck.setReportsNotSendNumber(3);
        reportCheck.setSundayCheck(true);
        reportCheck.setCheckDate(checkDate);
        reportCheck.setPassedDays(passedDays);
        storedReportCheck = store(reportCheck);
        ReportCheck reportCheck2 = new ReportCheck();
        reportCheck2.setEmployee(employee2);
        reportCheck2.setDivision(employee2.getDivision());
        reportCheck2.setReportsNotSendNumber(2);
        reportCheck2.setSundayCheck(true);
        reportCheck2.setCheckDate(checkDate);
        reportCheck2.setPassedDays(passedDays2);
        storedReportCheck2 = store(reportCheck2);

        Employee employee3 = findRandomEmployeeForVacationWithNotNullLeaders();

        Date date6 = new Date(113, 7, 20);
        String string6 = df.format(date6);
        List<String> passedDays3 = new ArrayList<String>();
        passedDays.add(string6);

        ReportCheck reportCheck3 = new ReportCheck();
        reportCheck3.setEmployee(employee3);
        reportCheck3.setDivision(employee3.getDivision());
        reportCheck3.setReportsNotSendNumber(1);
        reportCheck3.setSundayCheck(true);
        reportCheck3.setCheckDate(checkDate);
        reportCheck3.setPassedDays(passedDays3);
        storedReportCheck3 = store(reportCheck3);

        reportCheckList = new ArrayList<ReportCheck>();

        reportCheckList.add(storedReportCheck);
        reportCheckList.add(storedReportCheck2);
        reportCheckList.add(storedReportCheck3);

        emailList = new ArrayList<String>();
        emailList.add(storedReportCheck.getEmployee().getEmail());
        if (storedReportCheck.getEmployee().getEmail() != storedReportCheck2.getEmployee().getEmail() )  {
            emailList.add(storedReportCheck2.getEmployee().getEmail());
        }
        if (storedReportCheck2.getEmployee().getEmail() != storedReportCheck3.getEmployee().getEmail() )  {
            emailList.add(storedReportCheck3.getEmployee().getEmail());
        }

        personalAlertSender = new PersonalAlertSender(sendMailService,propertyProvider);
    }

    @After
    public void clearData (){

        delete(storedReportCheck);
        delete(storedReportCheck2);
        delete(storedReportCheck3);
    }

    @Test
    public void testManagerAlertSenderTest () {

        List<Mail> mails = getMailList(reportCheckList);
        List<String> actual = new ArrayList<String>();
        actual = conventingMailToString(mails);
        java.util.Collections.sort(emailList);
        java.util.Collections.sort(actual);
        logger.info("expected = {}", emailList);
        logger.info("actual = {}", actual);

        assertEquals(emailList, actual);
    }
}