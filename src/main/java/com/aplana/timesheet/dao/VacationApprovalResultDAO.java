package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.VacationApprovalResult;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public VacationApprovalResult store(VacationApprovalResult vacationApprovalResult) {
        vacationApprovalResult = entityManager.merge(vacationApprovalResult);
        entityManager.flush();

        return vacationApprovalResult;
    }

    public VacationApprovalResult getVacationApprovalResult(String uid) {
        Query query = entityManager.createQuery("from VacationApprovalResult as var " +
                "where var.uid = :uid")
                .setParameter("uid", uid);
        return (VacationApprovalResult) query.getSingleResult();
    }
}
