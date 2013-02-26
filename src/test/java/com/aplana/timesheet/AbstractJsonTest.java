package com.aplana.timesheet;

import static junit.framework.Assert.assertEquals;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractJsonTest extends AbstractTest {

    protected final String decodeHtmlEntities(String str) {
        return str.replace("&quot;", "\"");
    }

    protected final void assertJsonEquals(String expected, String actual) {
        assertEquals(expected, decodeHtmlEntities(actual));
    }

}
