package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Holiday;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class  CalendarDAO {
	private static final Logger logger = LoggerFactory.getLogger(CalendarDAO.class);

    private static final String BEGIN_DATE = "beginDate";
    private static final String END_DATE = "endDate";
    private static final String REGION = "region";

    private static final String HOLIDAYS_FOR_REGION_BETWEEN_DATES = String.format(
            "from Holiday as h where ((h.calDate.calDate between :%s and :%s) and (h.region is null or h.region = :%s))",
            BEGIN_DATE,
            END_DATE,
            REGION
    );

    @PersistenceContext
	private EntityManager entityManager;

	public Calendar find(Timestamp date) {
		Query query = entityManager.createQuery(
                "select c from Calendar as c where c.calDate=:calDate"
        ).setParameter( "calDate", date );

		try {
            return (Calendar) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Date '{}' not found in Calendar.", date.toString());
            return null;
		}
	}

	@SuppressWarnings("unchecked")
	public Calendar getMinDateList() {
        Calendar min = ( Calendar ) entityManager.createQuery("SELECT min(c) as min FROM Calendar as c").getResultList().get( 0 );
        logger.info( "getMinMaxYearsList MIN {}", min.toString() );
        return min;
    }

	@SuppressWarnings("unchecked")
	public Calendar getMaxDateList() {
		Calendar max = ( Calendar ) entityManager.createQuery("SELECT max(c) as max FROM Calendar as c").getResultList().get( 0 );
        logger.info( "getMinMaxYearsList MAX {}", max.toString() );
		return max;
	}

	public String getMonthTxt (Integer month){
		Query query = entityManager.createQuery(
                "select distinct(c.monthTxt) from Calendar as c where month=:monthPar"
        ).setParameter( "monthPar", month );

        return query.getResultList().get(0).toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getMonth(Integer year){
		Query query = entityManager.createQuery(
                "select distinct(c.month) from Calendar as c " +
                        "where month in " +
                            "(select distinct(c.month) from Calendar as c " +
                                    "where year=:yearPar) order by c.month asc"
        ).setParameter( "yearPar", year );

        return ( ( List<Integer> ) query.getResultList() );
	}
	/**
	 * Возвращает все даты
	 */
	@SuppressWarnings("unchecked")
	public List<Calendar> getDateList(Integer year, Integer month) {
		Query query = entityManager.createQuery(
                "from Calendar as c where c.year=:yearPar and c.month=:monthPar order by c.calDate asc"
        ).setParameter("yearPar", year).setParameter("monthPar", month);

		return query.getResultList();
	}
	
	/**
	 * Проверяет наличие года в системе
	 * param year
	 * return true если год существует в системе
	 * return false если год не существует в системе
	 */
	public boolean yearValid(Integer year){
		Query query = entityManager.createQuery(
                "select distinct(c.year) from Calendar as c where c.year =:yearPar"
        ).setParameter("yearPar", year);

        return query.getResultList() != null;
	}

	public boolean monthValid(Integer year, Integer month) {
		Query query = entityManager.createQuery(
                "select c.calDate from Calendar as c where c.year =:yearPar and c.month =:monthPar"
        ).setParameter("yearPar", year).setParameter("monthPar", month);

        return query.getResultList() != null;
	}

    /**
     * Возвращает последний рабочий день месяца для переданной даты (включая саму дату).
     *
     * @param day
     * @return Calendar
     */
    public Calendar getLastWorkDay(Calendar day) {
        Date monthLastDay = DateTimeUtil.stringToTimestamp(DateTimeUtil.endMonthDay(day.getCalDate()));

        Query query = entityManager.createQuery(
                "select max(c) from Calendar as c " +
                "left outer join c.holidays as h with h.region.id is null " +
                "where c.calDate<=:calDatePar and h.id is null "
        ).setParameter("calDatePar", monthLastDay);

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
                        "left outer join c.holidays as h with h.region.id is null or h.region.id=:region " +
                        "where c.calDate>:calDatePar and h.id is null " +
                        "order by c.calDate asc " +
                        "limit 1"
        ).setParameter("calDatePar", new Date(day.getCalDate().getTime())).setParameter("region", region.getId());

        return ( Calendar ) query.getResultList().get( 0 );
    }

    /**
     * Возвращает количество выходных дней за выбранный период для конкретного региона
     */
    public Integer getHolidaysCountForRegion(Date beginDate, Date endDate, Region region){
        Query query = entityManager.createQuery("select count (*) " + HOLIDAYS_FOR_REGION_BETWEEN_DATES);

        setParametersForHolidaysQuery(beginDate, endDate, region, query);

        return ((Long) query.getSingleResult()).intValue();
    }

    public List<Holiday> getHolidaysForRegion(Date beginDate, Date endDate, Region region) {
        final Query query = entityManager.createQuery(HOLIDAYS_FOR_REGION_BETWEEN_DATES);

        setParametersForHolidaysQuery(beginDate, endDate, region, query);

        return query.getResultList();
    }

    private void setParametersForHolidaysQuery(Date beginDate, Date endDate, Region region, Query query) {
        query.setParameter(BEGIN_DATE, beginDate)
             .setParameter(END_DATE, endDate)
             .setParameter(REGION, region);
    }

    public int getWorkDaysCountForRegion(Region region, Integer year, Integer month, @NotNull Date fromDate) {
        final Query query = entityManager.createQuery(
                "select count(c) - count(h)" +
                    " from Calendar c" +
                    " left outer join c.holidays h" +
                    " where YEAR(c.calDate) = :year and MONTH(c.calDate) = :month" +
                    " and (h.region is null or h.region = :region) and c.calDate >= :from_date"
        ).setParameter("region", region).setParameter("year", year).
                setParameter("month", month).setParameter("from_date", fromDate);

        return ((Long) query.getSingleResult()).intValue();
    }

    public int getWorkDaysCountForRegion(Region region, Integer year, Integer month, @Nullable Date fromDate,
                                         @Nullable Date toDate) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        final Root<Calendar> from = criteriaQuery.from(Calendar.class);
        final Join<Object, Object> join = from.join("holidays", JoinType.LEFT);
        final CriteriaQuery<Object> select = criteriaQuery.select(criteriaBuilder.diff(
                criteriaBuilder.count(from),
                criteriaBuilder.count(join)
        ));
        final List<Predicate> predicates = new ArrayList<Predicate>();
        final Path<Date> calDatePath = from.get("calDate");
        final Path<Region> regionPath = join.get("region");

        predicates.add(
                criteriaBuilder.and(
                        criteriaBuilder.equal(
                                criteriaBuilder.function("YEAR", Integer.class, calDatePath),
                                year
                        ),
                        criteriaBuilder.equal(
                                criteriaBuilder.function("MONTH", Integer.class, calDatePath),
                                month
                        ),
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(regionPath),
                                criteriaBuilder.equal(regionPath, region)
                        )
                )
        );

        if (fromDate != null) {
            predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            calDatePath,
                            fromDate
                    )
            );
        }

        if (toDate != null) {
            predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                            calDatePath,
                            toDate
                    )
            );
        }

        select.where(predicates.toArray(new Predicate[predicates.size()]));

        return ((Long) entityManager.createQuery(select).getSingleResult()).intValue();
    }
}