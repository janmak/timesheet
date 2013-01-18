package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("divisionService")
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
}