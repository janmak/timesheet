package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


@Repository
public class ProjectParticipantDAO {
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

}