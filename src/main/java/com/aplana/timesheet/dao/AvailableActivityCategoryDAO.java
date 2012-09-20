package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class AvailableActivityCategoryDAO {
	@PersistenceContext
    private EntityManager entityManager;
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<AvailableActivityCategory> getAvailableActivityCategories(
														DictionaryItem actType, ProjectRole projectRole) {
		Query query = entityManager.createQuery("from AvailableActivityCategory as ac where "
				+ "ac.actType=:actType and ac.projectRole=:projectRole");
		query.setParameter("actType", actType);
		query.setParameter("projectRole", projectRole);
		List<AvailableActivityCategory> result = query.getResultList();

		return result;
	}
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param project
	 * 			Проект\Пресейл.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<AvailableActivityCategory> getAvailableActivityCategories(
										DictionaryItem actType, Project project, ProjectRole projectRole) {
		Query query = entityManager.createQuery("from AvailableActivityCategory as ac where "
				+ "ac.actType=:actType and ac.project=:project and ac.projectRole=:projectRole");
		query.setParameter("actType", actType);
		query.setParameter("project", project);
		query.setParameter("projectRole", projectRole);
		List<AvailableActivityCategory> result = query.getResultList();

		return result;
	}
}