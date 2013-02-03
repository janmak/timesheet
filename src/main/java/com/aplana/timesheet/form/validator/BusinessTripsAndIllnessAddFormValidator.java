package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.form.BusinessTripsAndIllnessAddForm;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/**
 * User: vsergeev
 * Date: 28.01.13
 */
@Service
public class BusinessTripsAndIllnessAddFormValidator extends AbstractValidator {
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(BusinessTripsAndIllnessAddForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        BusinessTripsAndIllnessAddForm form = (BusinessTripsAndIllnessAddForm) o;

        if (form.getBeginDate().after(form.getEndDate())){
            errors.rejectValue("beginDate", "error.businesstripsandilnessaddform.begindate.wrong", "Дата начала должна быть меньше даты конца!");
        }

        if (form.getComment() != null && form.getComment().length() > 200) {
            errors.rejectValue("beginDate", "error.businesstripsandilnessaddform.comment.wrong", "Комментарий слишком длинный! (максимально допускается 200 символов)");
        }

    }
}
