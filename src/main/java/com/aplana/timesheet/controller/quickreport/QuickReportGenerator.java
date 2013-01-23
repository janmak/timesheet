package com.aplana.timesheet.controller.quickreport;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;

import java.util.Date;

/**
 * User: vsergeev
 * Date: 22.01.13
 */
public interface QuickReportGenerator<T extends QuickReport> {

    T generate(Employee employee, Date periodBeginDate, Date periodEndDate, Date yearBeginDate, Date yearEndDate) throws BusinessTripsAndIllnessControllerException;

}
