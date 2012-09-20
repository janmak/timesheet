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

	@SuppressWarnings("unchecked")
	public Region find(String regionName) {
		logger.info("===== RegionName {} =====", regionName);
		Query query = entityManager.createQuery("from Region as r where r.name =:regionName");
		query.setParameter("regionName", regionName);
		List<Region> regionList = new ArrayList<Region>();
		regionList = query.getResultList();
		return regionList.get(0);
	}
	@SuppressWarnings("unchecked")
	public Region find(Integer regionId) {
		Query query = entityManager.createQuery("from Region as r where r.id =:regionId");
		query.setParameter("regionId", regionId);
		List<Region> regionList = new ArrayList<Region>();
		regionList = query.getResultList();
		return regionList.get(0);
	}
	@SuppressWarnings("unchecked")
	public Region findRegionByCity(String city) {
		logger.info("===== City: {} =======",city);
		Query query = entityManager.createQuery("from Region as r where r.ldapCity like '%'||:city||'%'");
		query.setParameter("city", city);
		List<Region> regionList = new ArrayList<Region>();
		regionList = query.getResultList();
		return regionList.get(0);
	}
	@SuppressWarnings("unchecked")
	public List<Region> getRegions() {
		Query query = entityManager.createQuery("from Region as r");
		List<Region> regionList = new ArrayList<Region>();
		regionList = query.getResultList();
		return regionList;
	}
}