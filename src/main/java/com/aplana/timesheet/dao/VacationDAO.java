package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.google.common.collect.Lists;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.aplana.timesheet.enums.VacationStatusEnum.APPROVED;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Repository
public class VacationDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Список заявлений на отпуск для конкретного сотрудника
     */
    public List<Vacation> findVacations(Integer employeeId, Integer year) {
        final Query query =
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year);

        return query.getResultList();
    }

    public List<Vacation> findVacations(Integer employeeId, Date beginDate, Date endDate, DictionaryItem typeId){
        final Query query = typeId != null ?
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and v.beginDate <= :endDate " +
                        "and v.endDate >= :beginDate and v.type = :typeId order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("beginDate", beginDate)
                        .setParameter("endDate", endDate).setParameter("typeId", typeId) :
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and v.beginDate <= :endDate " +
                        "and v.endDate >= :beginDate order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("beginDate", beginDate)
                        .setParameter("endDate", endDate);
        return query.getResultList();
    }

    public void store(Vacation vacation) {
        final Vacation mergedVacation = entityManager.merge(vacation);

        entityManager.flush();

        vacation.setId(mergedVacation.getId());
    }

    public void delete(Vacation vacation) {
        Hibernate.initialize(vacation);

        entityManager.remove(vacation);
    }

    public Long getIntersectVacationsCount(Integer employeeId, Date fromDate, Date toDate) {
        final Query query = entityManager.createQuery(
                "select count(*) as c " +
                "from Vacation v, DictionaryItem di " +
                "where di.id = :status_id and ((:from_date between v.beginDate and v.endDate) or (:to_date between v.beginDate and v.endDate) or (v.beginDate between :from_date and :to_date))" +
                        " and not v.status = di and v.employee.id = :emp_id"
        ).setParameter("from_date", fromDate).setParameter("to_date", toDate).
                setParameter("status_id", VacationStatusEnum.REJECTED.getId()).setParameter("emp_id", employeeId);

        return (Long) query.getSingleResult();
    }

    public Vacation findVacation(Integer vacationId) {
        final Query query = entityManager.createQuery("from Vacation v where v.id = :id").setParameter("id", vacationId);
        return (Vacation) query.getSingleResult();
    }

    public Boolean isDayVacation(Employee employee, Date date){
        Query query = entityManager.createQuery(
                "SELECT i FROM Vacation AS i WHERE i.employee = :employee AND :date BETWEEN i.beginDate AND i.endDate AND i.status.id = :statusId"
        ).setParameter("employee", employee).setParameter("date", date).setParameter("statusId", APPROVED.getId());
        if (query.getResultList().isEmpty()) {
            return false;
        }
        return true;
    }

    public List<Integer> getAllNotApprovedVacationsIds() {
        return entityManager.createQuery("select v.id from Vacation as v where v.status.id in :notApprovedStatuses")
                .setParameter("notApprovedStatuses", VacationStatusEnum.getNotApprovedStatuses()).getResultList();
    }

    public int getVacationsWorkdaysCount(Employee employee, Integer year, Integer month, VacationStatusEnum status) {
        /*
            Здравствуй, мой юный друг! Я понимаю, в каком ты пребываешь состоянии от ниже написанных строчек кода, но,
            пожалуйста, если ты знаешь, как сделать рабочий вариант на HQL - сделай это за меня.

            P.S.: проблема в том, что вариант на HQL ВСЕГДА возвращает 0.
        */

        final Query query = entityManager.createNativeQuery(
            String.format(
                "select" +
                "        (count(c) - count(h)) as days" +
                "    from" +
                "        vacation as v" +
                "    left outer join calendar as c on (date_trunc('month', c.caldate) = {ts '%1$s'}) and (c.caldate between v.begin_date and v.end_date)" +
                "    left outer join holiday as h on (c.caldate = h.caldate) and (h.region is null or h.region = :region)" +
                "    where" +
                "        v.employee_id = :employee_id" +
                "        and v.status_id = :status_id" +
                "        and {ts '%1$s'} between date_trunc('month', v.begin_Date) and date_trunc('month', v.end_Date)",
                String.format("%d-%d-1", year, month)
            )
        ).setParameter("employee_id", employee.getId()).setParameter("status_id", status.getId()).
                setParameter("region", employee.getRegion().getId());

        return ((Number) query.getSingleResult()).intValue();
    }

    public Vacation tryFindVacation(Integer vacationId) {
        try {
            return findVacation(vacationId);
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<Vacation> findVacationsNeedApproval(Integer employeeId) {
        List<Integer> statuses = Lists.newArrayList(VacationStatusEnum.APPROVED.getId(),VacationStatusEnum.REJECTED.getId());
        final Query query =
                entityManager.createQuery("select distinct v from VacationApproval va " +
                        "left outer join va.vacation as v " +
                        "left outer join va.manager as m " +
                        "left outer join v.status as s " +
                        "where (m.id = :emp_id ) and (s.id not in (:statuses)) " +
                        "order by v.beginDate")
                        .setParameter("emp_id", employeeId)
                        .setParameter("statuses", statuses);

        return query.getResultList();
    }
}
