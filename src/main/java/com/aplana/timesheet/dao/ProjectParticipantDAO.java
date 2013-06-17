package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;


@Repository
public class ProjectParticipantDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProjectParticipantDAO.class);

	@PersistenceContext
	private EntityManager entityManager;
	
	public ProjectParticipant find(Integer id) {
		return entityManager.find(ProjectParticipant.class, id);
	}

    public Boolean isProjectManager(Employee employee, Project project) {
        final Query query =  entityManager.createQuery("from ProjectParticipant p " +
                "where p.project = :project and p.employee = :employee")
                .setParameter("project", project).setParameter("employee", employee);
        return query.getResultList().size() != 0;
    }

    public void save(ProjectParticipant participant)  {
        entityManager.merge(participant);
        entityManager.flush();
        logger.debug("Flushed participant object id = {}", participant.getId());
    }

    public List<ProjectParticipant> findByEmployee(Employee employee) {
        final Query query = entityManager.createQuery("from ProjectParticipant p " +
                "where p.employee = :employee").setParameter("employee", employee);
        return query.getResultList();
    }

}