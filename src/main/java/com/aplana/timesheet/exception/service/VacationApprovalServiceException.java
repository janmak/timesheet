package com.aplana.timesheet.exception.service;

/**
 * User: vsergeev
 * Date: 08.02.13
 */
public class VacationApprovalServiceException extends Exception{

    public VacationApprovalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public VacationApprovalServiceException(Throwable cause) {
        super(cause);
    }

    public VacationApprovalServiceException(String msg) {
        super(msg);
    }
}
