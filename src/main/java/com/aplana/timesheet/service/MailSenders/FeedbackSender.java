package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.Arrays;
import java.util.List;

public class FeedbackSender extends MailSender<FeedbackForm> {

    public FeedbackSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected InternetAddress[] initCcAddresses(Mail mail) {
        if (StringUtils.isNotBlank(mail.getCcEmail())) {
            try {
                return new InternetAddress[]{new InternetAddress(mail.getCcEmail())};
            } catch (MessagingException e) {
                logger.error("Employee email address has wrong format.", e);
            }
        }
        return null;
    }

    @Override
    protected InternetAddress initFromAddresses(Mail mail) {
        String employeeEmail = mail.getFromEmail();
        logger.debug("From Address = {}", employeeEmail);
        try {
            return new InternetAddress(employeeEmail);
        } catch (MessagingException e) {
            throw new IllegalArgumentException(String.format("Email address %s has wrong format.", employeeEmail), e);
        }
    }

    @Override
    protected InternetAddress[] getToAddresses(Mail mail) throws AddressException{
        String toAddress = propertyProvider.getMailProblemsAndProposalsCoaddress();
        return InternetAddress.parse(toAddress);
    }

    @Override
    protected void initMessageSubject(Mail mail, MimeMessage message) {
        String messageSubject = mail.getSubject();
        try {
            message.setSubject(messageSubject, "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) {
        try {
            Multipart multiPart = new MimeMultipart();

            MimeBodyPart messageText = new MimeBodyPart();
            messageText.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);

            for ( MultipartFile path : mail.getFilePahts()) {
                if ( ( path != null ) && ( ! path.isEmpty() ) ) {
                    MimeBodyPart attach = new MimeBodyPart();
                    attach.setDataHandler( new DataHandler(new ByteArrayDataSource(path.getBytes(), path.getContentType())) );
                    attach.setFileName( path.getOriginalFilename() );
                    multiPart.addBodyPart( attach );
                }
            }
            message.setContent(multiPart);
        } catch (Exception e) {
            logger.error("Error while init message body.", e);
        }
    }

    public void sendFeedbackMessage(FeedbackForm form) {
        sendMessage(form, new MailFunction<FeedbackForm>() {
            @Override
            public List<Mail> performMailing(@Nullable FeedbackForm input) throws MessagingException {
                Mail mail = new Mail();
                mail.setCcEmail(input.getEmail());
                mail.setFromEmail(sendMailService.getEmployeeEmail(input.getEmployeeId()));
                mail.setSubject(input.getFeedbackTypeName());
                mail.setFilePahts(Arrays.asList(input.getFile1Path(), input.getFile2Path()));
                mail.setPreconstructedMessageBody(getMessageBody(input));
                return Arrays.asList(mail);
            }
        });
    }

    private String getMessageBody(FeedbackForm input) {
        StringBuilder bodyTxt = new StringBuilder();

        if (StringUtils.isNotBlank(input.getName())) {
            bodyTxt.append("Сообщение пришло от: ").append(input.getName()).append("\n");
        }
        if (StringUtils.isNotBlank(input.getEmail())) {
            bodyTxt.append("С адреса: ").append(input.getEmail()).append("\n");
        }
        bodyTxt.append(StringEscapeUtils.escapeHtml4(input.getFeedbackDescription()));

        return bodyTxt.toString();
    }
}
