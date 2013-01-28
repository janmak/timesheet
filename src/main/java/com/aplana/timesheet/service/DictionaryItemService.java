package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryItemService {

    @Autowired
	private DictionaryItemDAO dictionaryItemDAO;

	public List<DictionaryItem> getCategoryOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(Dictionary.CATEGORY_OF_ACTIVITY.getId());
	}

	public List<DictionaryItem> getTypesOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(Dictionary.TYPES_OF_ACTIVITY.getId());
	}

	public DictionaryItem find(Integer id) {
		return dictionaryItemDAO.find(id);
	}

    public List<DictionaryItem> getWorkplaces() {
        return dictionaryItemDAO.getItemsByDictionaryId(Dictionary.WORKPLACE.getId());
    }

    public List<DictionaryItem> getOvertimeCauses() {
        return dictionaryItemDAO.getItemsByDictionaryId(Dictionary.OVERTIME_CAUSE.getId());
    }

    public List<DictionaryItem> getUnfinishedDayCauses() {
        return dictionaryItemDAO.getItemsByDictionaryId(Dictionary.UNFINISHED_DAY_CAUSE.getId());
    }
}