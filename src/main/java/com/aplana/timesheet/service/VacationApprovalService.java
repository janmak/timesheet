package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationApprovalDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author iziyangirov
 */
@Service
public class VacationApprovalService {

    @Autowired
    private VacationApprovalDAO vacationApprovalDAO;

    public VacationApproval getVacationApproval(String uid){
        return vacationApprovalDAO.findVacationApproval(uid);
    }

    @Transactional
    public VacationApproval store(VacationApproval vacationApproval){
        return vacationApprovalDAO.store(vacationApproval);
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId){
        return vacationApprovalDAO.getVacationApprovalEmailList(vacationId);
    }

    public List<VacationApproval> getAllApprovalsForVacation(Vacation vacation) {
        return vacationApprovalDAO.getAllApprovalsForVacation(vacation);
    }

    public List<VacationApproval> getProjectManagerApprovalsForVacationByProject(Vacation vacation, Project project) {
        return vacationApprovalDAO.getProjectManagerApprovalsForVacationByProject(vacation, project);
    }

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        return vacationApprovalDAO.tryGetManagerApproval(vacation, manager);
    }
}
