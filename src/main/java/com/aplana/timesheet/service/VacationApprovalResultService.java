package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationApprovalResultDAO;
import com.aplana.timesheet.dao.entity.VacationApprovalResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: vsergeev
 * Date: 08.02.13
 */
@Service
public class VacationApprovalResultService {

    @Autowired
    private VacationApprovalResultDAO vacationApprovalResultDAO;

    public VacationApprovalResult store(VacationApprovalResult vacationApprovalResult) {
        return vacationApprovalResultDAO.store(vacationApprovalResult);
    }


    public VacationApprovalResult getVacationApprovalResult(String uid) {
        return vacationApprovalResultDAO.getVacationApprovalResult(uid);
    }
}
