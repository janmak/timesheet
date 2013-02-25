package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public class VacationDeletedSender extends  AbstractVacationSenderWithCopyToAuthor {

    public VacationDeletedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    public List<Mail> getMainMailList(Vacation vacation) {
        final Mail mail = new TimeSheetMail();

        mail.setToEmails(getToEmails(vacation));
        mail.setSubject(getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacation));

        return Arrays.asList(mail);
    }

    private Table<Integer, String, String> getParamsForGenerateBody(Vacation params) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(params));

        return table;
    }

    private String getBody(Vacation params) {
        final StringBuilder stringBuilder = new StringBuilder(
                String.format("Сотрудник %s удалил ", sendMailService.getSecurityPrincipal().getEmployee().getName())
        );

        final Employee employee = params.getEmployee();

        if (params.getAuthor().equals(employee)) {
            stringBuilder.append("своё заявление");
        } else {
            stringBuilder.append(
                    String.format("заявление сотрудника \"%s\"", employee.getName())
            );
        }

        stringBuilder.append(
                String.format(
                        " на %s за период с %s по %s",
                        WordUtils.uncapitalize(params.getType().getValue()),
                        DateFormatUtils.format(params.getBeginDate(), DATE_FORMAT),
                        DateFormatUtils.format(params.getEndDate(), DATE_FORMAT)
                )
        );

        return stringBuilder.toString();
    }

    private String getSubject(Vacation params) {
        return  String.format(" Заявление на отпуск сотрудника \"%s\" удалено", params.getEmployee().getName());
    }

    private Iterable<String> getToEmails(Vacation params) {
        final List<String> vacationApprovalEmailList = sendMailService.getVacationApprovalEmailList(params.getId());

        vacationApprovalEmailList.add(params.getEmployee().getEmail());

        return vacationApprovalEmailList;
    }

}
