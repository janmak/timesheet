package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileOutputStream;

public class FeedbackSender extends MailSender {

    private FeedbackForm tsForm;

    private InternetAddress ccAddress;

    public FeedbackSender(SendMailService sendMailService) {
        super(sendMailService);
    }

    @Override
    protected void initCcAddresses() {
        if (tsForm.getEmail() != null && (!tsForm.getEmail().isEmpty())) {
            try {
                ccAddress = new InternetAddress(tsForm.getEmail());
                message.setRecipients(MimeMessage.RecipientType.CC, new InternetAddress[]{ccAddress});
            } catch (MessagingException e) {
                logger.error("Employee email address has wrong format.", e);
            }
        }
    }

    @Override
    protected void initFromAddresses() {
        Integer employeeId = tsForm.getEmployeeId();
        String employeeEmail;
        if (employeeId != null) {
			employeeEmail = sendMailService.employeeService.find(employeeId).getEmail();
		} else {
			return;
		}
        logger.debug("From Address = {}", employeeEmail);
        try {
            fromAddr = new InternetAddress(employeeEmail);

            message.setFrom(fromAddr);
            message.setRecipients(MimeMessage.RecipientType.TO, toAddr);

        } catch (MessagingException e) {
            logger.error("Employee email address has wrong format.", e);
        }
    }

    @Override
    protected void initToAddresses() {
        String toAddress = sendMailService.mailConfig.getProperty("mail.ProblemsAndProposals.toaddress");
        try {
            toAddr = InternetAddress.parse(toAddress);
            logger.debug("To Address = {}", toAddress);
        } catch (AddressException e) {
            logger.error("Email address {} has wrong format.", toAddress, e);
        }
    }

    @Override
    protected void initMessageSubject() {
        String messageSubject = tsForm.getFeedbackTypeName();
        try {
            message.setSubject(messageSubject, "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    @Override
    protected void initMessageBody() {
        Multipart multiPart = new MimeMultipart();
        MimeBodyPart messageText = new MimeBodyPart();
        MultipartFile[] paths = {tsForm.getFile1Path(), tsForm.getFile2Path()};
        StringBuilder bodyTxt = new StringBuilder();

        if (tsForm.getName() != null && !tsForm.getName().isEmpty()) {
            bodyTxt.append("Сообщение пришло от: ").append(tsForm.getName()).append("\n");
        }
        if (tsForm.getEmail() != null && !tsForm.getEmail().isEmpty()) {
            bodyTxt.append("С адреса: ").append(tsForm.getEmail()).append("\n");
        }
        bodyTxt.append(StringEscapeUtils.escapeHtml4(tsForm.getFeedbackDescription()));
        try {
            messageText.setText(bodyTxt.toString(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);
            for ( MultipartFile path : paths ) {
                if ( ( path != null ) && ( ! path.isEmpty() ) ) {
                    MimeBodyPart attach = new MimeBodyPart();
                    File f = File.createTempFile( "tmp_", path.getOriginalFilename() );
                    FileOutputStream fw = new FileOutputStream( f );
                    fw.write( path.getBytes() );
                    fw.close();

                    DataSource file = new FileDataSource( f );

                    attach.setDataHandler( new DataHandler( file ) );
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

        tsForm = form;

        try {
            initSender();

            message = new MimeMessage(session);
            initMessageHead();
            initMessageBody();

            sendMessage();

        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.",
                    sendMailService.mailConfig.getProperty("mail.transport.protocol"), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        } finally {
            deInitSender();
        }
    }

}
