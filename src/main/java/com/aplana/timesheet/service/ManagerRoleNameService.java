package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.VacationApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервис, который возвращает название роли руководителя для конкретного сотрудника
 * @author Aalikin
 * @since 16.05.13
 */

@Service
public class ManagerRoleNameService {

    private final String LINE_MANAGER = "Линейный Руководитель";
    private final String PROJECT_LEADER = "Руководитель проекта \"%s\"";
    private final String SENIOR_ANALYST = "Ведущий аналитик \"%s\"";
    private final String TEAM_LEADER = "Тим-лидер \"%s\"";

    @Autowired
    private ProjectParticipantService projectParticipantService;
    @Autowired
    private VacationApprovalResultService vacationApprovalResultService;

    /** Получить проектную роль согласующего*/
    public String getManagerRoleName(VacationApproval vacationApproval){
        if (isLineManager(vacationApproval)){
            return LINE_MANAGER;
        }
        Project project = getProject(vacationApproval);

        if (project.getManager().getId().equals(vacationApproval.getManager().getId())){
//                ||(isProjectManager(vacationApproval.getManager(), project))){
            return String.format(PROJECT_LEADER, project.getName());
        }

        if (vacationApproval.getManager().getJob().getId().equals(15)){
            return String.format(SENIOR_ANALYST, project.getName());
        }
        return String.format(TEAM_LEADER, project.getName());
    }

    private Project getProject(VacationApproval vacationApproval){
        return vacationApprovalResultService.getVacationApprovalResultByManager(vacationApproval).getProject();
    }

    /** Проверка на линейного руководителя*/
    private Boolean isLineManager(VacationApproval vacationApproval){
        Integer manager = vacationApproval.getVacation().getEmployee().getManager() != null
                ? vacationApproval.getVacation().getEmployee().getManager().getId() : null;
        Integer manager2 = vacationApproval.getVacation().getEmployee().getManager2() != null
                ? vacationApproval.getVacation().getEmployee().getManager2().getId() : null;
        return ( vacationApproval.getManager().getId().equals(manager)) || (vacationApproval.getManager().getId().equals(manager2));
    }

    /** Проверка на тот случай, когда у проекта может быть больше руководителей проекта*/
    private Boolean isProjectManager(Employee manager, Project project){
        return projectParticipantService.isProjectManager(manager, project);
    }
}
