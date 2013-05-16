package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectParticipantDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectParticipantService {
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

}