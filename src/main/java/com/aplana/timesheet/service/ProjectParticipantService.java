package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectParticipantDAO;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
	
	
}