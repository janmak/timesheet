package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.CategoryOfActivity;
import com.aplana.timesheet.enums.TypeOfActivity;
import com.aplana.timesheet.enums.WorkPlace;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class TimeSheetSender extends MailSender<TimeSheetForm> {

    public static final String WORK_PLACE = "workPlace";
    public static final String ACT_TYPE = "actType";
    public static final String PROJECT_NAME = "projectName";
    public static final String CATEGORY_OF_ACTIVITY = "categoryOfActivity";
    public static final String CQ_ID = "cqId";
    public static final String DURATION = "duration";
    public static final String DESCRIPTION_STRINGS = "descriptionStrings";
    public static final String PROBLEM_STRINGS = "problemStrings";
    public static final String PLAN_STRINGS = "planStrings";
    public static final String REASON = "reason";
    public static final String BEGIN_LONG_DATE = "beginLongDate";
    public static final String END_LONG_DATE = "endLongDate";
    public static final String SENDER_NAME = "senderName";
    public static final String OVERTIME_CAUSE = "overtimeCause";

    public TimeSheetSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initMessageBody(Mail mail, MimeMessage message) {
        Map model = new HashMap();

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

    @Override
    protected List<Mail> getMailList(TimeSheetForm params) {
        logger.info("Performing timesheet mailing.");
        Mail mail = new Mail();

        mail.setFromEmail(sendMailService.getEmployeeEmail(params.getEmployeeId()));
        mail.setToEmails(getToEmails(params));
        mail.setSubject(getSubject(params));
        mail.setParamsForGenerateBody(getBody(params));

        return Arrays.asList(mail);
    }

    private String getSubject(TimeSheetForm params) {
        return "Status report - " +
                (!params.isLongIllness() && !params.isLongVacation()
                    ? params.getCalDate() : params.getBeginLongDate());
    }

    private Collection<String> getToEmails(TimeSheetForm params) {
        Set<String> toEmails = Sets.newHashSet(Iterables.transform(
                sendMailService.getRegionManagerList(params.getEmployeeId()),
                new Function<Employee, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Employee params) {
                        return params.getEmail();
                    }
                }));

        toEmails.add(sendMailService.getEmployeeEmail(params.getEmployeeId()));
        toEmails.add(sendMailService.getEmployeesManagersEmails(params.getEmployeeId()));
        toEmails.add(sendMailService.getProjectsManagersEmails(params));
        toEmails.add(sendMailService.getProjectParticipantsEmails(params));
        return toEmails;
    }

    private Table<Integer, String, String> getBody(TimeSheetForm tsForm) {
        Table<Integer, String, String> result = HashBasedTable.create();
        int FIRST = 0;

        List<TimeSheetTableRowForm> tsRows = tsForm.getTimeSheetTablePart();

        result.put(FIRST, SENDER_NAME, sendMailService.getSecurityPrincipal().getEmployee().getName());

        if (tsRows != null) {
            for (int i = 0; i < tsRows.size(); i++) {
                TimeSheetTableRowForm tsRow = tsRows.get(i);

                WorkPlace workPlace = WorkPlace.getById(tsRow.getWorkplaceId());
                result.put(i, WORK_PLACE, workPlace != null ? workPlace.getName() : "Неизвестно");

                Integer actTypeId = tsRow.getActivityTypeId();
                result.put(i, ACT_TYPE, TypeOfActivity.getById(actTypeId).getName());

                String projectName = null;
                if (actTypeId <= 13) {
                    Integer projectId = tsRow.getProjectId();
                    if (projectId != null) {
                        result.put(i, PROJECT_NAME, (projectName = sendMailService.getProjectName(projectId)));
                    }
                }
                Integer actCatId = tsRow.getActivityCategoryId();
                if (actCatId != null && actCatId > 0) {
                    result.put(i, CATEGORY_OF_ACTIVITY, CategoryOfActivity.getById(actCatId).getName());
                }
                putIfIsNotBlank(i, result, CQ_ID, tsRow.getCqId());
                putIfIsNotBlank(i, result, DURATION, tsRow.getDuration());
                putIfIsNotBlank(i, result, DESCRIPTION_STRINGS, tsRow.getDescriptionEscaped());
                putIfIsNotBlank(i, result, PROBLEM_STRINGS, tsRow.getProblemEscaped());

            }
            putIfIsNotBlank(FIRST, result, PLAN_STRINGS, tsForm.getPlanEscaped());
            putIfIsNotBlank(FIRST, result, OVERTIME_CAUSE,sendMailService.getOvertimeCause(tsForm) );
        } else if (tsForm.isLongIllness() || tsForm.isLongVacation()) {
            if (tsForm.isLongIllness()) {
                result.put(FIRST, REASON, "Болезнь с");
            } else if (tsForm.isLongVacation()) {
                result.put(FIRST, REASON, "Отпуск с");
            }
            result.put(FIRST, BEGIN_LONG_DATE, DateTimeUtil.formatDateString(tsForm.getBeginLongDate()));
            result.put(FIRST, END_LONG_DATE, DateTimeUtil.formatDateString(tsForm.getEndLongDate()));
        }

        return result;
    }

    private void putIfIsNotBlank(int i, Table<Integer,String,String> result, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            result.put(i, key, value);
        }
    }
}
