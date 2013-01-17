package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryItemService {

    @Autowired
	private DictionaryItemDAO dictionaryItemDAO;

	public List<DictionaryItem> getCategoryOfActivity() {
		return dictionaryItemDAO.getCategoryOfActivity();
	}

	public List<DictionaryItem> getTypesOfActivity() {
		return dictionaryItemDAO.getTypesOfActivity();
	}

	public DictionaryItem find(Integer id) {
		return dictionaryItemDAO.find(id);
	}
}