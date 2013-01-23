package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Vacation;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

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
     * @param employeeId
     * @return
     */
    public List<Vacation> findVacations(Integer employeeId, Integer year) {
        final Query query =
                entityManager.createQuery("from Vacation v where v.employee.id = :emp_id and (YEAR(v.beginDate) = :year or YEAR(v.endDate) = :year) order by v.beginDate")
                        .setParameter("emp_id", employeeId).setParameter("year", year);

        return query.getResultList();
    }

}
