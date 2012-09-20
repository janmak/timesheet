package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
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
public class DivisionDAO {
	private static final Logger logger = LoggerFactory.getLogger(DivisionDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Возвращает список подразделений
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Division> getDivisions() {
		Query query = entityManager
			.createQuery("from Division as d where d.active=:active order by d.name asc");
		query.setParameter("active", true);
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public Division find(Integer id) {
		return entityManager.find(Division.class, id);
	}
	
	/**
	 * Ищет активное подразделение с указанным именем.
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	@Transactional(readOnly = true)
	public Division find(String title) {
		Division result = null;
		Query query = entityManager
			.createQuery("from Division as d where d.active=:active and d.ldapName=:title");
		query.setParameter("active", true);
		query.setParameter("title", title);
		try {
			result = (Division) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Department with title '{}' not found.", title);
		}
		return result;
	}
	
//	@Transactional
//	public void storeDivision(Division division) {
//		Division divMerged = (Division) entityManager.merge(division);
//		logger.info("Division merged.");
//		entityManager.flush();
//		logger.info("Persistence context synchronized to the underlying database.");
//		logger.debug("Flushed Division object id = {}", divMerged.getId());
//	}
}