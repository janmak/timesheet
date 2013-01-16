package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.controller.ViewReportsController;
import com.aplana.timesheet.form.ViewReportsForm;
import com.aplana.timesheet.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Service
public class ViewReportsFormValidator extends AbstractValidator {
	private static final Logger logger = LoggerFactory.getLogger(ViewReportsController.class);

	@Autowired
	private CalendarService calendarService;
	
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(ViewReportsForm.class);
	}

	public void validate(Object target, Errors errors) {
		ViewReportsForm tsForm = (ViewReportsForm) target;
		Integer year = tsForm.getYear();
		Integer month = tsForm.getMonth();
		logger.debug("Year = {} and Month = {}.", year, month);
		
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
		
		// Месяц не выбран.
        if ( isNotChoosed( month ) ) {
            errors.rejectValue( "month",
                    "error.tsform.month.required",
                    "Не выбран месяц." );
        }

        // Неверный месяц
        else if ( ! isMonthValid( year, month ) ) {
            errors.rejectValue( "month",
                    "error.tsform.month.required",
                    "Выбран неверный месяц." );
        }
		
	}
	private boolean isYearValid(Integer year) {
		return calendarService.yearValid(year);
	}
	
	private boolean isMonthValid(Integer year, Integer month) {
		return calendarService.monthValid(year,month);
	}
}