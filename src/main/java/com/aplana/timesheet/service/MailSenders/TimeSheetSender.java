package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class TimeSheetSender extends MailSender {

    private TimeSheetForm tsForm;

    public TimeSheetSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initFromAddresses() {
        String employeeEmail = sendMailService.getEmployeeEmail(tsForm.getEmployeeId());
        try {
            logger.debug("From Address = {}", employeeEmail);
            fromAddr = new InternetAddress(employeeEmail);
        } catch (AddressException e) {
            logger.error("Employee email address has wrong format.", e);
        }
    }

    @Override
    protected void initToAddresses() {
        StringBuilder toAddresses = new StringBuilder();

        toAddresses.append(sendMailService.getEmployeeEmail(tsForm.getEmployeeId())).append(",");
        toAddresses.append(sendMailService.getEmployeesManagersEmails(tsForm.getEmployeeId()));
        logger.debug("EmployeesManagersEmails: {}", toAddresses.toString());
        toAddresses.append(sendMailService.getProjectsManagersEmails(tsForm));
        logger.debug(" + ProjectsManagersEmail: {}", toAddresses.toString());
        toAddresses.append(sendMailService.getProjectParticipantsEmails(tsForm.getEmployeeId(), tsForm));
        logger.debug(" + ProjectParticipantsEmails: {}", toAddresses.toString());
        List<Employee> managers = sendMailService.getRegionManagerList(tsForm.getEmployeeId());
        for(Employee manager:managers) {
            toAddresses.append( "," ).append( manager.getEmail() );
        }
        logger.debug(" + To Addresses: {}", toAddresses.toString());
        String uniqueSendingEmails = deleteEmailDublicates(toAddresses.toString());
        logger.debug(" + To Addresses: {}", uniqueSendingEmails);
        try {
            toAddr = InternetAddress.parse(uniqueSendingEmails);
        } catch (AddressException e) {
            logger.error("Email address has wrong format.", e);
        }
    }

    @Override
    protected void initMessageSubject() {
        String calDate = tsForm.getCalDate();
        String beginLongDate = tsForm.getBeginLongDate();
        String date;
        StringBuilder messageSubject = new StringBuilder();
        messageSubject.append("Status report - ");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

        if (!tsForm.isLongIllness() && !tsForm.isLongVacation()) {
            date = sdf.format(DateTimeUtil.stringToDate(calDate, "yyyy-MM-dd"));
        } else {
            date = sdf.format(DateTimeUtil.stringToDate(beginLongDate, "yyyy-MM-dd"));
        }
        messageSubject.append(date);
        logger.debug("Message subject: {}", messageSubject.toString());
        try {
            message.setSubject(messageSubject.toString(), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initMessageBody() {
        Map model = sendMailService.getPreFilledModel();

        model.put("tsForm", tsForm);

        logger.info("follows initialization output from velocity");
        String messageBody =
                VelocityEngineUtils.mergeTemplateIntoString(sendMailService.velocityEngine, "sendmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    public void sendTimeSheetMessage(TimeSheetForm form) {

        tsForm = form;

        try {
            initSender();

            logger.info("Performing timesheet mailing.");

            message = new MimeMessage(session);
            initMessageHead();
            initMessageBody();

            sendMessage();

        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.", propertyProvider.getMailTransportProtocol(), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        } finally {
            deInitSender();
        }

    }
}
