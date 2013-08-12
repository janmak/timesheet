package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author iziyangirov
 */

@Repository
public class VacationApprovalDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Vacation findRandomVacation() {
       Vacation vacation;
      final  Query query =
                entityManager.createQuery("from Vacation order by id desc") ;
        return (Vacation) query.getResultList().get(2);
    }

    public void delete(VacationApproval vacationApproval) {
        Hibernate.initialize(vacationApproval);

        entityManager.remove(vacationApproval);
    }

    /**
     * Согласование на отпуск по переданному uid
     * @param uid
     * @return
     */
    public VacationApproval findVacationApproval(String uid) {
        final Query query =
                entityManager.createQuery("from VacationApproval va where va.uid = :uid")
                        .setParameter("uid", uid);

        if (query.getResultList().isEmpty()){
            return null;
        }

        return (VacationApproval)query.getSingleResult();
    }

    public VacationApproval findVacationApprovalDAO(Vacation vacation) {
        final Query query =
                entityManager.createQuery("from VacationApproval va where va.vacation = :vacation")
                        .setParameter("vacation", vacation);

        if (query.getResultList().isEmpty()){
            return null;
        }

        return (VacationApproval)query.getResultList().get(0);
    }

    public VacationApproval store(VacationApproval vacationApproval){
        vacationApproval = entityManager.merge(vacationApproval);
        entityManager.flush();

        return vacationApproval;
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId) {
        final Query query = entityManager.createQuery("select va.manager.email from VacationApproval va where va.vacation.id = :vac_id")
                .setParameter("vac_id", vacationId);

        return query.getResultList();
    }

    /**
     * получаем данные о согласовании отпуска конкретным менеджером
     */
    public VacationApproval getManagerApproval(Vacation vacation, Employee manager) {
        try {
            Query query = entityManager.createQuery("from VacationApproval as va " +
                    "where va.manager = :manager and va.vacation = :vacation")
                    .setParameter("manager", manager)
                    .setParameter("vacation", vacation);

            return (VacationApproval) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public VacationApproval tryGetManagerApproval(Vacation vacation, Employee manager) {
        try {
            Query query = entityManager.createQuery("from VacationApproval as va " +
                    "where va.vacation = :vacation and va.manager = :manager")
                    .setParameter("vacation", vacation)
                    .setParameter("manager", manager);

            return (VacationApproval) query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public List<VacationApproval> getProjectManagerApprovalsForVacationByProject(Vacation vacation, Project project) {
        Query query = entityManager.createQuery("select distinct va from VacationApproval as va " +
                "left outer join va.vacationApprovalResults as var " +
                "left outer join var.project as p " +
                "where (va.vacation = :vacation) and (p = :project or va.manager = :manager)")
                .setParameter("project", project)
                .setParameter("manager", project.getManager())
                .setParameter("vacation", vacation);

        return query.getResultList();
    }

    public List<VacationApproval> getAllApprovalsForVacation(Vacation vacation) {
        return entityManager.createQuery("from VacationApproval as va where va.vacation = :vacation")
                .setParameter("vacation", vacation).getResultList();
    }
}
