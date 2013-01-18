package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class MailSender {

    protected static final Logger logger = LoggerFactory.getLogger(MailSender.class);

    protected MimeMessage message;
    protected Transport transport;
    protected InternetAddress[] toAddr;
    protected InternetAddress fromAddr;
    protected InternetAddress[] ccAddr;
    protected Session session;

    protected SendMailService sendMailService;
    protected TSPropertyProvider propertyProvider;

    public MailSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) { //TODO костыль
        this.sendMailService = sendMailService;
        this.propertyProvider = propertyProvider;
    }

    protected void initMessageHead() {
        initFromAddresses();
        initCcAddresses();
        initToAddresses();
        initMessageSubject();
        try {
            message.setRecipients(MimeMessage.RecipientType.CC, ccAddr);
            message.setRecipients(MimeMessage.RecipientType.TO, toAddr);
            message.setFrom(fromAddr);
        } catch (MessagingException e) {
            logger.error("Error while init message recipients.", e);
         }
    }

    protected void initFromAddresses() {
        String fromAddress = propertyProvider.getMailFromAddress();
        try {
            fromAddr = InternetAddress.parse(fromAddress)[0];
            logger.debug("From Address = {}", fromAddress);
        } catch (AddressException e) {
            logger.error("Email address {} has wrong format.", fromAddress, e);
        }
    }

    protected void initCcAddresses() {
        logger.debug("MailSender.initCcAddresses");
    }

    protected void initToAddresses() {
        logger.debug("MailSender.initToAddresses");
    }

    protected void initMessageSubject() {
        logger.debug("MailSender.initMessageSubject");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody() {
        logger.debug("MailSender.initMessageBody");
    }

    public void initSender() throws MessagingException {
        session = getMailSession();

        transport = session.getTransport();
        transport.connect();
    }

    public void sendMessage() throws MessagingException {
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

    public void deInitSender() {
        try {
            transport.close();
        } catch (MessagingException e) {
            logger.error("Error while closing transport.", e);
        }
    }

    protected Session getMailSession() {
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

    /**
     * Возвращает адреса имеил без дубликатов
     *
     * @param emails
     */
    public static String deleteEmailDublicates(String emails) {
        Set<String> uniqueEmails = new HashSet<String>();
        String[] splittedEmails = emails.split(",");
        for (String splitEmail : splittedEmails) {
            uniqueEmails.add(splitEmail.trim());
        }

        String join = org.apache.commons.lang.StringUtils.join(uniqueEmails, ",");
        logger.debug("splitted emails: {} ", join);
        return join;
    }

}