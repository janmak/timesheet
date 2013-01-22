package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.List;

/**
 * User: eyaroslavtsev
 * Date: 03.08.12
 * Time: 14:13
 */
public class LoginProblemSender extends MailSender<AdminMessageForm> {

    public LoginProblemSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) {

        try {
            MimeBodyPart messageText = new MimeBodyPart();
            Multipart multiPart = new MimeMultipart();

            messageText.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
            multiPart.addBodyPart(messageText);

            message.setContent(multiPart);
        } catch (Exception e) {
            logger.error("Error while init message body.", e);
        }
    }

    public void sendLoginProblem(AdminMessageForm form) {
        sendMessage(form, new MailFunction<AdminMessageForm>() {
            @Override
            public List<Mail> performMailing(@Nullable AdminMessageForm input) throws MessagingException {
                logger.info("Login problem mailing.");
                Mail mail = new Mail();

                StringBuilder bodyTxt = new StringBuilder();

                bodyTxt.append("Логин: ").append(input.getName()).append("\n");
                bodyTxt.append("Указаный адрес: ").append(input.getEmail()).append("\n");
                bodyTxt.append("Ошибка: ").append(input.getError()).append("\n");
                bodyTxt.append("Время: ").append(input.getDate()).append("\n");
                bodyTxt.append("Описание пользователя: ").append(input.getDescription()).append("\n");
                bodyTxt.append(StringEscapeUtils.escapeHtml4(input.getDescription()));

                logger.info(input.toString());

                mail.setPreconstructedMessageBody(bodyTxt.toString());

                return Arrays.asList(mail);
            }
        });
    }
}
