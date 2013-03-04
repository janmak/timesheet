package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Dictionary;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class DictionaryDAO {

    @PersistenceContext
	private EntityManager entityManager;

	public Dictionary find(Integer id) {
        return entityManager.find(Dictionary.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Dictionary> getDictionaries() {
        return entityManager.createQuery("from Dictionary").getResultList();
	}
}