package com.aplana.timesheet;

import com.aplana.timesheet.dao.entity.Employee;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Transactional(readOnly = true)
public abstract class AbstractJsonTest extends AbstractTest {

    protected final String decodeHtmlEntities(String str) {
        return str.replace("&quot;", "\"");
    }

    protected final void assertJsonEquals(String expected, String actual) {
        assertEquals(expected, decodeHtmlEntities(actual));
    }

    /* сравнение по некоторым данным сотрудника */
    protected final void assertEmployeeFieldsEquals(Employee expected, Map<String, String> actual) {
        /* проверим id */
        assertEquals(String.valueOf(expected.getId()), actual.get("id"));
        /* проверим name */
        assertEquals(expected.getName(), actual.get("name"));
        /* проверим manager */
        assertEquals(String.valueOf(expected.getManager().getId()), actual.get("manager"));
        /* проверим region */
        assertEquals(String.valueOf(expected.getRegion().getId()), actual.get("region"));
        /* проверим job */
        assertEquals(String.valueOf(expected.getJob().getId()), actual.get("job"));
        /* проверим startDate */
        assertEquals(new SimpleDateFormat("dd.MM.yyyy").format(expected.getStartDate()), actual.get("startDate"));
        /* проверим наличие даты ... */
        //todo дописать проверку этой даты
        assertTrue( (actual.get("dateByDefault") != null) && (!actual.get("dateByDefault").isEmpty()) );
    }

}
