package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.OvertimeCause;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author eshangareev
 * @version 1.0
 */
@Repository
public class OvertimeCauseDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public void store(OvertimeCause overtimeCause) {
        OvertimeCause merge = entityManager.merge(overtimeCause);
        entityManager.flush();
        overtimeCause.setId(merge.getId());
    }
}


