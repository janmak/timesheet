package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeePlan;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Repository
public class EmployeePlanDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<EmployeePlan> find(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "from EmployeePlan ep where ep.employee = :employee and ep.year = :year and ep.month = :month"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        return query.getResultList();
    }

    public void store(EmployeePlan employeePlan) {
        final EmployeePlan merged = entityManager.merge(employeePlan);

        employeePlan.setId(merged.getId());
    }

    public EmployeePlan find(Employee employee, Integer year, Integer month, DictionaryItem dictionaryItem) {
        final Query query = entityManager.createQuery(
                "from EmployeePlan ep where ep.employee = :employee and ep.year = :year and ep.month = :month and ep.type = :type"
        ).setParameter("employee", employee).setParameter("year", year).
                setParameter("month", month).setParameter("type", dictionaryItem);

        return (EmployeePlan) query.getSingleResult();
    }

    public void remove(EmployeePlan employeePlan) {
        if (employeePlan.getId() != null)
          entityManager.remove(employeePlan);
    }

    public EmployeePlan tryFind(Employee employee, Integer year, Integer month, DictionaryItem dictionaryItem) {
        try {
            return find(employee, year, month, dictionaryItem);
        } catch (NoResultException nre) {
            return null;
        }
    }
}
