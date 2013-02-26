package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.ProjectTaskDAO;
import com.aplana.timesheet.dao.entity.Project;
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

    public String getProjectTaskListJson(List<Project> projects) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < projects.size(); i++) {
            Integer projectId = projects.get(i).getId();
            List<ProjectTask> tasks = getProjectTasks(projectId);
            sb.append("{projId:'");
            sb.append(projectId);
            sb.append("', projTasks:[");
            for (int j = 0; j < tasks.size(); j++) {
                sb.append("{id:'");
                sb.append(tasks.get(j).getCqId());
                sb.append("', value:'");
                sb.append(tasks.get(j).getCqId());
                sb.append("'}");
                if (j < (tasks.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]}");
            if (i < (projects.size() - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

}