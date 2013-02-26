package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectRoleDAO;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectRoleService {	

	@Autowired
	private ProjectRoleDAO projectRoleDAO;

	/** Возвращает объект класса ProjectRole по указанному идентификатору */
	public ProjectRole find(Integer id) {
		return projectRoleDAO.find(id);
	}
	
	/**
	 * Возвращает объект класса ProjectRole по указанному идентификатору,
	 * соответсвующий активной проектой роли, либо null.
	 */
	public ProjectRole findActive(Integer id) {
		return projectRoleDAO.findActive(id);
	}
	
	/** Возвращает активную проектную роль по названию */
	public ProjectRole find(String title) {
		return projectRoleDAO.find(title);
	}
	/** Возвращает активные проектные роли. */
	public List<ProjectRole> getProjectRoles() {
		return projectRoleDAO.getProjectRoles();
	}
	
	/** Возвращает объект роли, которая не определена. */
	public ProjectRole getUndefinedRole() { //TODO заменить вызовы на установку enum'a
        //роль не определена
        return projectRoleDAO.findByCode("ND");
	}

    public ProjectRole getSysRole(Integer roleId) {
        return projectRoleDAO.getSysRole(roleId);
    }

    public String getProjectRoleListJson(Iterable<ProjectRole> projectRoleList) {
        StringBuilder projectRoleListJson = new StringBuilder();
        projectRoleListJson.append("[");
        for (ProjectRole item : projectRoleList) {
            projectRoleListJson.append("{id:'");
            projectRoleListJson.append(item.getId().toString());
            projectRoleListJson.append("', value:'");
            projectRoleListJson.append(item.getName());
            projectRoleListJson.append("'},");
        }

        return projectRoleListJson.toString().substring(0, (projectRoleListJson.toString().length() - 1)) + "]";
    }

}