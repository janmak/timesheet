package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.DateTimeUtil;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class CalendarDAO {
	private static final Logger logger = LoggerFactory.getLogger(CalendarDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public Calendar find(Timestamp date) {
		Query query = entityManager.createQuery("select c from Calendar as c where c.calDate=:calDate");
		query.setParameter("calDate", date);
		Calendar result = null;
		try {
			result = (Calendar) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Date '{}' not found in Calendar.", date.toString());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Calendar> getMinMaxDateList() {		
		Query query = entityManager.createQuery("SELECT max(c) as max FROM Calendar as c");
		Calendar max = ((List<Calendar>)query.getResultList()).get(0);		
		query = entityManager.createQuery("SELECT min(c) as min FROM Calendar as c");
		Calendar min = ((List<Calendar>)query.getResultList()).get(0);
		logger.info("getMinMaxYearsList MIN {} MAX {}",min.toString(), max.toString());
		List<Calendar> result = new ArrayList<Calendar>();
		result.add(min);
		result.add(max);		
		return result;
	}

	public String getMonthTxt (Integer month){
		Query query = entityManager.createQuery("select distinct(c.monthTxt) from Calendar as c where month=:monthPar");
		query.setParameter("monthPar", month);

        return query.getResultList().get(0).toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getMonth(Integer year){
		Query query = entityManager.createQuery("select distinct(c.month) from Calendar as c where month in (select distinct(c.month) from Calendar as c where year=:yearPar) order by c.month asc");
		query.setParameter("yearPar", year);
        return ( ( List<Integer> ) query.getResultList() );
	}
	/**
	 * Возвращает все даты
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<Calendar> getDateList(Integer year, Integer month) {
		Query query = entityManager
			.createQuery("from Calendar as c where c.year=:yearPar and c.month=:monthPar order by c.calDate asc");

		query.setParameter("yearPar", year);
		query.setParameter("monthPar", month);
		return query.getResultList();
	}
	
	/**
	 * Проверяет наличие года в системе
	 * param year
	 * return true если год существует в системе
	 * return false если год не существует в системе
	 */
	public boolean yearValid(Integer year){
		Query query = entityManager.createQuery("select distinct(c.year) from Calendar as c where c.year =:yearPar");
		query.setParameter("yearPar", year);
        return query.getResultList() != null;
	}

	public boolean monthValid(Integer year, Integer month) {
		Query query = entityManager.createQuery("select c.calDate from Calendar as c where c.year =:yearPar and c.month =:monthPar");
		query.setParameter("yearPar", year);
		query.setParameter("monthPar", month);
        return query.getResultList() != null;
	}

    /**
     * Возвращает последний рабочий день месяца для переданной даты (включая саму дату).
     * @param day
     * @return Calendar
     */
    public Calendar getLastWorkDay(Calendar day, Region region) {
        Query query = entityManager.createQuery(
                "select max(c) " +
                "from Calendar as c " +
                "left outer join c.holidays as h with h.region.id is null " +
                "where c.calDate<=:calDatePar and h.id is null ");
        Date monthLastDay = DateTimeUtil.stringToTimestamp(DateTimeUtil.endMonthDay(day.getCalDate()));
        query.setParameter("calDatePar", monthLastDay);

        return (Calendar) query.getSingleResult();

    }

    /**
     * Возвращает следующий рабочий день месяца для переданной даты.
     * @param day
     * @param region
     * @return Calendar
     */
    public Calendar getNextWorkDay(Calendar day, Region region) {
        Query query = entityManager.createQuery(
                "select c " +
                        "from Calendar as c " +
                        "left outer join c.holidays as h with h.region.id is null " +
                        "where c.calDate>:calDatePar and h.id is null " +
                        "order by c.calDate asc " +
                        "limit 1");
        query.setParameter("calDatePar", new Date(day.getCalDate().getTime()));
        return ( Calendar ) query.getResultList().get( 0 );

    }
	
}