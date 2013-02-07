package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationApprovalDAO;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User: vsergeev
 * Date: 06.02.13
 */
@Service
public class VacationApprovalService {

    @Autowired
    VacationApprovalDAO vacationApprovalDAO;

    public void store(VacationApproval vacationApproval) {
        vacationApprovalDAO.store(vacationApproval);
    }

    public List<String> getEmailAddressesOfManagersThatDoesntApproveVacation(List<Integer> projectRolesIds, Project project, Vacation vacation) {
        return vacationApprovalDAO.getEmailAddressesOfManagersThatDoesntApproveVacation(projectRolesIds, project, vacation);
    }

}
