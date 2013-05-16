package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.VacationApproval;
import com.aplana.timesheet.dao.entity.VacationApprovalResult;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * User: vsergeev
 * Date: 08.02.13
 */
@Repository
public class VacationApprovalResultDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public VacationApprovalResult store(VacationApprovalResult vacationApprovalResult) {
        vacationApprovalResult = entityManager.merge(vacationApprovalResult);
        entityManager.flush();

        return vacationApprovalResult;
    }

    public VacationApprovalResult getVacationApprovalResultByManager(VacationApproval vacationApproval){
        Query query = entityManager.createQuery("from VacationApprovalResult as var " +
                "where var.vacationApproval = :vacationApproval")
                .setParameter("vacationApproval", vacationApproval);
        return (VacationApprovalResult) query.getSingleResult();
    }
}
