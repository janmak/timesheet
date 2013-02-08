package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author iziyangirov
 */

public class VacationApprovalAcceptanceSender extends MailSender<VacationApproval> {

    final String DATE_FORMAT = "dd.MM.yyyy";
    final String MAIL_ACCEPT_SUBJECT = "Согласование \"%s\" сотрудника %s на период с %s - %s";
    final String MAIL_ACCEPT_BODY = "%s согласовал \"%s\" сотрудника %s из г. %s на период с %s - %s.";
    final String MAIL_REFUSE_SUBJECT = "Согласование \"%s\" сотрудника %s на период с %s - %s";
    final String MAIL_REFUSE_BODY = "%s не согласовал \"%s\" сотрудника %s из г. %s на период с %s - %s.";

    public VacationApprovalAcceptanceSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected List<Mail> getMailList(VacationApproval params) {
        Mail mail = new Mail();

        Integer vacationId = params.getVacation().getId();
        String matchingFIO = params.getManager().getName();
        String vacationType = params.getVacation().getType().getValue();
        String employeeFIO = params.getVacation().getEmployee().getName();
        String region = params.getVacation().getEmployee().getRegion().getName();
        String dateBegin = DateFormatUtils.format(params.getVacation().getBeginDate(), DATE_FORMAT);
        String dateEnd = DateFormatUtils.format(params.getVacation().getEndDate(), DATE_FORMAT);
        Boolean result = params.getResult();

        String subject = result ? String.format(MAIL_ACCEPT_SUBJECT, vacationType, employeeFIO, dateBegin, dateEnd) :
                String.format(MAIL_REFUSE_SUBJECT, vacationType, employeeFIO, dateBegin, dateEnd);
        String text = result ? String.format(MAIL_ACCEPT_BODY, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd) :
                String.format(MAIL_REFUSE_BODY, matchingFIO, vacationType, employeeFIO, region, dateBegin, dateEnd);

        mail.setFromEmail(propertyProvider.getMailFromAddress());
        mail.setSubject(propertyProvider.getVacationMailMarker() + " " + subject); // APLANATS-573
        mail.setPreconstructedMessageBody(text);
        mail.setToEmails( Arrays.asList(params.getVacation().getEmployee().getEmail()) );
        mail.setCcEmails(sendMailService.getVacationApprovalEmailList(vacationId));

        return Arrays.asList(mail);
    }

}
