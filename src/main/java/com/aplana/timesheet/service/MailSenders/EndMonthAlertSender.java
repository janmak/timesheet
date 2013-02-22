package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;

import static com.aplana.timesheet.util.DateTimeUtil.currentDay;
import static com.aplana.timesheet.util.DateTimeUtil.getMonthTxt;

public class EndMonthAlertSender extends MailSender<List<ReportCheck>> {

    public EndMonthAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "alertendmonthmail.vm", new HashMap());
        logger.debug("Message Body: {}", messageBody);

        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected List<Mail> getMailList(List<ReportCheck> params) {
        Mail mail = new TimeSheetMail();
        
        mail.setEmployeeList(getEmployees(params));
        mail.setSubject(String.format("Не забудьте списать занятость за %s", getMonthTxt(currentDay())));
        mail.setDivision(Iterables.getFirst(params, null).getDivision());
        mail.setToEmails(getToEmails(mail.getEmployeeList(), mail.getDivision()));
        
        return Lists.newArrayList(mail);
    }

    private Iterable<Employee> getEmployees(List<ReportCheck> params) {
        return Iterables.transform(params, new Function<ReportCheck, Employee>() {
            @Nullable @Override
            public Employee apply(ReportCheck params) {
                return params.getEmployee();
            }
        });
    }

    private Iterable<String> getToEmails(Iterable<Employee> employeeList, Division division) {
        return Iterables.transform(
                Sets.difference(
                        // Формируем список сотрудников, у которых нет долгов по отчетности
                        Sets.newHashSet(sendMailService.getEmployeesList(division)),
                        Sets.newHashSet(employeeList)
                ),
                new Function<Employee, String>() {
                    @Nullable @Override
                    public String apply(Employee params) {
                        return params.getEmail();
                    }
                });
    }
}
