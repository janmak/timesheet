package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Vacation;
import com.aplana.timesheet.dao.entity.VacationApproval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author iziyangirov
 */

@Repository
public class VacationApprovalDAO {

    @PersistenceContext
    private EntityManager entityManager;

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

    @Transactional
    public void store(VacationApproval vacationApproval){
        entityManager.merge(vacationApproval);
        entityManager.flush();
    }

    public List<String> getVacationApprovalEmailList(Integer vacationId) {
        final Query query = entityManager.createQuery("select va.manager.email from VacationApproval va where va.vacation.id = :vac_id")
                .setParameter("vac_id", vacationId);

        return query.getResultList();
    }

    /**
     * Получаем электронные адреса РП, которые еще не ответили на письмо о согласовании отпуска, по ID их ролей на проекте
     */
    public List<String> getEmailAddressesOfManagersThatDoesntApproveVacation(List<Integer> projectRolesIds, Project project, Vacation vacation) {
        Query query = entityManager.createQuery("select pp.employee.email from ProjectParticipant as pp " +
                "where pp.project = :project and pp.projectRole.id in (:roleIds) and pp.employee not in " +
                "(select va.manager from VacationApproval as va where va.vacation = :vacation and va.result is not null)")
                .setParameter("project", project)
                .setParameter("roleIds", projectRolesIds)
                .setParameter("vacation", vacation);

        return query.getResultList();
    }

}
