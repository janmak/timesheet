package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class PersonalAlertSender extends MailSender {
    private List<ReportCheck> reportCheckList;
    private ReportCheck currentReportCheck;

    public PersonalAlertSender(SendMailService sendMailService) {
        super(sendMailService);
    }

    @Override
    protected void initToAddresses() {
        String email = currentReportCheck.getEmployee().getEmail();
        logger.debug("To Address: {}", email);
        try {
            toAddr = InternetAddress.parse(email);
        } catch (AddressException e) {
            logger.error("Email address has wrong format.", e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody() {
        Map model = new HashMap();
        model.put("currentReportCheck", currentReportCheck);
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "alertpersonalmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected void initMessageSubject() {
        StringBuilder messageSubject = new StringBuilder();
        messageSubject.append("Cрочно списать занятость за ");

        List<String> monthList = new ArrayList<String>();
        
        List<String> passedDays = currentReportCheck.getPassedDays();

        String monthName;

        for (Iterator<String> iterator = passedDays.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            monthName = DateTimeUtil.getMonthTxt(next);
            if (!monthList.contains(monthName))
                monthList.add(monthName);
        }

        String text = "";

        for (Iterator<String> iterator = monthList.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();

            text += next;
            if (iterator.hasNext())
                text += ", ";
        }

        messageSubject.append(text);

        logger.debug("Message subject: {}", messageSubject.toString());
        try {
            message.setSubject(messageSubject.toString(), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    public void sendAlert(List<ReportCheck> rCheckList) {
        reportCheckList = rCheckList;

        try {
            initSender();

            logger.info("Performing personal mailing.");

            Iterator<ReportCheck> iterator = reportCheckList.iterator();
            while (iterator.hasNext()) {
                currentReportCheck = iterator.next();
                message = new MimeMessage(session);
                initMessageHead();
                initMessageBody();

                sendMessage();
            }
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
