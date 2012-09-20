package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectTaskDAO;
import com.aplana.timesheet.dao.entity.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTaskService {
	@Autowired
	private ProjectTaskDAO projectTaskDAO;

	/**
	 * Возвращает активные проектные задачи по указанному проекту.
	 * @param projectId идентификатор проекта в базе данных
	 * @return список проектных задач
	 */
	public List<ProjectTask> getProjectTasks(Integer projectId) {
		return projectTaskDAO.getProjectTasks(projectId);
	}
	
	/**
	 * Возвращает активную проектную задачу, относящуюся к указанному проекту,
	 * либо null, если проект или код задачи null, или такой задачи нет.
	 */
	public ProjectTask find(Integer projectId, String cqId) {
		return projectTaskDAO.find(projectId, cqId);
	}
}