package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Manager;
import com.aplana.timesheet.dao.entity.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Random;

/**
 * @author iziyangirov
 */
@Repository
public class ManagerDAO {
    private static final Logger logger = LoggerFactory.getLogger(ManagerDAO.class);

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

    public Manager findRandomManager() {
        final  Query query =
                entityManager.createQuery("from Manager as man"  + " where man.employee.manager is not null");
        List resultList = query.getResultList();
        logger.info("lalalalo = {}", resultList);

        Integer size = resultList.size();
        Random random = new Random();
        Integer number = random.nextInt(size);
        return (Manager) resultList.get(number);
    }
}
