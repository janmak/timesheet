package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class MailSender<T> {

    protected static final Logger logger = LoggerFactory.getLogger(MailSender.class);
    protected static final String DATE_FORMAT = "dd.MM.yyyy";
    protected static final String MAIL_BODY = "mail_body";
    protected static final int FIRST = 0;

    protected SendMailService sendMailService;
    protected TSPropertyProvider propertyProvider;

    public MailSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) { //TODO костыль
        this.sendMailService = sendMailService;
        this.propertyProvider = propertyProvider;
    }

    public final void sendMessage(T params) {
        try {
            initAndSendMessage(getMailList(params));
        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.", propertyProvider.getMailTransportProtocol(), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        }
    }

    protected void initAndSendMessage(List<Mail> mailList) throws MessagingException {
        Transport transport = null;
        try {
            Session session = getMailSession(propertyProvider);

            transport = session.getTransport();
            transport.connect();

            for (Mail mail : mailList) {
                MimeMessage message = new MimeMessage(session);

                initMessageHead(mail, message);
                initMessageBody(mail, message);

                logger.info("Sending message.");
                if (Boolean.parseBoolean(propertyProvider.getMailSendEnable())) {
                    transport.sendMessage(message, message.getAllRecipients());
                    logger.info("Message sended.");
                } else {
                    try {
                        logger.info("Message is formed, but the sending off in the options. Message text: " + message.getContent().toString());
                    } catch (IOException e) {
                        logger.debug("Sending error", e);
                    }
                }
            }
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (MessagingException e) {
                logger.error("Error while closing transport.", e);
            }
        }
    }

    private void initMessageHead(Mail mail, MimeMessage message) {
        InternetAddress fromAddr = initFromAddresses(mail);
        InternetAddress[] ccAddresses = initCcAddresses(mail);
        InternetAddress[] toAddresses = initToAddresses(mail);
        logger.debug("CC Addresses: {}", toAddresses.toString());

        try {
            initMessageSubject(mail, message);

            message.setRecipients(MimeMessage.RecipientType.TO, toAddresses);
            if (ccAddresses != null) {
                message.setRecipients(MimeMessage.RecipientType.CC, ccAddresses);
            }
            message.setFrom(fromAddr);
        } catch (MessagingException e) {
            logger.error("Error while init message recipients.", e);
         }
    }

    @VisibleForTesting
    InternetAddress initFromAddresses(Mail mail) {
        String employeeEmail = mail.getFromEmail();
        logger.debug("From Address = {}", employeeEmail);
        try {
            return new InternetAddress(employeeEmail);
        } catch (MessagingException e) {
            throw new IllegalArgumentException(String.format("Email address %s has wrong format.", employeeEmail), e);
        }
    }
    @VisibleForTesting
    InternetAddress[] initCcAddresses(Mail mail) {
        if (StringUtils.isNotBlank(mail.getCcEmail())) {
            try {
                return InternetAddress.parse(mail.getCcEmail());
            } catch (MessagingException e) {
                logger.error("Employee email address has wrong format.", e);
            }
        }
        return null;
    }
    @VisibleForTesting
    InternetAddress[] initToAddresses(Mail mail) {
        try {
            InternetAddress[] toAddresses = InternetAddress.parse(Joiner.on(",").join(mail.getToEmails()));
            logger.debug("To Address = {}", toAddresses);
            return toAddresses;
        } catch (AddressException e) {
            throw new IllegalArgumentException("Email address has wrong format.", e);
        }
    }
    @VisibleForTesting
    void initMessageSubject(Mail mail, MimeMessage message) throws MessagingException {
        String messageSubject = mail.getSubject();
        logger.debug("Message subject: {}", messageSubject);
        message.setSubject(messageSubject, "UTF-8");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        message.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
    }

    protected List<Mail> getMailList(T params) {
        throw new IllegalAccessError("You must to ovverid getMailList method!");
    }

    @VisibleForTesting
    Session getMailSession(TSPropertyProvider propertyProvider) {
        Properties sysProperties = TSPropertyProvider.getProperties();
        if (StringUtils.isBlank(propertyProvider.getMailSmtpPort())) {
            sysProperties.put("mail.smtp.port", "25");
        }
        Session session;
        if (Boolean.parseBoolean(propertyProvider.getMailSmtpAuth())) {
            session = Session.getInstance(sysProperties, new SMTPAuthenticator());
        } else {
            session = Session.getInstance(sysProperties);
        }
        session.setDebug(true);
        return session;
    }

    private class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(propertyProvider.getMailUsername(), propertyProvider.getMailPassword());
        }
    }
}