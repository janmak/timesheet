package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Iterables;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

import static com.aplana.timesheet.util.DateTimeUtil.formatDateString;

public class TimeSheetDeletedSender extends AbstractSenderWithAssistants<TimeSheet> {

    public TimeSheetDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = new HashMap();


        model.put("managerName", sendMailService.getSecurityPrincipal().getEmployee().getName());
        model.put("employee", Iterables.getFirst(mail.getEmployeeList(), null));
        model.put("dateStr", formatDateString(mail.getDate()));

        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "timesheetdeleted.vm", model) +
                mail.getPreconstructedMessageBody();
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(TimeSheet params) {
        final Employee employee = params.getEmployee();
        logger.info("Performing mailing about deleted timesheet.");
        Mail mail = new TimeSheetMail();
        mail.setToEmails(getToEmails(params));
        mail.setCcEmails(Arrays.asList(getAssistantEmail(getManagersEmails(mail, employee))));
        mail.setEmployeeList(Arrays.asList(employee));
        String date = DateTimeUtil.formatDate(params.getCalDate().getCalDate());
        mail.setDate(date);
        mail.setSubject(getSubject(employee, date ));
        //APLANATS-574 дополняем бэкапом
        mail.setPreconstructedMessageBody(sendMailService.initMessageBodyForReport(params));
        return Arrays.asList(mail);
    }

    private String getSubject(Employee employee, String date) {
        return  propertyProvider.getTimesheetMailMarker() + // APLANATS-571
                String.format(" Удален отчет за %s", date);
    }

    private Collection<String> getToEmails(TimeSheet input) {
        Integer empId = input.getEmployee().getId();

        Set<String> result = new HashSet<String>();

        result.add(input.getEmployee().getEmail());
        result.add(TSPropertyProvider.getMailFromAddress());
        result.add(sendMailService.getEmployeesManagersEmails(empId));
        result.add(sendMailService.getProjectsManagersEmails(input));
        result.add(sendMailService.getProjectParticipantsEmails(input));

        return result;
    }
}
