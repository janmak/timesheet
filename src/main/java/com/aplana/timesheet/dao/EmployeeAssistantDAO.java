package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeAssistant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Repository
public class EmployeeAssistantDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public EmployeeAssistant find(Employee employee) {
        final Query query = entityManager.createQuery(
                "from EmployeeAssistant ea where ea.employee = :employee"
        ).setParameter("employee", employee);

        return (EmployeeAssistant) query.getSingleResult();
    }

}
