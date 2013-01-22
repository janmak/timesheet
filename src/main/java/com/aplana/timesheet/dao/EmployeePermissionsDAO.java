package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.EmployeePermissions;
import com.aplana.timesheet.dao.entity.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class EmployeePermissionsDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public int getEmployeePermissionId(Integer employeeId) {
        int result = 0;
        Query query = entityManager.createQuery(
                "select ep.permission.id from EmployeePermissions as ep where ep.employee.id=:employeeId"
        ).setParameter("employeeId", employeeId);

        List permissions = query.getResultList();

        if ( permissions != null && ! permissions.isEmpty() ) {
            result = (Integer) permissions.get(0);
        }

        return result;
    }

}
