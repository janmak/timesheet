package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public abstract class MailSender<T> {

    protected static final Logger logger = LoggerFactory.getLogger(MailSender.class);

    protected SendMailService sendMailService;
    protected TSPropertyProvider propertyProvider;

    public MailSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) { //TODO костыль
        this.sendMailService = sendMailService;
        this.propertyProvider = propertyProvider;
    }

    private void initMessageHead(Mail mail, MimeMessage message) {
        InternetAddress fromAddr = initFromAddresses(mail);
        InternetAddress[] ccAddresses = initCcAddresses(mail);
        InternetAddress[] toAddresses = initToAddresses(mail);
        logger.debug("CC Addresses: {}", toAddresses.toString());

        initMessageSubject(mail, message);
        try {
            message.setRecipients(MimeMessage.RecipientType.TO, toAddresses);
            if (ccAddresses != null) {
                message.setRecipients(MimeMessage.RecipientType.CC, ccAddresses);
            }
            message.setFrom(fromAddr);
        } catch (MessagingException e) {
            logger.error("Error while init message recipients.", e);
         }
    }

    protected InternetAddress initFromAddresses(Mail mail) {
        String fromAddress = propertyProvider.getMailFromAddress();
        try {
            return InternetAddress.parse(fromAddress)[0];
        } catch (AddressException e) {
            throw new IllegalArgumentException(String.format("Email address %s has wrong format.", fromAddress), e);
        }
    }

    protected InternetAddress[] initCcAddresses(Mail mail) {
        logger.debug("MailSender.initCcAddresses");
        return null;
    }

    private InternetAddress[] initToAddresses(Mail mail) {
        try {
            InternetAddress[] toAddresses = getToAddresses(mail);
            logger.debug("To Address = {}", toAddresses);
            return toAddresses;
        } catch (AddressException e) {
            throw new IllegalArgumentException("Email address has wrong format.", e);
        }
    }

    protected InternetAddress[] getToAddresses(Mail mail) throws AddressException{
        logger.debug("MailSender.initToAddresses");
        return null;
    }

    protected void initMessageSubject(Mail mail, MimeMessage message) {
        logger.debug("MailSender.initMessageSubject");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        logger.debug("MailSender.initMessageBody");
    }


    protected void initAndSendMessage(List<Mail> mailList) throws MessagingException {
        Transport transport = null;
        try {
            Session session = getMailSession();

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


    public interface MailFunction<F> {
        List<Mail> performMailing(@Nullable F input) throws MessagingException;
    }

    protected void sendMessage(T params, MailFunction<T> performFunction ) {
        try {
            List<Mail> mails = performFunction.performMailing(params);
            initAndSendMessage(mails);
        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.", propertyProvider.getMailTransportProtocol(), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        }
    }

    private Session getMailSession() {
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
        return Joiner.on(",").join(Sets.newHashSet(
                Iterables.transform(Arrays.asList(emails.split(",")), new Function<String, String>() {
                    @Nullable @Override public String apply(@Nullable String s) {
                        return s.trim();
        } })));
    }

}