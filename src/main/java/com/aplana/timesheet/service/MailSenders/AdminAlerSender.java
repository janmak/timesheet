package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.dao.entity.ldap.DivisionLdap;
import com.aplana.timesheet.service.SendMailService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 *
 * @author aimamutdinov
 */
public class AdminAlerSender extends MailSender {
	List<DivisionLdap> divisionList;

	public AdminAlerSender(SendMailService sendMailService) {
		super(sendMailService);
	}
	
	public void sendAlert(List<DivisionLdap> divisions) {
		 try {
            initSender();
			divisionList = divisions;
			 message = new MimeMessage(session);
                    initMessageHead();
                    initMessageBody();

                    sendMessage();
		} catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.",
                    sendMailService.mailConfig.getProperty("mail.transport.protocol"), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        } finally {
            deInitSender();
        }
	}
	
	@Override
	protected void initToAddresses() {
		String targetAddr = sendMailService.mailConfig.getProperty("mail.ProblemsAndProposals.toaddress");
		try {
			toAddr = InternetAddress.parse(targetAddr);
		} catch (AddressException ex) {
			logger.error("can not parse destination address", ex);
		}
	}
	
	@Override
    protected void initMessageSubject() {
		try {
			message.setSubject("Список не корректных подразделений", "UTF-8");
		} catch (MessagingException ex) {
			logger.error("can not set message subject");
		}
	}
	
	@Override
    protected void initMessageBody() {
		Map<String, List<DivisionLdap>> model = new HashMap<String, List<DivisionLdap>>();
		model.put("divisionList", divisionList);
		
		String messageBody = VelocityEngineUtils.mergeTemplateIntoString(
                sendMailService.velocityEngine, "alertmail.vm", model);
        logger.debug("Message Body: {}", messageBody);
        try {
            message.setText(messageBody, "UTF-8", "html");
        } catch (MessagingException e) {
            logger.error("Error while init message body.", e);
        }
	}
}
