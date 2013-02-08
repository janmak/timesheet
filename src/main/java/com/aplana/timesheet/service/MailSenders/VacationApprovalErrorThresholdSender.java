package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Arrays;
import java.util.List;

/**
 * @author iziyangirov
 */

public class VacationApprovalErrorThresholdSender extends MailSender<String> {

    public VacationApprovalErrorThresholdSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected List<Mail> getMailList(String str) {
        Mail mail = new Mail();

        mail.setFromEmail(propertyProvider.getMailFromAddress());
        mail.setSubject("Попытка подбора guid для сервиса согласования отпусков.");
        mail.setPreconstructedMessageBody("Обнаружена попытка подбора guid для сервиса согласования отпусков, подробности в логах сервера.");
        mail.setToEmails(Arrays.asList(propertyProvider.getMailProblemsAndProposalsCoaddress()));
        mail.setFromEmail(propertyProvider.getMailFromAddress());
        return Arrays.asList(mail);
    }

}
