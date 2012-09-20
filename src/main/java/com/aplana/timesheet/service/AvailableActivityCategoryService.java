package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.entity.AvailableActivityCategory;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailableActivityCategoryService {
	@Autowired
	AvailableActivityCategoryDAO availableActivityCategoryDAO;
	
	/**
	 * Возвращает доступные категории активности
	 * @param actType
	 * 			Тип активности.
	 * @param projectRole
	 * 			Проектная роль.
	 * @return List<AvailableActivityCategory>
	 * 			Список доступных категорий активности.
	 */
	public List<AvailableActivityCategory> getAvailableActivityCategories(
														DictionaryItem actType, ProjectRole projectRole) {
		return availableActivityCategoryDAO.getAvailableActivityCategories(actType, projectRole);
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
	public List<AvailableActivityCategory> getAvailableActivityCategories(
										DictionaryItem actType, Project project, ProjectRole projectRole) {
		return availableActivityCategoryDAO.getAvailableActivityCategories(actType, project, projectRole);
	}
}