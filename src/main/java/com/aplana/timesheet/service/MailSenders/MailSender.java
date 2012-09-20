package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.service.SendMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class MailSender {

    protected static final Logger logger = LoggerFactory
            .getLogger(MailSender.class);

    protected MimeMessage message;
    protected Transport transport;
    protected InternetAddress[] toAddr;
    protected InternetAddress fromAddr;
    protected InternetAddress[] ccAddr;
    protected Session session;

    protected SendMailService sendMailService;

    public MailSender(SendMailService sendMailService) {
        this.sendMailService = sendMailService;
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
        String fromAddress = sendMailService.mailConfig.getProperty("mail.pcg.toaddress");
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
        if (Boolean.parseBoolean(sendMailService.mailConfig.getProperty("mail.send.enable"))) {
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
        Properties sysProperties = System.getProperties();
        sysProperties.put("mail.transport.protocol",
                sendMailService.mailConfig.getProperty("mail.transport.protocol"));
        sysProperties.put("mail.smtp.host",
                sendMailService.mailConfig.getProperty("mail.smtp.host"));
        sysProperties.put("mail.smtp.auth",
                sendMailService.mailConfig.getProperty("mail.smtp.auth"));
        if (!"".equals(sendMailService.mailConfig.getProperty("mail.smtp.port"))) {
            sysProperties.put("mail.smtp.port",
                    sendMailService.mailConfig.getProperty("mail.smtp.port"));
        } else {
            sysProperties.put("mail.smtp.port", "25");
        }
        Authenticator auth = new SMTPAuthenticator();
        Session session = null;
        if (Boolean.parseBoolean(sendMailService.mailConfig.getProperty("mail.smtp.auth"))) {
            session = Session.getInstance(sysProperties, auth);
        } else {
            session = Session.getInstance(sysProperties);
        }
        session.setDebug(true);
        return session;
    }

    private class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = sendMailService.mailConfig.getProperty("mail.username");
            String password = sendMailService.mailConfig.getProperty("mail.password");
            return new PasswordAuthentication(username, password);
        }
    }

}