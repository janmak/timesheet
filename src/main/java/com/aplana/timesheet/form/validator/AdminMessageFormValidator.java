package com.aplana.timesheet.form.validator;

/**
 * User: eyaroslavtsev
 * Date: 03.08.12
 * Time: 11:33
 */
import com.aplana.timesheet.form.AdminMessageForm;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
public class AdminMessageFormValidator extends AbstractValidator {

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(AdminMessageForm.class);
    }

    public void validate(Object target, Errors errors) {
        AdminMessageForm amf=(AdminMessageForm) target;

        if( StringUtils.isBlank( amf.getDescription() ) ) {
            errors.rejectValue("description", "error.fbform.feedbackDescription.required", "Суть проблемы не описана.");
        }

        String email=amf.getEmail();

        if ( StringUtils.isBlank( email ) ) {
            errors.rejectValue( "email", "error.fbform.email.required", "Не введен почтовый адрес отправителя." );
        } else if ( ! validateEmail( email ) ){
            errors.rejectValue( "email", "error.fbform.email.invalid2", "Неверный почтовый адрес" );
        }
    }
}
