package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.util.TimeSheetConstans;
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
import java.util.*;

@Repository
public class TimeSheetDAO {

    private static final Logger logger = LoggerFactory.getLogger(TimeSheetDAO.class);
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void storeTimeSheet(TimeSheet timeSheet) {
        TimeSheet tsMerged = (TimeSheet) entityManager.merge(timeSheet);
        logger.info("timeSheet merged.");
        entityManager.flush();
        logger.info("Persistence context synchronized to the underlying database.");
        timeSheet.setId(tsMerged.getId());
        logger.debug("Flushed TimeSheet object id = {}", tsMerged.getId());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public TimeSheet findForDateAndEmployee(Calendar date, Integer employeeId) {
        Query query = entityManager
                .createQuery("select ts from TimeSheet as ts where ts.calDate = :calDate and ts.employee.id = :employeeId");
        query.setParameter("calDate", date);
        query.setParameter("employeeId", employeeId);
        List<TimeSheet> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }

        return result.get(0);
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
        Query query = entityManager.createNativeQuery("select c.caldate caldate, h.id holiday_id, ts.id timesheet_id, SUM(tsd.duration), tsd.act_type "
                + "from calendar c "
                + "left outer join time_sheet as ts on ts.emp_id = :employeeId and ts.caldate=c.caldate "
                + "left outer join holiday h on c.caldate=h.caldate and (h.region is null or h.region=:region) "
                + "left outer join time_sheet_detail as tsd on ts.id=tsd.time_sheet_id "
                + "where c.year=:yearPar and c.month=:monthPar "
                + "group by c.caldate, h.id, ts.id, tsd.act_type "
                + "order by c.calDate asc");

        query.setParameter("yearPar", year);
        query.setParameter("monthPar", month);
        query.setParameter("region", region);
        query.setParameter("employeeId", employee.getId());
        ArrayList result = (ArrayList) query.getResultList();

        List<DayTimeSheet> dayTSList = new ArrayList<DayTimeSheet>();


        if (!result.isEmpty()) {


            Timestamp calDate;
            Boolean holiday;
            Integer act_type;
            BigDecimal duration;
            Set<Integer> set = new HashSet<Integer>();


            HashMap<Long, DayTimeSheet> map = new HashMap<Long, DayTimeSheet>();
            for (int i = 0; i < result.size(); i++) {
                if (set.contains(i)) {
                    break;
                }
                Object[] item = (Object[]) result.get(i);
                //дата в месяце
                calDate = new Timestamp(((Date) item[0]).getTime());
                //если айдишник из таблицы календарь есть то это выходной
                holiday = new Boolean(item[1] != null);
                //айдишник в ts. нужен нам, чтобы отчет за один день суммировать
                Integer tsId = item[2] != null ? ((BigDecimal) item[2]).intValue() : null;
                //время за каждую деятельность(может быть несколько за один день)
                duration = item[3] != null ? ((BigDecimal) item[3]) : null;
                //по этому полю определяем отпуск\отгул и т.п.
                act_type = item[4] != null ? ((Integer) item[4]) : null;

                if (!map.containsKey(calDate.getTime())) {
                    DayTimeSheet ds = new DayTimeSheet(calDate, holiday, tsId, act_type, duration, employee);
                    ds.setTimeSheetDAO(this);
                    map.put(calDate.getTime(), ds);
                } else {
                    DayTimeSheet dts = map.get(calDate.getTime());
                    if (duration != null && (TimeSheetConstans.DETAIL_TYPE_OUTPROJECT.equals(act_type)
                            || TimeSheetConstans.DETAIL_TYPE_PRESALE.equals(act_type)
                            || TimeSheetConstans.DETAIL_TYPE_PROJECT.equals(act_type))) {
                        dts.setDuration(dts.getDuration().add(duration));
                    }

                }

            }
            for (DayTimeSheet val : map.values()) {
                dayTSList.add(val);
            }

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
        Query query = entityManager.createQuery(""
                + "select ts "
                + "from TimeSheet as ts "
                + "where ts.calDate <:calDate "
                + "and ts.employee.id = :employeeId "
                + "order by ts.calDate desc");
        query.setParameter("calDate", date);
        query.setParameter("employeeId", employeeId);
        List<TimeSheet> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
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
                + "order by ts.calDate asc");
        query.setParameter("calDate", nextDate);
        query.setParameter("employeeId", employeeId);
        List<TimeSheet> result = query.getResultList();
        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    public TimeSheet find(Integer id) {
        return entityManager.find(TimeSheet.class, id);
    }

    @Transactional
    public void delete(TimeSheet timeSheet) {
        entityManager.remove(timeSheet);
    }
}