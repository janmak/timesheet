package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Dictionary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class DictionaryDAO {
	public static final Integer CATEGORY_OF_ACTIVITY_ID = 1;
	public static final Integer TYPES_OF_ACTIVITY_ID = 2;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public Dictionary find(Integer id) {
		Dictionary dictionary = entityManager.find(Dictionary.class, id);
		return dictionary;
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Dictionary> getDictionaries() {
		Query query = entityManager.createQuery("from Dictionary");
		List<Dictionary> result = query.getResultList();
		return result;
	}
}