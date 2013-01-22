package com.aplana.timesheet.service.MailSenders;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eshangareev
 * @version 1.0
 */
public class MailSenderTest {
    @Test
    public void testDeleteEmailDublicates() throws Exception {
        String initial = "123@123.12,123@123.12,123@123.12,243@24.24,123@123.12";
        String processed = MailSender.deleteEmailDublicates(initial);

        Assert.assertTrue(processed.split(",").length == 2);
        Assert.assertEquals("123@123.12,243@24.24", processed);
    }
}
