package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.CalendarDAO;
import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Holiday;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.form.validator.VacationsFormValidator;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.EnumsUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private SendMailService sendMailService;

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

                final VacationStatusEnum vacationStatus =
                        EnumsUtils.getEnumById(vacation.getStatus().getId(), VacationStatusEnum.class);

                if (vacationStatus == VacationStatusEnum.APPROVED) {
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

                if (vacationStatus == VacationStatusEnum.REJECTED) {
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
        modelAndView.addObject("curEmployee", securityService.getSecurityPrincipal().getEmployee());

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

    @RequestMapping(value = "/deleteVacation/{vac_id}", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteVacation(
            @PathVariable("vac_id") Integer vacationId
    ) {
        try {
            final Vacation vacation = vacationDAO.findVacation(vacationId);

            if (vacation == null) { // если вдруг удалил автор, а не сотрудник
                return "Запись не найдена";
            }

            final Employee employee = securityService.getSecurityPrincipal().getEmployee();
            final boolean isAdmin = employeeService.isEmployeeAdmin(employee.getId());

            final DictionaryItem statusDictionaryItem = vacation.getStatus();
            final VacationStatusEnum vacationStatus =
                    EnumsUtils.getEnumById(statusDictionaryItem.getId(), VacationStatusEnum.class);

            if (
                    employee.equals(vacation.getEmployee()) ||
                    employee.equals(vacation.getAuthor()) ||
                    isAdmin
            ) {
                if (!isAdmin && (vacationStatus == VacationStatusEnum.REJECTED || vacationStatus == VacationStatusEnum.APPROVED)) {
                    return String.format(
                            "Нельзя удалить заявление на отпуск в статусе \"%s\". Для удаления данного заявления " +
                                    "необходимо написать на timesheet@aplana.com",
                            statusDictionaryItem.getValue()
                    );
                }

                vacationDAO.delete(vacation);

                sendMailService.performVacationDeletedMailing(vacation);

                return StringUtils.EMPTY;
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }

        return "Ошибка доступа";
    }

}
