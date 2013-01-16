package com.aplana.timesheet.form.validator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eshangareev
 * @version 1.0
 */
public class AbstractValidatorTest {

    @Test
    public void testValidate() throws Exception {
        Assert.assertTrue( AbstractValidator.validateEmail( "eshangareev@aplana.com" ) );
        Assert.assertTrue( AbstractValidator.validateEmail( "eshangareev.some@aplana.com" ) );
        Assert.assertTrue( AbstractValidator.validateEmail( "eshangareev@aplana.some.com" ) );
        Assert.assertTrue( AbstractValidator.validateEmail( "eshangareev.some@aplana.some.com" ) );

    }
}
