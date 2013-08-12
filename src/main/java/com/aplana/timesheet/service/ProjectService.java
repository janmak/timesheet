package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.util.JsonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static argo.jdom.JsonNodeBuilders.*;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private static final String ID = "id";
    private static final String VALUE = "value";

    @Autowired
	private ProjectDAO projectDAO;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private TSPropertyProvider propertyProvider;



	/**
	 * Возвращает активные проекты без разделения по подразделениям.
	 */
    @Transactional(readOnly = true)
    public List<Project> getProjects() {
		return projectDAO.getProjects();
	}

	/**
	 * Возвращает все активные проекты\пресейлы.
	 */
    @Transactional(readOnly = true)
    public List<Project> getAll() {
		return projectDAO.getAll();
	}

	/**
	 * Возвращает объект класса Project по указанному идентификатору
	 * либо null.
	 */
    @Transactional(readOnly = true)
    public Project find(Integer id) {
		return projectDAO.find(id);
	}
	
	/**
	 * Возвращает объект класса Project по указанному идентификатору,
	 * соответсвующий активному проекту, либо null.
	 */
    @Transactional(readOnly = true)
    public Project findActive(Integer id) {
		return projectDAO.findActive(id);
	}

	/**
	 * Возвращает все активные проекты\пресейлы для которых в CQ заведены
	 * проектные задачи. (cq_required=true)
	 */
    @Transactional(readOnly = true)
    public List<Project> getProjectsWithCq() {
		return projectDAO.getProjectsWithCq();
	}
	
	/**
	 * Возвращает список всех участников указанного проекта.
	 * @param project
	 * @return
	 */
    @Transactional(readOnly = true)
    public List<ProjectParticipant> getParticipants(Project project) {
		return projectDAO.getParticipants(project);
	}
	
	/**
	 *Возвращает для указанного сотрудника список проектных ролей в проекте 
	 *@param Project project проект
	 *@param Employee employee сотрудник
	 *@return List<ProjectRole> список проектных ролей
	 */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public String getProjectListWithOwnerDivisionJson(List<Division> divisions) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();
        final List<Project> projectList = projectDAO.getProjects();

        for (Project project : projectList) {
            final JsonObjectNodeBuilder projectBuilder = getProjectBuilder(project);

            projectBuilder.withField(
                    "ownerDivisionId",
                    JsonUtil.aStringBuilder(project.getManager().getDivision().getId())
            );

            builder.withElement(projectBuilder);
        }

        return JsonUtil.format(builder);
    }

    /**
     * Возвращает JSON списка проектов, связанного с подразделениями
     *
     * @param divisions
     * @return
     */
    public String getProjectListJson(List<Division> divisions) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Division division : divisions) {
            final JsonArrayNodeBuilder projectsBuilder = anArrayBuilder();
            final Set<Project> projects = division.getProjects();

            if (projects.isEmpty()) {
                projectsBuilder.withElement(
                        anObjectBuilder().
                                withField(ID, JsonUtil.aStringBuilder(0)).
                                withField(VALUE, aStringBuilder(StringUtils.EMPTY))
                );
            } else {
                logger.debug("For division {} available {} projects.", division.getId(), projects.size());

                for (Project project : projects) {
                    if (project.isActive()) {
                        projectsBuilder.withElement(getProjectBuilder(project));
                    }
                }
            }

            builder.withElement(
                    anObjectBuilder().
                            withField("divId", JsonUtil.aStringBuilder(division.getId())).
                            withField("divProjs", projectsBuilder)
            );
        }

        return JsonUtil.format(builder);
    }

    /**
     * Возвращает JSON полного списка проектов
     *
     * @return
     */
    public String getProjectListJson() {
        return getProjectListAsJson(getProjects());
    }

    public String getProjectListAsJson(List<Project> projects) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        if (projects.isEmpty()) {
            builder.withElement(
                    anObjectBuilder().
                            withField(ID, JsonUtil.aStringBuilder(0)).
                            withField(VALUE, aStringBuilder(StringUtils.EMPTY))
            );
        } else {
            for (Project project : projects) {
                builder.withElement(
                        getProjectBuilder(project)
                );
            }
        }

        return JsonUtil.format(builder);
    }

    private JsonObjectNodeBuilder getProjectBuilder(Project project) {
        return anObjectBuilder().
                withField(ID, JsonUtil.aStringBuilder(project.getId())).
                withField(VALUE, aStringBuilder(project.getName())).
                withField("state", JsonUtil.aStringBuilder(project.getState().getId()));
    }

    public List<Project> getEmployeeProjectPlanByDates(Employee employee, HashMap<Integer, Set<Integer>> dates) {
        return projectDAO.getEmployeeProjectPlanByDates(employee, dates);
    }

    public List<Project> getEmployeeProjectsFromTimeSheetByDates(Date beginDate, Date endDate, Employee employee) {
        return projectDAO.getEmployeeProjectsFromTimeSheetByDates(beginDate, endDate, employee);
    }

    /**
     * получаем список проектов, менеджерам которых разосланы письма с просьбой согласовать данный отпуск
     */
    public List<Project> getProjectsAssignedToVacation(Vacation vacation) {
        return projectDAO.getProjectsAssignedToVacation(vacation);
    }

    public List<Project> getProjectsByStatesForDateAndDivisionId(List<Integer> projectStates, Date date,
                                                                 Integer divisionId) {
        return projectDAO.getProjectsByStatesForDateAndDivisionId(projectStates, date, divisionId);
    }

    /**
     * получаем проекты, участие в которых запланировано у сотрудника, по датам
     */
    public List<Project> getEmployeeProjectPlanByDates(Date beginDate, Date endDate, Employee employee) {
        //некоторых месяцев может не быть - поэтому получаем список доступных месяцев из БД
        HashMap<Integer, Set<Integer>> dates = calendarService.getMonthsAndYearsNumbers(beginDate, endDate);

        return getEmployeeProjectPlanByDates(employee, dates);
    }

    /**
     * получаем список проектов, с руководителями которых сотрудник будет согласовывать отпуск
     */
    public List<Project> getProjectsForVacation (Vacation vacation) {
        List<Project> employeeProjects = getEmployeeProjectPlanByDates(vacation.getBeginDate(), vacation.getEndDate(), vacation.getEmployee());
        if (employeeProjects.isEmpty()) {
            Integer beforeVacationDays = propertyProvider.getBeforeVacationDays();
            Date periodBeginDate = DateUtils.addDays(vacation.getCreationDate(), 0 - beforeVacationDays);
            employeeProjects = getEmployeeProjectsFromTimeSheetByDates(periodBeginDate, vacation.getCreationDate(), vacation.getEmployee());
        }

        return employeeProjects;
    }

    public List<Project> getProjectsForPeriod(Date fromDate, Date toDate) {
        return projectDAO.getProjectsForPeriod(fromDate, toDate);
    }
}