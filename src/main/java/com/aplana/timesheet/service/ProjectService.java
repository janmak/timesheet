package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


@Service
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

	@Autowired
	private ProjectDAO projectDAO;

	/**
	 * Возвращает активные проекты без разделения по подразделениям.
	 */
	public List<Project> getProjects() {
		return projectDAO.getProjects();
	}

	/**
	 * Возвращает все активные проекты\пресейлы.
	 */
	public List<Project> getAll() {
		return projectDAO.getAll();
	}

	/**
	 * Возвращает активные пресейлы без разделения по подразделениям.
	 */
	public List<Project> getPresales() {
		return projectDAO.getPresales();
	}

	/**
	 * Возвращает объект класса Project по указанному идентификатору
	 * либо null.
	 */
	public Project find(Integer id) {
		return projectDAO.find(id);
	}
	
	/**
	 * Возвращает объект класса Project по указанному идентификатору,
	 * соответсвующий активному проекту, либо null.
	 */
	public Project findActive(Integer id) {
		return projectDAO.findActive(id);
	}

	/**
	 * Возвращает все активные проекты\пресейлы для которых в CQ заведены
	 * проектные задачи. (cq_required=true)
	 */
	public List<Project> getProjectsWithCq() {
		return projectDAO.getProjectsWithCq();
	}
	
	/**
	 * Возвращает список всех участников указанного проекта.
	 * @param project
	 * @return
	 */
	public List<ProjectParticipant> getParticipants(Project project) {
		return projectDAO.getParticipants(project);
	}
	
	/**
	 *Возвращает для указанного сотрудника список проектных ролей в проекте 
	 *@param Project project проект
	 *@param Employee employee сотрудник
	 *@return List<ProjectRole> список проектных ролей
	 */
	public List<ProjectParticipant> getEmployeeProjectRoles(Project project, Employee employee){
		return projectDAO.getEmployeeProjectRoles(project, employee);
	}

    public List<Project> getProjectsByDates(Date beginDate, Date endDate){
        return projectDAO.getProjectsByDates(beginDate, endDate);
    }

    /**
     * Возвращает список проектов с указанием подразделения РП проекта
     *
     */
    public String getProjectListWithOwnerDivisionJson(List<Division> divisions) {
        StringBuilder result = new StringBuilder();
        result.append("[");
        List<Project> projectList = projectDAO.getProjects();
        for (Project project : projectList) {
            result.append("{id:'");
            result.append(project.getId());
            result.append("', value:'");
            result.append(project.getName());
            result.append("', state:'");
            result.append(project.getState().getId());
            result.append("', ownerDivisionId:'");
            result.append(project.getManager().getDivision().getId());
            result.append("'}");
            result.append(", ");
        }
        result.deleteCharAt(result.length() - 2);
        result.append("]");
        return result.toString();
    }

    /**
     * Возвращает JSON списка проектов, связанного с подразделениями
     *
     * @param divisions
     * @return
     */
    public String getProjectListJson(List<Division> divisions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < divisions.size(); i++) {
            sb.append("{divId:'");
            sb.append(divisions.get(i).getId());
            Set<Project> projects = divisions.get(i).getProjects();
            sb.append("', divProjs:[");
            if (projects.size() > 0) {
                int count = 0;
                logger.debug("For division {} available {} projects.", divisions.get(i).getId(), projects.size());
                for (Project project : projects) {
                    if (project.isActive()) {
                        sb.append("{id:'");
                        sb.append(project.getId());
                        sb.append("', value:'");
                        sb.append(project.getName());
                        sb.append("', state:'");
                        sb.append(project.getState().getId());
                        sb.append("'}");
                        sb.append(", ");
                    }
                    count++;
                }
                sb.deleteCharAt(sb.length() - 2);
                sb.append("]}");
            } else {
                sb.append("{id:'0', value:''}]}");
            }

            if (i < (divisions.size() - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Возвращает JSON полного списка проектов
     *
     * @return
     */
    public String getProjectListJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
            List<Project> projects = getProjects();
            if (projects.size() > 0) {
                int count = 0;
                for (Project project : projects) {
                    if (project.isActive()) {
                        sb.append("{id:'");
                        sb.append(project.getId());
                        sb.append("', value:'");
                        sb.append(project.getName());
                        sb.append("', state:'");
                        sb.append(project.getState().getId());
                        sb.append("'}");
                        sb.append(", ");
                    }
                    count++;
                }
                sb.deleteCharAt(sb.length() - 2);
            } else {
                sb.append("{id:'0', value:''}");
            }
        sb.append("]");
        return sb.toString();
    }

    public String getProjectListAsJson(List<Project> projects){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (projects.size() > 0) {
            for (Project project : projects) {
                sb.append("{id:'");
                sb.append(project.getId());
                sb.append("', value:'");
                sb.append(project.getName());
                sb.append("', state:'");
                sb.append(project.getState().getId());
                sb.append("'}");
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 2);
        } else {
            sb.append("{id:'0', value:''}");
        }
        sb.append("]");
        return sb.toString();
    }

    public List<Project> getEmployeeProjectPlanByDates(Employee employee, HashMap<Integer, Set<Integer>> dates) {
        return projectDAO.getEmployeeProjectPlanByDates(employee, dates);
    }

    public List<Project> getEmployeeProjectsByDates(Date beginDate, Date endDate, Employee employee) {
        return projectDAO.getEmployeeProjectsByDates(beginDate, endDate, employee);
    }
}