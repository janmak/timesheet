package com.aplana.timesheet.dao;


import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import com.aplana.timesheet.dao.entity.Vacation;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Repository
public class ProjectParticipantDAO {
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional(readOnly = true)
	public ProjectParticipant find(Integer id) {
		return entityManager.find(ProjectParticipant.class, id);
	}

    /**
     * Получаем РП, которые еще не ответили на письмо о согласовании отпуска, по ID их ролей на проекте
     */
    public List<ProjectParticipant> getProjectParticipantsOfManagersThatDoesntApproveVacation(List<Integer> projectRolesIds, Project project, Vacation vacation) {
        Query query = entityManager.createQuery("from ProjectParticipant as pp " +
                "where pp.project = :project and pp.projectRole.id in (:roleIds) and pp.employee not in " +
                "(select va.manager from VacationApproval as va where va.vacation = :vacation and va.result is not null)")
                .setParameter("project", project)
                .setParameter("roleIds", projectRolesIds)
                .setParameter("vacation", vacation);

        return query.getResultList();
    }
}