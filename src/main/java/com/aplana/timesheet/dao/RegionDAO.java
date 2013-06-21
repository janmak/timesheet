package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.enums.RegionsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<Region> regions = (List<Region>) entityManager.createQuery("from Region as r order by r.name").getResultList();
        Region region = null;
        for (int i = 0; i <= regions.size(); i++) {
            if (regions.get(i).getId().equals(RegionsEnum.OTHERS.getId())) {
                region = regions.get(i);
                regions.remove(i);
                break;
            }
        }
        regions.add(regions.size(),region);
        return regions;
    }
}