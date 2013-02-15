package com.aplana.timesheet.util;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class ResourceUtils {

    public static String getResRealPath(String path, ServletContext application) {
        final long modified = new File(application.getRealPath(path)).lastModified();

        return new StringBuilder(application.getContextPath()).append(path).append("?").append(modified).toString();
    }

}
