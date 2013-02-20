package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectParticipantDAO;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;


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
    public List<ProjectParticipant> getProjectParticipantsOfManagersThatDoesntApproveVacation(Project project, Vacation vacation) {
        return projectParticipantDAO.getProjectParticipantsOfManagersThatDoesntApproveVacation(project, vacation);
    }

    /**
     * получаем список электронных адресов руководителей проектов, на которых сотрудник планирует свою занятость в даты отпуска.
     * если таких проектов нет, то получаем список проектов, по которым сотрудник списывал занятость за определенное (задается в настройках либо берется дефолтное значение)
     * количество дней до подачи заявление на отпуск.
     */
    public Iterable<ProjectParticipant> getJuniorProjectManagerProjectParticipants(List<Project> employeeProjects, Vacation vacation)
            throws CalendarServiceException {
        return getJuniorManagerProjectParticipantsInProjects(employeeProjects, vacation);
    }

    /**
     * получаем список электронных адресов руководителей сотрудника на выбранных проектах, которые не ответили на письмо о согласовании.
     */
    private Iterable<ProjectParticipant> getJuniorManagerProjectParticipantsInProjects(List<Project> employeeProjects, Vacation vacation) {
        HashSet<ProjectParticipant> chiefsProjectParticipants = new HashSet<ProjectParticipant>();
        for (Project project : employeeProjects) {
            chiefsProjectParticipants.addAll(getProjectParticipantsOfManagersThatDoesntApproveVacation(project, vacation));
        }

        chiefsProjectParticipants.remove(vacation.getEmployee().getEmail()); //удаляем емаил сотрудника, если он сам руководитель

        return chiefsProjectParticipants;
    }

}