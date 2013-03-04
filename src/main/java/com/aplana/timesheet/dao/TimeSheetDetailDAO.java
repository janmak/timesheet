package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.TimeSheetDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class TimeSheetDetailDAO {
	private static final Logger logger = LoggerFactory.getLogger(TimeSheetDetailDAO.class);

	@PersistenceContext
	private EntityManager entityManager;

	public void storeTimeSheetDetail(TimeSheetDetail timeSheetDetail) {
		TimeSheetDetail tsdMerged = entityManager.merge(timeSheetDetail);
		logger.info("timeSheetDetail merged.");
		entityManager.flush();
		logger.info("Persistence context synchronized to the underlying database.");
		timeSheetDetail.setId(tsdMerged.getId());
		logger.debug("Flushed TimeSheetDetail object id = {}", tsdMerged.getId());
	}
}