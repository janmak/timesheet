package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.ProjectRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ProjectRoleDAO {
	private static final Logger logger = LoggerFactory.getLogger(ProjectRoleDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * Возвращает объект класса ProjectRole по указанному идентификатору или null.
	 */
	@Transactional(readOnly = true)
	public ProjectRole find(Integer id) {
		if (id == null) { return null; }
		return entityManager.find(ProjectRole.class, id);
	}
	
	/**
	 * Возвращает объект класса ProjectRole по указанному идентификатору,
	 * соответсвующий активной проектой роли, либо null.
	 */
	@Transactional(readOnly = true)
	public ProjectRole findActive(Integer id) {
		if (id == null) { return null; }
		ProjectRole result;
		Query query = entityManager
			.createQuery("from ProjectRole as pr where pr.id=:id and pr.active=:active");
		query.setParameter("active", true);
		query.setParameter("id", id);
		try {
			result = (ProjectRole) query.getSingleResult();
		} catch (NoResultException e) {
			result = null;
		}
		return result;
	}
	
	/**
	 * Возвращает активные проектные роли.
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<ProjectRole> getProjectRoles() {
		Query query = entityManager.createQuery("from ProjectRole as pr where pr.active=:active");
		query.setParameter("active", true);
		return query.getResultList();
	}
	
	/**
	 * Возвращает активную проектную роль по названию
	 */
	@Transactional(readOnly = true)
	public ProjectRole find(String title) {
		ProjectRole result = null;
		Query query = entityManager
			.createQuery("from ProjectRole as pr where pr.active=:active and pr.ldapTitle like :title");
		query.setParameter("active", true);
		query.setParameter("title", "%"+title+"%");
            try {
                result = (ProjectRole) query.getSingleResult();
            } catch (NoResultException e) {
                result=findByCode("ND");
            }
		return result;
	}

    public ProjectRole findByName(String s) {
        Query query=entityManager.createQuery("FROM ProjectRole AS pr WHERE pr.active=:active AND pr.name like :name");
        query.setParameter("active", true);
        query.setParameter("name", "%"+s+"%");
        return (ProjectRole) query.getSingleResult();
    }

    public ProjectRole findByCode(String s) {
        Query query=entityManager.createQuery("FROM ProjectRole AS pr WHERE pr.active=:active AND pr.code=:code");
        query.setParameter("active", true);
        query.setParameter("code", s);
        return (ProjectRole) query.getResultList().get(0);
    }

    public ProjectRole getSysRole(Integer roleId) {
        Query query=entityManager.createQuery("FROM ProjectRole AS pr WHERE pr.id=:id");
        query.setParameter("id", roleId);
        return (ProjectRole) query.getResultList().get(0);
    }
}