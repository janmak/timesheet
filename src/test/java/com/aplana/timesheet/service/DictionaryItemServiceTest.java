package com.aplana.timesheet.service;

import com.aplana.timesheet.AbstractJsonTest;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class DictionaryItemServiceTest extends AbstractJsonTest {

    @Autowired
    private DictionaryItemService dictionaryItemService;

    private String getDictionaryItemsInJson(List<DictionaryItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (DictionaryItem item : items) {
            builder.append("{\"id\":\"");
            builder.append(item.getId().toString());
            builder.append("\",\"value\":\"");
            builder.append(item.getValue());
            builder.append("\"},");
        }
        if (builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }

        builder.append("]");
        return builder.toString();
    }

    @Test
    public void testGetDictionaryItemsInJson() throws Exception {
        final List<DictionaryItem> categoryOfActivity = dictionaryItemService.getCategoryOfActivity();

        assertJsonEquals(getDictionaryItemsInJson(categoryOfActivity), dictionaryItemService.getDictionaryItemsInJson(categoryOfActivity));
    }
}
