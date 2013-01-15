package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RegionDAO {
	private static final Logger logger = LoggerFactory.getLogger(RegionDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;

	public Region find(String regionName) {
		logger.info("===== RegionName {} =====", regionName);
		Query query = entityManager.createQuery(
                "from Region as r where r.name =:regionName"
        ).setParameter( "regionName", regionName );

        return ( Region ) query.getResultList().get( 0 );
	}

	public Region find(Integer regionId) {
		Query query = entityManager.createQuery(
                "from Region as r where r.id =:regionId"
        ).setParameter( "regionId", regionId );

        return ( Region ) query.getResultList().get( 0 );
	}

	public Region findRegionByCity(String city) {
		logger.info("===== City: {} =======",city);
		Query query = entityManager.createQuery(
                "from Region as r where r.ldapCity like '%'||:city||'%'"
        ).setParameter( "city", city );

        return ( Region ) query.getResultList().get( 0 );
	}

    @SuppressWarnings("unchecked")
	public List<Region> getRegions() {
        return ( List<Region> ) entityManager.createQuery("from Region as r").getResultList();
	}
}