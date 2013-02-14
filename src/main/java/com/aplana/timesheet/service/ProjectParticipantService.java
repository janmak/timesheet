package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectParticipantDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.ParticipantMailHierarchyEnum;
import com.aplana.timesheet.enums.ProjectRolesEnum;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class ProjectParticipantService {
	@Autowired
	ProjectParticipantDAO projectParticipantDAO;
	
	/**
	 * Возвращает объект класса ProjectParticipant по указанному идентификатору
	 */
	public ProjectParticipant find(Integer id) {
		return projectParticipantDAO.find(id);
	}

    /**
     * Получаем список менеджеров, которые еще не приняли решение по отпуску
     */
    public List<ProjectParticipant> getProjectParticipantsOfManagersThatDoesntApproveVacation(List<Integer> projectRolesIds, Project project, Vacation vacation) {
        return projectParticipantDAO.getProjectParticipantsOfManagersThatDoesntApproveVacation(projectRolesIds, project,
                vacation);
    }

    /**
     * получаем список электронных адресов руководителей проектов, на которых сотрудник планирует свою занятость в даты отпуска.
     * если таких проектов нет, то получаем список проектов, по которым сотрудник списывал занятость за определенное (задается в настройках либо берется дефолтное значение)
     * количество дней до подачи заявление на отпуск.
     */
    public Iterable<ProjectParticipant> getJuniorProjectManagerProjectParticipants(List<Project> employeeProjects, Vacation vacation)
            throws CalendarServiceException {
        return getJuniorManagerProjectParticipantsInProjects(vacation.getEmployee(), employeeProjects, vacation);
    }

    /**
     * получаем список электронных адресов руководителей сотрудника на выбранных проектах, которые не ответили на письмо о согласовании.
     */
    private Iterable<ProjectParticipant> getJuniorManagerProjectParticipantsInProjects(Employee employee, List<Project> employeeProjects, Vacation vacation) {
        HashSet<ProjectParticipant> chiefsProjectParticipants = new HashSet<ProjectParticipant>();
        for (Project project : employeeProjects) {
            Set<ProjectRolesEnum> chiefRoles = getChiefsRolesEnumByEmployeeRole(employee.getJob());
            List<Integer> chiefRolesIds = CollectionUtils.transform(chiefRoles, new Transformer() {
                @Override
                public Integer transform(Object o) {
                    return ((ProjectRolesEnum) o).getId();
                }
            });
            chiefsProjectParticipants.addAll(getProjectParticipantsOfManagersThatDoesntApproveVacation(chiefRolesIds, project, vacation));
        }

        chiefsProjectParticipants.remove(employee.getEmail()); //удаляем емаил сотрудника, если он сам руководитель

        return chiefsProjectParticipants;
    }

    /**
     * получаем список ролей, которые являются руководителями для роли сотрудника на проекте
     */
    private Set<ProjectRolesEnum> getChiefsRolesEnumByEmployeeRole(ProjectRole employeeProjectRole) {
        ParticipantMailHierarchyEnum mailHierarchyEnum = ParticipantMailHierarchyEnum.tryFindEnumByRoleId(employeeProjectRole.getId());
        if (mailHierarchyEnum != null) {
            return mailHierarchyEnum.getChiefsProjectRolesEnums();
        } else {
            return ParticipantMailHierarchyEnum.getDafaultChiefsRolesEnum();
        }
    }
}