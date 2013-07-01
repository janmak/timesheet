package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.form.CreateVacationForm;
import com.aplana.timesheet.service.vacationapproveprocess.VacationApprovalProcessService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationService extends AbstractServiceWithTransactionManagement {

    @Autowired
    private VacationDAO vacationDAO;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Autowired
    private VacationApprovalProcessService vacationApprovalProcessService;

    private static final Logger logger = LoggerFactory.getLogger(VacationService.class);

    @Transactional
    public void store(Vacation vacation) {
        vacationDAO.store(vacation);
    }

    @Transactional
    public Boolean isDayVacation(Employee employee, Date date){
        return vacationDAO.isDayVacation(employee, date);
    }

    @Transactional
    public List<Integer> getAllNotApprovedVacationsIds() {
        return vacationDAO.getAllNotApprovedVacationsIds();
    }

    public long getIntersectVacationsCount(Integer employeeId, Timestamp fromDate, Timestamp toDate, DictionaryItem item) {
        return vacationDAO.getIntersectVacationsCount(
                employeeId,
                fromDate,
                toDate,
                item
        );
    }

    public Long getIntersectPlannedVacationsCount(Integer employeeId, Date fromDate, Date toDate, DictionaryItem item) {
        return vacationDAO.getIntersectPlannedVacationsCount(
                employeeId,
                fromDate,
                toDate,
                item
        );
    }

    public List<Vacation> findVacations(Integer employeeId, Integer year) {
        return vacationDAO.findVacations(employeeId, year);
    }

    public List<Vacation> findVacations(Integer year, Integer month, Integer employeeId) {
        return vacationDAO.findVacations(year,month,employeeId);
    }

    public List<Vacation> findVacations(Integer employeeId, Date beginDate, Date endDate, DictionaryItem typeId) {
        return vacationDAO.findVacations(employeeId, beginDate, endDate, typeId);
    }

    public List<Vacation> findVacationsByTypes(Integer year, Integer month, Integer employeeId,  List<DictionaryItem> types) {
        return vacationDAO.findVacationsByTypes(year,  month,  employeeId, types);
    }

    public List<Vacation> findVacationsByType(Integer year, Integer month, Integer employeeId,  DictionaryItem type) {
        return vacationDAO.findVacationsByType(year, month, employeeId, type);
    }

    @Transactional
    public Vacation tryFindVacation(Integer vacationId) {
        return vacationDAO.tryFindVacation(vacationId);
    }

    @Transactional
    public void delete(Vacation vacation) {
        vacationDAO.delete(vacation);
    }

    @Transactional
    public void deleteVacation(Integer vacationId) {
        final Vacation vacation = tryFindVacation(vacationId);

        if (vacation == null) { // если вдруг удалил автор, а не сотрудник
            throw new DeleteVacationException("Запись не найдена");
        }

        final Employee employee = securityService.getSecurityPrincipal().getEmployee();
        final boolean isAdmin = employeeService.isEmployeeAdmin(employee.getId());

        final DictionaryItem statusDictionaryItem = vacation.getStatus();
        final VacationStatusEnum vacationStatus =
                EnumsUtils.getEnumById(statusDictionaryItem.getId(), VacationStatusEnum.class);

        if (
                employee.equals(vacation.getEmployee()) ||
                employee.equals(vacation.getAuthor()) ||
                isAdmin
        ) {
            if (!isAdmin && (vacationStatus == VacationStatusEnum.REJECTED || vacationStatus == VacationStatusEnum.APPROVED)) {
                throw new DeleteVacationException(String.format(
                        "Нельзя удалить заявление на отпуск в статусе \"%s\". Для удаления данного заявления " +
                                "необходимо написать на timesheet@aplana.com",
                        statusDictionaryItem.getValue()
                ));
            }

            sendMailService.performVacationDeletedMailing(vacation);    //todo переделать, чтобы рассылка все-таки была после удаления?

            delete(vacation);

            return;
        }

        throw new DeleteVacationException("Ошибка доступа");
    }

    @Transactional
    public Vacation findVacation(Integer vacationId) {
        return vacationDAO.findVacation(vacationId);
    }


    public int getVacationsWorkdaysCount(Employee employee, Integer year, Integer month, VacationStatusEnum status) {
        return vacationDAO.getVacationsWorkdaysCount(employee, year, month, VacationStatusEnum.APPROVED);
    }

    public Map<DictionaryItem, List<Vacation>> splitVacationByTypes(List<Vacation> vacations) {
        Map<DictionaryItem, List<Vacation>> map = new HashMap<DictionaryItem, List<Vacation>>();
        for (Vacation vac: vacations){
            if(map.keySet().contains(vac.getType())){
                map.get(vac.getType()).add(vac);
            } else{
                map.put(vac.getType(), Lists.newArrayList(vac));
            }
        }
        return map;
    }

    public List<Vacation> findVacationsNeedsApproval(Integer employeeId) {
        return vacationDAO.findVacationsNeedApproval(employeeId);
    }

    public void createAndMailngVacation(CreateVacationForm createVacationForm, Employee employee, Employee curEmployee, boolean isApprovedVacation)
            throws VacationApprovalServiceException {

        final Vacation vacation = new Vacation();

        vacation.setCreationDate(new Date());
        vacation.setBeginDate(DateTimeUtil.stringToTimestamp(createVacationForm.getCalFromDate()));
        vacation.setEndDate(DateTimeUtil.stringToTimestamp(createVacationForm.getCalToDate()));
        vacation.setComment(createVacationForm.getComment().trim());
        vacation.setType(dictionaryItemService.find(createVacationForm.getVacationType()));
        vacation.setAuthor(curEmployee);
        vacation.setEmployee(employee);

        vacation.setStatus(dictionaryItemService.find(
                isApprovedVacation ? VacationStatusEnum.APPROVED.getId() : VacationStatusEnum.APPROVEMENT_WITH_PM.getId()
        ));

        TransactionStatus transactionStatus = null;

        try {
            transactionStatus = getNewTransaction();

            store(vacation);

            if (needsToBeApproved(vacation)) {
                vacationApprovalProcessService.sendVacationApproveRequestMessages(vacation);       //рассылаем письма о согласовании отпуска
            } else {
                vacationApprovalProcessService.sendBackDateVacationApproved(vacation);
            }
            commit(transactionStatus);
        } catch (VacationApprovalServiceException e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
                logger.error("Transaction rollbacked. Error saving vacation: {} ",e);
            } else {
                logger.error("TransactionStatus is null.");
            }
        }
        sendMailService.performVacationCreateMailing(vacation);
    }

    private boolean needsToBeApproved(Vacation vacation) {
        return !vacation.getStatus().getId().equals(VacationStatusEnum.APPROVED.getId());
    }
}
