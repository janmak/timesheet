package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.hibernate.Hibernate;
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
     * Возвращает список активных подразделений
     */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Division> getActiveDivisions() {
        Query query = entityManager.createQuery(
                "from Division as d where d.active=:active order by d.name asc"
        ).setParameter("active", true);

        return query.getResultList();
    }

    @Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Division> getAllDivisions() {
        return  entityManager.createQuery(
                "from Division as d order by d.name asc"
        ).getResultList();
	}

    @Transactional(readOnly = true)
    public List<Division> getDivisionCheck() {
        return  entityManager.createQuery(
                "from Division as d where d.isCheck = true"
        ).getResultList();
    }

	@Transactional(readOnly = true)
	public Division find(Integer id) {
        Division division = entityManager.find(Division.class, id);
        Hibernate.initialize(division.getLeaderId());
        return division;
	}
	
	/**
	 * Ищет активное подразделение с указанным именем.
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	@Transactional(readOnly = true)
	public Division find(String title) {
		Query query = entityManager.createQuery(
                "from Division as d where d.active=:active and lower(d.ldapName)=:title"
        ).setParameter( "active", true ).setParameter( "title", title.toLowerCase() );

        try {
			return  (Division) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Department with title '{}' not found.", title);
            return null;
		}
	}

    @Transactional(readOnly = true)
    public Division findByDepartmentName(String departmentName) {
        logger.debug("findByDepartmentName: departmentName = {}", departmentName);
        Integer id = (Integer) entityManager.createNativeQuery(
                "SELECT id FROM division as d WHERE lower(department_name) SIMILAR TO  :departmentName"
        ).setParameter("departmentName", ("(%_,|)" + departmentName + "(,_%|)").toLowerCase()).getSingleResult();
        return (Division) find(id);
    }

    @Transactional
    public void save(Division division) {
        if (division.getId() != null) {
            entityManager.merge(division);
        } else {
            division.setId((Integer) entityManager.createQuery("SELECT MAX(id) FROM Division").getSingleResult() + 1);
            entityManager.persist(division);
        }
        entityManager.flush();
    }
}