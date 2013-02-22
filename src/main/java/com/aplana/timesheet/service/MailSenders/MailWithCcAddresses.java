package com.aplana.timesheet.service.MailSenders;

import java.util.List;

/**
 * User: vsergeev
 * Date: 22.02.13
 */
public interface MailWithCcAddresses<T> {

    List<Mail> getMainMailList(T params);

    String getCcEmail(T params);
}
