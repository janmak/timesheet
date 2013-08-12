package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.AbstractTest;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Emakedonskaya
 * Date: 08.08.13
 */
public class VacationApprovalErrorThresholdSenderTest extends AbstractTest {
    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalErrorThresholdSenderTest.class);

    @Autowired
    public SendMailService sendMailService;
    @Autowired
    public TSPropertyProvider propertyProvider;

    public List<String> emailList;
    public VacationApprovalErrorThresholdSender vacationApprovalErrorThresholdSender;
    public Iterable<String> iterable;
    public String str;

    public static <String> List<String> toList(Iterable<String> iterable) {
        if(iterable instanceof List) {
            return (List<String>) iterable;
        }
        ArrayList<String> list = new ArrayList<String>();
        if(iterable != null) {
            for(String e: iterable) {
                list.add(e);
            }
        }
        return list;
    }

    public List<Mail> getMailList(String str) {
        return vacationApprovalErrorThresholdSender.getMailList(str);
    }

        @Before
    public void gettingData () {

        String email = propertyProvider.getMailProblemsAndProposalsCoaddress(0);

        emailList = new ArrayList<String>();
        emailList.add(email);
        str = "Подбор guid для сервиса согласования отпусков.";

        vacationApprovalErrorThresholdSender = new VacationApprovalErrorThresholdSender(sendMailService, propertyProvider);
    }

    @Test
    public void testVacationApprovalErrorThresholdSenderTest () {
        List<Mail> mails = getMailList(str);
        Mail mail = mails.get(0);
        iterable = mail.getToEmails();

        List<String> actual = toList(iterable);
        java.util.Collections.sort(emailList);
        java.util.Collections.sort(actual);

        assertEquals(emailList, actual);
    }
}
