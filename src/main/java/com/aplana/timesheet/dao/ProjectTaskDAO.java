package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ProjectTaskDAO {
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ProjectDAO projectDAO;

	/**
	 * Возвращает активные проектные задачи по указанному проекту.
	 * 
	 * @param projectId
	 *            идентификатор проекта в базе данных
	 * @return список проектных задач
	 */
	@SuppressWarnings("unchecked")
	public List<ProjectTask> getProjectTasks(Integer projectId) {
		Query query = entityManager.createQuery(
                "from ProjectTask as pt where pt.project=:project and pt.active=:active"
        ).setParameter("project", projectDAO.find(projectId)).setParameter("active", true);

        return query.getResultList();
	}
	
	/**
	 * Возвращает активную проектную задачу, относящуюся к указанному проекту,
	 * либо null, если проект или код задачи null, или такой задачи нет.
	 */
    public ProjectTask find(Integer project, String task) {
		Project proj = projectDAO.findActive(project);
		if (proj == null || task == null) { return null; }

        Query query = entityManager.createQuery(
                "from ProjectTask as pt where pt.project=:project and cqId=:task and pt.active=:active"
        ).setParameter("project", proj).setParameter("task", task).setParameter("active", true);
		try {
			return (ProjectTask) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}