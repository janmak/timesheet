package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class DictionaryItemDAO {

	@PersistenceContext
	private EntityManager entityManager;

    @Autowired
    private DictionaryDAO dictionaryDAO;

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<DictionaryItem> getItemsByDictionaryId(Integer dictionaryId) {
		Query query = entityManager.createQuery( 
                "from DictionaryItem as di where di.dictionary = :dictionary order by di.value desc"
        ).setParameter("dictionary", dictionaryDAO.find(dictionaryId));
        
        return query.getResultList();
	}

	@Transactional(readOnly = true)
	public DictionaryItem find(Integer id) {
        return entityManager.find(DictionaryItem.class, id);
	}
}