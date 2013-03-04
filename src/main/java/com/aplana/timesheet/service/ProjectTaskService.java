package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import com.aplana.timesheet.dao.ProjectTaskDAO;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectTask;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static argo.jdom.JsonNodeBuilders.*;

@Service
public class ProjectTaskService {
	@Autowired
	private ProjectTaskDAO projectTaskDAO;

	/**
	 * Возвращает активные проектные задачи по указанному проекту.
	 * @param projectId идентификатор проекта в базе данных
	 * @return список проектных задач
	 */
    @Transactional(readOnly = true)
    public List<ProjectTask> getProjectTasks(Integer projectId) {
		return projectTaskDAO.getProjectTasks(projectId);
	}
	
	/**
	 * Возвращает активную проектную задачу, относящуюся к указанному проекту,
	 * либо null, если проект или код задачи null, или такой задачи нет.
	 */
    @Transactional(readOnly = true)
    public ProjectTask find(Integer projectId, String cqId) {
		return projectTaskDAO.find(projectId, cqId);
	}

    @Transactional(readOnly = true)
    public String getProjectTaskListJson(List<Project> projects) {
        final JsonArrayNodeBuilder builder = anArrayBuilder();

        for (Project project : projects) {
            final Integer projectId = project.getId();
            final List<ProjectTask> tasks = getProjectTasks(projectId);
            final JsonArrayNodeBuilder tasksBuilder = anArrayBuilder();

            for (ProjectTask task : tasks) {
                tasksBuilder.withElement(
                        anObjectBuilder().
                                withField("id", aStringBuilder(task.getCqId())).
                                withField("value", aStringBuilder(task.getCqId()))
                );
            }

            builder.withElement(
                    anObjectBuilder().
                            withField("projId", JsonUtil.aStringBuilder(projectId)).
                            withField("projTasks", tasksBuilder)
            );
        }

        return JsonUtil.format(builder);
    }

}