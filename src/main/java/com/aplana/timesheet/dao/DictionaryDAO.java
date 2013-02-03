package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Dictionary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class DictionaryDAO {

    @PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public Dictionary find(Integer id) {
        return entityManager.find(Dictionary.class, id);
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Dictionary> getDictionaries() {
        return entityManager.createQuery("from Dictionary").getResultList();
	}
}