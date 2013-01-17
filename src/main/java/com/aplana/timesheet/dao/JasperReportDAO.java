package com.aplana.timesheet.dao;

import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

@Repository
public class JasperReportDAO {

    private static Map<Class, String[]> fieldsMap = new HashMap<Class, String[]>( 6 );

    static {
        //TODO может быть это вынести в сам класс Reports?
        fieldsMap.put( Report01.class, new String[] { "id", "name", "caldate", "projnames", "overtime", "duration",
                "holiday", "region", "projdetail", "durationdetail", "region_name" } );
        fieldsMap.put( Report02.class, new String[] { "name", "empldivision", "project",
                "taskname", "duration", "holiday", "region" } );
        fieldsMap.put( Report03.class, new String[] { "name", "empldivision", "project", "taskname",
                "caldate", "duration", "holiday", "region" } );
        fieldsMap.put( Report04.class, new String[] { "date", "name", "region_name" } );
        fieldsMap.put( Report05.class, new String[] { "calDate", "name", "value", "pctName", "actType",
                "pctRole", "taskName", "duration", "description", "problem" } );
        fieldsMap.put( Report06.class, new String[] { "duration", "act_type", "name", "act_cat" } );
    }

    private static final Logger logger = LoggerFactory.getLogger(JasperReportDAO.class);

    private static final String DIVISION_CLAUSE = "d.id=:emplDivisionId AND ";
    private static final String EMPLOYEE_CLAUSE = "empl.id=:emplId AND ";
    private static final String REGION_CLAUSE   = "empl.region.id in :regionIds AND ";
    private static final String WITHOUT_CLAUSE  = "";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public HibernateQueryResultDataSource getReportData(BaseReport report) {
        List resultList     = getResultList     ( report );

        if (resultList != null && !resultList.isEmpty()) {
            return new HibernateQueryResultDataSource(resultList, fieldsMap.get( report.getClass() ) );
        } else {
            return null;
        }
    }

    private List getResultList( BaseReport baseReport ) {
        //TODO выпилить это, заменить на иерархию классов
        if ( baseReport instanceof Report01 ) {
            Report01 report = ( Report01 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report02 ) {
            Report02 report = ( Report02 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report03 ) {
            Report03 report = ( Report03 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report04 ) {
            Report04 report = ( Report04 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report05 ) {
            Report05 report = ( Report05 ) baseReport;
            return getResultList( report );
        } else if ( baseReport instanceof Report06 ) {
            Report06 report = ( Report06 ) baseReport;
            return getResultList( report );
        }

        throw new IllegalArgumentException();
    }

    private Collection<String> getProjectNamesList( List projResultList, Integer emplId, Timestamp date ) {
        Set<String> projNamesList = new HashSet<String>();

        for ( Object aProjResultList : projResultList ) {
            Object[] o = ( Object[] ) aProjResultList;

            if ( o[ 0 ].equals( emplId ) && o[ 1 ].equals( date ) ) {
                String projName = ( String ) o[ 2 ];
                projNamesList.add( projName );
            }
        }
        return projNamesList;
    }

    //TODO переписать с использование уже готовых библиотек
    private String getStringWithProjectNames( Collection<String> projNamesList ) {
        StringBuffer projNames = new StringBuffer();

        for ( Iterator<String> namesIterator = projNamesList.iterator(); namesIterator.hasNext(); ) {
            String nextName = namesIterator.next();

            projNames.append( nextName );
            if ( namesIterator.hasNext() )
                projNames.append( ", " );
        }
        return projNames.toString();
    }

    private List getProjResultList( Report01 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        // Запрос достанет для сотрудников наименования проектов по датам
        Query projQuery = entityManager.createQuery(
                "select empl.id, ts.calDate.calDate, td.project.name " +
                "from TimeSheetDetail td " +
                    "inner join td.timeSheet ts " +
                    "inner join ts.employee empl " +
                "where empl.division.id = :divisionId and " +
                    (withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE ) +
                    "ts.calDate.calDate between :beginDate and :endDate ");

        if (withRegionClause)
            projQuery.setParameter("regionIds", report.getRegionIds());
        projQuery.setParameter("divisionId", report.getDivisionId());
        projQuery.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        projQuery.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return projQuery.getResultList();
    }

    private List getResultList( Report01 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        String regionClause2 = withRegionClause ? "region.id in :regionIds and " : "";

        String workDaySeparator = "";

        if(OverTimeCategory.Holiday.equals(report.getCategory())) {
            workDaySeparator="and h.calDate is not null ";
        } else if(OverTimeCategory.Simple.equals(report.getCategory())) {
            workDaySeparator="and h.calDate is null ";
        }

        Query query = entityManager.createQuery(
                "select " +
                        "em.id, " +
                        "em.name, " +
                        "ts.calDate.calDate, " +
                        "cast('' as string), " +
                        "sum(td.duration)-8, " +
                        "sum(td.duration), " +
                        "h.id, " +
                        "h.region.id, " +
						"(case when h is not null then " +
                            "(case when project is not null then " +
                                "project.name " +
                            "else cast('Внепроектная деятельность' as string) " +
                            "end) " +
                        "else cast('%NO_GROUPING%' as string) end), " +
                        "(case when h is not null then td.duration else cast(-1 as float) end)," +
                        " region.name " +
                    "from TimeSheetDetail td " +
                    "inner join td.timeSheet ts " +
                    "inner join ts.employee em " +
                    "inner join em.region as region " +
                    "left outer join ts.calDate.holidays h " +
                    "left outer join td.project project " +
                    "where em.division.id = :divisionId " +
                        // В этом отчете учитываются только следующие виды деятельности "Проектная", "Пресейловая", "Внепроектная" (соответсвенно)
                        "and td.actType.id in (12, 13, 14) " +
                        "and " + regionClause2 +
                        "ts.calDate.calDate between :beginDate and :endDate " +
                        "and ((h.region.id is null) or (h.region.id=region.id)) " +
						workDaySeparator +
                    "group by em.id, em.name, ts.calDate.calDate, h, h.region.id, 9, 10, region.name " +
                    "having (sum(td.duration) > 8) or (h is not null) " +
                    "order by em.name, h.id desc, ts.calDate.calDate");

        if (withRegionClause) {
            query.setParameter("regionIds", report.getRegionIds());
		}

        query.setParameter("divisionId", report.getDivisionId())
                .setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ))
                .setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        List resultList = query.getResultList();
        //TODO похоже это нужно вынести в запрос, и не делать этого в Java
        List projResultList = getProjResultList ( report );

        // Пробежим весь список отчетов и заполним в них списки проектов, над которыми работали сотрудники
        for ( Object aResultList : resultList ) {
            Object[] next = ( Object[] ) aResultList;

            if ( next[ 6 ] != null ) continue;

            next[ 3 ] = getStringWithProjectNames(
                    getProjectNamesList( projResultList, ( Integer ) next[ 0 ], ( Timestamp ) next[ 2 ] ) );
        }
        return resultList;
    }

    @Language("HQL")
    private static final String report02QueryString =
                        "SELECT " +
                                "empl.name, " +
                                "d.name, " +
                                "p.name, " +
                                "pt.cqId, " +
                                "sum(tsd.duration), " +
                                "(case when h is null then 0 " +
                                "else " +
                                    "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                                    "else 1 end end), " +
                                "max(h.region.id) " +
                        "FROM TimeSheetDetail tsd " +
                            "join tsd.timeSheet ts " +
                            "join ts.employee empl " +
                            "join ts.calDate c " +
                            "join empl.division d " +
                            "join tsd.project p " +
                            "left outer join tsd.projectTask as pt " +
                            "left outer join c.holidays h " +
                            " %s " +
                        "WHERE " +
                            "tsd.duration > 0 AND " +
                            " %s %s %s %s" +
                            "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, pt.cqId, 6 " +
                        "ORDER BY empl.name, p.name, pt.cqId ";

    private List getResultList( Report02 report ) {
        boolean hasProject = report.getProjectId() != null && report.getProjectId() != 0;

        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0;
        boolean withEmployeeClasue = report.getEmployeeId()     != null && report.getEmployeeId    () != 0;

        Query query;
        if (hasProject) {
            // Выборка по конкретному проекту
            query = entityManager.createQuery( String.format( report02QueryString,
                    "",
                    "tsd.project.id=:projectId AND ",
                    withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            )).setParameter("projectId", report.getProjectId());
        } else if ( report.getFilterProjects()) {
            // Выборка по всем проектам центра
            query = entityManager.createQuery( String.format( report02QueryString,
                    "join p.divisions dp ",
                    "dp.id=:divisionId AND ",
                    withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            ) ).setParameter( "divisionId", report.getDivisionId() );
        } else {
            // Выборка по всем проектам всех центров
            query = entityManager.createQuery( String.format( report02QueryString,
                    "",
                    "",
                    withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            ) );
        }
        if ( withRegionClause )
            query.setParameter("regionIds", report.getRegionIds());
        if ( withEmployeeClasue )
            query.setParameter("emplId", report.getEmployeeId());
        if ( withDivisionClause )
            query.setParameter("emplDivisionId", report.getEmplDivisionId());

        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    @Language("HQL")
    private static final String report03QueryString =
                    "SELECT " +
                            "empl.name, " +
                            "d.name, " +
                            "p.name, " +
                            "pt.cqId, " +
                            "c.calDate, " +
                            "sum(tsd.duration), " +
                            "(case when h is null then 0 " +
                            "else " +
                                "case when h.region.id is not null and h.region.id<>empl.region.id then 0 " +
                                "else 1 end " +
                            "end), " +
                            "h.region.id " +
                    "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "left outer  join tsd.projectTask as pt " +
                        "left outer join c.holidays h " +
                        "%s " +
                    "WHERE " +
                        "tsd.duration > 0 AND " +
                        "%s %s %s %s " +
                        "c.calDate between :beginDate AND :endDate " +
                    "GROUP BY empl.name, d.name, p.name, pt.cqId, c.calDate, h.id, h.region.id, empl.region.id " +
                    "ORDER BY empl.name, p.name, pt.cqId, c.calDate ";

    private List getResultList( Report03 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0;
        boolean withEmployeeClasue = report.getEmployeeId()     != null && report.getEmployeeId    () != 0;

        Query query;

        if ( report.getProjectId() != null && report.getProjectId() != 0 ) {
            // Выборка по конкретному проекту
            query = entityManager.createQuery(
                    String.format( report03QueryString,
                            "",
                            "tsd.project.id=:projectId AND ",
                            withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                            withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                            withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            ) );
			query.setParameter("projectId", report.getProjectId());
        } else if ( report.getFilterProjects()) {
            // Выборка по всем проектам центра
            query = entityManager.createQuery(
                    String.format( report03QueryString,
                            "join p.divisions dp ",
                            "dp.id=:divisionId AND ",
                            withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                            withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                            withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            ) );
            query.setParameter( "divisionId", report.getDivisionId() );
        } else {
            // Выборка по всем проектам всех центров
            query = entityManager.createQuery(
                    String.format( report03QueryString,
                            "",
                            "",
                            withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                            withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                            withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            ) );
        }

        if ( withRegionClause )
            query.setParameter("regionIds", report.getRegionIds());
        if (withEmployeeClasue)
            query.setParameter("emplId", report.getEmployeeId());
        if (withDivisionClause)
            query.setParameter("emplDivisionId", report.getEmplDivisionId());

        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    private List getResultList( Report04 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getDivisionId() != null && report.getDivisionId() != 0;

        Query query = entityManager.createQuery(
                "select " +
                        "c.calDate, " +
                        "empl.name, " +
                        "empl.region.name " +
                "from " +
                        "Calendar c, " +
                        "Employee empl " +
                        "join empl.division d "  +
                "where " +
                        "c.calDate between :beginDate and " +
                        "(CASE  WHEN empl.endDate IS NOT NULL THEN " +
                        "   (CASE  WHEN empl.endDate < :endDate THEN " +
                        "       empl.endDate " +
                        "   ELSE :endDate END) " +
                        "ELSE :endDate END) AND " +
                        ( withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE ) +
                        ( withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE ) +
                        "(empl.notToSync = false) AND " +
                        "empl.manager.id is not null AND " +
                        "c.calDate not in " +
                            "(select ts.calDate.calDate " + //note: date don't have report
                            "from TimeSheet ts " +
                            "where ts.employee.id = empl.id) AND " +
                        "c.calDate not in " +
                            "(select h.calDate.calDate " +  //note: date is not holiday in employee's region
                            "from Holiday h " +
                            "where h.region.id is null " +
                                "or  h.region.id = empl.region.id ) AND " +
                        "(empl.startDate <= c.calDate) " +
                "order by empl.name, c.calDate");

        if (withRegionClause) {
            query.setParameter("regionIds", report.getRegionIds());
		}
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    private List getResultList( Report05 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getDivisionId() != null && report.getDivisionId() != 0;
        boolean withEmployeeClasue = report.getEmployeeId() != null && report.getEmployeeId    () != 0;

        Query query = entityManager.createQuery(
                "select " +
                        "ts.calDate.calDate, " +
                        "empl.name, " +
                        "td.actType.value, " +
                        "td.project.name, " +
                        "td.actCat.value, " +
                        "empl.job.name, " +
                        "COALESCE(pt.cqId, ''), " +
                        "td.duration, " +
                        "td.description, " +
                        "td.problem " +
                "from TimeSheetDetail td " +
                    "left outer join td.projectTask as pt " +
                    "inner join td.timeSheet ts " +
                    "inner join ts.employee empl " +
                    "join empl.division d " +
                "where " +
                    ( withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE ) +
                    ( withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE ) +
                    ( withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE ) +
                    "td.timeSheet.calDate.calDate between :beginDate and :endDate " +
                "order by td.timeSheet.employee.name,td.timeSheet.calDate.calDate");

        if (withRegionClause) {
			query.setParameter("regionIds", report.getRegionIds());
		}
        if (withEmployeeClasue) {
            query.setParameter("emplId", report.getEmployeeId());
        }
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    private List getResultList( Report06 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();

        Query query = entityManager.createQuery(
                "SELECT " +
                        "sum(tsd.duration), " +
                        "act.projectRole.name, " +
                        "tsd.timeSheet.employee.name, " +
                        "tsd.actCat.value " +
                "FROM " +
                        "TimeSheetDetail tsd, " +
                        "AvailableActivityCategory act " +
                        "join tsd.timeSheet.employee empl " +
                "WHERE " +
                        "tsd.project.id=:projectId AND " +
                        "tsd.actType=act.actType AND " +
                        "tsd.actCat=act.actCat AND " +
                        ( withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE ) +
                        "tsd.timeSheet.calDate.calDate between :beginDate AND :endDate AND " +
                        "act.projectRole=tsd.timeSheet.employee.job " +
                "GROUP BY act.projectRole.name, tsd.timeSheet.employee.name, tsd.actCat.value " +
                "ORDER BY tsd.timeSheet.employee.name asc");

        if (withRegionClause)
			query.setParameter("regionIds", report.getRegionIds());
        query.setParameter("projectId", report.getProjectId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }
}
