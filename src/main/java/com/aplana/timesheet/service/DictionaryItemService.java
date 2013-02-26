package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

@Service
public class DictionaryItemService {

    @Autowired
	private DictionaryItemDAO dictionaryItemDAO;

	public List<DictionaryItem> getCategoryOfActivity() {
		return dictionaryItemDAO.getItemsByDictionaryId(DictionaryEnum.CATEGORY_OF_ACTIVITY.getId());
	}

	public List<DictionaryItem> getTypesOfActivity() {
        List<DictionaryItem> actTypes = dictionaryItemDAO.getItemsByDictionaryId(DictionaryEnum.TYPES_OF_ACTIVITY.getId());

        return Lists.newArrayList(Iterables.filter(actTypes, new Predicate<DictionaryItem>() {      //убираем из результатов отпуск
            @Override
            public boolean apply(@Nullable DictionaryItem input) {
                return !input.getId().equals(TypesOfActivityEnum.VACATION.getId());
            }
        }));
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
        return dictionaryItemDAO.getItemsByDictionaryIdAndOrderById(DictionaryEnum.UNDERTIME_CAUSE.getId());
    }

    public Object getItemsByDictionaryId(int dictId) {
        return dictionaryItemDAO.getItemsByDictionaryId(dictId);
    }

    public String getDictionaryItemsInJson(List<DictionaryItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (DictionaryItem item : items) {
            builder.append("{id:'");
            builder.append(item.getId().toString());
            builder.append("', value:'");
            builder.append(item.getValue());
            builder.append("'},");
        }
        if (builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }

        builder.append("]");
        return builder.toString();
    }

}