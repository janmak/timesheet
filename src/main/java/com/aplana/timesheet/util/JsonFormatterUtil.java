package com.aplana.timesheet.util;

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.jdom.JsonRootNode;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class JsonFormatterUtil {

    private final static JsonFormatter JSON_FORMATTER = new CompactJsonFormatter();

    public static String format(JsonRootNode rootNode) {
        return JSON_FORMATTER.format(rootNode);
    }

}
