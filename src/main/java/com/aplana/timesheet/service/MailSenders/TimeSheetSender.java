package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.CategoryOfActivity;
import com.aplana.timesheet.enums.TypeOfActivity;
import com.aplana.timesheet.enums.WorkPlace;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.ProjectService;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class TimeSheetSender extends MailSender<TimeSheetForm> {

    public TimeSheetSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected InternetAddress initFromAddresses(Mail mail) {
        try {
            String employeeEmail = mail.getFromEmail();
            logger.debug("From Address = {}", employeeEmail);
            return new InternetAddress(employeeEmail);
        } catch (AddressException e) {
            logger.error("Employee email address has wrong format.", e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected InternetAddress[] getToAddresses(Mail mail) throws AddressException {
        return InternetAddress.parse(Joiner.on(",").join(mail.getToEmails()));
    }

    @Override
    protected void initMessageSubject(Mail mail, MimeMessage message) {
        try {
            message.setSubject(mail.getSubject(), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = sendMailService.getPreFilledModel();

        model.put("paramsForGenerateBody", mail.getParamsForGenerateBody());

        logger.info("follows initialization output from velocity");
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "sendmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    public void sendTimeSheetMessage(TimeSheetForm form) {
        sendMessage(form, new MailFunction<TimeSheetForm>() {
            @Override
            public List<Mail> performMailing(@Nullable TimeSheetForm input) throws MessagingException {
                logger.info("Performing timesheet mailing.");
                Mail mail = new Mail();

                mail.setFromEmail(sendMailService.getEmployeeEmail(input.getEmployeeId()));
                mail.getToEmails().addAll(getToEmails(input));
                mail.setSubject(getSubject(input));
                mail.setParamsForGenerateBody(getBody(input));

                return Arrays.asList(mail);
            }
        });
    }

    private String getSubject(TimeSheetForm input) {
        return "Status report - " +
                (!input.isLongIllness() && !input.isLongVacation()
                    ? input.getCalDate() : input.getBeginLongDate());
    }

    private Collection<? extends String> getToEmails(TimeSheetForm input) {
        Set<String> toEmails = Sets.newHashSet(Iterables.transform(
                sendMailService.getRegionManagerList(input.getEmployeeId()),
                new Function<Employee, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Employee input) {
                        return input.getEmail();
                    }
                }));

        toEmails.add(sendMailService.getEmployeeEmail(input.getEmployeeId()));
        toEmails.add(sendMailService.getEmployeesManagersEmails(input.getEmployeeId()));
        toEmails.add(sendMailService.getProjectsManagersEmails(input));
        toEmails.add(sendMailService.getProjectParticipantsEmails(input));
        return toEmails;
    }

    private Table<Integer, String, String> getBody(TimeSheetForm tsForm) {
        Table<Integer, String, String> result = HashBasedTable.create();
        ProjectService projectService = null;
        int FIRST = 0;

        List<TimeSheetTableRowForm> tsRows = tsForm.getTimeSheetTablePart();

        if (tsRows != null) {
            for (int i = 0; i < tsRows.size(); i++) {
                TimeSheetTableRowForm tsRow = tsRows.get(i);

                WorkPlace workPlace = WorkPlace.getById(tsRow.getWorkplaceId());
                result.put(i, "workPlace", workPlace != null ? workPlace.getName() : "Неизвестно");

                Integer actTypeId = tsRow.getActivityTypeId();
                result.put(i, "actType", TypeOfActivity.getById(actTypeId).getName());

                String projectName = null;
                if (actTypeId <= 13) {
                    Integer projectId = tsRow.getProjectId();
                    if (projectId != null) {
                        result.put(i, "projectName", (projectName = sendMailService.getProjectName(projectId)));
                    }
                }
                Integer actCatId = tsRow.getActivityCategoryId();
                if (actCatId != null && actCatId > 0) {
                    result.put(i, "categoryOfActivity", CategoryOfActivity.getById(actCatId).getName());
                }
                putIfIsNotBlank(i, result, "cqId", tsRow.getCqId());
                putIfIsNotBlank(i, result, "duration", tsRow.getDuration());
                putIfIsNotBlank(i, result, "descriptionStrings", tsRow.getDescriptionEscaped());
                putIfIsNotBlank(i, result, "problemStrings", tsRow.getProblemEscaped());

            }
            putIfIsNotBlank(FIRST, result, "planStrings", tsForm.getPlanEscaped());
        } else if (tsForm.isLongIllness() || tsForm.isLongVacation()) {
            if (tsForm.isLongIllness()) {
                result.put(FIRST, "reason", "Болезнь с");
            } else if (tsForm.isLongVacation()) {
                result.put(FIRST, "reason", "Отпуск с");
            }
            result.put(FIRST, "beginLongDate", DateTimeUtil.formatDateString(tsForm.getBeginLongDate()));
            result.put(FIRST, "endLongDate", DateTimeUtil.formatDateString(tsForm.getEndLongDate()));
        }

        return result;
    }

    private void putIfIsNotBlank(int i, Table<Integer,String,String> result, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            result.put(i, key, value);
        }
    }
}
