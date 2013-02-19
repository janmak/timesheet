package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.DeleteVacationException;
import com.aplana.timesheet.util.EnumsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationService {

    @Autowired
    private VacationDAO vacationDAO;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmployeeService employeeService;

    public void store(Vacation vacation) {
        vacationDAO.store(vacation);
    }

    public Boolean isDayVacation(Employee employee, Date date){
        return vacationDAO.isDayVacation(employee, date);
    }

    public List<Vacation> getAllNotApprovedVacations() {
        return vacationDAO.getAllNotApprovedVacations();
    }

    public long getIntersectVacationsCount(Integer employeeId, Timestamp fromDate, Timestamp toDate) {
        return vacationDAO.getIntersectVacationsCount(
                employeeId,
                fromDate,
                toDate
        );
    }

    public List<Vacation> findVacations(Integer employeeId, Integer year) {
        return vacationDAO.findVacations(employeeId, year);
    }

    public Vacation tryFindVacation(Integer vacationId) {
        try {
            return vacationDAO.findVacation(vacationId);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public void delete(Vacation vacation) {
        vacationDAO.delete(vacation);
    }

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
}
