package com.aplana.timesheet.dao;

import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import com.aplana.timesheet.util.TimeSheetConstans;
import com.aplana.timesheet.util.report.Report7Period;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class JasperReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportDAO.class);

    private DecimalFormat doubleFormat = new DecimalFormat("#.##");

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport01Data(Report01 report) {
        String workDaySeparator;
        if (OverTimeCategory.Holiday.equals(report.getCategory())) {
            workDaySeparator = "and h.calDate is not null ";
        } else if (OverTimeCategory.Simple.equals(report.getCategory())) {
            workDaySeparator = "and h.calDate is null ";
        } else {
            workDaySeparator = "";
        }
        boolean hasRegion;
        String regionClause;
        String regionClause2;
        if (report.hasRegions() && !report.isAllRegions()) {
            regionClause = "em.region.id in :regionIds and ";
            regionClause2 = "region.id in :regionIds and ";
            hasRegion = true;
        } else {
            regionClause = "";
            regionClause2 = "";
            hasRegion = false;
        }
        String divisionWhere;
        // Самый лучший билдер запросов(тут так принято)
        if (report.getDivisionId().equals(0)) {
            divisionWhere = "";
        } else {
            divisionWhere = "em.division.id = :divisionId and ";
        }
        Query query = entityManager.createQuery(
                "select em.id, em.name, ts.calDate.calDate, cast('' as string), " +
                        "sum(td.duration)-8, sum(td.duration), h.id, h.region.id, " +
                        "(case when h is not null then (case when project is not null then project.name else cast('Внепроектная деятельность' as string) end) else cast('%NO_GROUPING%' as string) end), " +
                        "(case when h is not null then td.duration else cast(-1 as float) end) " +
                        ", region.name " +
                        "from TimeSheetDetail td " +
                        "inner join td.timeSheet ts " +
                        "inner join ts.employee em " +
                        "inner join em.region as region " +
                        "left outer join ts.calDate.holidays h " +
                        "left outer join td.project project " +
                        "where " +
                        divisionWhere +
                        // В этом отчете учитываются только следующие виды деятельности "Проектная", "Пресейловая", "Внепроектная" (соответсвенно)
                        "td.actType.id in (12, 13, 14) and " +
                        regionClause2 +
                        "ts.calDate.calDate between :beginDate and :endDate " +
                        "and ((h.region.id is null) or (h.region.id=region.id)) " +
                        workDaySeparator +
                        "group by em.id, em.name, ts.calDate.calDate, h, h.region.id, 9, 10, region.name " +
                        "having (sum(td.duration) > 8) or (h is not null) " +
                        "order by em.name, h.id desc, ts.calDate.calDate");

        if (hasRegion) {
            query.setParameter("regionIds", report.getRegionIds());
        }
        if (!report.getDivisionId().equals(0)) {
            query.setParameter("divisionId", report.getDivisionId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        // Запрос достанет для сотрудников наименования проектов по датам
        Query projQuery = entityManager.createQuery("select em.id, ts.calDate.calDate, td.project.name " +
                "from TimeSheetDetail td " +
                "inner join td.timeSheet ts " +
                "inner join ts.employee em " +
                "where " + divisionWhere +
                regionClause +
                "ts.calDate.calDate between :beginDate and :endDate ");

        if (hasRegion)
            projQuery.setParameter("regionIds", report.getRegionIds());
        if (!report.getDivisionId().equals(0)) {
            projQuery.setParameter("divisionId", report.getDivisionId());
        }
        projQuery.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        projQuery.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List projResultList = projQuery.getResultList();

        String projNames = "";

        List<String> projNamesList;

        // Пробежим весь список отчетов и заполним в них списки проектов, над которыми работали сотрудники
        for (Iterator iterator = resultList.iterator(); iterator.hasNext(); ) {
            Object[] next = (Object[]) iterator.next();

            if (next[6] != null)
                continue;

            Integer emplId = (Integer) next[0];
            Timestamp date = (Timestamp) next[2];

            projNamesList = new ArrayList<String>();

            for (Iterator iterator1 = projResultList.iterator(); iterator1.hasNext(); ) {
                Object[] o = (Object[]) iterator1.next();

                if (o[0].equals(emplId) && o[1].equals(date)) {
                    if (!projNamesList.contains(o[2])) {
                        projNamesList.add((String) o[2]);
                    }
                }
            }

            projNames = "";

            for (Iterator<String> namesIterator = projNamesList.iterator(); namesIterator.hasNext(); ) {
                String nextName = namesIterator.next();

                projNames += nextName;
                if (namesIterator.hasNext())
                    projNames += ", ";
            }

            next[3] = projNames;

        }

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"id", "name", "caldate", "projnames", "overtime", "duration", "holiday", "region", "projdetail", "durationdetail", "region_name"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    /**
     * Просто проверяет пустое ли условие. Использовал для определения нужно
     * ли добавлять значение для той или иной переменной в запросе
     *
     * @param clause условие используемое запросе
     * @return true - условие заполненино, false - условие пустое
     */
    private boolean hasParameter(String clause) {
        boolean val;
        if (clause != null && !clause.isEmpty()) {
            val = true;
        } else {
            val = false;
        }

        return val;
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport02Data(Report02 report) {
        Query query = null;
        boolean hasProject;

        if (report.getProjectId() != null && report.getProjectId() != 0) {
            hasProject = true;
        } else {
            hasProject = false;
        }
        String regionClause;
        if (report.hasRegions() && !report.isAllRegions()) {
            regionClause = "empl.region.id in :regionIds AND ";
        } else {
            regionClause = "";
        }

        String divisionClause;
        if (report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0) {
            divisionClause = "d.id=:emplDivisionId AND ";
        } else {
            divisionClause = "";
        }

        String employeeClause;
        if (report.getEmployeeId() != null && report.getEmployeeId() != 0) {
            employeeClause = "empl.id=:emplId AND ";
        } else {
            employeeClause = "";
        }

        if (hasProject) {
            // Выборка по конкретному проекту
            query = entityManager.createQuery("SELECT empl.name, d.name, p.name, pt.cqId, sum(tsd.duration), " +
                    "(case when h is null then 0 " +
                    "else " +
                    "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                    "else 1 end end),  max(h.region.id) " +
                    "FROM TimeSheetDetail tsd " +
                    "join tsd.timeSheet ts " +
                    "join ts.employee empl " +
                    "join ts.calDate c " +
                    "join empl.division d " +
                    "join tsd.project p " +
                    "left outer  join tsd.projectTask as pt " +
                    "left outer join c.holidays h " +
                    "WHERE " +
                    "tsd.duration > 0 AND " +
                    "tsd.project.id=:projectId AND " +
                    employeeClause +
                    divisionClause +
                    regionClause +
                    "c.calDate between :beginDate AND :endDate " +
                    "GROUP BY empl.name, d.name, p.name, pt.cqId, 6 " +
                    "ORDER BY empl.name, p.name, pt.cqId ");

            query.setParameter("projectId", report.getProjectId());

        } else {
            if (report.getFilterProjects()) {
                // Выборка по всем проектам центра
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, pt.cqId, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), max(h.region.id) " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "join p.divisions dp " +
                        "left outer join tsd.projectTask as pt " +
                        "left outer join c.holidays h " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        "dp.id=:divisionId AND " +
                        employeeClause +
                        divisionClause +
                        regionClause +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, pt.cqId, 6 " +
                        "ORDER BY empl.name, p.name, pt.cqId ");

                query.setParameter("divisionId", report.getDivisionId());

            } else {

                // Выборка по всем проектам всех центров
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, pt.cqId, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), max(h.region.id) " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "left outer join c.holidays h " +
                        "left outer join tsd.projectTask as pt " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        employeeClause +
                        divisionClause +
                        regionClause +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, pt.cqId, 6 " +
                        "ORDER BY empl.name, p.name, pt.cqId ");
            }
        }
        if (hasParameter(regionClause))
            query.setParameter("regionIds", report.getRegionIds());
        if (hasParameter(employeeClause))
            query.setParameter("emplId", report.getEmployeeId());
        if (hasParameter(divisionClause))
            query.setParameter("emplDivisionId", report.getEmplDivisionId());


        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();


        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"name", "empldivision", "project",
                    "taskname", "duration", "holiday", "region"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport03Data(Report03 report) {

        Query query = null;
        boolean hasProject;
        boolean hasEmployee;
        String employeeClause;
        boolean hasEmployeeDivision;
        String employeeDivisionClause;
        boolean hasRegion;
        String regionClause;

        if (report.hasRegions() && !report.isAllRegions()) {
            hasEmployee = true;
            employeeClause = "empl.id=:emplId AND ";
        } else {
            hasEmployee = false;
            employeeClause = "";
        }

        if (report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0) {
            hasEmployeeDivision = true;
            employeeDivisionClause = "d.id=:emplDivisionId AND ";
        } else {
            hasEmployeeDivision = false;
            employeeDivisionClause = "";
        }

        if (report.getEmployeeId() != null && report.getEmployeeId() != 0) {
            hasRegion = true;
            regionClause = "empl.region.id in :regionIds AND ";
        } else {
            hasRegion = false;
            regionClause = "";
        }

        if (report.getProjectId() != null && report.getProjectId() != 0) {
            hasProject = true;
        } else {
            hasProject = false;
        }

        if (hasProject) {

            // Выборка по конкретному проекту
            query = entityManager.createQuery("SELECT empl.name, d.name, p.name, pt.cqId, c.calDate, sum(tsd.duration), " +
                    "(case when h is null then 0 " +
                    "else " +
                    "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                    "else 1 end end), h.region.id " +
                    "FROM TimeSheetDetail tsd " +
                    "join tsd.timeSheet ts " +
                    "join ts.employee empl " +
                    "join ts.calDate c " +
                    "join empl.division d " +
                    "join tsd.project p " +
                    "left outer  join tsd.projectTask as pt " +
                    "left outer join c.holidays h " +
                    "WHERE " +
                    "tsd.duration > 0 AND " +
                    "tsd.project.id=:projectId AND " +
                    employeeClause +
                    employeeDivisionClause +
                    regionClause +
                    "c.calDate between :beginDate AND :endDate " +
                    "GROUP BY empl.name, d.name, p.name, pt.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                    "ORDER BY empl.name, p.name, pt.cqId, c.calDate ");

            query.setParameter("projectId", report.getProjectId());


        } else {
            if (report.getFilterProjects()) {
                // Выборка по всем проектам центра
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, pt.cqId, c.calDate, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), h.region.id " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "join p.divisions dp " +
                        "left outer join c.holidays h " +
                        "left outer  join tsd.projectTask as pt " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        "dp.id=:divisionId AND " +
                        employeeClause +
                        employeeDivisionClause +
                        regionClause +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, pt.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                        "ORDER BY empl.name, p.name, pt.cqId, c.calDate ");

                query.setParameter("divisionId", report.getDivisionId());

            } else {

                // Выборка по всем проектам всех центров
                query = entityManager.createQuery("SELECT empl.name, d.name, p.name, pt.cqId, c.calDate, sum(tsd.duration), " +
                        "(case when h is null then 0 " +
                        "else " +
                        "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                        "else 1 end end), h.region.id " +
                        "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "left outer join c.holidays h " +
                        "left outer  join tsd.projectTask as pt " +
                        "WHERE " +
                        "tsd.duration > 0 AND " +
                        employeeClause +
                        employeeDivisionClause +
                        regionClause +
                        "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, pt.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                        "ORDER BY empl.name, p.name, pt.cqId, c.calDate ");
            }
        }
        if (hasParameter(regionClause))
            query.setParameter("regionIds", report.getRegionIds());
        if (hasParameter(employeeClause))
            query.setParameter("emplId", report.getEmployeeId());
        if (hasParameter(employeeDivisionClause))
            query.setParameter("emplDivisionId", report.getEmplDivisionId());

        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"name", "empldivision", "project", "taskname", "caldate", "duration", "holiday", "region"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }

    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport04Data(Report04 report) {

        String regionClause;
        if (report.hasRegions() && !report.isAllRegions()) {
            regionClause = "and emp.region.id in :regionIds ";
        } else {
            regionClause = "";
        }

        String divisionClause;
        if (report.getDivisionId() != null && report.getDivisionId() != 0) {
            divisionClause = "and emp.division.id = :divisionId ";
        } else {
            divisionClause = "";
        }

        Query query = entityManager.createQuery("select c.calDate, emp.name, emp.region.name " +
                "from Calendar c, Employee emp " +
                "where c.calDate between :beginDate and " +
                "(CASE  " +
                "WHEN emp.endDate IS NOT NULL THEN " +
                "CASE  " +
                "WHEN emp.endDate < :endDate THEN emp.endDate " +
                "ELSE :endDate " +
                "END " +
                "ELSE :endDate " +
                "END) " +
                divisionClause +
                regionClause +
                "and (emp.notToSync = false) " +
                "and emp.manager.id is not null " +
                "and c.calDate not in (select ts.calDate.calDate " + //note: date don't have report
                "from TimeSheet ts " +
                "where ts.employee.id = emp.id) " +
                "and c.calDate not in (select h.calDate.calDate " +  //note: date is not holiday in employee's region
                "from Holiday h " +
                "where h.region.id is null " +
                "or  h.region.id = emp.region.id ) " +
                "and (emp.startDate <= c.calDate) " +
                "order by emp.name, c.calDate");

        if (hasParameter(regionClause)) {
            query.setParameter("regionIds", report.getRegionIds());
        }
        if (hasParameter(divisionClause)) {
            query.setParameter("divisionId", report.getDivisionId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"date", "name", "region_name"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport05Data(Report05 report) {

        String regionClause;
        if (report.hasRegions() && !report.isAllRegions()) {
            regionClause = "td.timeSheet.employee.region.id in :regionIds and ";
        } else {
            regionClause = "";
        }

        String employeeClause;
        if (report.getEmployeeId() != null && report.getEmployeeId() != 0) {
            employeeClause = "em.id = :employeeId and ";
        } else {
            employeeClause = "";
        }

        String divisionClause;
        if (report.getDivisionId() != null && report.getDivisionId() != 0) {
            divisionClause = "td.timeSheet.employee.division.id = :divisionId and ";
        } else {
            divisionClause = "";
        }

        Query query = entityManager.createQuery(
                "select ts.calDate.calDate, em.name, td.actType.value, td.project.name, td.actCat.value, em.job.name, " +
                        "COALESCE(pt.cqId, ''), td.duration, td.description, td.problem " +
                        "from TimeSheetDetail td " +
                        "left outer join td.projectTask as pt " +
                        "inner join td.timeSheet ts " +
                        "inner join ts.employee em " +
                        "where " +
                        divisionClause +
                        regionClause +
                        employeeClause +
                        "td.timeSheet.calDate.calDate between :beginDate and :endDate " +
                        "order by td.timeSheet.employee.name,td.timeSheet.calDate.calDate");

        if (hasParameter(regionClause)) {
            query.setParameter("regionIds", report.getRegionIds());
        }
        if (hasParameter(employeeClause)) {
            query.setParameter("employeeId", report.getEmployeeId());
        }
        if (hasParameter(divisionClause)) {
            query.setParameter("divisionId", report.getDivisionId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));


        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"calDate", "name", "value", "pctName", "actType",
                    "pctRole", "taskName", "duration", "description", "problem"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport06Data(Report06 report) {

        String regionClause;
        if (report.hasRegions() && !report.isAllRegions()) {
            regionClause = "tsd.timeSheet.employee.region.id in :regionIds AND ";
        } else {
            regionClause = "";
        }

        Query query = entityManager.createQuery(
                "SELECT sum(tsd.duration), act.projectRole.name, tsd.timeSheet.employee.name, tsd.actCat.value " +
                        "FROM TimeSheetDetail tsd, AvailableActivityCategory act " +
                        "WHERE tsd.project.id=:projectId AND tsd.actType=act.actType AND tsd.actCat=act.actCat AND " +
                        regionClause +
                        "tsd.timeSheet.calDate.calDate between :beginDate AND :endDate AND act.projectRole=tsd.timeSheet.employee.job " +
                        "GROUP BY act.projectRole.name, tsd.timeSheet.employee.name, tsd.actCat.value " +
                        "ORDER BY tsd.timeSheet.employee.name asc");

        if (hasParameter(regionClause))
            query.setParameter("regionIds", report.getRegionIds());
        query.setParameter("projectId", report.getProjectId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            String[] fields = new String[]{"duration", "act_type", "name", "act_cat"};
            return new HibernateQueryResultDataSource(resultList, fields);
        } else {
            return null;
        }

    }

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReport07Data(Report07 report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            FileInputStream propertiesFile = new FileInputStream(TimeSheetConstans.PROPERTY_PATH);
            Properties tsProperties = new Properties();
            tsProperties.load(propertiesFile);
            ArrayList rolesDev = this.readRolesFromString(tsProperties.getProperty("project.role.developer"));
            ArrayList rolesRp = this.readRolesFromString(tsProperties.getProperty("project.role.rp"));
            ArrayList rolesTest = this.readRolesFromString(tsProperties.getProperty("project.role.test"));
            ArrayList rolesAnalyst = this.readRolesFromString(tsProperties.getProperty("project.role.analyst"));
            ArrayList rolesSystem = this.readRolesFromString(tsProperties.getProperty("project.role.system"));
            Date start = sdf.parse(report.getBeginDate());
            Date end = sdf.parse(report.getEndDate());
            HashMap<String, Object> itogo = new HashMap<String, Object>();
            Query query1;
            if (report.getFilterDivisionOwner()) {
                query1 = this.getReport07Query1(start, end, report.getDivisionEmployee(), report.getDivisionOwner());
            } else {
                query1 = this.getReport07Query1(start, end, report.getDivisionEmployee());
            }
            List dataSource = new ArrayList();
            Double temp = null;
            HashMap<String, HashMap<String, Double>> periodsDuration = new HashMap();
            HashMap<String, Double> durations = new HashMap();
            Report7Period itogoPeriod = new Report7Period("Итого");
            for (Iterator i = query1.getResultList().iterator(); i.hasNext(); ) {
                Object[] projects = (Object[]) i.next();
                Integer projectId = (Integer) projects[0];
                Integer projectDivision = (Integer) projects[2];
                Date periodStart = start;
                String projectName = null;
                Integer periodNumber = null;
                Date periodEnd = periodStart;
                Double periodByRP = Double.valueOf(0);
                Double periodByAnalyst = Double.valueOf(0);
                Double periodByDev = Double.valueOf(0);
                Double periodBySystem = Double.valueOf(0);
                Double periodByTest = Double.valueOf(0);
                Double periodByCenterOwner = Double.valueOf(0);
                Double periodByCenterEtc = Double.valueOf(0);
                HashMap<String, Double> periodRegions = new HashMap<String, Double>();
                for (periodNumber = 1; periodEnd.before(end); periodNumber = periodNumber + 1) {
                    periodEnd = DateUtils.addMonths(periodStart, report.getPeriodType());
                    if (periodEnd.after(end)) {
                        periodEnd = end;
                    }
                    Report7Period period = new Report7Period(periodNumber, periodStart, end, report.getPeriodType());
                    Query query2 = this.getReport07Query2(periodStart, periodEnd, projectId, report.getDivisionEmployee());
                    Double durationByRP = Double.valueOf(0);
                    Double durationByAnalyst = Double.valueOf(0);
                    Double durationByDev = Double.valueOf(0);
                    Double durationBySystem = Double.valueOf(0);
                    Double durationByTest = Double.valueOf(0);
                    HashMap<String, Double> regions = new HashMap<String, Double>();
                    Double durationByCenterOwner = Double.valueOf(0);
                    Double durationByCenterEtc = Double.valueOf(0);
                    Double durationPeriod = Double.valueOf(0);
                    for (Iterator j = query2.getResultList().iterator(); j.hasNext(); ) {
                        Object[] values = (Object[]) j.next();
                        String projectRegion = (String) values[5];
                        Double pDuration = (Double) values[3];
                        Integer job = (Integer) values[2];
                        projectName = String.valueOf(values[4]);
                        Integer projectEmpDivision = (Integer) values[1];

                        if (rolesRp.indexOf(job) != -1) {
                            durationByRP = durationByRP + pDuration;
                        }
                        if (rolesAnalyst.indexOf(job) != -1) {
                            durationByAnalyst = durationByAnalyst + pDuration;
                        }
                        if (rolesDev.indexOf(job) != -1) {
                            durationByDev = durationByDev + pDuration;
                        }
                        if (rolesSystem.indexOf(job) != -1) {
                            durationBySystem = durationBySystem + pDuration;
                        }
                        if (rolesTest.indexOf(job) != -1) {
                            durationByTest = durationByTest + pDuration;
                        }
                        durationPeriod += pDuration;

                        if (regions.get(projectRegion) == null) {
                            regions.put(projectRegion, pDuration);
                        } else {
                            regions.put(projectRegion, regions.get(projectRegion) + pDuration);
                        }
                        if (durations.get(projectName) == null) {
                            durations.put(projectName, pDuration);
                        } else {
                            durations.put(projectName, pDuration + durations.get(projectName));
                        }

                        // Подсчёт относительных затрат
                        if (projectDivision.equals(projectEmpDivision)) {
                            durationByCenterOwner = durationByCenterOwner + pDuration;
                        } else {
                            durationByCenterEtc = durationByCenterEtc + pDuration;
                        }
                    }

                    if (projectName != null) {
                        // по должностям
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Руководитель проекта ч. (%)",
                                this.report7GenerateValue(durationByRP, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Аналитик ч. (%)",
                                this.report7GenerateValue(durationByAnalyst, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Разработчик ч. (%)",
                                this.report7GenerateValue(durationByDev, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Системный инженер ч. (%)",
                                this.report7GenerateValue(durationBySystem, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Тестирование ч. (%)",
                                this.report7GenerateValue(durationByTest, durationPeriod)));

                        // Подсчитаем к итоговому периоду
                        periodByAnalyst += durationByAnalyst;
                        periodByRP += durationByRP;
                        periodByDev += durationByDev;
                        periodBySystem += durationBySystem;
                        periodByTest += durationByTest;

                        // Подсчёт по регионам
                        for (Map.Entry<String, Double> region : regions.entrySet()) {
                            dataSource.add(this.report7DataSourceRow(period, projectName, "По Регионам", (String) region.getKey().concat(" ч. (%)"),
                                    this.report7GenerateValue(region.getValue(), durationPeriod)));
                            // Посчитаем для итого
                            if (periodRegions.get(region.getKey().concat(" ч. (%)")) == null) {
                                periodRegions.put(region.getKey().concat(" ч. (%)"), region.getValue());
                            } else {
                                periodRegions.put(region.getKey().concat(" ч. (%)"), region.getValue() + periodRegions.get(region.getKey().concat(" ч. (%)")));
                            }
                        }
                        if (durationPeriod > 0) {
                            dataSource.add(this.report7DataSourceRow(period, projectName, "Трудозатраты", "Общие (ч.)", doubleFormat.format(durationPeriod)));
                        }

                        if (periodsDuration.get(period.getNumber().toString()) == null) {
                            HashMap<String, Double> value = new HashMap<String, Double>();
                            value.put(projectName, durationPeriod);
                            periodsDuration.put(period.getNumber().toString(), value);
                        } else {
                            periodsDuration.get(period.getNumber().toString()).put(projectName, durationPeriod);
                        }

                        // Относительные затраты по центрам
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Центрам", "Центр владельца проекта ч. (%)",
                                this.report7GenerateValue(durationByCenterOwner, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Центрам", "Другие центры ч. (%)",
                                this.report7GenerateValue(durationByCenterEtc, durationPeriod)));
                        periodByCenterEtc += durationByCenterEtc;
                        periodByCenterOwner += durationByCenterOwner;
                        regions.clear();
                    }

                    //durations.put(projectName, Double.valueOf(0));
                    periodStart = periodEnd;    // Обязательно
                }
                // Вывод итого в dataSource
                if (projectName != null) {
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Руководитель проекта ч. (%)",
                            this.report7GenerateValue(periodByRP, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Аналитик ч. (%)",
                            this.report7GenerateValue(periodByAnalyst, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Разработчик ч. (%)",
                            this.report7GenerateValue(periodByDev, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Системный инженер ч. (%)",
                            this.report7GenerateValue(periodBySystem, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Тестирование ч. (%)",
                            this.report7GenerateValue(periodByTest, durations.get(projectName))));

                    // Относительные затраты по центрам
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Центрам", "Центр владельца проекта ч. (%)",
                            this.report7GenerateValue(periodByCenterOwner, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Центрам", "Другие центры ч. (%)",
                            this.report7GenerateValue(periodByCenterEtc, durations.get(projectName))));

                    // Трудозатраты
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "Трудозатраты", "Общие (ч.)", doubleFormat.format(durations.get(projectName))));

                    // По регионам
                    for (Map.Entry<String, Double> region : periodRegions.entrySet()) {
                        dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Регионам", (String) region.getKey(),
                                this.report7GenerateValue(region.getValue(), durations.get(projectName))));
                    }
                }
            }
            for (Map.Entry<String, HashMap<String, Double>> period : periodsDuration.entrySet()) {
                Double sum = Double.valueOf(0);
                for (Map.Entry<String, Double> project : period.getValue().entrySet()) {
                    sum = sum + project.getValue();
                }
                for (Map.Entry<String, Double> project : period.getValue().entrySet()) {
                    if (sum > 0 && project.getValue() > 0) {
                        temp = project.getValue() / sum * 100;
                        dataSource.add(this.report7DataSourceRow(itogoPeriod, project.getKey(), "Трудозатраты", "Относительные (%)",
                                doubleFormat.format(temp).concat("%")));
                    }
                }
            }

            Double sum = Double.valueOf(0);
            for (Map.Entry<String, Double> period : durations.entrySet()) {
                sum += period.getValue();
            }

            for (Map.Entry<String, Double> period : durations.entrySet()) {
                temp = period.getValue() / sum * 100;
                dataSource.add(this.report7DataSourceRow(itogoPeriod, period.getKey(), "Трудозатраты", "Относительные (%)",
                        doubleFormat.format(temp).concat("%")));
            }
            String[] fields = {"period", "name", "group", "type", "value"};
            return new HibernateQueryResultDataSource(dataSource, fields);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String Report7PeriodName(Integer type, Date d) throws Exception {
        if (type == 1) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM.YYYY");
            return sdf.format(d);
        } else if (type == 3) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            Integer number = new Integer(sdf.format(d));
            SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY");
            if (number > 0 && number < 4) {
                return "1-ый квартал " + sdf2.format(d);
            } else if (number > 2 && number < 7) {
                return "2-ой квартал " + sdf2.format(d);
            } else if (number > 5 && number < 8) {
                return "3-ий квартал " + sdf2.format(d);
            } else if (number > 7) {
                return "4-ый квартал " + sdf2.format(d);
            }
        } else if (type == 6) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            SimpleDateFormat sdf2 = new SimpleDateFormat("YYYY");
            Integer number = new Integer(sdf.format(d));
            if (number > 0 && number < 7) {
                return "1-ый квартал " + sdf2.format(d);
            } else {
                return "2-ой квартал" + sdf2.format(d);
            }
        } else if (type == 12) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
            return sdf.format(d) + " г.";
        }
        throw new Exception();
    }

    private String report7GenerateValue(Double projectDuration, Double periodDuration) {
        if (projectDuration.isNaN() || projectDuration == null)
            projectDuration = Double.valueOf(0);
        if (projectDuration.isNaN() || projectDuration == null)
            periodDuration = Double.valueOf(0);
        Double result;
        if (periodDuration > 0) {
            result = projectDuration / periodDuration * 100;
        } else {
            result = Double.valueOf(0);
        }
        return doubleFormat.format(projectDuration) + " (" + doubleFormat.format(result) + "%)";
    }

    private Object[] report7DataSourceRow(Report7Period period, String name, String group, String type, String value) {
        ArrayList list = new ArrayList();
        list.add(period);
        list.add(name);
        list.add(group);
        list.add(type);
        if (value.equals("0 (0%)")) {
            list.add("-");
        } else {
            list.add(value);
        }
        return list.toArray();
    }

    private ArrayList readRolesFromString(String s) {
        String[] roles = s.split(",");
        ArrayList role = new ArrayList();
        for (Integer i = 0; i < roles.length; i = i + 1) {
            role.add(Integer.parseInt(roles[i]));
        }
        return role;
    }

    private Query getReport07Query1(Date periodStart, Date periodEnd, Integer divisionEmployeeId) {
        Query query;
        if (divisionEmployeeId != 0) {
            query = entityManager.createQuery("SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                    "LEFT JOIN project.timeSheetDetail tsd " +
                    "JOIN tsd.timeSheet ts " +
                    "JOIN ts.employee emp " +
                    "JOIN project.manager as manager " +
                    "JOIN manager.division as md " +
                    "WHERE emp.division.id = :divisionEmployeeId " +
                    "AND ts.calDate.calDate between :beginDate AND :endDate " +
                    "GROUP BY 1, 3 " +
                    "ORDER BY 2 DESC ");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
            query.setParameter("divisionEmployeeId", divisionEmployeeId);
        } else {
            query = entityManager.createQuery("SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                    "LEFT JOIN project.timeSheetDetail tsd " +
                    "JOIN  tsd.timeSheet ts " +
                    "JOIN ts.employee emp " +
                    "JOIN project.manager as manager " +
                    "JOIN manager.division as md " +
                    "WHERE ts.calDate.calDate between :beginDate AND :endDate " +
                    "GROUP BY 1, 3 " +
                    "ORDER BY 2 DESC ");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
        }
        return query;
    }

    private Query getReport07Query1(Date periodStart, Date periodEnd, Integer divisionEmployeeId, Integer divisionOwnerId) {
        Query query;
        if (divisionEmployeeId != 0) {
            query = entityManager.createQuery(
                    "SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                            "LEFT JOIN project.timeSheetDetail tsd " +
                            "JOIN project.divisions division " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee emp " +
                            "JOIN project.manager as manager " +
                            "JOIN manager.division as md " +
                            "WHERE emp.division.id = :divisionEmployeeId AND division.id = :divisionOwnerId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3 " +
                            "ORDER BY 2 DESC");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
            query.setParameter("divisionEmployeeId", divisionEmployeeId);
            query.setParameter("divisionOwnerId", divisionOwnerId);
        } else {
            query = entityManager.createQuery(
                    "SELECT project.id as id, SUM (tsd.duration) as allduration, md.id FROM Project project " +
                            "LEFT JOIN project.timeSheetDetail tsd " +
                            "JOIN project.divisions division " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee emp " +
                            "JOIN project.manager as manager " +
                            "JOIN manager.division as md " +
                            "WHERE division.id = :divisionOwnerId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3 " +
                            "ORDER BY 2 DESC");
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
            query.setParameter("divisionOwnerId", divisionOwnerId);
        }
        return query;
    }

    private Query getReport07Query2(Date periodStart, Date periodEnd, Integer projectId, Integer divisionEmployeeId) {
        Query query;
        if (divisionEmployeeId == 0) {
            query = entityManager.createQuery(
                    "SELECT emp.region.id as region, emp.division.id as division, emp.job.id as job, SUM(tsd.duration) as duration, p.name, emp.region.name " +
                            "FROM Project p " +
                            "LEFT JOIN p.timeSheetDetail tsd " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee as emp " +
                            "WHERE p.id = :projectId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3, 2, 5, 6"
            );
            query.setParameter("projectId", projectId);
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
        } else {
            query = entityManager.createQuery(
                    "SELECT emp.region.id as region, emp.division.id as division, emp.job.id as job, SUM(tsd.duration) as duration, p.name, emp.region.name " +
                            "FROM Project p " +
                            "LEFT JOIN p.timeSheetDetail tsd " +
                            "JOIN tsd.timeSheet ts " +
                            "JOIN ts.employee as emp " +
                            "WHERE p.id = :projectId AND emp.division.id = :divisionEmployeeId " +
                            "AND ts.calDate.calDate between :beginDate AND :endDate " +
                            "GROUP BY 1, 3, 2, 5, 6"
            );
            query.setParameter("projectId", projectId);
            query.setParameter("divisionEmployeeId", divisionEmployeeId);
            query.setParameter("beginDate", new Timestamp(periodStart.getTime()));
            query.setParameter("endDate", new Timestamp(periodEnd.getTime()));
        }
        return query;
    }
}
