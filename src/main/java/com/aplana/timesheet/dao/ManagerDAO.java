package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Manager;
import com.aplana.timesheet.dao.entity.Region;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author iziyangirov
 */
@Repository
public class ManagerDAO {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Manager> getManagerList(){
        Query query = entityManager.createQuery("from Manager as m");

        return query.getResultList();
    }

    public Manager getDivisionRegionManager(Division division, Region region){
        Query query = entityManager.createQuery(
                "FROM Manager AS m WHERE m.division.id=:divId AND m.region.id=:regId"
        ).setParameter("divId", division.getId()).setParameter("regId", region.getId());

        if (query.getResultList() == null || query.getResultList().size() == 0) {
            return null;
        }
        return (Manager)query.getSingleResult();
    }

    public void deleteManager(Manager manager){
        entityManager.remove(manager);
        entityManager.flush();
    }

    public void updateInsertManager(Manager manager){
        entityManager.merge(manager);
        entityManager.flush();
    }

}
