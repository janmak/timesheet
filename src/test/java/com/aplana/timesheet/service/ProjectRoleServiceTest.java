package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
        */
        public class ProjectRoleServiceTest extends AbstractJsonTest {

            @Autowired
            private ProjectRoleService projectRoleService;

            private String getProjectRoleListJson(Iterable<ProjectRole> projectRoleList) {
                StringBuilder projectRoleListJson = new StringBuilder();
                projectRoleListJson.append("[");
                for (ProjectRole item : projectRoleList) {
                    projectRoleListJson.append("{\"id\":\"");
                    projectRoleListJson.append(item.getId().toString());
                    projectRoleListJson.append("\",\"value\":\"");
                    projectRoleListJson.append(item.getName());
                    projectRoleListJson.append("\"},");
                }

        return projectRoleListJson.toString().substring(0, (projectRoleListJson.toString().length() - 1)) + "]";
    }

    @Test
    public void testGetProjectRoleListJson() throws Exception {
        final List<ProjectRole> projectRoles = projectRoleService.getProjectRoles();

        assertJsonEquals(getProjectRoleListJson(projectRoles), projectRoleService.getProjectRoleListJson(projectRoles));
    }
}
