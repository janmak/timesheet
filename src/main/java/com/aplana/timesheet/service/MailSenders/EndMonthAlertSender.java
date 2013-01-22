package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;

public class EndMonthAlertSender extends MailSender<List<ReportCheck>> {

    public EndMonthAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected InternetAddress[] getToAddresses(Mail mail) throws AddressException {
        String uniqueSendingEmails = deleteEmailDublicates(
                Joiner.on(",").join(
                        Sets.difference(
                                // Формируем список сотрудников, у которых нет долгов по отчетности
                                Sets.newHashSet(sendMailService.getEmployeesList(mail.getDivision())),
                                Sets.newHashSet(mail.getEmployeeList())
                        )));
        logger.debug("EmployeesEmails: {}", uniqueSendingEmails);

        return InternetAddress.parse(uniqueSendingEmails);
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
    protected void initMessageSubject(Mail mail, MimeMessage message) {
        try {
            message.setSubject(
                String.format("Не забудьте списать занятость за %s",
                        DateTimeUtil.getMonthTxt(DateTimeUtil.currentDay())), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    public void sendAlert(List<ReportCheck> rCheckList) {
        sendMessage(rCheckList, new MailFunction<List<ReportCheck>>() {
            @Override
            public List<Mail> performMailing(@Nullable List<ReportCheck> input) throws MessagingException {
                Mail mail = new Mail();
                mail.setEmployeeList(Iterables.transform(input, new Function<ReportCheck, Employee>() {
                    @Nullable
                    @Override
                    public Employee apply(@Nullable ReportCheck input) {
                        return input.getEmployee();
                    }
                }));
                mail.setDivision(input.get(0).getDivision());
                return Lists.newArrayList(mail);
            }
        });
    }
}
