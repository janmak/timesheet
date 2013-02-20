package com.aplana.timesheet.service.vacationapproveprocess;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.exception.service.CalendarServiceException;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * User: vsergeev
 * Date: 19.02.13
 */
@Service
public class VacationApprovalProcessService extends AbstractVacationApprovalProcessService {

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalProcessService.class);

    /**
     * рекурсивно поднимаемся по руководителям (employee.manager.manager...) пока не найдем последнего,
     * кому отправлялся запрос согласования. (продолаем рекурсивно подниматься)
     */
    protected VacationApproval getTopLineManagerApprovalRecursive(VacationApproval vacationApproval) throws VacationApprovalServiceException {

        if (vacationApproval.getResult() != null) {        //линейный вынес решение об отпуске
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

        return getTopLineManagerApprovalRecursive(managerOfManagerApproval);       //проверяем следующего по иерархии линейного руководителя
    }

    /**
     * проверяем, согласован ли отпуск с линейным руководителем
     */
    protected Boolean vacationIsApprovedByLineManager(Vacation vacation) throws VacationApprovalServiceException {
        if (vacation.getStatus() != null && approvedByLineManager.contains(vacation.getStatus().getId())) {        //проверка уже утвержденного отпуска
            return vacationIsNotRejected(vacation);
        }

        VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation);

        if (lineManagerApproval.getResult() != null) {    //нашли результат у одного из линейных
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, lineManagerApproval.getResult());
            return lineManagerApproval.getResult();
        }

        return null;
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

            for (VacationApproval vacationApproval : tempVacationApprovals) {
                sendMailService.performVacationApprovedSender(vacationApproval);
            }

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
     * проверяем, одобрено ли заявление на отпуск руководителями передаваемого проекта
     */
    protected Boolean getManagersApproveResultForVacationByProject(Project project, Vacation vacation) {
        List<VacationApproval> projectManagerApprovals = vacationApprovalService.getProjectManagerApprovalsForVacationByProject(vacation, project);
        return checkAllManagerApprovedVacation(projectManagerApprovals);
    }

}
