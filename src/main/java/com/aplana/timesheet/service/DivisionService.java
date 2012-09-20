package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DivisionService {
	
	@Autowired
	DivisionDAO divisionDAO;
	@Autowired
	ProjectDAO projectDAO;

	public List<Division> getDivisions() {
		return divisionDAO.getDivisions();
	}

	/**
	 * Ищет активное подразделение с указанным именем.
	 * 
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	public Division find(String title) {
		return divisionDAO.find(title);
	}
	
	/**
	 * Ищет активное подразделение с указанным идентификатором.
	 * 
	 * @param division идентификатор подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	public Division find(Integer division) {
		return divisionDAO.find(division);
	}

	//	public void addProjectsToDivision() {
//		Division division = divisionDAO.find(1);
//		Set<Project> projects = new HashSet<Project>();
//		projects.add(projectDAO.find(3));
//		projects.add(projectDAO.find(4));
//		projects.add(projectDAO.find(5));
//		projects.add(projectDAO.find(6));
//		projects.add(projectDAO.find(7));
//		projects.add(projectDAO.find(8));
//		projects.add(projectDAO.find(9));
//		projects.add(projectDAO.find(19));
//		projects.add(projectDAO.find(25));
//		projects.add(projectDAO.find(23));
//		division.setProjects(projects);
//		divisionDAO.storeDivision(division);
//	}
}