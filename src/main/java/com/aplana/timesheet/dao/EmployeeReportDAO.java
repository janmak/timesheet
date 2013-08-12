package com.aplana.timesheet.dao;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/* класс для запросов для таблички детализации месячных данных по проектам */
@Repository
public class EmployeeReportDAO {

    @PersistenceContext
    private EntityManager entityManager;

    /* получаем список типов активности и суммы времени по каждому*/
    public List<Object[]> getEmployeeMonthData(Integer employee_id, Integer year, Integer month) {
        final Query query = entityManager.createQuery(
                " select min(ac)," +
                "        p," +
                "        sum(tsd.duration)" +
                " from Calendar c" +
                " inner join c.timeSheets ts" +
                " inner join ts.timeSheetDetails tsd" +
                " inner join tsd.actType ac" +
                " left outer join tsd.project p" +
                " where c.year = :year and c.month = :month" +
                " and ts.employee.id = :employee" +
                " group by p.id" +
                " order by 1 asc, 2 desc"
        ).setParameter("year", year).setParameter("month", month).setParameter("employee", employee_id);

        return query.getResultList();
    }
}
