package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EndMonthAlertSender extends MailSender {
    private List<ReportCheck> reportCheckList;

    public EndMonthAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initToAddresses() {
        StringBuilder toAddresses = new StringBuilder();

        // Формируем список сотрудников, у которых нет долгов по отчетности
        List<Employee> employeeList = sendMailService.getEmployeesList(reportCheckList.get(0).getDivision());

        for (ReportCheck reportCheck : reportCheckList) {

            for (Employee employee : employeeList) {
                if (reportCheck.getEmployee().getId().equals(employee.getId())) {
                    employeeList.remove(employee);
                    break;
                }
            }
        }

        for (Employee employee : employeeList) {
            toAddresses.append(employee.getEmail());
            toAddresses.append(",");
        }

        logger.debug("EmployeesEmails: {}", toAddresses.toString());
        String uniqueSendingEmails = deleteEmailDublicates(toAddresses
                .toString());
        try {
            toAddr = InternetAddress.parse(uniqueSendingEmails);
            logger.debug("CC Addresses: {}", toAddresses.toString());
        } catch (AddressException e) {
            logger.error("Email address has wrong format.", e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody() {
        Map model = new HashMap();
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "alertendmonthmail.vm", model);
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
        messageSubject.append("Не забудьте списать занятость за ");
        messageSubject.append(DateTimeUtil.getMonthTxt(DateTimeUtil.currentDay()));
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

            logger.info("Performing last day of month mailing.");

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
