package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.OvertimeCause;
import com.aplana.timesheet.dao.entity.TimeSheet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public OvertimeCause getOvertimeCause(TimeSheet timeSheet) {
        return (OvertimeCause) entityManager.createQuery(
                "from OvertimeCause o WHERE o.timeSheet.id = :id"
        ).setParameter("id", timeSheet.getId()).getSingleResult();
    }

    @Transactional
    public void store(OvertimeCause overtimeCause) {
        OvertimeCause merge = entityManager.merge(overtimeCause);
        entityManager.flush();
        overtimeCause.setId(merge.getId());
    }
}


