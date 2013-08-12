package com.aplana.timesheet;

import org.springframework.transaction.annotation.Transactional;

import static junit.framework.Assert.assertEquals;

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

}
