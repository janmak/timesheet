package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Holiday;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Iterator;
import java.util.List;

@Repository
public class HolidayDAO {

    private static final Logger logger = LoggerFactory.getLogger(CalendarDAO.class);

    @PersistenceContext
    private EntityManager entityManager;

    public Boolean isWorkDay(String date, Region region) {
        Boolean result = true;
        Query query = entityManager.createQuery("select h from Holiday as h where h.calDate.calDate=:calDate");
        query.setParameter("calDate", DateTimeUtil.stringToTimestamp(date));
        List holidayList = query.getResultList();

        for ( Object aHolidayList : holidayList ) {
            Holiday next = ( Holiday ) aHolidayList;

            result = ! ( ( next.getRegion() == null ) || ( next.getRegion().equals( region ) ) );
        }

        return result;
    }

    public Boolean isWorkDay(String date) {

        return isWorkDay(date, null);
    }

}
