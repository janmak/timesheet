package com.aplana.timesheet.controller.quickreport.generators;

import com.aplana.timesheet.controller.quickreport.BusinessTripsQuickReport;
import com.aplana.timesheet.dao.entity.BusinessTrip;
import com.aplana.timesheet.dao.entity.Employee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Date: 21.01.13
 * Time: 22:38
 */
@Component
@Qualifier(value = "businessTripsQuickReportGenerator")
public class BusinessTripsQuickReportGenerator extends AbstractQuickReportGenerator<BusinessTripsQuickReport, BusinessTrip> {

    @Override
    protected List<BusinessTrip> getAllPeriodicals(Employee employee) {
        return businessTripService.getEmployeeBusinessTrips(employee);
    }

    @Override
    protected BusinessTripsQuickReport addDifferentPartOfPeriodicleToMounthStatistics(BusinessTripsQuickReport report, BusinessTrip periodical) {
        return report;
    }

    @Override
    protected BusinessTripsQuickReport createQuickReport() {
        return new BusinessTripsQuickReport();
    }

    @Override
    protected BusinessTripsQuickReport addMounthStatisticsToYearStatistics(BusinessTripsQuickReport report) {
        return report;
    }

    @Override
    protected BusinessTripsQuickReport addPartOfPeriodicleToYearStatistics(BusinessTripsQuickReport report, BusinessTrip periodical, Date beginDate, Date date) {
        return report;
    }

    @Override
    protected BusinessTripsQuickReport addPeriodicalToYearStatistics(BusinessTripsQuickReport report, BusinessTrip periodical, Date yearBeginDate, Date yearEndDate) {
        return report;
    }
}
