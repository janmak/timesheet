package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationApprovalDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.util.DateTimeUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author iziyangirov
 */
@Service
public class VacationApprovalService {

    private static final Integer VACATION_CRAEATE_TRESHOLD_DEFAULT = 14;
    private static final Integer VACATION_PROJECT_MANAGER_OVERRIDE_TRESHOLD_DEFAULT = 7;
    private static final Integer VACATION_URGENT_PROJECT_MANAGER_OVERRIDE_TRESHOLD_DEFAULT = 3;
    private static final Integer VACATION_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT = 5;
    private static final Integer VACATION_URGENT_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT = 2;

    private List<Integer> approvedByProjectManager = Arrays.asList(VacationStatusEnum.APPROVED_BY_PM.getId(),
            VacationStatusEnum.APPROVEMENT_WITH_LM.getId(), VacationStatusEnum.APPROVED.getId(), VacationStatusEnum.REJECTED.getId());

    private List<Integer> approvedByLineManager = Arrays.asList(VacationStatusEnum.APPROVED.getId(), VacationStatusEnum.REJECTED.getId());

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalService.class);

    private static final String VACATION_APPROVE_MAILS_SEND_FAILED_EXCEPTION_MESSAGE = "Отпуск создан, но рассылка о согласовании не произведена!";

    @Autowired
    private VacationApprovalDAO vacationApprovalDAO;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private TSPropertyProvider propertyProvider;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private VacationService vacationService;
    @Autowired
    private SendMailService sendMailService;
    @Autowired
    private VacationApprovalResultService vacationApprovalResultService;
    @Autowired
    private ProjectParticipantService projectParticipantService;

    public VacationApproval getVacationApproval(String uid){
        return vacationApprovalDAO.findVacationApproval(uid);
    }

    public VacationApproval store(VacationApproval vacationApproval){
        return vacationApprovalDAO.store(vacationApproval);
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId){
        return vacationApprovalDAO.getVacationApprovalEmailList(vacationId);
    }

    /**
     * Рассылаем уведомления о согласовании отпуска задним числом
     * (такой отпуск создается уполномоченным сотрудником и не нуждается в согласовании)
     */
    public void sendBackDateVacationApproved (Vacation vacation) throws VacationApprovalServiceException {
        try {
            Map<String, Employee> managers = new HashMap<String, Employee>();
            List<Project> projects = projectService.getProjectsForVacation(vacation);
            List<ProjectParticipant> juniorManagerProjectParticipants =
                    Lists.newArrayList(projectParticipantService.getJuniorProjectManagerProjectParticipants(projects, vacation));
            for (ProjectParticipant projectParticipant : juniorManagerProjectParticipants) {
                managers.put(projectParticipant.getEmployee().getEmail(), projectParticipant.getEmployee());
            }
            for (Project project : projects) {
                managers.put(project.getManager().getEmail(), project.getManager());
            }

            List<VacationApproval> tempVacationApprovals = createTempVacationApprovals(managers, vacation);
        } catch (CalendarServiceException ex) {
            logger.error(ex.getMessage(), ex);
            throw new VacationApprovalServiceException(ex);
        }
    }

    /**
     * создаем временный список для рассылки писем о согласовании отпуска задним числом
     */
    private List<VacationApproval> createTempVacationApprovals(Map<String, Employee> managers, Vacation vacation) {
        List<VacationApproval> vacationApprovals = new ArrayList<VacationApproval>();
        Date requestDate = new Date();

        for (Employee manager : managers.values()) {
            VacationApproval vacationApproval = new VacationApproval();
            vacationApproval.setRequestDate(requestDate);
            vacationApproval.setManager(manager);
            vacationApproval.setVacation(vacation);

            vacationApprovals.add(vacationApproval);
        }

        return vacationApprovals;
    }

    /**
     * Рассылаем всем сотрудникам, которым высылались письма с просьбой согласовать отпуск, сообщения, что
     * отпуск согласован
     */
    public void sendVacationApprovedMessages (Vacation vacation) {
        List<VacationApproval> vacationApprovals = vacationApprovalDAO.getAllApprovalsForVacation(vacation);
        for (VacationApproval vacationApproval : vacationApprovals) {
            sendMailService.performVacationApprovedSender(vacationApproval);
        }
    }

    /**
     * запускаем проверку для всех несогласованных отпусков
     */
    public void checkAllVacations () throws VacationApprovalServiceException {
        List<Vacation> vacations = vacationService.getAllNotApprovedVacations();
        for (Vacation vacation : vacations) {
            checkVacationIsApproved(vacation);
        }
    }

    /**
     * Проверяем, согласован ли отпуск
     */
    public Boolean checkVacationIsApproved(Vacation vacation) throws VacationApprovalServiceException {
        if (vacationIsApprovedByProjectManagers(vacation)) {        //проверяем руководителей проектов
            Boolean lineManagersResult = vacationIsApprovedByLineManager(vacation);     //проверяем линейного
            return (lineManagersResult == null) ? false : lineManagersResult;
        }
        return false;
    }

    /**
     * проверяем, согласован ли отпуск с линейным руководителем
     */
    private Boolean vacationIsApprovedByLineManager(Vacation vacation) throws VacationApprovalServiceException {
        if (approvedByLineManager.contains(vacation.getStatus().getId())) {        //проверка уже утвержденного отпуска
            return true;
        }

        int lineManagerDaysToApprove = getControlTimeForLineManager(vacation);
        VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation, lineManagerDaysToApprove);

        if (lineManagerApproval.getResult() != null) {    //нашли результат у одного из линейных
            approveVacationByLineManager(vacation, lineManagerApproval.getResult());
            return lineManagerApproval.getResult();
        }

        if (lineManagerHasTimeToApproveVacation(lineManagerDaysToApprove, lineManagerApproval)) { //у линейного еще есть время подумать
            return lineManagerApproval.getResult();
        }

        VacationApproval approvalResult = prepareApproveLetterForLineManagerOfEmployee(vacation, lineManagerApproval.getManager());
        if (approvalResult != null) {
            sendMailService.performVacationApproveRequestSender(approvalResult); //посылаем письмо с уведомлением следующему в иерархии линейному руководителю
        } else {
            if (managerExists(vacation.getEmployee())){     //проверяем, что сотрудник - не сам себе руководитель. для таких отпуска автоматом не утверждаются
                vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()));
                vacationService.store(vacation);
                sendVacationApprovedMessages(vacation);  //делаем рассылку о том, что отпуск согласован
            }
        }

        return false;
    }

    /**
     * заносим в БД данные о согласовании отпуска линейным руководителем
     */
    private VacationApproval prepareApproveLetterForLineManagerOfEmployee(Vacation vacation, Employee employee) {
        Employee managerWithoutApproval = getFirstManagerWithoutApproval(vacation, employee);

        if (managerWithoutApproval == null) { //выше уже нет линейных
            return null;
        }

        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setManager(managerWithoutApproval);
        vacationApproval.setVacation(vacation);
        vacationApproval.setUid(UUID.randomUUID().toString());
        vacationApproval.setRequestDate(new Date());

        store(vacationApproval);

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
     * поднимаемся по руководителям (employee.manager.manager...) пока не найдем последнего,
     * кому отправлялся запрос согласования. (начинаем подниматься...)
     */
    private VacationApproval getTopLineManagerApproval(Vacation vacation, int lineManagerDaysToApprove) throws VacationApprovalServiceException {
        Employee manager = vacation.getEmployee().getManager();
        VacationApproval lineManagerOfEmployeeApproval = tryGetManagerApproval(vacation, manager);
        if (lineManagerOfEmployeeApproval == null) {
            return null;
        }

        return getTopLineManagerApproval(lineManagerOfEmployeeApproval, lineManagerDaysToApprove);
    }

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        return vacationApprovalDAO.tryGetManagerApproval(vacation, manager);
    }
    
    /**
     * рекурсивно поднимаемся по руководителям (employee.manager.manager...) пока не найдем последнего,
     * кому отправлялся запрос согласования. (продолаем рекурсивно подниматься)
     */
    private VacationApproval getTopLineManagerApproval(VacationApproval vacationApproval, int lineManagerDaysToApprove) throws VacationApprovalServiceException {

        if (vacationApproval.getResult() != null) {        //линейный вынес решение об отпуске
            return vacationApproval;
        }

        if (lineManagerHasTimeToApproveVacation(lineManagerDaysToApprove, vacationApproval)) { //у линейного еще есть время подумать
            return vacationApproval;
        }

        Employee manager = vacationApproval.getManager();

        if (! managerExists(manager)) {  //у линейного нет руководителя или он сам себе руководитель
            return vacationApproval;
        }

        Vacation vacation = vacationApproval.getVacation();
        VacationApproval managerOfManagerApproval = tryGetManagerApproval(vacation, manager.getManager());
        if (managerOfManagerApproval == null) {  //письмо линейному руководителю этого линейного еще не посылалось
            return vacationApproval;
        }

        return getTopLineManagerApproval(managerOfManagerApproval, lineManagerDaysToApprove);       //проверяем следующего по иерархии линейного руководителя
    }

    /**
     * проверяем, есть ли у сотрудника линейный или он последний в иерархии
     */
    private boolean managerExists(Employee employee) {
        Employee manager = employee.getManager();

        return ! ( manager == null || manager.getId().equals(employee.getId()) );
    }

    /**
     * Проверяем, успевает ли линейный руководитель вынести решение по заявлению на отпуск
     */
    private boolean lineManagerHasTimeToApproveVacation(int lineManagerDaysToApprove, VacationApproval lineManagerApproval) {
        Date lastLineManagerApproveDate = lineManagerApproval.getRequestDate();
        return DateTimeUtil.getAllDaysCount(lastLineManagerApproveDate, new Date()) >= lineManagerDaysToApprove;
    }

    /**
     * устанавливаем у отпуска статус
     */
    private void approveVacationByLineManager(Vacation vacation, Boolean result) {
        if (result) {
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED.getId()));
        } else {
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.REJECTED.getId()));
        }
        vacationService.store(vacation);
    }


    /**
     * проверяет, согласован ли отпуск с руководителями проектов, которые получали уведомления
     * о согласовании переданного отпуска
     */
    private boolean vacationIsApprovedByProjectManagers(Vacation vacation) throws VacationApprovalServiceException {

        if (approvedByProjectManager.contains(vacation.getStatus().getId())){   //проверяется уже утвержденный отпуск
            return (! vacation.getStatus().getId().equals(VacationStatusEnum.REJECTED.getId()));
        }

        List<Project> projectsForVacation = projectService.getProjectsAssignedToVacation(vacation);
        boolean timeIsOver = checkTimeIsOverForProjectManagers(vacation);
        Map<Project, Boolean> managerApproveResult = checkManagerApproveResultForVacationByProjects(vacation, projectsForVacation, timeIsOver);
        if (! managerApproveResult.values().contains(false)) {       //если отпуск утвержден по всем проектам (не имеет отказов или ожиданий согласования)
            vacation.setStatus(dictionaryItemService.find(VacationStatusEnum.APPROVED_BY_PM.getId()));              //в БД отмечаем, что отпуск согласован руководителями проектов
            vacationService.store(vacation);

            int lineManagerDaysToApprove = getControlTimeForLineManager(vacation);
            VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation, lineManagerDaysToApprove);   //проверяем, посылалось ли письмо линейным руководителям
            if (lineManagerApproval == null) {      //если не посылалось, посылаем
                lineManagerApproval = prepareApproveLetterForLineManagerOfEmployee(vacation, vacation.getEmployee());
                sendMailService.performVacationApproveRequestSender(lineManagerApproval);
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * проверяет, закончилось ли у руководителей проекта время для согласования отпуска
     */
    private boolean checkTimeIsOverForProjectManagers(Vacation vacation) throws VacationApprovalServiceException {
        Integer controlTimeForProjectManager = getControlTimeForProjectManager(vacation);
        Date date = new Date();
        return (date.before(DateUtils.addDays(vacation.getCreationDate(), controlTimeForProjectManager)));
    }

    /**
     * проверяет, закончилось ли у линейного руководителя время для согласования отпуска
     */
    private boolean checkTimeIsOverForLineManagers(Vacation vacation) throws VacationApprovalServiceException {
        Integer controlTimeForProjectManager = getControlTimeForProjectManager(vacation);
        Date date = new Date();
        return (date.before(DateUtils.addDays(vacation.getCreationDate(), controlTimeForProjectManager)));
    }

    /**
     * проверяем, одобрено ли заявление на отпуск руководителями переданных проектов
     */
    private Map<Project, Boolean> checkManagerApproveResultForVacationByProjects(Vacation vacation, List<Project> projectsForVacation, boolean timeIsOver) throws VacationApprovalServiceException {
        Map<Project, Boolean> managerApproveResults = new HashMap<Project, Boolean>();
        for (Project project : projectsForVacation) {
            managerApproveResults.put(project, getManagersApproveResultForVacationByProject(project, vacation, timeIsOver));
        }

        return managerApproveResults;
    }

    /**
     * проверяем, одобрено ли заявление на отпуск руководителями передаваемого проекта
     */
    private Boolean getManagersApproveResultForVacationByProject(Project project, Vacation vacation, boolean timeIsOver) throws VacationApprovalServiceException {
        List<VacationApproval> projectManagerApprovals = vacationApprovalDAO.getProjectManagerApprovalsForVacationByProject(vacation, project);
        if ( ! timeIsOver) {
            return checkAllManagerApprovedVacation(projectManagerApprovals);
        } else {
            return checkOneManagerApprovedVacation(projectManagerApprovals, project);
        }
    }

    /**
     * проверяем, что все менеджеры дали решение по поводу отпуска (если сроки согласования отпуска еще не прошли)
     */
    private Boolean checkAllManagerApprovedVacation(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals) {
            if (approval.getResult() == null || approval.getResult() == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * если сроки согласования для отпуска прошли, то для согласования заявления на отпуск достаточно того, что хотя бы один
     * из руководителей согласовал заявление. сперва смотрим решение РП, если он не согласовал - смотрим решения нижестоящих руководителей
     */
    private Boolean checkOneManagerApprovedVacation(List<VacationApproval> projectManagerApprovals, Project project) throws VacationApprovalServiceException {
        Boolean projectManagerApproveResult = getProjectManagerApproveResult(projectManagerApprovals, project);
        if (projectManagerApproveResult != null) {
            return projectManagerApproveResult;
        } else {
            return getJuniorProjectManagersApproveResults(projectManagerApprovals);
        }
    }

    /**
     * Проверяем, что хотя бы один из руководителей согласовал заявление на отпуск.
     * //todo: метод расчитан на то, что на проекте один тимлид или один старший аналитик. конфликт решений двух тимлидов не обрабатывается: вернется первый попавшийся результат
     */
    private Boolean getJuniorProjectManagersApproveResults(List<VacationApproval> projectManagerApprovals) {
        for (VacationApproval approval : projectManagerApprovals) {
            if (approval.getResult() != null) {
                return approval.getResult();
            }
        }

        return false;
    }

    /**
     * проверяем, согласовал ли РП заявление на отпуск. Возвращаем его решение. если письмо руководителю проекта не посылалось, то
     * выбрасывается VacationApprovalServiceException
     */
    private Boolean getProjectManagerApproveResult(List<VacationApproval> projectManagerApprovals, Project project) throws VacationApprovalServiceException {
        Integer projectManagerId = project.getManager().getId();
        for (VacationApproval approval : projectManagerApprovals) {
            Integer managerId = approval.getManager().getId();
            if (managerId.equals(projectManagerId)){
                return approval.getResult();
            }
        }

        Integer vacationId = projectManagerApprovals.get(0).getVacation().getId();
        throw new VacationApprovalServiceException(String.format("В БД не найдены данные о письме руководителю проекта %s для подтверждения отпуска №%s!",
                project.getName(), vacationId));
    }

    /**
     * получаем мексимальное количество дней, за которое руководители проекта должны согласовать заявление на отпуск
     */
    private Integer getControlTimeForProjectManager(Vacation vacation) throws VacationApprovalServiceException {
        Long daysForApprove = DateTimeUtil.getAllDaysCount(vacation.getCreationDate(), vacation.getBeginDate());
        Integer vacationTreshold = getVacationTreshold();
        if (daysForApprove >= vacationTreshold) {
            return getVacationProjectManagerOverrideThreshold();
        } else {
            return getVacationUrgentProjectManagerOverrideThreshold();
        }
    }

    /**
     * получаем мексимальное количество дней, за которое линейный руководитель должен утвердить заявление на отпуск
     */
    private Integer getControlTimeForLineManager(Vacation vacation) throws VacationApprovalServiceException {
        Long daysForApprove = DateTimeUtil.getAllDaysCount(vacation.getCreationDate(), vacation.getBeginDate());
        Integer vacationTreshold = getVacationTreshold();
        if (daysForApprove >= vacationTreshold) {
            return getVacationLineManagerOverrideThreshold();
        } else {
            return getVacationUrgentLineManagerOverrideThreshold();
        }
    }

    /**
     * получаем количество дней, за которые линейный руководитель должен согласовать заявление на отпуск
     * в ускоренном режиме
     */
    private Integer getVacationUrgentLineManagerOverrideThreshold() {
        try {
            return propertyProvider.getVacationUrgentLineManagerOverrideThreshold();
        } catch (NullPointerException ex) {
            return VACATION_URGENT_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        } catch (NumberFormatException ex) {
            logger.error("В файле настроек указано неверное число в vacationUrgentLineManagerOverrideThreshold!", ex);
            return VACATION_URGENT_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        }
    }

    /**
     * получаем количество дней, за которые линейный руководитель должен согласовать заявление на отпуск
     */
    private Integer getVacationLineManagerOverrideThreshold() {
        try {
            return propertyProvider.getVacationLineManagerOverrideThreshold();
        } catch (NullPointerException ex) {
            return VACATION_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        } catch (NumberFormatException ex) {
            logger.error("В файле настроек указано неверное число в vacationLineManagerOverrideThreshold!", ex);
            return VACATION_LINE_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        }
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

    /***
     * получаем максимальное количество дней для согласования отпуска руководителем проекта в обычном режиме
     */
    public Integer getVacationProjectManagerOverrideThreshold() throws VacationApprovalServiceException {
        try {
            return propertyProvider.getVacationProjectManagerOverrideThreshold();
        } catch (NullPointerException ex) {
            return VACATION_PROJECT_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        } catch (NumberFormatException ex) {
            logger.error("В файле настроек указано неверное число в vacationProjectManagerOverrideThreshold !", ex);
            return VACATION_PROJECT_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        }
    }

    /**
     * получаем максимальное количество дней для согласования отпуска руководителем проекта в ускоренном режиме
     */
    public Integer getVacationUrgentProjectManagerOverrideThreshold() throws VacationApprovalServiceException {
        try {
            return propertyProvider.getVacationUrgentLineManagerOverrideThreshold();
        } catch (NullPointerException ex) {
            return VACATION_URGENT_PROJECT_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        } catch (NumberFormatException ex) {
            logger.error("В файле настроек указано неверное число в vacationUrgentProjectManagerOverrideThreshold!", ex);
            return VACATION_URGENT_PROJECT_MANAGER_OVERRIDE_TRESHOLD_DEFAULT;
        }
    }

    /**
     * подготавливаем данные для рассылки писем об утверждении отпуска
     */
    public void sendVacationApproveRequestMessages(Vacation vacation) throws VacationApprovalServiceException {
        try {
            List<Project> projects = projectService.getProjectsForVacation(vacation);
            Map<String, VacationApproval> juniorProjectManagersVacationApprovals = prepareJuniorProjectManagersVacationApprovals(projects, vacation);
            Map<String, VacationApproval> allManagerVacationApprovals = prepareProjectManagersVacationApprovals(juniorProjectManagersVacationApprovals, projects, vacation);
            for (VacationApproval vacationApproval : allManagerVacationApprovals.values()) {
                sendMailService.performVacationApproveRequestSender(vacationApproval);
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
        vacationApproval = store(vacationApproval);
        approvals.put(employee.getEmail(), vacationApproval);

        return vacationApproval;
    }

    /**
     * создаем новую заготовку
     */
    private VacationApproval createNewVacationApproval(Vacation vacation, Date requestDate, Employee employee) {
        VacationApproval vacationApproval = new VacationApproval();
        vacationApproval.setVacation(vacation);
        vacationApproval.setManager(employee);
        vacationApproval.setRequestDate(requestDate);
        vacationApproval.setUid(UUID.randomUUID().toString());

        return vacationApproval;
    }

}
