package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * User: vsergeev
 * Date: 15.02.13
 */
public abstract class AbstractVacationApprovalSender extends AbstractSenderWithAssistants<VacationApproval>{

    private static final String DEFAULT_VACATION_APPROVAL_MAIL_MARKER = "[VACATION REQUEST]";

    public AbstractVacationApprovalSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    final protected String getSubjectFormat() {
        String marker = null;

        try {
            marker = propertyProvider.getVacationMailMarker();
        } catch (NullPointerException ex) {
            // do nothing
        }

        if (StringUtils.isBlank(marker)) {
            marker = DEFAULT_VACATION_APPROVAL_MAIL_MARKER;
        }

        return marker + " %s";
    }

    @Override
    final protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        try {
            if (mail.getParamsForGenerateBody() != null) {
                message.setText(mail.getParamsForGenerateBody().get(FIRST, MAIL_BODY), "UTF-8", "plain");
            }else{
                super.initMessageBody(mail, message);
            }
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }
}
