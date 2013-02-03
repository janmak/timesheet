package com.aplana.timesheet.exception.controller;

/**
 * User: vsergeev
 * Date: 25.01.13
 */
public class BusinessTripsAndIllnessAddException extends Exception{

    public BusinessTripsAndIllnessAddException(String message) {
        super(message);
    }

    public BusinessTripsAndIllnessAddException(String message, Throwable th) {
        super(message, th);
    }
}
