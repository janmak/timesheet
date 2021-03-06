package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeProjectPlan;
import com.aplana.timesheet.dao.entity.Project;
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
public class EmployeeProjectPlanDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<EmployeeProjectPlan> find(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "from EmployeeProjectPlan epp where epp.employee = :employee and epp.year = :year and epp.month = :month"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        return query.getResultList();
    }

    public void store(EmployeeProjectPlan employeeProjectPlan) {
        final EmployeeProjectPlan merged = entityManager.merge(employeeProjectPlan);

        employeeProjectPlan.setId(merged.getId());
    }

    public EmployeeProjectPlan find(Employee employee, Integer year, Integer month, Project project) {
        final Query query = entityManager.createQuery(
                "from EmployeeProjectPlan epp where epp.employee = :employee and epp.year = :year and epp.month = :month and epp.project = :project"
        ).setParameter("employee", employee).setParameter("year", year).
                setParameter("month", month).setParameter("project", project);

        return (EmployeeProjectPlan) query.getSingleResult();
    }

    public void remove(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "delete from EmployeeProjectPlan epp where epp.employee = :employee and epp.year = :year and epp.month = :month"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        query.executeUpdate();
    }

    public EmployeeProjectPlan tryFind(Employee employee, Integer year, Integer month, Project project) {
        try {
            return find(employee, year, month, project);
        } catch (NoResultException nre) {
            return null;
        }
    }

    public void remove(EmployeeProjectPlan employeeProjectPlan) {
        if (employeeProjectPlan.getId() != null)
          entityManager.remove(employeeProjectPlan);
    }
}
