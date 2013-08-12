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
public class VacationCreateSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(VacationCreateSenderTest.class);

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
    public List<String> emailList;
    public VacationCreateSender vacationCreateSender;
    public Iterable<String> iterable;

    public Vacation findRandomVacation (){
        return vacationApprovalDAO.findRandomVacation();
    }

    public Vacation store (Vacation vacation){
        return vacationDAO.storeVacation(vacation);
    }

    public Employee findRandomEmployeeWithNotNullParams (){
        return employeeDAO.findRandomEmployeeWithNotNullParams();
    }

    public VacationApproval findVacationApprovalDAO(Vacation  vacationId) {
        return vacationApprovalDAO.findVacationApprovalDAO(vacationId);
    }

    public void  delete (Vacation vacation){
        vacationDAO.delete(vacation);
    }

    public List<Mail> getMailList(Vacation vacation) {
        return vacationCreateSender.getMailList(vacation);
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
        Employee employee = findRandomEmployeeWithNotNullParams();
        Employee manager = employee.getManager();

        Date beginDate = new Date(114, 0, 10);
        Date endDate = new Date(114, 0, 16);
        Date today = new Date(113, 7, 10);
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
        vacation.setComment("comment_for_vacation_testVacationApprovalResultDAOTest_store");
        vacation.setCreationDate(today);
        storedVacation = store(vacation);

        emailList = new ArrayList<String>();
        if (vacation.getEmployee().getDivision().getVacationEmail() != null) {
            emailList.add(vacation.getEmployee().getDivision().getVacationEmail());
        }

        vacationCreateSender = new VacationCreateSender(sendMailService, propertyProvider,vacationApprovalService,managerRoleNameService);
    }

    @Test
    public void testVacationCreateSenderTest () {
        List<Mail> mails = getMailList(storedVacation);
        Mail mail = mails.get(0);
        iterable = mail.getToEmails();

        List<String> actual = toList(iterable);
        java.util.Collections.sort(emailList);
        java.util.Collections.sort(actual);

        assertEquals(emailList, actual);
    }
}