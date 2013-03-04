package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

	@SuppressWarnings("unchecked")
	public List<DictionaryItem> getItemsByDictionaryId(Integer dictionaryId) {
        return getDictionaryItemsByDictId(dictionaryId, "value desc");
	}

    @SuppressWarnings("unchecked")
    public List<DictionaryItem> getItemsByDictionaryIdAndOrderById(Integer dictionaryId) {
        return getDictionaryItemsByDictId(dictionaryId, "id asc");
    }

    private List<DictionaryItem> getDictionaryItemsByDictId(Integer dictionaryId, String orderComponent) {
        Query query = entityManager.createQuery(
            "from DictionaryItem as di where di.dictionary = :dictionary order by di." + orderComponent
        ).setParameter("dictionary", dictionaryDAO.find(dictionaryId));

        return query.getResultList();
    }

	public DictionaryItem find(Integer id) {
        return id != null ? entityManager.find(DictionaryItem.class, id) : null;
	}
}