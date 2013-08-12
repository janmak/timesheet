package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.VacationApprovalDAO;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
  * User: Emakedonskaya
  * Date: 17.07.13
  */

public class VacationApprovedSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(VacationApprovedSenderTest.class);

    @Autowired
    public EmployeeDAO employeeDAO;
    @Autowired
    public VacationApprovalDAO vacationApprovalDAO;
    @Autowired
    public VacationDAO vacationDAO;

    Vacation storedVacation;
    List<String> finalMailList;
    Date today;
    Employee employee;
    Iterable<String> iterable;

    @Autowired
    public SendMailService sendMailService;

    @Autowired
    public TSPropertyProvider propertyProvider;

    public VacationApprovedSender vacationApprovedSender;

    public Employee findRandomEmployeeForVacationApprovedWithNotNullLeaders (){
        return employeeDAO.findRandomEmployeeForVacationApprovedWithNotNullLeaders();
    }

    public Vacation findRandomVacation (){
        return vacationApprovalDAO.findRandomVacation();
    }

    public VacationApproval findVacationApprovalDAO(Vacation  vacationId) {
        return vacationApprovalDAO.findVacationApprovalDAO(vacationId);
    }

    public Vacation store (Vacation vacation){
        return vacationDAO.storeVacation(vacation);
    }

    public List<Mail> getMainMailList(Vacation vacation) {
        return vacationApprovedSender.getMainMailList(vacation);
    }

    public void  delete (Vacation vacation){
        vacationDAO.delete(vacation);
    }

    public Collection<String> getAdditionalEmailsForRegion2 (Region region) {
        String additionalEmails = region.getAdditionalEmails().trim();

        return  (StringUtils.isNotBlank(additionalEmails)) ? Arrays.asList(additionalEmails.split("\\s*,\\s*")) : Arrays.asList(StringUtils.EMPTY);
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
    public void setData() {

        employee = findRandomEmployeeForVacationApprovedWithNotNullLeaders();

        Date beginDate = new Date(114, 0, 10);
        Date endDate = new Date(114, 0, 16);
        today = new Date(113, 7, 10);
        Vacation testVacation = findRandomVacation();
        DictionaryItem dictionaryItem  = testVacation.getType();
        DictionaryItem status = testVacation.getStatus();

        Vacation vacation = new Vacation();
        vacation.setEmployee(employee);
        vacation.setBeginDate(beginDate);
        vacation.setEndDate(endDate);
        vacation.setAuthor(employee);
        vacation.setStatus(status);
        vacation.setType(dictionaryItem);
        vacation.setComment("comment_for_VacationApprovedSenderTest");
        vacation.setCreationDate(today);
        storedVacation = store(vacation);

        Region region = employee.getRegion();
        finalMailList = new ArrayList<String>(getAdditionalEmailsForRegion2(region));

        Division employeeDivision = employee.getDivision();   //достаем мейл отдела кадров
        String employeeVacationEmail = employee.getDivision().getVacationEmail();

        if (employeeVacationEmail != null){//добавляем мейл отдела кадров в общий список
            finalMailList.add(employeeVacationEmail);
        }

        Employee leaderId = employeeDivision.getLeaderId(); //достаем мейл РКЦ
        String leaderEmail = leaderId.getEmail();


        if (leaderEmail != null) {
            finalMailList.add(leaderEmail);
        }

        Employee manager2 = employee.getManager2();  //достаем второго линейного руководителя
        String manager2Email = manager2.getEmail();

        if (manager2Email != null) {
            finalMailList.add(manager2Email);
        }

        List<String> emails = new ArrayList<String>();

        vacationApprovedSender = new VacationApprovedSender(sendMailService,propertyProvider, emails);


        List<Mail> testMailVacation = getMainMailList(storedVacation);
        Mail mail = testMailVacation.get(0);
        iterable = mail.getCcEmails();
    }

    @After
    public void deleteData () {

        delete(storedVacation);
    }

    @Test
    public void testVacationApprovedSenderTest (){

        List<String> expected = new ArrayList<String>(finalMailList);
        List<String> actual = toList(iterable);
        java.util.Collections.sort(expected);
        java.util.Collections.sort(actual);

        assertEquals(expected, actual);
    }
}