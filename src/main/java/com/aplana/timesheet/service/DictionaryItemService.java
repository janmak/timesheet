package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryDAO;
import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryItemService {
	public static final Integer VACATION_ID = 16;
	public static final Integer ILLNESS_ID = 17;
	
	@Autowired
	private DictionaryItemDAO dictionaryItemDAO;

	public List<DictionaryItem> getCategoryOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(DictionaryDAO.CATEGORY_OF_ACTIVITY_ID);
	}

	public List<DictionaryItem> getTypesOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(DictionaryDAO.TYPES_OF_ACTIVITY_ID);
	}

	public DictionaryItem find(Integer id) {
		return dictionaryItemDAO.find(id);
	}

    public List<DictionaryItem> getWorkplaces() {
        return dictionaryItemDAO.getItemsByDictionaryId(DictionaryDAO.WORKPLACE_ID);
    }
}