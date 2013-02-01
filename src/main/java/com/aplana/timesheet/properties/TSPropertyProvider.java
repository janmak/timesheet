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

    private static boolean needUpdate = true;
    private static Properties properties;

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

    public Integer getQuickreportMoskowBeginDay() {
        return Integer.parseInt(getProperties().getProperty("quickreport.moskow.beginday"));
    }

    public Integer getQuickreportMoskowBeginMonth() {
        return Integer.parseInt(getProperties().getProperty("quickreport.moskow.beginmonth"));
    }

    public Integer getQuickreportRegionsBeginDay() {
        return Integer.parseInt(getProperties().getProperty("quickreport.regions.beginday"));
    }

    public Integer getQuickreportRegionsBeginMonth() {
        return Integer.parseInt(getProperties().getProperty("quickreport.regions.beginmonth"));
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

    public Double getOvertimeThreshold() {
        return new Double(getProperties().getProperty("overtime.threshold"));
    }


    /**
     * Единый метод для загрузки почтовых настроек
     *
     * @param mailConfig
     */
    public static Properties getProperties() {
        if (needUpdate || properties == null) {
            try {
                properties = new Properties();
                properties.load(new FileInputStream( System.getProperty("pathToTsProperties") ));

                needUpdate = false;

                return properties;
            } catch (FileNotFoundException e1) {
                logger.error("File timesheet.properties not found.");
            } catch (InvalidPropertiesFormatException e) {
                logger.error("Invalid timesheet.properties file format.");
            } catch (IOException e) {
                logger.error("Input-output error.");
            }
            throw new IllegalStateException("File with system properties not founded!");
        } else {
            return properties;
        }
    }

    public static void updateProperties() {
        needUpdate = true;
    }
}
