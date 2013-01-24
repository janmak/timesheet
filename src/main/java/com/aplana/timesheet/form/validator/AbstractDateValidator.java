package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractDateValidator extends AbstractValidator {

    @Autowired
    private CalendarService calendarService;

    protected void validateYear(Integer year, Errors errors) {
        // Год не выбран.
        if ( isNotChoosed( year ) ) {
            errors.rejectValue( "year",
                    "error.tsform.year.required",
                    "Не выбран год." );
        }
        // Неверный год
        else if ( ! isYearValid( year ) ) {
            errors.rejectValue( "year",
                    "error.tsform.year.invalid",
                    "Выбран неверный год." );
        }
    }

    private boolean isYearValid(Integer year) {
        return calendarService.yearValid(year);
    }

    protected void validateMonth(Integer year, Integer month, Errors errors) {
        // Месяц не выбран.
        if ( isNotChoosed( month ) ) {
            errors.rejectValue( "month",
                    "error.tsform.month.required",
                    "Не выбран месяц." );
        }

        // Неверный месяц
        else if ( ! calendarService.monthValid(year, month) ) {
            errors.rejectValue( "month",
                    "error.tsform.month.required",
                    "Выбран неверный месяц." );
        }
    }
}
