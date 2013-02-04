package com.aplana.timesheet.dao;

import com.aplana.timesheet.controller.TimeSheetController;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.dao.entity.ProjectRolePermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ProjectRolePermissionsDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProjectRolePermissionsDAO.class);
    @PersistenceContext
    private EntityManager entityManager;

    public Permission getProjectRolePermission(ProjectRole projectRole) {
        logger.debug("ProjectRole{}", projectRole == null ? " is null":" id =" + projectRole.getId());
        return getProjectRolePermission(projectRole.getId());
    }

    public Permission getProjectRolePermission(Integer projectRoleId) {
        logger.debug("ProjectRoleId = {}", projectRoleId);
        Query query = entityManager.createQuery(
                "select prp from ProjectRolePermissions as prp where prp.projectRole.id=:projectRoleId"
        ).setParameter("projectRoleId", projectRoleId);

        List permissions = query.getResultList();
        if ( permissions != null && ! permissions.isEmpty() ) {
            return ((ProjectRolePermissions)(permissions.get(0))).getPermission();
        }
        return null;
    }

}
