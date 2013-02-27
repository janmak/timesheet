package com.aplana.timesheet.properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
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

    public String getPentahoUrl() {
        return getProperties().getProperty("pentaho.url");
    }

    public String getMailTransportProtocol() {
        return getProperties().getProperty("mail.transport.protocol");
    }

    public String getMailProblemsAndProposalsCoaddress() {
        return getProperties().getProperty("mail.ProblemsAndProposals.toaddress");
    }

    private static final String DEFAULT_TIME_SHEET_MAIL_ADRESS = "timesheet@aplana.com";
    public static String getMailFromAddress() {
        return getProperties().getProperty("mail.fromaddress", DEFAULT_TIME_SHEET_MAIL_ADRESS);
    }

    public String getMailSendEnable() {
        return getProperties().getProperty("mail.send.enable");
    }

    public String getMailDebugAddress() {
        return  getProperties().getProperty("mail.debug.address");
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

    public String getVacationMailMarker() {
        return getProperties().getProperty("vacation.mail.marker");
    }

    final String DEFAULT_TIMESHEET_MAIL_MARKER = "[TIMESHEET]";
    public String getTimesheetMailMarker() {
        String result = getProperties().getProperty("ts.mail.marker=");
        return (result == null || result == "") ? DEFAULT_TIMESHEET_MAIL_MARKER : result;
    }

    final Integer DEFAULT_VACATION_APPROVAL_ERROR_THRESHOLD = 100;
    public Integer getVacationApprovalErrorThreshold(){
        String result = getProperties().getProperty("vacation.approval.error.threshold");
        return (result == null || result == "") ? DEFAULT_VACATION_APPROVAL_ERROR_THRESHOLD : new Integer(result);
    }

    public Integer getBeforeVacationDays() {
        return Integer.parseInt(getProperties().getProperty("vacations.before.vacation.days"));
    }
    public Integer getVacationCreateThreshold() {
        return Integer.parseInt(getProperties().getProperty("vacations.vacation.create.threshold"));
    }

    public Integer getVacationProjectManagerOverrideThreshold() {
        return Integer.parseInt(getProperties().getProperty("vacations.vacation.project.manager.override.threshold"));
    }

    public Integer getVacationUrgentProjectManagerOverrideThreshold() {
        return Integer.parseInt(getProperties().getProperty("vacations.vacation.urgent.project.manager.override.threshold"));
    }

    public Integer getVacationLineManagerOverrideThreshold() {
        return Integer.parseInt(getProperties().getProperty("vacations.vacation.line.manager.override.threshold"));
    }

    public Integer getVacationUrgentLineManagerOverrideThreshold() {
        return Integer.parseInt(getProperties().getProperty("vacations.vacation.urgent.manager.override.threshold"));
    }

    public String getTimeSheetURL() {
        return getProperties().getProperty("timesheet.url");
    }

    public String getFeedbackMarker() {
        return getProperties().getProperty("feedback.mail.marker", "[TS FEEDBACK] Сообщение от пользователя системы списания занятости");
    }

    public List<String> getExceptionsIgnoreClassNames() {
        final String ignoreClassNames = getProperties().getProperty("exceptions.ignoreClassNames", StringUtils.EMPTY);

        return Arrays.asList(ignoreClassNames.split("\\s*,\\s*"));
    }

    public static String getFooterText() {
        return getProperties().getProperty("footer.text");
    }

    public static String getTimesheetHelpUrl() {
        return getProperties().getProperty("timesheet.help.url");
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
        getProperties();
    }

    public static String getProperiesFilePath() {
        return System.getProperty("pathToTsProperties");
    }
}
