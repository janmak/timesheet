package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.EmployeePermissions;
import com.aplana.timesheet.dao.entity.Permission;
import org.apache.commons.lang.exception.NestableError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EmployeePermissionsDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Permission> getEmployeePermissions(Integer employeeId) {

        Query query = entityManager.createQuery(
                "select ep.permission from EmployeePermissions as ep where ep.employee.id=:employeeId"
        ).setParameter("employeeId", employeeId);

        List<Permission> result = query.getResultList();

        if ( result != null && ! result.isEmpty() ) {
            return result;
        }

        return null;
    }

}
