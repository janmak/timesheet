package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.form.VacationsForm;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.SecurityService;
import com.aplana.timesheet.service.VacationService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationsFormValidator extends AbstractDateValidator {

    @Autowired
    private CalendarService calendarService;

    private static final String WRONG_YEAR_ERROR_MESSAGE = "Календарь на %i год еще не заполнен, " +
            "оформите заявление позже или обратитесь в службу поддержки системы";

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(VacationsForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        final VacationsForm vacationsForm = (VacationsForm) o;

        validateYear(vacationsForm.getYear(), errors);
    }

    public void validate(VacationsForm vacationsForm, Errors errors){
        final String calFromDate = vacationsForm.getCalFromDate();
        final String calToDate = vacationsForm.getCalToDate();

        final boolean calFromDateIsNotEmpty = StringUtils.length(calFromDate) > 0;
        final boolean calToDateIsNotEmpty = StringUtils.length(calToDate) > 0;

        if (calFromDateIsNotEmpty && calToDateIsNotEmpty) {
            final Timestamp fromDate = DateTimeUtil.stringToTimestamp(calFromDate);
            final Timestamp toDate = DateTimeUtil.stringToTimestamp(calToDate);

            if (fromDate.after(toDate)) {
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.wrongtodate",
                        "Дата окончания отпуска не может быть больше даты начала"
                );
            }
            if (calendarService.find(toDate) == null){
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.wrongyear",
                        new Object[]{toDate.getYear()+1900},
                        WRONG_YEAR_ERROR_MESSAGE
                );
            }
        } else {
            if (!calFromDateIsNotEmpty) {
                errors.rejectValue(
                        "calFromDate",
                        "error.createVacation.fromdate.required",
                        "Не указана дата начала отпуска"
                );
            }

            if (!calToDateIsNotEmpty) {
                errors.rejectValue(
                        "calToDate",
                        "error.createVacation.todate.required",
                        "Не указана дата окончания отпуска"
                );
            }
        }
    }
}
