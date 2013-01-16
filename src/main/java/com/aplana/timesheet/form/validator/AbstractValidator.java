package com.aplana.timesheet.form.validator;

import org.springframework.validation.Validator;

import java.util.regex.Pattern;

/**
 * @author eshangareev
 * @version 1.0
 */
public abstract class AbstractValidator implements Validator {
    private static Pattern emailPattern =
            Pattern.compile(
                    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                            "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\" +
                            "[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
                            "@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\" +
                            "[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|" +
                            "[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]" +
                            "|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])" );


    public static boolean validateEmail( String email ) {
        return emailPattern.matcher( email ).matches();
    }

    public static boolean isNotChoosed( Integer id ) {
        return id == null || id == 0;
    }
}
