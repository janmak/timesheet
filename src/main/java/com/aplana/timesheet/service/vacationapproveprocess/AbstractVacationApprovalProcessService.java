package com.aplana.timesheet.service.vacationapproveprocess;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.google.common.collect.Lists;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * User: vsergeev
 * Date: 19.02.13
 */
public abstract class AbstractVacationApprovalProcessService {

    private static final Integer VACATION_CRAEATE_TRESHOLD_DEFAULT = 14;

    private List<Integer> approvedByProjectManager = Arrays.asList(VacationStatusEnum.APPROVED_BY_PM.getId(),
            VacationStatusEnum.APPROVEMENT_WITH_LM.getId(), VacationStatusEnum.APPROVED.getId(), VacationStatusEnum.REJECTED.getId());

    protected List<Integer> approvedByLineManager = Arrays.asList(VacationStatusEnum.APPROVED.getId(), VacationStatusEnum.REJECTED.getId());

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalService.class);

    private static final String VACATION_APPROVE_MAILS_SEND_FAILED_EXCEPTION_MESSAGE = "Отпуск создан, но рассылка о согласовании не произведена!";

    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected TSPropertyProvider propertyProvider;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    protected VacationService vacationService;
    @Autowired
    protected SendMailService sendMailService;
    @Autowired
    private VacationApprovalResultService vacationApprovalResultService;
    @Autowired
    protected ProjectParticipantService projectParticipantService;
    @Autowired
    protected VacationApprovalService vacationApprovalService;
    @Autowired
    protected EmployeeService employeeService;

    /**
     * Рассылаем всем сотрудникам, которым высылались письма с просьбой согласовать отпуск, сообщения, что
     * отпуск согласован
     */
    public void sendVacationApprovedMessages (Vacation vacation) {
        List<VacationApproval> vacationApprovals = vacationApprovalService.getAllApprovalsForVacation(vacation);
        vacationApprovals.add(createNewVacationApproval(vacation, new Date(), vacation.getEmployee()));
        for (VacationApproval vacationApproval : vacationApprovals) {
            sendMailService.performVacationApprovedSender(vacationApproval);
        }
    }

    /**
     * Проверяем, согласован ли отпуск
     */
    public Boolean checkVacationIsApproved(Vacation vacation) throws VacationApprovalServiceException {
        if (BooleanUtils.isTrue(vacationIsApprovedByProjectManagers(vacation))) {        //проверяем руководителей проектов
            Boolean lineManagersResult = vacationIsApprovedByLineManager(vacation);     //проверяем линейного
            return BooleanUtils.isTrue(lineManagersResult);
        }
        return false;
    }

    protected abstract Boolean vacationIsApprovedByLineManager(Vacation vacation) throws VacationApprovalServiceException;

    /**
     * поднимаемся по руководителям (employee.manager.manager...) пока не найдем последнего,
     * кому отправлялся запрос согласования. (начинаем подниматься...)
     */
    protected VacationApproval getTopLineManagerApproval(Vacation vacation) throws VacationApprovalServiceException {
        Employee manager = vacation.getEmployee().getManager();
        VacationApproval lineManagerOfEmployeeApproval = tryGetManagerApproval(vacation, manager);
        if (lineManagerOfEmployeeApproval == null) {
            return null;
        }

        return getTopLineManagerApprovalRecursive(lineManagerOfEmployeeApproval);
    }

    protected abstract VacationApproval getTopLineManagerApprovalRecursive(VacationApproval lineManagerOfEmployeeApproval) throws VacationApprovalServiceException;

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        return vacationApprovalService.tryGetManagerApproval(vacation, manager);
    }

    /**
     * проверяем, есть ли у сотрудника линейный или он последний в иерархии
     */
    protected boolean managerExists(Employee employee) {
        Employee manager = employee.getManager();

        return ! ( manager == null || manager.getId().equals(employee.getId()) );
    }

    /**
     * устанавливаем у отпуска статус "согласован" и рассылаем об этом уведомления
     */
    protected Boolean setFinalStatusForVacationAndSendVacationApprovedMessages(Vacation vacation, Boolean status) {
        if (status) {
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()));
        } else {
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.REJECTED.getId()));
        }
        vacationService.store(vacation);

        sendVacationApprovedMessages(vacation);  //делаем рассылку о том, что отпуск согласован
        
        return status;
    }

    /**
     * проверяет, согласован ли отпуск с руководителями проектов, которые получали уведомления
     * о согласовании переданного отпуска
     */
    private Boolean vacationIsApprovedByProjectManagers(Vacation vacation) throws VacationApprovalServiceException {

        if (approvedByProjectManager.contains(vacation.getStatus().getId())){   //проверяется уже утвержденный руководителями проектов отпуск
            return vacationIsNotRejected(vacation);
        }

        List<Project> projectsForVacation = projectService.getProjectsAssignedToVacation(vacation);
        Map<Project, Boolean> managerApproveResult = checkManagerApproveResultForVacationByProjects(vacation, projectsForVacation);

        if (managerApproveResult.values().contains(false)) {        //один из менеджеров отказал в отпуке! :(
            return setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, false);
        }
        
        if (managerApproveResult.values().contains(null)) {       //если отпуск по каким-то проектам еще ожидает решения
            return null;
        }

        setApprovementWithLineManagerStatusAndSendMessages(vacation);

        return true;
    }

    /**
     * Ставим отпуску статус "на согласовании с линейным руководителем" и делаем рассылку по линейным с просьбой согласовать отпуск
     */
    private void setApprovementWithLineManagerStatusAndSendMessages(Vacation vacation) throws VacationApprovalServiceException {

        if (! managerExists(vacation.getEmployee())) {      //если линейных нет или сам себе линейный - утверждаем без проверок
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, true);
            return;
        }

        vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVEMENT_WITH_LM.getId()));     //в БД отмечаем, что отпуск теперь на согласовании линейного руководителя
        vacationService.store(vacation);

        VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation);   //проверяем, посылалось ли письмо линейным руководителям
        if (lineManagerApproval == null) {      //если не посылалось, посылаем
            lineManagerApproval = prepareApproveLetterForLineManagerOfEmployee(vacation, vacation.getEmployee());
            sendMailService.performVacationApproveRequestSender(lineManagerApproval);
        }
    }

    protected boolean vacationIsNotRejected(Vacation vacation) {
        return ! vacation.getStatus().getId().equals(VacationStatusEnum.REJECTED.getId());
    }

    /**
     * заносим в БД данные о согласовании отпуска линейным руководителем
     */
    protected VacationApproval prepareApproveLetterForLineManagerOfEmployee(Vacation vacation, Employee employee) {
        Employee managerWithoutApproval = getFirstManagerWithoutApproval(vacation, employee);

        if (managerWithoutApproval == null) { //выше уже нет линейных
            return null;
        }

        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setManager(managerWithoutApproval);
        vacationApproval.setVacation(vacation);
        vacationApproval.setUid(UUID.randomUUID().toString());
        vacationApproval.setRequestDate(new Date());

        vacationApprovalService.store(vacationApproval);

        return vacationApproval;
    }

    /**
     * Получаем следующего в иерархии линейного руководителя, которому еще не рассылалось письмо с просьбой подтвердить отпуск
     */
    private Employee getFirstManagerWithoutApproval(Vacation vacation, Employee employee) {
        VacationApproval vacationApproval;
        do {
            if (! managerExists(employee)) {
                return null;
            }
            employee = employee.getManager();
            vacationApproval = tryGetManagerApproval(vacation, employee.getManager());
        } while (vacationApproval != null);

        return employee;
    }
    
    /**
     * проверяем, одобрено ли заявление на отпуск руководителями переданных проектов
     */
    private Map<Project, Boolean> checkManagerApproveResultForVacationByProjects(Vacation vacation, List<Project> projectsForVacation) throws VacationApprovalServiceException {
        Map<Project, Boolean> managerApproveResults = new HashMap<Project, Boolean>();
        for (Project project : projectsForVacation) {
            managerApproveResults.put(project, getManagersApproveResultForVacationByProject(project, vacation));
        }

        return managerApproveResults;
    }

    protected abstract Boolean getManagersApproveResultForVacationByProject(Project project, Vacation vacation) throws VacationApprovalServiceException;


    /**
     * проверяем, что все менеджеры дали решение по поводу отпуска
     */
    protected Boolean checkAllManagerApprovedVacation(List<VacationApproval> projectManagerApprovals) {
        if (rejectionsExists(projectManagerApprovals)) {
            return false;
        }
        if (delaysExists(projectManagerApprovals)) {
            return null;
        }

        return true;
    }

    /**
     * проверяем, есть ли еще письма, ожидающие решения руководителей проекта
     */
    private boolean delaysExists(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals) {
            if (approval.getResult() == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяем, есть ли отказы у руководителей проектов. если есть хотя бы один отказ - отпуск сразу отклоняется
     */
    protected boolean rejectionsExists(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals) {
            if (BooleanUtils.isFalse(approval.getResult())) {
                return true;
            }
        }

        return false;
    }

    /**
     * получаем количество дней, по которому в дальнейшем будет определяться,
     * согласовывать отпуск в обычном режиме или в ускоренном
     */
    public Integer getVacationTreshold() throws VacationApprovalServiceException {
        try {
            return propertyProvider.getVacationCreateThreshold();
        } catch (NullPointerException ex) {
            return VACATION_CRAEATE_TRESHOLD_DEFAULT;
        } catch (NumberFormatException ex) {
            logger.error("В файле настроек указано неверное число в vacationCreateThreshold!", ex);
            return VACATION_CRAEATE_TRESHOLD_DEFAULT;
        }
    }    

    /**
     * подготавливаем данные для рассылки писем об утверждении отпуска
     */
    public void sendVacationApproveRequestMessages(Vacation vacation) throws VacationApprovalServiceException {
        try {
            List<Project> projects = projectService.getProjectsForVacation(vacation);
            if (projects.isEmpty()) {       //если нет проектов ни в планах, ни в списаниях
                setApprovementWithLineManagerStatusAndSendMessages(vacation);       //сразу утверждаем у линейного
            } else {
                Map<String, VacationApproval> juniorProjectManagersVacationApprovals = prepareJuniorProjectManagersVacationApprovals(projects, vacation);
                Map<String, VacationApproval> allManagerVacationApprovals = prepareProjectManagersVacationApprovals(juniorProjectManagersVacationApprovals, projects, vacation);
                for (VacationApproval vacationApproval : allManagerVacationApprovals.values()) {
                    sendMailService.performVacationApproveRequestSender(vacationApproval);
                }
            }
        } catch (CalendarServiceException ex) {
            throw new VacationApprovalServiceException (VACATION_APPROVE_MAILS_SEND_FAILED_EXCEPTION_MESSAGE, ex);
        }

    }

    /**
     * создаем заготовки для писем о согласовании отпуска руководителям проектов
     */
    private Map<String, VacationApproval> prepareProjectManagersVacationApprovals(Map<String, VacationApproval> approvals, List<Project> projects, Vacation vacation) {
        Date requestDate = new Date();

        for (Project project : projects) {
            Employee manager = project.getManager();
            tryAddNewEmployeeToApprovals(vacation, requestDate, approvals, null, manager);
        }

        return approvals;
    }

    /**
     * создаем заготовки для писем о согласовании отпуска для младших менеджеров на проекте (тимлид, старший аналитик, заместители...)
     */
    private Map<String, VacationApproval> prepareJuniorProjectManagersVacationApprovals(List<Project> projects, Vacation vacation) throws CalendarServiceException {
        List<ProjectParticipant> juniorManagerProjectParticipants =
                Lists.newArrayList(projectParticipantService.getJuniorProjectManagerProjectParticipants(projects, vacation));

        return createJuniorManagersVacationApprovals(juniorManagerProjectParticipants, vacation);
    }

    /**
     * Создаем записи для утверждения отпусков младшими менеджерами в таблицах.
     */
    private Map<String, VacationApproval> createJuniorManagersVacationApprovals(List<ProjectParticipant> employeeProjectManagersProjectParticipants, Vacation vacation) {
        Date requestDate = new Date();
        Map<String, VacationApproval> approvals = new HashMap<String, VacationApproval>();

        for (ProjectParticipant projectParticipant : employeeProjectManagersProjectParticipants) {
            Employee manager = projectParticipant.getEmployee();
            tryAddNewEmployeeToApprovals(vacation, requestDate, approvals, projectParticipant, manager);
        }

        return approvals;
    }

    /**
     * пытаемся добавить сотрудника к рассылке. Если сотрудник уже есть в рассылке - не добавляем его
     * для тимлидов и старших аналитиков обязательно указать связь с таблицей project_participant
     * для руководителей проектов эта связь всегда будет null, т.к. их в таблице project_participant нет
     */
    private void tryAddNewEmployeeToApprovals(Vacation vacation, Date requestDate, Map<String, VacationApproval> approvals,
                                              ProjectParticipant projectParticipant, Employee manager) {
        VacationApproval vacationApproval = (approvals.get(manager.getEmail()) != null) ?
                approvals.get(manager.getEmail()) : addNewVacationApproval(approvals, vacation, requestDate, manager);

        VacationApprovalResult vacationApprovalResult = new VacationApprovalResult();
        vacationApprovalResult.setProjectParticipant(projectParticipant);
        vacationApprovalResult.setVacationApproval(vacationApproval);

        vacationApprovalResultService.store(vacationApprovalResult);
    }

    /**
     * добавляем в мапу с заготовками новую заготовку
     */
    private VacationApproval addNewVacationApproval(Map<String, VacationApproval> approvals, Vacation vacation, Date requestDate, Employee employee) {
        VacationApproval vacationApproval = createNewVacationApproval(vacation, requestDate, employee);
        vacationApproval = vacationApprovalService.store(vacationApproval);
        approvals.put(employee.getEmail(), vacationApproval);

        return vacationApproval;
    }

    /**
     * создаем новую заготовку
     */
    private VacationApproval createNewVacationApproval(Vacation vacation, Date requestDate, Employee manager) {
        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setVacation(vacation);
        vacationApproval.setManager(manager);
        vacationApproval.setRequestDate(requestDate);
        vacationApproval.setUid(UUID.randomUUID().toString());

        return vacationApproval;
    }

    /**
     * Проверяем, есть ли среди писем с просьбой утвердить отпуск положительные ответы
     */
    protected boolean approvesExists(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals){
            if (BooleanUtils.isTrue(approval.getResult())) {
                return true;
            }
        }

        return false;
    }
}
