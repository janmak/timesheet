package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.MailUtils;
import com.aplana.timesheet.util.TimeSheetUser;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeSheetDeletedSender extends MailSender {

    private TimeSheet deletedTimeSheet;

    public TimeSheetDeletedSender(SendMailService sendMailService) {
        super(sendMailService);
    }

    @Override
    protected void initToAddresses() {
        Integer empId = deletedTimeSheet.getEmployee().getId();
        StringBuilder email = new StringBuilder();
        email.append(deletedTimeSheet.getEmployee().getEmail());
        email.append(",").append(sendMailService.mailConfig.getProperty("mail.fromaddress")).append(",");
        email.append(",").append(sendMailService.getEmployeesManagersEmails(empId)).append(",");
        email.append(",").append(sendMailService.getProjectParticipantsEmails(deletedTimeSheet));
        List<Employee> managers = sendMailService.employeeService.getRegionManager(deletedTimeSheet.getEmployee().getId());
        for(Employee manager:managers) {
            email.append( "," ).append( manager.getEmail() );
        }
        String uniqueSendingEmails = MailUtils.deleteEmailDublicates(email.toString());
        logger.debug("To address: {}", uniqueSendingEmails);
        try {
            toAddr = InternetAddress.parse(uniqueSendingEmails);
        } catch (AddressException e) {
            logger.error("Email address has wrong format.", e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody() {
        Map model = new HashMap();

        TimeSheetUser securityPrincipal = sendMailService.securityService.getSecurityPrincipal();

        model.put("managerName", securityPrincipal.getEmployee().getName());
        model.put("deletedTimeSheet", deletedTimeSheet);
        model.put("dateStr", DateTimeUtil.formatDate(deletedTimeSheet.getCalDate().getCalDate()));

        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "timesheetdeleted.vm", model);
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
        messageSubject.append("Удален отчет сотрудника ").append(deletedTimeSheet.getEmployee().getName()).append(" за ").append(DateTimeUtil.formatDate(deletedTimeSheet.getCalDate().getCalDate()));
        logger.debug("Message subject: {}", messageSubject.toString());
        try {
            message.setSubject(messageSubject.toString(), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    public void sendMessage(TimeSheet timeSheet) {

        deletedTimeSheet = timeSheet;

        try {
            initSender();

            logger.info("Performing mailing about deleted timesheet.");

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
