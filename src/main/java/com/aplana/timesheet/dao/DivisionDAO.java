package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
		Query query = entityManager.createQuery(
                "from Division as d where d.active=:active order by d.name asc"
        ).setParameter( "active", true );

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
		Query query = entityManager.createQuery(
                "from Division as d where d.active=:active and d.ldapName=:title"
        ).setParameter( "active", true ).setParameter( "title", title );

        try {
			return  (Division) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Department with title '{}' not found.", title);
            return null;
		}
	}
}