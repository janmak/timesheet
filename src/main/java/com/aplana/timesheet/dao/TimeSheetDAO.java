package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.DayTimeSheet;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.TimeSheet;
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
        //logger.debug("findForDateAndEmployee List<TimeSheet> result size = {}", result.size());

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

        /* Query query = entityManager
               .createQuery("select c.calDate, h.id, h1.id, ts.id from Calendar as c " +
                       "left outer join c.timeSheets as ts with ts.employee.id = :employeeId " +
                       "left outer join c.holidays as h with h.region.id=:region " +
                       "left outer join c.holidays as h1 with h.region is null " +
                       "where c.year=:yearPar and c.month=:monthPar " +
                       "order by c.calDate asc"
               );
        */

        // Я не знаю как написать это на HQL, но на SQL пишется легко и непринужденно.
        Query query = entityManager.createNativeQuery("select c.caldate caldate, h.id holiday_id, ts.id timesheet_id, SUM(tsd.duration), tsd.act_type " +
                "from calendar c " +
                "left outer join time_sheet as ts on ts.emp_id = :employeeId and ts.caldate=c.caldate " +
                "left outer join holiday h on c.caldate=h.caldate and (h.region is null or h.region=:region) " +
                "left outer join time_sheet_detail as tsd on ts.id=tsd.time_sheet_id " +
                "where c.year=:yearPar and c.month=:monthPar " +
                "group by c.caldate, h.id, ts.id, tsd.act_type " +
                "order by c.calDate asc");

        query.setParameter("yearPar", year);
        query.setParameter("monthPar", month);
        query.setParameter("region", region);
        query.setParameter("employeeId", employee.getId());
        ArrayList result = (ArrayList) query.getResultList();

        List<DayTimeSheet> dayTSList = new ArrayList<DayTimeSheet>();


        if (!result.isEmpty()) {


            Timestamp calDate;
            Boolean holiday;
            Integer tsId;
            Integer act_type;
            BigDecimal duration;
            Set<Integer> set = new HashSet<Integer>();


            HashMap<Long, DayTimeSheet> map = new HashMap<Long, DayTimeSheet>();
            for (int i = 0; i < result.size(); i++) {
                if (set.contains(i)) break;
                Object[] item = (Object[]) result.get(i);
                //дата в месяце
                calDate = new Timestamp(((Date) item[0]).getTime());
                //если айдишник из таблицы календарь есть то это выходной
                holiday = new Boolean(item[1] != null);
                //айдишник в ts. нужен нам, чтобы отчет за один день суммировать
                tsId = item[2] != null ? ((BigDecimal) item[2]).intValue() : null;
                //время за каждую деятельность(может быть несколько за один день)
                duration = item[3] != null ? ((BigDecimal) item[3]) : null;
                //по этому полю определяем отпуск\отгул и т.п.
                act_type = item[4] != null ? ((Integer) item[4]) : null;

                if (!map.containsKey(calDate.getTime())) {
                    map.put(calDate.getTime(), new DayTimeSheet(calDate, holiday, tsId, duration != null ? duration : new BigDecimal(0.0), act_type));
                } else {
                    DayTimeSheet dts = map.get(calDate.getTime());
                    if ((act_type == 15) || (act_type == 24)) {
                        //dts.setDuration(dts.getDuration().subtract(duration));
                    } else {
                        if (duration != null)
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
     * Ищет в таблице timesheet запись, соответсвующую сотруднику с
     * идентификатором employeeId и возвращает объект типа TimeSheet.
     *
     * @param employee Идентификатор сотрудника в базе данных.
     * @return List типа TimeSheet, либо null, если объект не найден.
     */
    // (9.8.2012) Внимание. Метод поломаный какой-то
    @SuppressWarnings("unchecked")
    public List<TimeSheet> getReport(Employee employee, Calendar date) {
        List<TimeSheet> report = new ArrayList<TimeSheet>();
        Query query = entityManager.createQuery("select ts from TimeSheet as ts where ts.employee = :employeePar and ts.calDate = :datePar)");
        query.setParameter("employeePar", employee);
        query.setParameter("datePar", date);
        report = ((List<TimeSheet>) query.getResultList());
        logger.debug("TimeSheetDAO getReport {}", report.toString());
        return report;
    }

    /**
     * Возвращает самый последний план относительно date
     *
     * @param date
     * @param employeeId
     * @return отчет
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public TimeSheet findLastTimeSheetBefore(Calendar date, Integer employeeId) {
        Query query = entityManager.createQuery("" +
                "select ts " +
                "from TimeSheet as ts " +
                "where ts.calDate <:calDate " +
                "and ts.employee.id = :employeeId " +
                "order by ts.calDate desc");
        query.setParameter("calDate", date);
        query.setParameter("employeeId", employeeId);
        List<TimeSheet> result = query.getResultList();
        if (result.size() == 0)
            return null;
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
        Query query = entityManager.createQuery("" +
                "select ts " +
                "from TimeSheet as ts " +
                "where ts.calDate = :calDate " +
                "and ts.employee.id = :employeeId " +
                "order by ts.calDate asc");
        query.setParameter("calDate", nextDate);
        query.setParameter("employeeId", employeeId);
        List<TimeSheet> result = query.getResultList();
        if (result.size() == 0)
            return null;
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