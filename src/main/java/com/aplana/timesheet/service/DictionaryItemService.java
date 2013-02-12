package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.DictionaryEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryItemService {

    @Autowired
	private DictionaryItemDAO dictionaryItemDAO;

	public List<DictionaryItem> getCategoryOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(DictionaryEnum.CATEGORY_OF_ACTIVITY.getId());
	}

	public List<DictionaryItem> getTypesOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(DictionaryEnum.TYPES_OF_ACTIVITY.getId());
	}

	public DictionaryItem find(Integer id) {
		return dictionaryItemDAO.find(id);
	}

    public List<DictionaryItem> getWorkplaces() {
        return dictionaryItemDAO.getItemsByDictionaryId(DictionaryEnum.WORKPLACE.getId());
    }

    public List<DictionaryItem> getOvertimeCauses() {
        return dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.OVERTIME_CAUSE.getId());
    }

    public List<DictionaryItem> getUnfinishedDayCauses() {
        return dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.UNFINISHED_DAY_CAUSE.getId());
    }

    public Object getItemsByDictionaryId(int dictId) {
        return dictionaryItemDAO.getItemsByDictionaryId(dictId);
    }
}