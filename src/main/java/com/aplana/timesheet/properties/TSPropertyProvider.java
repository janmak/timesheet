package com.aplana.timesheet.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class TSPropertyProvider {

    private static final Logger logger = LoggerFactory.getLogger(TSPropertyProvider.class);

    public String getJiraIssueCreateUrl() {
        return getProperties().getProperty("jira.issue.create.url");
    }

    public String getOqUrl() {
        return getProperties().getProperty("OQ.url");
    }

    public String getMailDivisions() {
        return getProperties().getProperty("mail.divisions");
    }

    public String getPentahoUrl() {
        return getProperties().getProperty("pentaho.url");
    }

    public String getMailTransportProtocol() {
        return getProperties().getProperty("mail.transport.protocol");
    }

    public String getMailProblemsAndProposalsCoaddress() {
        return getProperties().getProperty("mail.ProblemsAndProposals.toaddress");
    }

    public String getMailFromAddress() {
        return getProperties().getProperty("mail.fromaddress");
    }

    public String getMailSendEnable() {
        return getProperties().getProperty("mail.send.enable");
    }

    public String getMailSmtpPort() {
        return getProperties().getProperty("mail.smtp.port");
    }

    public String getMailSmtpAuth() {
        return getProperties().getProperty("mail.smtp.auth");
    }

    public String getMailUsername() {
        return getProperties().getProperty("mail.username");
    }

    public String getMailPassword() {
        return getProperties().getProperty("mail.password");
    }

    public Integer getQuickreportMoskowBegindate() {
        return Integer.parseInt(getProperties().getProperty("quickreport.moskow.begindate"));
    }

    public Integer getQuickreportMoskowBeginmounth() {
        return Integer.parseInt(getProperties().getProperty("quickreport.moskow.beginmounth"));
    }

    public Integer getQuickreportRegionsBegindate() {
        return Integer.parseInt(getProperties().getProperty("quickreport.regions.begindate"));
    }

    public Integer getQuickreportRegionsBeginmounth() {
        return Integer.parseInt(getProperties().getProperty("quickreport.regions.beginmounth"));
    }

    /**
     * Единый метод для загрузки почтовых настроек
     *
     * @param mailConfig
     */
    public static Properties getProperties() {
        /**
         * путь к property файлу
         */
        String PROPERTY_PATH = "./webapps/timesheet.properties";

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream( PROPERTY_PATH ));
            return properties;
        } catch (FileNotFoundException e1) {
            logger.error("File timesheet.properties not found.");
        } catch (InvalidPropertiesFormatException e) {
            logger.error("Invalid timesheet.properties file format.");
        } catch (IOException e) {
            logger.error("Input-output error.");
        }
        throw new IllegalStateException("File with system properties not founded!");
    }

    public String getProjectRoleDeveloper() {
        return getProperties().getProperty("project.role.developer");
    }

    public String getProjectRoleRp() {
        return getProperties().getProperty("project.role.rp");
    }

    public String getProjectRoleTest() {
        return getProperties().getProperty("project.role.test");
    }

    public String getProjectRoleAnalyst() {
        return getProperties().getProperty("project.role.analyst");
    }

    public String getProjectRoleSystem() {
        return getProperties().getProperty("project.role.system");
    }
}
