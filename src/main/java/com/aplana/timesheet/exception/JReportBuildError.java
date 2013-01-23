package com.aplana.timesheet.exception;

public class JReportBuildError extends Exception {
    public JReportBuildError(String message, Throwable cause) {
        super(message, cause);
    }

    public JReportBuildError(String s) {
        super(s);
    }
}
