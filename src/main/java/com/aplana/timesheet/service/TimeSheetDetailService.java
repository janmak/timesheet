package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.TimeSheetDetailDAO;
import com.aplana.timesheet.dao.entity.TimeSheetDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeSheetDetailService
{
	private static final Logger logger = LoggerFactory.getLogger(TimeSheetDetailService.class);
	
	@Autowired
	private TimeSheetDetailDAO timeSheetDetailDAO;
	
	public void storeTimeSheetDetail(TimeSheetDetail timeSheetDetail)
	{
		timeSheetDetailDAO.storeTimeSheetDetail(timeSheetDetail);
		logger.info("TimeSheetDetail object saved.");
	}
}