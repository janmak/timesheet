package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.DayTimeSheet;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.enums.TypeOfActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Repository
public class TimeSheetDAO {

    private static final Logger logger = LoggerFactory.getLogger(TimeSheetDAO.class);
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void storeTimeSheet(TimeSheet timeSheet) {
        TimeSheet tsMerged = entityManager.merge(timeSheet);
        logger.info("timeSheet merged.");
        entityManager.flush();
        logger.info("Persistence context synchronized to the underlying database.");
        timeSheet.setId(tsMerged.getId());
        logger.debug("Flushed TimeSheet object id = {}", tsMerged.getId());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public TimeSheet findForDateAndEmployee(Calendar date, Integer employeeId) {
        Query query = entityManager.createQuery(
                "select ts from TimeSheet as ts where ts.calDate = :calDate and ts.employee.id = :employeeId"
        ).setParameter( "calDate", date ).setParameter("employeeId", employeeId);

        List<TimeSheet> result = query.getResultList();

        return result.isEmpty() ? null : result.get( 0 );
    }

    /**
     * Формирует список объектов, хранящих дату и работы по сотруднику за каждый день месяца
     *
     * @param year
     * @param month
     * @param employee
     * @return List<DayTimeSheet>
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Integer year, Integer month, Integer region, Employee employee) {

        // Я не знаю как написать это на HQL, но на SQL пишется легко и непринужденно.
        Query query = entityManager.createNativeQuery(
                "select " +
                        "c.caldate caldate, " +
                        "h.id holiday_id, " +
                        "ts.id timesheet_id, " +
                        "SUM(tsd.duration), " +
                        "tsd.act_type "
                + "from calendar c "
                + "left outer join time_sheet as ts " +
                        "on ts.emp_id = :employeeId and ts.caldate=c.caldate "
                + "left outer join holiday h " +
                        "on c.caldate=h.caldate and (h.region is null or h.region=:region) "
                + "left outer join time_sheet_detail as tsd " +
                        "on ts.id=tsd.time_sheet_id "
                + "where c.year=:yearPar " +
                        "and c.month=:monthPar "
                + "group by " +
                        "c.caldate, " +
                        "h.id, " +
                        "ts.id, " +
                        "tsd.act_type "
                + "order by c.calDate asc"
        )       .setParameter( "yearPar", year ).setParameter( "monthPar", month )
                .setParameter( "region", region ).setParameter("employeeId", employee.getId());

        List result = query.getResultList();

        List<DayTimeSheet> dayTSList = new ArrayList<DayTimeSheet>();

        HashMap<Long, DayTimeSheet> map = new HashMap<Long, DayTimeSheet>();
        for (Object object : result ) {
            Object[] item = (Object[]) object;
            //дата в месяце
            Timestamp calDate = new Timestamp(((Date) item[0]).getTime());
            //если айдишник из таблицы календарь есть то это выходной
            Boolean holiday = item[1] != null;
            //айдишник в ts. нужен нам, чтобы отчет за один день суммировать
            Integer tsId = item[2] != null ? ((BigDecimal) item[2]).intValue() : null;
            //время за каждую деятельность(может быть несколько за один день)
            BigDecimal duration = item[3] != null ? ((BigDecimal) item[3]) : null;
            //по этому полю определяем отпуск\отгул и т.п.
            Integer actType = item[4] != null ? ((Integer) item[4]) : null;

            if (!map.containsKey(calDate.getTime())) {
                DayTimeSheet ds = new DayTimeSheet(calDate, holiday, tsId, actType, duration, employee);
                ds.setTimeSheetDAO(this);
                map.put(calDate.getTime(), ds);
            } else {
                DayTimeSheet dts = map.get(calDate.getTime());
                if (duration != null && TypeOfActivity.isEfficientActivity( actType ) ) {
                    dts.setDuration(dts.getDuration().add(duration));
                }
            }
        }
        for (DayTimeSheet val : map.values()) {
            dayTSList.add(val);
        }

        Collections.sort(dayTSList);
        return dayTSList;
    }

    /**
     * Возвращает самый последний план относительно date
     *
     * @param date
     * @param employeeId
     * @return отчет И МОЖЕТ быть null
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public TimeSheet findLastTimeSheetBefore(Calendar date, Integer employeeId) {
        Query query = entityManager.createQuery(
                "select ts "
                + "from TimeSheet as ts "
                + "where ts.calDate <:calDate "
                    + "and ts.employee.id = :employeeId "
                + "order by ts.calDate desc"
        ).setParameter("calDate", date).setParameter("employeeId", employeeId);

        List<TimeSheet> result = query.getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Возвращает следующий план относительно date
     *
     * @param nextDate
     * @param employeeId
     * @return отчет
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public TimeSheet findNextTimeSheetAfter(Calendar nextDate, Integer employeeId) {
        Query query = entityManager.createQuery(""
                + "select ts "
                + "from TimeSheet as ts "
                + "where ts.calDate = :calDate "
                    + "and ts.employee.id = :employeeId "
                + "order by ts.calDate asc"
        ).setParameter("calDate", nextDate).setParameter("employeeId", employeeId);

        List<TimeSheet> result = query.getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public TimeSheet find(Integer id) {
        return entityManager.find(TimeSheet.class, id);
    }

    public boolean isEmplyeeHasTsForDate(Integer employeeId, java.util.Date date){
        Query query = entityManager.createQuery(
                "SELECT COUNT(ts.calDate.calDate) FROM TimeSheet as ts WHERE ts.employee.id = :employeeId AND ts.calDate.calDate = :date"
        ).setParameter("employeeId", employeeId).setParameter("date", date);

        return ((Long) query.getResultList().get(0)).equals(1L);
    }

    @Transactional
    public void delete(TimeSheet timeSheet) {
        entityManager.remove(timeSheet);
    }

    public List<TimeSheet> getTimeSheetsForEmployee(Employee employee, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                "from TimeSheet ts where ts.employee = :employee and YEAR(ts.calDate.calDate) = :year and MONTH(ts.calDate.calDate) = :month"
        ).setParameter("employee", employee).setParameter("year", year).setParameter("month", month);

        return query.getResultList();
    }
}