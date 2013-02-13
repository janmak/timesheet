package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.List;

/**
 * User: vsergeev
 * Date: 13.02.13
 */
public class VacationApprovedSender extends MailSender<VacationApproval> {

    protected static final Logger logger = LoggerFactory.getLogger(VacationApprovedSender.class);

    public VacationApprovedSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected List<Mail> getMailList(VacationApproval vacationApproval) {
        final Mail mail = new Mail();
        Vacation vacation = vacationApproval.getVacation();

        mail.setFromEmail(sendMailService.getEmployeeEmail(vacation.getEmployee().getId()));
        mail.setToEmails(Arrays.asList(vacationApproval.getManager().getEmail()));
        mail.setCcEmails(getAdditionalEmailsForRegion(vacation.getEmployee().getRegion()));
        mail.setSubject(propertyProvider.getVacationMailMarker() + " " + getSubject(vacation));
        mail.setParamsForGenerateBody(getParamsForGenerateBody(vacationApproval));

        return Arrays.asList(mail);
    }

    private Iterable<String> getAdditionalEmailsForRegion(Region region) {
        return Arrays.asList(region.getAdditionalEmails().split("\\s*,\\s*"));
    }

    private Table<Integer, String, String> getParamsForGenerateBody(VacationApproval vacationApproval) {
        final Table<Integer, String, String> table = HashBasedTable.create();

        table.put(FIRST, MAIL_BODY, getBody(vacationApproval));

        return table;
    }

    private String getBody(VacationApproval vacationApproval) {
        Vacation vacation = vacationApproval.getVacation();
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        return String.format("Успешно согласован %s сотрудника %s из г. %s на период с %s - %s",
                vacation.getType(), vacation.getEmployee().getName(), vacation.getEmployee().getRegion().getName(), beginDateStr, endDateStr);
    }

    private String getSubject(Vacation vacation) {
        String beginDateStr = DateFormatUtils.format(vacation.getBeginDate(), DATE_FORMAT);
        String endDateStr = DateFormatUtils.format(vacation.getEndDate(), DATE_FORMAT);

        return  propertyProvider.getVacationMailMarker() +
                String.format("[VACATION REQUEST] Согласование %s сотрудника %s на период с %s - %s", vacation.getStatus().getValue(), vacation.getEmployee().getName(),
                        beginDateStr, endDateStr);
    }

    @Override
    protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        try {
            message.setText(mail.getParamsForGenerateBody().get(FIRST, MAIL_BODY), "UTF-8", "plain");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

}
