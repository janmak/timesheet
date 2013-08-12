package com.aplana.timesheet.dao;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.dao.entity.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 04.07.13
*/
public class VacationApprovalResultDAOTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalResultDAOTest.class);

    @Autowired
    public VacationApprovalResultDAO vacationApprovalResultDAO;
    @Autowired
    public VacationApprovalDAO vacationApprovalDAO;
    @Autowired
    public EmployeeDAO employeeDAO;
    @Autowired
    public VacationDAO vacationDAO;

    Vacation storedVacation;
    VacationApproval testVacationApproval;
    VacationApproval testVacationApproval_newRead;
    VacationApprovalResult storeVacationApprovalResult;
    VacationApprovalResult finalListVacationApprovalResult;
    List<VacationApprovalResult> listVacationApprovalResult;

    public VacationApproval findVacationApprovalDAO(Vacation  vacationId) {
        return vacationApprovalDAO.findVacationApprovalDAO(vacationId);
    }

    public List<VacationApprovalResult> getVacationApprovalResultByManager(VacationApproval vacationApproval){
        return vacationApprovalResultDAO.getVacationApprovalResultByManager(vacationApproval);
    }

    public Vacation findRandomVacation (){
        return vacationApprovalDAO.findRandomVacation();
    }

    public Employee findRandomEmployee (){
        return employeeDAO.findRandomEmployee();
    }

    public VacationApprovalResult store (VacationApprovalResult vacationApprovalResult){
        return vacationApprovalResultDAO.store(vacationApprovalResult);
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

    public void  delete (VacationApprovalResult vacationApprovalResult){
        vacationApprovalResultDAO.delete(vacationApprovalResult);
    }

    @Test
    public void testVacationApprovalResultDao () {

        Vacation testVacation = findRandomVacation();
        VacationApproval testVacationApproval = findVacationApprovalDAO(testVacation);
        List<VacationApprovalResult> testVacationApprovalResult = getVacationApprovalResultByManager(testVacationApproval);
        Set<VacationApprovalResult> setVacationApprovalResult = testVacationApproval.getVacationApprovalResults();
        List<VacationApprovalResult> testVacationApprovalResult2 = new ArrayList<VacationApprovalResult> ();
        testVacationApprovalResult2.addAll(setVacationApprovalResult);
        logger.info("lalala = {}", testVacationApprovalResult);

        assertEquals(testVacationApprovalResult, testVacationApprovalResult2);
    }

    @Before
    public void setData (){

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

        VacationApprovalResult vacationApprovalResult = new VacationApprovalResult();
        vacationApprovalResult.setVacationApproval(testVacationApproval);
        storeVacationApprovalResult = store(vacationApprovalResult);

        testVacationApproval_newRead = findVacationApprovalDAO(storedVacation);
        listVacationApprovalResult = getVacationApprovalResultByManager(testVacationApproval_newRead);
        finalListVacationApprovalResult = listVacationApprovalResult.get(0);
    }

    @After
    public void tearData (){

        delete(storeVacationApprovalResult);
        delete(finalListVacationApprovalResult);
        delete(testVacationApproval_newRead);
        delete(testVacationApproval);
        listVacationApprovalResult.clear();
        delete(storedVacation);
    }

    @Test
    public void testVacationApprovalResultDAOTest_store () {

        final VacationApprovalResult testVacationApprovalResult = storeVacationApprovalResult;
        final VacationApprovalResult testVacationApprovalResult2 = finalListVacationApprovalResult;
        logger.info("lalala = {}", testVacationApprovalResult);

        assertEquals(testVacationApprovalResult, testVacationApprovalResult2);
    }
}