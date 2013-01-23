package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.form.VacationsForm;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationsFormValidator extends AbstractDateValidator {
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(VacationsForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        final VacationsForm vacationsForm = (VacationsForm) o;

        validateYear(vacationsForm.getYear(), errors);
    }
}
