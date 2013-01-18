package com.aplana.timesheet.form.validator;

/**
 * User: eyaroslavtsev
 * Date: 03.08.12
 * Time: 11:33
 */
import com.aplana.timesheet.form.AdminMessageForm;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdminMessageFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(AdminMessageForm.class);
    }

    public void validate(Object target, Errors errors) {
        AdminMessageForm amf=(AdminMessageForm) target;
        if(!isDescriptionValid(amf.getDescription())) {
            errors.rejectValue("description", "error.fbform.feedbackDescription.required", "Суть проблемы не описана.");
        }
        String email=amf.getEmail();
        if(email==null || email.isEmpty()){
            errors.rejectValue("email", "error.fbform.email.required", "Неввведен почтовый адрес отправителя.");
        }else{
            if(!isEmailValid(email))
                errors.rejectValue("email", "error.fbform.email.invalid2", "Неверный почтовый адрес");
        }
    }
    private boolean isDescriptionValid(String description) {
        return ! ( description == null || description.equals( "" ) );
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("[a-zA-Z][a-zA-Z\\d\\u002E\\u005F]+@([a-zA-Z]+\\u002E){1,2}((net)|(com)|(org)|(ru))");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
