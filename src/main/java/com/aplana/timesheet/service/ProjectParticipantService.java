package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectParticipantDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectParticipantService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectParticipantService.class);

	@Autowired
	ProjectParticipantDAO projectParticipantDAO;

	/**
	 * Возвращает объект класса ProjectParticipant по указанному идентификатору
	 */
    @Transactional(readOnly = true)
    public ProjectParticipant find(Integer id) {
		return projectParticipantDAO.find(id);
	}

    /**
     * Возвращает объект класса ProjectParticipant, если найдено совпадение по паре employee/project
     */
    @Transactional(readOnly = true)
    public Boolean isProjectManager(Employee employee, Project project){
        return projectParticipantDAO.isProjectManager(employee, project);
    }

    /**
     * Определяет есть ли активные Project Participant у сотрудника
     */
    @Transactional(readOnly = true)
    public Boolean hasActiveParticipantEmployee(Employee employee) {
        List<ProjectParticipant> empProjectParticipants = projectParticipantDAO.findByEmployee(employee);
        for (ProjectParticipant participant : empProjectParticipants) {
            if (participant.isActive())
                return true;
        }
        return false;
    }

    public void deactivateEmployeesRights(List<Employee> employees) {
        for (Employee employee : employees) {
                List<ProjectParticipant> empProjectParticipants = projectParticipantDAO.findByEmployee(employee);
                if (empProjectParticipants != null) {
                    for (ProjectParticipant participant : empProjectParticipants) {
                        participant.setActive(false);
                        projectParticipantDAO.save(participant);
                    }
                }
        }
    }

    @Transactional(readOnly = true)
    public List<Integer> findProjectsIdByEmployee(Employee employee) {
        List<Integer> result = new ArrayList<Integer>();
        List<ProjectParticipant> projectParticipantList = projectParticipantDAO.findByEmployee(employee);
        for (ProjectParticipant pp : projectParticipantList){
            result.add(pp.getProject().getId());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Boolean isProjectParticipant(Project project, Employee employee){
        return projectParticipantDAO.isProjectParticipant(project, employee);
    }

}