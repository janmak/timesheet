package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.CalendarDAO;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Holiday;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatus;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class VacationsController extends AbstractControllerForEmployeeWithYears {

    @Autowired
    private VacationsFormValidator vacationsFormValidator;

    @Autowired
    private VacationDAO vacationDAO;

    @Autowired
    private CalendarDAO calendarDAO;

    @RequestMapping(value = "/vacations", method = RequestMethod.GET)
    public String prepareToShowVacations() {
        final Calendar calendar = Calendar.getInstance();
        final Employee employee = securityService.getSecurityPrincipal().getEmployee();

        return String.format(
                "redirect:/vacations/%s/%s/%s",
                employee.getDivision().getId(),
                employee.getId(),
                calendar.get(Calendar.YEAR)
        );
    }

    @RequestMapping(value = "/vacations/{divisionId}/{employeeId}/{year}")
    public ModelAndView showVacations(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("year") Integer year,
            @ModelAttribute("vacationsForm") VacationsForm vacationsForm,
            BindingResult result
    ) {
        vacationsFormValidator.validate(vacationsForm, result);

        final ModelAndView modelAndView = createModelAndViewForEmployee("vacations", employeeId, divisionId);

        final Employee employee = (Employee) modelAndView.getModel().get(EMPLOYEE);
        final List<Vacation> vacations = vacationDAO.findVacations(employeeId, year);

        final int vacationsSize = vacations.size();

        final List<Integer> calDays = new ArrayList<Integer>(vacationsSize);
        final List<Integer> workDays = new ArrayList<Integer>(vacationsSize);

        int summaryApproved = 0;
        int summaryRejected = 0;
        int summaryCalDays = 0;
        int summaryWorkDays = 0;

        if (!vacations.isEmpty()) {
            final Vacation firstVacation = vacations.get(0);

            Date minDate = firstVacation.getBeginDate();
            Date maxDate = firstVacation.getEndDate();

            Date beginDate, endDate;
            for (Vacation vacation : vacations) {
                beginDate = vacation.getBeginDate();
                endDate = vacation.getEndDate();

                if (minDate.after(beginDate)) {
                    minDate = beginDate;
                }

                if (maxDate.before(endDate)) {
                    maxDate = endDate;
                }

                calDays.add(getDiffInDays(beginDate, endDate));
            }

            final List<Holiday> holidaysForRegion =
                    calendarDAO.getHolidaysForRegion(minDate, maxDate, employee.getRegion());
            final Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, year);
            final Date currentYearBeginDate = DateUtils.truncate(calendar.getTime(), Calendar.YEAR);

            calendar.setTime(currentYearBeginDate);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.YEAR, year);
            final Date currentYearEndDate = calendar.getTime();

            for (int i = 0; i < vacationsSize; i++) {
                final Vacation vacation = vacations.get(i);
                final int holidaysCount = getHolidaysCount(holidaysForRegion, vacation.getBeginDate(), vacation.getEndDate());

                final int calDaysCount = calDays.get(i);
                final int workDaysCount = calDaysCount - holidaysCount;

                workDays.add(workDaysCount);

                final VacationStatus vacationStatus =
                        EnumsUtils.getEnumById(vacation.getStatus().getId(), VacationStatus.class);

                if (vacationStatus == VacationStatus.APPROVED) {
                    beginDate = vacation.getBeginDate();
                    endDate = vacation.getEndDate();

                    calendar.setTime(beginDate);
                    final int beginYear = calendar.get(Calendar.YEAR);

                    calendar.setTime(endDate);
                    final int endYear = calendar.get(Calendar.YEAR);

                    if (beginYear == year && year == endYear) {
                        summaryCalDays += calDaysCount;
                        summaryWorkDays += workDaysCount;
                    } else {
                        final long days = DateUtils.getFragmentInDays(endDate, Calendar.YEAR);

                        if (endYear == year) {
                            summaryCalDays += days;
                            summaryWorkDays += days - getHolidaysCount(holidaysForRegion, currentYearBeginDate, endDate);
                        } else {
                            final long daysInCurrentYear = calDaysCount - days;

                            summaryCalDays += daysInCurrentYear;
                            summaryWorkDays += daysInCurrentYear -
                                    getHolidaysCount(holidaysForRegion, beginDate, currentYearEndDate);
                        }
                    }

                    summaryApproved++;
                }

                if (vacationStatus == VacationStatus.REJECTED) {
                    summaryRejected++;
                }
            }
        }

        modelAndView.addObject("year", year);
        modelAndView.addObject("vacationsList", vacations);
        modelAndView.addObject("calDays", calDays);
        modelAndView.addObject("workDays", workDays);
        modelAndView.addObject("summaryApproved", summaryApproved);
        modelAndView.addObject("summaryRejected", summaryRejected);
        modelAndView.addObject("summaryCalDays", summaryCalDays);
        modelAndView.addObject("summaryWorkDays", summaryWorkDays);

        return modelAndView;
    }

    private int getDiffInDays(Date beginDate, Date endDate) {
        return (int) ((endDate.getTime() - beginDate.getTime()) / (24 * 3600 * 1000) + 1);
    }

    private int getHolidaysCount(List<Holiday> holidaysForRegion, final Date beginDate, final Date endDate) {
        return Iterables.size(Iterables.filter(holidaysForRegion, new Predicate<Holiday>() {
            @Override
            public boolean apply(@Nullable Holiday holiday) {
                final Timestamp calDate = holiday.getCalDate().getCalDate();

                return (
                        calDate.compareTo(beginDate) == 0 || calDate.compareTo(endDate) == 0 ||
                                calDate.after(beginDate) && calDate.before(endDate)
                );
            }
        }));
    }

}
