package com.aplana.timesheet.util;

public class JReportBuildError extends Exception {
    public JReportBuildError(String message, Throwable cause) {
        super(message, cause);
    }

    public JReportBuildError(String s) {
        super(s);
    }
}
