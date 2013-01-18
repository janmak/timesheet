package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DictionaryItemDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.AdminMessageForm;
import com.aplana.timesheet.form.FeedbackForm;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.MailSenders.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.*;

@Service
public class SendMailService{
    private static final Logger logger = LoggerFactory.getLogger(SendMailService.class);

    @Autowired
    public VelocityEngine velocityEngine;

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private TSPropertyProvider propertyProvider;


    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * Возвращает строку с адресами линейных руководителей сотрудника
     * (непосредственного и всех вышестоящих) разделёнными запятой.
     * @param empId
     */
    public String getEmployeesManagersEmails(Integer empId) {
        StringBuilder chiefEmails = new StringBuilder();
        Employee employee = employeeService.find(empId);
        String email = employee.getEmail();
        chiefEmails.append(email);
        chiefEmails.append(",");
        if (employee.getManager() != null) {
            chiefEmails.append(getEmployeesManagersEmails(employee.getManager().getId()));
        }
        return chiefEmails.toString();
    }

    /**
     * Возвращает строку с адресами линейных руководителей сотрудника
     * (непосредственного и всех вышестоящих) разделёнными запятой.
     * Без адреса самого сотрудника.
     *
     */
    public String getEmployeesManagersEmailsWithoutEmployeeEmail(Integer empId) {
        StringBuilder chiefEmails = new StringBuilder();
        Employee employee = employeeService.find(empId);

        if (employee.getManager() != null) {
            chiefEmails.append(getEmployeesManagersEmails(employee.getManager().getId()));
        }
        return chiefEmails.toString();
    }

    /**
     * Возвращает email сотрудника
     *
     * @param Id сотрудника
     * @return email сотрудника
     */
    public String getEmployeeEmail(Integer empId) {
        return employeeService.find(empId).getEmail();
    }
    /**
     * Возвращает строку с адресами менеджеров проектов/пресейлов
     * @param tsForm
     */
    public String getProjectsManagersEmails(TimeSheetForm tsForm) {
        StringBuilder managersEmails = new StringBuilder();
        List<TimeSheetTableRowForm> tsRows = tsForm.getTimeSheetTablePart();
        if (tsRows == null) {
            return "";
        } //Нет проектов\пресейлов, нет и менеджеров.
        for (TimeSheetTableRowForm tsRow : tsRows) {
            Integer actTypeId = tsRow.getActivityTypeId();
            if ( actTypeId.equals( DictionaryItemDAO.PROJECTS_ID ) || actTypeId.equals( DictionaryItemDAO.PRESALES_ID ) ) {
                Integer projectId = tsRow.getProjectId();
                Integer projectManagerId = projectService.find(projectId).getManager().getId();
                String projEmail = employeeService.find(projectManagerId).getEmail();
                managersEmails.append(projEmail);
                managersEmails.append(",");
            }
        }
        return managersEmails.toString();
    }
    
    /**
     * Получает email адреса всех менеджеров проектов указаных в TS
     * я не виноват так было принято до меня с этими менеджерами
     * @param timeSheet
     * @return 
     */
    public String getProjectsManagersEmails(TimeSheet timeSheet) {
        StringBuilder managersEmails = new StringBuilder();
        Set<TimeSheetDetail> details = timeSheet.getTimeSheetDetails();
        for (TimeSheetDetail detail : details) {
            Integer actTypeId = detail.getActType().getId();
            if ( actTypeId.equals( DictionaryItemDAO.PROJECTS_ID ) || actTypeId.equals( DictionaryItemDAO.PRESALES_ID ) ) {
                Integer projectId = detail.getProject().getId();
                Integer projectManagerId = projectService.find(projectId).getManager().getId();
                String projEmail = employeeService.find(projectManagerId).getEmail();
                managersEmails.append(projEmail);
                managersEmails.append(",");
            }
        }
        return managersEmails.toString();
    }

    /**
     * Возвращает строку с email адресами в соответствии с логикой
     * РП - все ролиу
     * Руководителю группы разработки - конструктор, разработчик, системный инженер, тестировщик.
     * Ведущему аналитику - аналитик и технический писатель.
     *
     *
     * @param empId - идентификатор сотрудника, пославшего отчет
     * @param tsForm
     * @return emails - строка с emailАМИ
     */
    public String getProjectParticipantsEmails(Integer empId, TimeSheetForm tsForm) {
        StringBuilder emails = new StringBuilder();
        List<TimeSheetTableRowForm> tsRows = tsForm.getTimeSheetTablePart();
        if (tsRows == null) {
            return "";
        }
        for (TimeSheetTableRowForm tsRow : tsRows) {
            Integer actTypeId = tsRow.getActivityTypeId();
            if ( actTypeId.equals( DictionaryItemDAO.PROJECTS_ID ) || actTypeId.equals( DictionaryItemDAO.PRESALES_ID ) ) {
                Integer projectId = tsRow.getProjectId();
                logger.debug("project id: {} ", projectId);
                logger.debug("projectRole id: {} ", tsRow.getProjectRoleId());
                List<ProjectParticipant> participants = projectService
                        .getParticipants(projectService.find(projectId));
                logger.debug("Project {} has {} participants.", projectId, participants.size());
                for (ProjectParticipant participant : participants) {
                    Integer participantRole = participant.getProjectRole().getId();
                    Integer participantId = participant.getEmployee().getId();
                    logger.debug("Project participant {} has role: {} ", participantId, participantRole);
                    if ( participantRole.equals( ProjectRoleService.PROJECT_MANAGER ) ) {
                        emails.append(participant.getEmployee().getEmail()).append(",");
                    } else if ( participantRole.equals( ProjectRoleService.PROJECT_LEADER ) ) {
                        if (Arrays.asList(
                                ProjectRoleService.PROJECT_DESIGNER
                                , ProjectRoleService.PROJECT_DEVELOPER
                                , ProjectRoleService.PROJECT_SYSENGINEER
                                , ProjectRoleService.PROJECT_TESTER)
                                .contains(tsRow.getProjectRoleId())) {
                            emails.append(participant.getEmployee().getEmail()).append(",");
                        }
                    } else if ( participantRole.equals( ProjectRoleService.PROJECT_ANALYST ) ) {
                        if (Arrays.asList(
                                ProjectRoleService.PROJECT_ANALYST
                                , ProjectRoleService.PROJECT_TECH_WRITER)
                                .contains(tsRow.getProjectRoleId())) {
                            emails.append(participant.getEmployee().getEmail()).append(",");
                        }
                    }
                    logger.debug("project id = {} prohodka.", projectId);
                }
            }
        }
        logger.debug("Participants emails: {} ", emails.toString());
        return emails.toString();
    }
    
    /**
     * Получает email адреса из всех проектов
     * @param ts
     * @return string строка содержащая email's которым относится данный timesheet
     */
    public String getProjectParticipantsEmails(TimeSheet ts) {
        StringBuilder emails = new StringBuilder(",");
        Set<TimeSheetDetail> details = ts.getTimeSheetDetails();
        Integer actTypeId, projectId;
        for (TimeSheetDetail detail : details) {
            actTypeId = detail.getActType().getId();
            if ( actTypeId.equals( DictionaryItemDAO.PROJECTS_ID ) || actTypeId.equals( DictionaryItemDAO.PRESALES_ID ) ) {
                projectId = detail.getProject().getId();
                List<ProjectParticipant> participants = projectService.getParticipants(projectService.find(projectId));
                for (ProjectParticipant participant : participants) {
                    Integer participantRole = participant.getProjectRole().getId();
                    Integer participantId = participant.getEmployee().getId();
                    if ( participantRole.equals( ProjectRoleService.PROJECT_MANAGER ) ) {
                        emails.append(participant.getEmployee().getEmail()).append(",");
                    } else if ( participantRole.equals( ProjectRoleService.PROJECT_LEADER ) ) {
                        if (Arrays.asList(
                                ProjectRoleService.PROJECT_DESIGNER
                                , ProjectRoleService.PROJECT_DEVELOPER
                                , ProjectRoleService.PROJECT_SYSENGINEER
                                , ProjectRoleService.PROJECT_TESTER)
                                .contains(detail.getProjectRole().getId())) {
                            emails.append(participant.getEmployee().getEmail()).append(",");
                        }
                    } else if ( participantRole.equals( ProjectRoleService.PROJECT_ANALYST ) ) {
                        if (Arrays.asList(
                                ProjectRoleService.PROJECT_ANALYST
                                , ProjectRoleService.PROJECT_TECH_WRITER)
                                .contains(detail.getProjectRole().getId())) {
                            emails.append(participant.getEmployee().getEmail()).append(",");
                        }
                    }
                }
            }
        }
        return emails.toString();
    }
    
    public List<Employee> getEmployeesList(Division division){
        return employeeService.getEmployees(division);
    }

    public void performMailing(TimeSheetForm form) {

        new TimeSheetSender(this, propertyProvider).sendTimeSheetMessage(form);
    }

    public void performFeedbackMailing(FeedbackForm form) {

        new FeedbackSender(this, propertyProvider).sendFeedbackMessage(form);
    }

    public void performLoginProblemMailing(AdminMessageForm form) {

        new LoginProblemSender(this, propertyProvider).SendLoginProblem(form);
    }

    public void performPersonalAlertMailing(List<ReportCheck> rCheckList) {

        new PersonalAlertSender(this, propertyProvider).sendAlert(rCheckList);
    }

    public void performManagerMailing(List<ReportCheck> rCheckList) {

        new ManagerAlertSender(this, propertyProvider).sendAlert(rCheckList);
    }

    public void performEndMonthMailing(List<ReportCheck> rCheckList) {

        new EndMonthAlertSender(this, propertyProvider).sendAlert(rCheckList);
    }

    public void performTimeSheetDeletedMailing(TimeSheet timeSheet) {

        new TimeSheetDeletedSender(this, propertyProvider).sendMessage(timeSheet);
    }


    public String initMessageBodyForReport(TimeSheet timeSheet) {
        Map<String, Object> model = getPreFilledModel(timeSheet);

        model.put("timeSheet", timeSheet);
        logger.info("follows initialization output from velocity");
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "report.vm", model);
    }

    public Map getPreFilledModel() {
        return getPreFilledModel(null);
    }

    public Map<String, Object> getPreFilledModel(TimeSheet timeSheet) {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("dictionaryItemService", dictionaryItemService);
        model.put("projectService", projectService);
        model.put("DateTimeUtil", DateTimeUtil.class);
        model.put("senderName",
                timeSheet == null
                        ? securityService.getSecurityPrincipal().getEmployee().getName()
                        : timeSheet.getEmployee().getName());
        return model;
    }

    public List<Employee> getRegionManagerList(Integer id) {
        return employeeService.getRegionManager(id);
    }
}