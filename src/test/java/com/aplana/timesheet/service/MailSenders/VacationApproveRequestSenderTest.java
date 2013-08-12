package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.VacationApprovalDAO;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.ManagerRoleNameService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.service.VacationApprovalService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 08.08.13
 */
public class VacationApproveRequestSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(VacationApproveRequestSenderTest.class);

    @Autowired
    public SendMailService sendMailService;
    @Autowired
    public TSPropertyProvider propertyProvider;
    @Autowired
    public EmployeeDAO employeeDAO;
    @Autowired
    public VacationApprovalDAO vacationApprovalDAO;
    @Autowired
    public VacationDAO vacationDAO;
    @Autowired
    public VacationApprovalService vacationApprovalService;
    @Autowired
    public ManagerRoleNameService managerRoleNameService;

    public Vacation storedVacation;
    public VacationApproval testVacationApproval;
    public List<String> emailList;
    public VacationApproveRequestSender vacationApproveRequestSender;
    public Iterable<String> iterable;

    public Employee findRandomEmployee (){
        return employeeDAO.findRandomEmployee();
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

    public VacationApproval store (VacationApproval vacationApproval){
        return vacationApprovalDAO.store(vacationApproval);
    }

    public void  delete (Vacation vacation){
        vacationDAO.delete(vacation);
    }

    public void  delete (VacationApproval vacationApproval){
        vacationApprovalDAO.delete(vacationApproval);
    }

    public List<Mail> getMainMailList(VacationApproval vacationApproval) {
        return vacationApproveRequestSender.getMailList(vacationApproval);
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
        Employee employee = findRandomEmployee();
        Employee manager = employee.getManager();

        Date beginDate = new Date(114, 0, 10);
        Date endDate = new Date(114, 0, 16);
        Date today = new Date(113, 7, 10);
        Date responseDate = new Date(113, 7, 15);
        Vacation testVacation = findRandomVacation();
        DictionaryItem dictionaryItem  = testVacation.getType();
        DictionaryItem status = testVacation.getStatus();
        VacationApproval testVacationApproval2 = findVacationApprovalDAO(testVacation);
        String uid =  testVacationApproval2.getUid();

        Vacation vacation = new Vacation();
        vacation.setEmployee(employee);
        vacation.setBeginDate(beginDate);
        vacation.setEndDate(endDate);
        vacation.setAuthor(employee);
        vacation.setStatus(status);
        vacation.setType(dictionaryItem);
        vacation.setComment("comment_for_vacation_testVacationApprovalResultDAOTest_store");
        vacation.setCreationDate(today);
        storedVacation = store(vacation);

        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setVacation(storedVacation);
        vacationApproval.setRequestDate(today);
        vacationApproval.setResult(true);
        vacationApproval.setManager(manager);
        vacationApproval.setUid(uid);
        vacationApproval.setResponseDate(responseDate);
        testVacationApproval = store(vacationApproval);

        emailList = new ArrayList<String>();
        emailList.add(vacationApproval.getManager().getEmail());

        vacationApproveRequestSender = new VacationApproveRequestSender(sendMailService, propertyProvider,vacationApprovalService, managerRoleNameService);
    }

    @After
    public void tearData (){

        delete(testVacationApproval);
        delete(storedVacation);
    }

    @Test
    public void testVacationApproveRequestSenderTest () {
        List<Mail> mails = getMainMailList(testVacationApproval);
        Mail mail = mails.get(0);
        iterable = mail.getToEmails();

        List<String> actual = toList(iterable);
        java.util.Collections.sort(emailList);
        java.util.Collections.sort(actual);

        assertEquals(emailList, actual);
    }
}