package com.aplana.timesheet.exception.service;

/**
 * User: vsergeev
 * Date: 07.02.13
 */
public class CalendarServiceException extends Exception {

    public CalendarServiceException(String message) {
        super(message);
    }

    public CalendarServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
