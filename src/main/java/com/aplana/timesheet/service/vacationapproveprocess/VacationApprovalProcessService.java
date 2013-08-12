package com.aplana.timesheet.service.vacationapproveprocess;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.exception.service.VacationApprovalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * User: vsergeev
 * Date: 19.02.13
 */
@Service
public class
        VacationApprovalProcessService extends AbstractVacationApprovalProcessService {

    private static final Logger logger = LoggerFactory.getLogger(VacationApprovalProcessService.class);

    /**
     * рекурсивно поднимаемся по руководителям только при автоматической проверке.
     * Здесь же просто возвращаем результат ЛР сотрудника
     */
    protected VacationApproval getTopLineManagerApprovalRecursive(VacationApproval vacationApproval) throws VacationApprovalServiceException {
        return vacationApproval;
    }

    /**
     * проверяем, согласован ли отпуск с линейным руководителем
     */
    protected Boolean vacationIsApprovedByLineManager(Vacation vacation) throws VacationApprovalServiceException {
        if (vacation.getStatus() != null && approvedByLineManager.contains(vacation.getStatus().getId())) {        //проверка уже утвержденного отпуска
            return vacationIsNotRejected(vacation);
        }

        VacationApproval lineManagerApproval = getTopLineManagerApproval(vacation);

        // Согласования ЛР еще не отправлялись
        if (lineManagerApproval == null) {
           return false;
        }

        if (lineManagerApproval.getResult() != null) {    //нашли результат у одного из линейных
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, lineManagerApproval.getResult());
            return lineManagerApproval.getResult();
        }

        /* APLANATS-865
        Boolean manager2VacationApproval = getManager2Result(vacation);     //проверяем результат второго линейного
        if (manager2VacationApproval != null) {
            setFinalStatusForVacationAndSendVacationApprovedMessages(vacation, manager2VacationApproval);
            return manager2VacationApproval;
        }*/

        return null;
    }

    /**
     * Рассылаем уведомления о согласовании отпуска задним числом
     * (такой отпуск создается уполномоченным сотрудником и не нуждается в согласовании)
     */
    public void sendBackDateVacationApproved (Vacation vacation) {
        Map<String, Employee> managers = new HashMap<String, Employee>();
        List<Project> projects = projectService.getProjectsForVacation(vacation);
        Map<Employee, List<Project>> juniorManagerProjectParticipants =
                (employeeService.getJuniorProjectManagersAndProjects(projects, vacation));
        for (Employee manager : juniorManagerProjectParticipants.keySet()) {
            managers.put(manager.getEmail(), manager);
        }
        for (Project project : projects) {
            managers.put(project.getManager().getEmail(), project.getManager());
        }

        if (managerExists(vacation.getEmployee())) {
            managers.put(vacation.getEmployee().getManager().getEmail(), vacation.getEmployee().getManager());
        }


        managers.put(vacation.getEmployee().getEmail(), vacation.getEmployee());

        List<VacationApproval> tempVacationApprovals = createTempVacationApprovals(managers, vacation);
        List<String> emails = prepareEmailsListForVacationApprovedMessage(tempVacationApprovals);
        sendMailService.performVacationApprovedSender(vacation, emails);
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
