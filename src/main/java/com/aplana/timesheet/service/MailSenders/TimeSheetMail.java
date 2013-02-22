package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.properties.TSPropertyProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
@Component
public class TimeSheetMail extends Mail {

    private String DEFAULT_TIME_SHEET_MAIL_ADRESS = "pcgtimesheet@aplana.com";

    @Autowired
    private TSPropertyProvider propertyProvider;

    @Override
    public String getFromEmail() {
        try {
            String fromEmail = propertyProvider.getMailFromAddress();
            return StringUtils.isNotBlank(fromEmail) ? fromEmail : DEFAULT_TIME_SHEET_MAIL_ADRESS;
        } catch (NullPointerException ex) {
            return DEFAULT_TIME_SHEET_MAIL_ADRESS;
        }
    }

}
