package com.aplana.timesheet.util;

import java.sql.SQLException;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class ExceptionUtils {

    public static Throwable getLastCause(Throwable throwable) {
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        return throwable;
    }

    public static Throwable getRealLastCause(Throwable throwable) {
        Throwable lastCause = getLastCause(throwable);

        if (lastCause.getMessage() != null && lastCause.getMessage().contains("getNextException") && lastCause instanceof SQLException) {
            return ((SQLException) lastCause).getNextException();
        }

        return lastCause;
    }

}
