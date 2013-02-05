package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class TimeSheetDeletedSender extends MailSender<TimeSheet> {

    public TimeSheetDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = new HashMap();


        model.put("managerName", sendMailService.getSecurityPrincipal().getEmployee().getName());
        model.put("employee", Iterables.getFirst(mail.getEmployeeList(), null));
        model.put("dateStr", mail.getDate());

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
    protected List<Mail> getMailList(TimeSheet params) {
        logger.info("Performing mailing about deleted timesheet.");
        Mail mail = new Mail();
        mail.setFromEmail(propertyProvider.getMailFromAddress());
        mail.setToEmails(getToEmails(params));
        mail.setEmployeeList(Arrays.asList(params.getEmployee()));
        String date = DateTimeUtil.formatDate(params.getCalDate().getCalDate());
        mail.setDate(date);
        mail.setSubject(getSubject(params.getEmployee(), date ));
        return Arrays.asList(mail);
    }

    private String getSubject(Employee employee, String date) {
        return String.format("Удален отчет сотрудника %s за %s", employee.getName(), date);
    }

    private Collection<String> getToEmails(TimeSheet input) {
        Integer empId = input.getEmployee().getId();

        Set<String> result = Sets.newHashSet(Iterables.transform(
                sendMailService.getRegionManagerList(input.getEmployee().getId()),
                new Function<Employee, String>() {
                    @Nullable @Override
                    public String apply(@Nullable Employee manager) {
                        return manager.getEmail();
                    }
                }));

        result.add(input.getEmployee().getEmail());
        result.add(propertyProvider.getMailFromAddress());
        result.add(sendMailService.getEmployeesManagersEmails(empId));
        result.add(sendMailService.getProjectParticipantsEmails(input));

        return result;
    }
}
