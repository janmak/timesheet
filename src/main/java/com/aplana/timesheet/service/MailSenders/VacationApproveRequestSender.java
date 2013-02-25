package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * User: vsergeev
 * Date: 04.02.13
 */
public class VacationApproveRequestSender extends AbstractVacationSender<VacationApproval> {

    protected static final Logger logger = LoggerFactory.getLogger(VacationApproveRequestSender.class);
    private static final String DEFAULT_TIMESHEET_URL = "http://timesheet.aplana.com";

    public VacationApproveRequestSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected List<Mail> getMailList(VacationApproval vacationApproval) {
        final Mail mail = new TimeSheetMail();
        Vacation vacation = vacationApproval.getVacation();

        mail.setToEmails(Arrays.asList(vacationApproval.getManager().getEmail()));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacationApproval));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(VacationApproval vacationApproval) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(vacationApproval));

        return table;
    }

    private String getBody(VacationApproval vacationApproval) {
        Vacation vacation = vacationApproval.getVacation();

        String vacationTypeStr = vacation.getType().getValue();
        String employeeNameStr = vacation.getEmployee().getName();
        String regionNameStr = vacation.getEmployee().getRegion().getName();
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);
        String commentStr = StringUtils.EMPTY;
        String approveURL = String.format("%s/vacation_approval?uid=%s", getTimeSheetURL(), vacationApproval.getUid());
        if (StringUtils.isNotBlank(vacation.getComment())) {
            commentStr = String.format("Комментарий: %s. ", vacation.getComment());
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Просьба принять решение по \"%s\" ", vacationTypeStr));
        stringBuilder.append(String.format("сотрудника %s ", employeeNameStr));
        stringBuilder.append(String.format("из г. %s ", regionNameStr));
        stringBuilder.append(String.format("на период с %s - %s. ", beginDateStr, endDateStr));
        stringBuilder.append(String.format("%s", commentStr));
        stringBuilder.append(String.format("Для регистрации Вашего решения нажмите на ссылку: %s.", approveURL));

        return stringBuilder.toString();
    }

    private String getTimeSheetURL() {
        try {
            String url = propertyProvider.getTimeSheetURL();
            return (StringUtils.isBlank(url)) ? DEFAULT_TIMESHEET_URL : url;
        } catch (NullPointerException ex) {
            return DEFAULT_TIMESHEET_URL;
        }
    }

    private String getSubject(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        return  String.format("Запрос согласования отпуска %s %s - %s", vacation.getEmployee().getName(),
                        beginDateStr, endDateStr);
    }

}
