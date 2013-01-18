package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.SendMailService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class ManagerAlertSender extends MailSender {
    private List<ReportCheck> currentReportCheckList;
    
    private Employee currentManager;

    private final HashMap<Employee, List> managerMap = new HashMap<Employee, List>();

    public ManagerAlertSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        super(sendMailService, propertyProvider);
    }

    @Override
    protected void initToAddresses() {
        String toAddresses = currentManager.getEmail();
        try {
            toAddr = InternetAddress.parse(toAddresses); 
            logger.debug("CC Addresses: {}", toAddresses);
        } catch (AddressException e) {
            logger.error("Email address has wrong format.", e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody() {
        Map model = new HashMap();
        model.put("division", currentReportCheckList.get(0).getDivision().getName());
        model.put("reportCheckList", currentReportCheckList);
        //если это центр заказной разработки
        if(currentReportCheckList.get(0).getDivision().getId()==1)
            model.put("region","show");
        else
            model.put("region","");
        String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "alertmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
    }

    @Override
    protected void initMessageSubject() {
        StringBuilder messageSubject = new StringBuilder();
        messageSubject.append("Отчет по списанию занятости за ");

        List<String> monthList = new ArrayList<String>();

        List<String> passedDays;

        String monthName;

        String text = "";

        for ( ReportCheck report : currentReportCheckList ) {
            passedDays = report.getPassedDays();

            for ( String next : passedDays ) {
                monthName = DateTimeUtil.getMonthTxt( next );
                if ( ! monthList.contains( monthName ) ) {
                    monthList.add( monthName );
                }
            }
        }

        for (Iterator<String> iterator = monthList.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();

            text += next;
            if (iterator.hasNext())
                text += ", ";
        }

        messageSubject.append(text);

        logger.debug("Message subject: {}", messageSubject.toString());
        try {
            message.setSubject(messageSubject.toString(), "UTF-8");
        } catch (MessagingException e) {
            logger.error("Error while init message subject.", e);
        }
    }

    /**
     * Процедура рекурсивно проходит всех руководителей изначально переданного сотрудника
     * и добавляет для этих руководителей в спец. списки ссылки на reportCheck сотрудника о несписанной занятости
     *
     * @param employee
     * @param reportCheck
     */
    private void addReportToManagerLists(Employee employee, ReportCheck reportCheck) {

        Employee manager = employee.getManager();

        if (manager != null) {
            if (!managerMap.containsKey(manager)) {
                managerMap.put(manager, new ArrayList<ReportCheck>());
            }

            managerMap.get(manager).add(reportCheck);

            addReportToManagerLists(manager, reportCheck);
        }
    }


    public void buildManagerCheckLists(List<ReportCheck> reportCheckList) {

        for ( ReportCheck reportCheck : reportCheckList ) {
            addReportToManagerLists( reportCheck.getEmployee(), reportCheck );
        }
    }

    public void sendAlert(List<ReportCheck> rCheckList) {

        try {
            initSender();

            logger.info("Performing manager mailing.");

            // Формируем для каждого руководителя отдельный список отчетов его подчиненных
            buildManagerCheckLists(rCheckList);

            // Для каждого руководителя формируем отдельное письмо со списком отчетов по его подчиненным
            for (Map.Entry<Employee, List> entry : managerMap.entrySet()) {
                currentManager = entry.getKey();
                //если руководитель помечен как archived
                //не было реал. в storeReportCheck тк список руководителей формируется здесь(выше)
                if(currentManager.isDisabled(null))
                    logger.info("Manager {} is disabled",currentManager.getName());
                else
                {
                    currentReportCheckList = entry.getValue();

                    message = new MimeMessage(session);
                    initMessageHead();
                    initMessageBody();

                    sendMessage();
                }
            }

        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.",
                    propertyProvider.getMailTransportProtocol(), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        } finally {
            deInitSender();
        }
    }
}
