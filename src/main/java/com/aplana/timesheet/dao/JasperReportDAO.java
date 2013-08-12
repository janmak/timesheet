package com.aplana.timesheet.dao;

import com.aplana.timesheet.enums.OvertimeCategory;
import com.aplana.timesheet.enums.Report07PeriodTypeEnum;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.HibernateQueryResultDataSource;
import com.aplana.timesheet.util.report.Report7Period;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.time.DateUtils;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.timesheet.enums.TypesOfActivityEnum.getProjectPresaleNonProjectActivityId;
import static com.aplana.timesheet.enums.VacationStatusEnum.APPROVED;

@Repository
public class JasperReportDAO {

    public static final String HOURS_WITH_PERCENTS = ", ч. (%)";
    private DecimalFormat doubleFormat = new DecimalFormat("#.##");

    private static Map<Class, String[]> fieldsMap = new HashMap<Class, String[]>( 6 );

    static {
        //может быть это вынести в сам класс Reports? kss - нет, это мапинг полей datasource для отчета, его логично делать там же, где формируются данные.
        fieldsMap.put( Report01.class, new String[] { "id", "name", "caldate", "projnames", "overtime", "duration",
                "holiday", "region", "projdetail", "durationdetail", "region_name", "vacation", "illness" } );
        fieldsMap.put( Report02.class, new String[] { "name", "empldivision", "project",
                "taskname", "duration", "holiday", "region", "region_name" } );
        fieldsMap.put( Report03.class, new String[] { "name", "empldivision", "project", "taskname",
                "caldate", "duration", "holiday", "region", "region_name" } );
        fieldsMap.put( Report04.class, new String[] { "date", "name", "region_name" } );
        fieldsMap.put( Report05.class, new String[] { "calDate", "name", "value", "pctName", "actType",
                "pctRole", "taskName", "duration", "description", "problem", "region_name" } );
        fieldsMap.put( Report06.class, new String[] { "duration", "act_type", "name", "act_cat", "region_name" } );
    }

    private static final Logger logger = LoggerFactory.getLogger(JasperReportDAO.class);

    private static final String DIVISION_CLAUSE = "d.id=:emplDivisionId AND ";
    private static final String EMPLOYEE_CLAUSE = "empl.id=:emplId AND ";
    private static final String REGION_CLAUSE   = "empl.region.id in :regionIds AND ";
    private static final String PROJECT_CLAUSE  = "tsd.project.id=:projectId AND ";

    private static final String WITHOUT_CLAUSE  = "";

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TSPropertyProvider propertyProvider;


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

    @VisibleForTesting
    List getProjResultList( Report01 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = ! report.getDivisionOwnerId().equals(0);
        // Запрос достанет для сотрудников наименования проектов по датам
        Query projQuery = entityManager.createQuery(
                "select empl.id, ts.calDate.calDate, td.project.name " +
                "from TimeSheetDetail td " +
                    "inner join td.timeSheet ts " +
                    "inner join ts.employee empl " +
                    "join empl.division d "+
                "where " +
                    (withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE) +
                    (withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE ) +
                    "ts.calDate.calDate between :beginDate and :endDate ");

        if (withRegionClause)
            projQuery.setParameter("regionIds", report.getRegionIds());
        if (withDivisionClause)
            projQuery.setParameter("emplDivisionId", report.getDivisionOwnerId());

        projQuery.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ))
                 .setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return projQuery.getResultList();
    }

    @VisibleForTesting
    List getResultList( Report01 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = ! report.getDivisionOwnerId().equals(0);

        String workDaySeparator = "";

        if(OvertimeCategory.HOLIDAY.equals(report.getCategory())) {
            workDaySeparator="holidays.calDate is not null AND";
        } else if(OvertimeCategory.SIMPLE.equals(report.getCategory())) {
            workDaySeparator="holidays.calDate is null AND";
        }

        // К сожалению HQL не может ворочить сложные запросы, прищлось писать native sql-запрос
        Query query = entityManager.createNativeQuery(
                "SELECT " +
                        "        employee.id AS col_0," +
                        "        employee.name AS col_1," +
                        "        timesheet.caldate AS col_2," +
                        "        cast('' AS varchar(255)) AS col_3," +
                        "        sum(timesheet_details.duration)-8 AS col_4," +
                        "        sum(timesheet_details.duration) AS col_5," +
                        "        holidays.id AS col_6," +
                        "        holidays.region AS col_7," +
                        "        CASE" +
                        "            WHEN (holidays.id is not null OR" +
                        "                  vacations.id is not null OR" +
                        "                  illnesses.id is not null) " +
                        "               THEN CASE" +
                        "                   WHEN project.id is not null THEN project.name " +
                        "                   ELSE cast('Внепроектная деятельность' AS varchar(255)) " +
                        "            END" +
                        "            ELSE cast('%NO_GROUPING%' AS varchar(255)) " +
                        "        END AS col_8," +
                        "        CASE" +
                        "            WHEN (holidays.id is not null OR" +
                        "                  vacations.id is not null OR " +
                        "                  illnesses.id is not null) " +
                        "               THEN SUM(timesheet_details.duration) " +
                        "            ELSE cast(-1 as float4) " +
                        "        END AS col_9," +
                        "        region.name AS col_10," +
                        "        vacations.id AS col_11," +
                        "        illnesses.id AS col_12," +
                        "        CASE" +
                        "           WHEN (holidays.id is not null) " +
                        "               THEN 1 " +
                        "           ELSE " +
                        "               CASE " +
                        "                   WHEN (vacations.id is not null) " +
                        "                       THEN 2 " +
                        "                   ELSE " +
                        "                       CASE " +
                        "                           WHEN (illnesses.id is not null) " +
                        "                               THEN 3 " +
                        "                           ELSE 0" +
                        "                       END" +
                        "               END" +
                        "        END AS day_type  " +
                        "FROM " +
                        "       time_sheet_detail timesheet_details " +
                        "       INNER JOIN time_sheet timesheet ON timesheet_details.time_sheet_id=timesheet.id " +
                        "       INNER JOIN employee employee    ON timesheet.emp_id=employee.id " +
                        "       INNER JOIN region region        ON employee.region=region.id " +
                        "       INNER JOIN division division    ON employee.division=division.id " +
                        "       LEFT OUTER JOIN calendar calendar  ON timesheet.caldate=calendar.caldate " +
                        "       LEFT OUTER JOIN holiday holidays   ON calendar.caldate=holidays.caldate " +
                        "       LEFT OUTER JOIN project project    ON timesheet_details.proj_id=project.id " +
                        "       LEFT OUTER JOIN project_role project_role ON timesheet_details.projectrole_id=project_role.id " +
                        "       LEFT OUTER JOIN vacation vacations ON " +
                        "               employee.id=vacations.employee_id AND " +
                        "               timesheet.caldate BETWEEN vacations.begin_date AND vacations.end_date " +
                        "               AND vacations.status_id=:status" +
                        "       LEFT OUTER JOIN illness illnesses ON " +
                        "               employee.id=illnesses.employee_id AND " +
                        "               timesheet.caldate BETWEEN illnesses.begin_date AND illnesses.end_date " +
                        "WHERE " +
                                (withDivisionClause ? "division.id = :emplDivisionId AND " : "") +
                                (withRegionClause ? "region.id in :regionIds AND " : "") +
                                workDaySeparator +
                        "        timesheet_details.act_type in :actTypes AND " +
                        "        timesheet.caldate BETWEEN :beginDate AND :endDate AND " +
                        "        (holidays.region is null OR holidays.region=region.id) " +
                        "GROUP BY" +
                        "        employee.id ," +
                        "        employee.name ," +
                        "        timesheet.caldate ," +
                        "        holidays.id ," +
                        "        holidays.region ," +
                        "        col_8 ," +
                        "        region.name ," +
                        "        vacations.id ," +
                        "        illnesses.id " +
                        "HAVING" +
                        "        sum(timesheet_details.duration) > 8 " +
                        "        OR holidays.id is not null " +
                        "        OR vacations.id is not null " +
                        "        OR illnesses.id is not null " +
                        "ORDER BY" +
                        "        employee.name," +
                        "        day_type," +
                        "        timesheet.caldate"
        );


        if (withRegionClause) {
            query.setParameter("regionIds", report.getRegionIds());
		}
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionOwnerId());
        }

        query   .setParameter("beginDate", DateTimeUtil.stringToTimestamp(report.getBeginDate()))
                .setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()))
                .setParameter("status", APPROVED.getId())
                .setParameter("actTypes", getProjectPresaleNonProjectActivityId()); // отчет только по этим типам активностей ( 12 , 13 , 14 )

        List resultList = query.getResultList();
        //похоже это нужно вынести в запрос, и не делать этого в Java - kss: это нельзя сделать в hql запросе - только в nativesql c использованием специфических для БД функций.
        List projResultList = getProjResultList ( report );

        // Пробежим весь список отчетов и заполним в них списки проектов, над которыми работали сотрудники
        for ( Object aResultList : resultList ) {
            Object[] next = ( Object[] ) aResultList;

            if ( next[ 6 ] != null ) continue;

            next[ 3 ] = getStringWithProjectNames(
                    getProjectNamesList( projResultList, ( Integer ) next[ 0 ], new Timestamp ( ((Date)next[ 2 ]).getTime() )
            ) );
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
                                "max(h.region.id), " +
                                "r.name " +
                        "FROM TimeSheetDetail tsd " +
                            "join tsd.timeSheet ts " +
                            "join ts.employee empl " +
                            "join ts.calDate c " +
                            "join empl.division d " +
                            "join tsd.project p " +
                            "join empl.region r " +
                            "left outer join tsd.projectTask as pt " +
                            "left outer join c.holidays h " +
                            " %s " +
                        "WHERE " +
                            "tsd.duration > 0 AND " +
                            " %s %s %s %s" +
                            "c.calDate between :beginDate AND :endDate " +
                        "GROUP BY empl.name, d.name, p.name, pt.cqId, 6, r.name " +
                        "ORDER BY empl.name, p.name, pt.cqId ";

    private List getResultList( Report02 report ) {
        boolean hasProject = report.getProjectId() != null && report.getProjectId() != 0;
        boolean hasDiv = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getEmplDivisionId() != null && report.getEmplDivisionId() != 0;
        boolean withEmployeeClasue = report.getEmployeeId()     != null && report.getEmployeeId    () != 0;

        Query query;
        if (hasProject) {
            // Выборка по конкретному проекту
            query = entityManager.createQuery( String.format( report02QueryString,
                    "",
                    PROJECT_CLAUSE,
                    withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            )).setParameter("projectId", report.getProjectId());
        } else if ( hasDiv ) {
            // Выборка по всем проектам центра
            query = entityManager.createQuery( String.format( report02QueryString,
                    "join p.divisions dp ",
                    "dp.id=:divisionId AND ",
                    withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE,
                    withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE,
                    withRegionClause   ? REGION_CLAUSE   : WITHOUT_CLAUSE
            ) ).setParameter( "divisionId", report.getDivisionOwnerId() );
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
                            "h.region.id, " +
                            "r.name " +
                    "FROM TimeSheetDetail tsd " +
                        "join tsd.timeSheet ts " +
                        "join ts.employee empl " +
                        "join ts.calDate c " +
                        "join empl.division d " +
                        "join tsd.project p " +
                        "join empl.region r " +
                        "left outer  join tsd.projectTask as pt " +
                        "left outer join c.holidays h " +
                        "%s " +
                    "WHERE " +
                        "tsd.duration > 0 AND " +
                        "%s %s %s %s " +
                        "c.calDate between :beginDate AND :endDate " +
                    "GROUP BY empl.name, d.name, p.name, pt.cqId, c.calDate, h.id, h.region.id, empl.region.id, r.name " +
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
                            PROJECT_CLAUSE,
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
            query.setParameter( "divisionId", report.getDivisionOwnerId() );
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
        boolean withDivisionClause = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;

        // К сожалению HQL не может ворочить сложные запросы, прищлось писать native sql-запрос
        Query query = entityManager.createNativeQuery(
            "SELECT\n" +
                    "        calendar.caldate AS col_0,\n" +
                    "        employee.name AS col_1,\n" +
                    "        region.name AS col_2\n" +
                    "FROM\n" +
                    "        calendar calendar \n" +
                    "        CROSS JOIN employee employee \n" +
                    "        INNER JOIN division division ON employee.division=division.id  \n" +
                    "        LEFT JOIN illness illnesses ON \n" +
                    "                employee.id=illnesses.employee_id AND \n" +
                    "                (calendar.caldate BETWEEN illnesses.begin_date AND illnesses.end_date) AND \n" +
                    "                (illnesses.begin_date <= :endDate AND illnesses.end_date >= :beginDate)\n" +
                    "        LEFT JOIN vacation vacations ON \n" +
                    "                employee.id=vacations.employee_id AND \n" +
                    "                (calendar.caldate BETWEEN vacations.begin_date AND vacations.end_date) AND \n" +
                    "                (vacations.begin_date <= :endDate AND vacations.end_date >= :beginDate) AND \n" +
                    "                (vacations.status_id = :statusId),\n" +
                    "        region region \n" +
                    "WHERE \n" +
                             ( withDivisionClause ? "        division.id=:emplDivisionId AND \n" : "") +
                             ( withRegionClause   ? "        (employee.region IN (:regionIds)) AND \n" : "" ) +
                    "        illnesses.id IS NULL AND\n" +
                    "        vacations.id IS NULL AND\n" +
                    "        employee.region=region.id AND \n" +
                    "        (calendar.caldate BETWEEN :beginDate AND \n" +
                    "                CASE WHEN employee.end_date IS NOT NULL THEN \n" +
                    "                        CASE WHEN employee.end_date<:endDate THEN employee.end_date ELSE :endDate END \n" +
                    "                ELSE :endDate end) AND\n" +
                    "        employee.not_to_sync=FALSE AND \n" +
                    "        (employee.manager IS NOT NULL) AND \n" +
                    "        (calendar.caldate NOT IN \n" +
                    "                (SELECT timesheet.caldate FROM time_sheet timesheet WHERE timesheet.emp_id=employee.id)) AND \n" +
                    "        (calendar.caldate NOT IN  \n" +
                    "                (SELECT holiday.caldate FROM holiday holiday WHERE holiday.region IS NULL OR holiday.region=employee.region)) AND \n" +
                    "        employee.start_date<=calendar.caldate \n" +
                    "ORDER BY\n" +
                    "        employee.name, \n" +
                    "        calendar.caldate\n"
        );

        if (withRegionClause) {
            query.setParameter("regionIds", report.getRegionIds());
		}
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionOwnerId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));
        query.setParameter("statusId", APPROVED.getId());

        return query.getResultList();
    }

    private List getResultList( Report05 report ) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();
        boolean withDivisionClause = report.getDivisionOwnerId() != null && report.getDivisionOwnerId() != 0;
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
                        "td.problem, " +
                        "r.name " +
                        "from TimeSheetDetail td " +
                        "left outer join td.projectTask as pt " +
                        "inner join td.timeSheet ts " +
                        "inner join ts.employee empl " +
                        "join empl.division d " +
                        "join empl.region r " +
                        "where " +
                        (withDivisionClause ? DIVISION_CLAUSE : WITHOUT_CLAUSE) +
                        (withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE) +
                        (withEmployeeClasue ? EMPLOYEE_CLAUSE : WITHOUT_CLAUSE) +
                        "td.timeSheet.calDate.calDate between :beginDate and :endDate " +
                        "order by td.timeSheet.employee.name,td.timeSheet.calDate.calDate");

        if (withRegionClause) {
			query.setParameter("regionIds", report.getRegionIds());
		}
        if (withEmployeeClasue) {
            query.setParameter("emplId", report.getEmployeeId());
        }
        if (withDivisionClause) {
            query.setParameter("emplDivisionId", report.getDivisionOwnerId());
        }
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    @VisibleForTesting
    List getResultList(Report06 report) {
        boolean withRegionClause   = report.hasRegions()                && !report.isAllRegions();

        boolean withProjectClause = !report.getProjectId().equals(0);

        Query query = entityManager.createQuery(
                "SELECT " +
                        "sum(tsd.duration), " +
                        "act.projectRole.name, " +
                        "tsd.timeSheet.employee.name, " +
                        "tsd.actCat.value, " +
                        "r.name " +
                "FROM " +
                        "TimeSheetDetail tsd, " +
                        "AvailableActivityCategory act " +
                        "join tsd.timeSheet.employee empl " +
                        "join empl.region r " +
                "WHERE " +
                        "tsd.actType=act.actType AND " +
                        "tsd.actCat=act.actCat AND " +
                        (withRegionClause ? REGION_CLAUSE : WITHOUT_CLAUSE) +
                        (withProjectClause ? PROJECT_CLAUSE : WITHOUT_CLAUSE) +
                        "tsd.timeSheet.calDate.calDate between :beginDate AND :endDate AND " +
                        "act.projectRole=tsd.timeSheet.employee.job " +
                "GROUP BY act.projectRole.name, tsd.timeSheet.employee.name, tsd.actCat.value, r.name " +
                "ORDER BY tsd.timeSheet.employee.name asc");

        if (withRegionClause)
			query.setParameter("regionIds", report.getRegionIds());
        if (withProjectClause)
            query.setParameter("projectId", report.getProjectId());
        query.setParameter("beginDate", DateTimeUtil.stringToTimestamp( report.getBeginDate() ));
        query.setParameter("endDate", DateTimeUtil.stringToTimestamp(report.getEndDate()));

        return query.getResultList();
    }

    public HibernateQueryResultDataSource getReport07Data(Report07 report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            final Report07PeriodTypeEnum periodType =
                    Report07PeriodTypeEnum.getByMonthsCount(report.getPeriodType());

            ArrayList rolesDev = this.readRolesFromString(propertyProvider.getProjectRoleDeveloper());
            ArrayList rolesRp = this.readRolesFromString(propertyProvider.getProjectRoleRp());
            ArrayList rolesTest = this.readRolesFromString(propertyProvider.getProjectRoleTest());
            ArrayList rolesAnalyst = this.readRolesFromString(propertyProvider.getProjectRoleAnalyst());
            ArrayList rolesSystem = this.readRolesFromString(propertyProvider.getProjectRoleSystem());
            Date start = sdf.parse(report.getBeginDate());
            Date end = sdf.parse(report.getEndDate());
            HashMap<String, Object> itogo = new HashMap<String, Object>();
            Query query1;
            if (report.getDivisionOwner() != 0) {
                query1 = this.getReport07Query1(start, end, report.getDivisionEmployee(), report.getDivisionOwner());
            } else {
                query1 = this.getReport07Query1(start, end, report.getDivisionEmployee());
            }
            List dataSource = new ArrayList();
            Double temp = null;
            HashMap<String, HashMap<String, Double>> periodsDuration = new HashMap<String, HashMap<String, Double>>();
            HashMap<String, Double> durations = new HashMap<String, Double>();
            Report7Period itogoPeriod = new Report7Period("Итого");
            for (Object o : query1.getResultList()) {
                Object[] projects = (Object[]) o;
                Integer projectId = (Integer) projects[0];
                Integer projectDivision = (Integer) projects[2];
                Date periodStart = start;
                String projectName = null;
                Integer periodNumber = null;
                Date periodEnd = periodStart;
                Double periodByRP = 0D;
                Double periodByAnalyst = 0D;
                Double periodByDev = 0D;
                Double periodBySystem = 0D;
                Double periodByTest = 0D;
                Double periodByCenterOwner = 0D;
                Double periodByCenterEtc = 0D;
                HashMap<String, Double> periodRegions = new HashMap<String, Double>();
                for (periodNumber = 1; periodEnd.before(end); periodNumber = periodNumber + 1) {
                    final Date maxEndOfPeriod = getMaxEndOfPeriod(end, periodStart, periodType);

                    periodEnd = getEndOfPeriod(periodType, periodEnd, maxEndOfPeriod);

                    Report7Period period = new Report7Period(periodNumber, periodStart, end, periodType);
                    Query query2 = this.getReport07Query2(periodStart, periodEnd, projectId, report.getDivisionEmployee());
                    Double durationByRP = 0D;
                    Double durationByAnalyst = 0D;
                    Double durationByDev = 0D;
                    Double durationBySystem = 0D;
                    Double durationByTest = 0D;
                    HashMap<String, Double> regions = new HashMap<String, Double>();
                    Double durationByCenterOwner = 0D;
                    Double durationByCenterEtc = 0D;
                    Double durationPeriod = 0D;
                    for (Object o1 : query2.getResultList()) {
                        Object[] values = (Object[]) o1;
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
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Руководитель проекта, ч. (%)",
                                this.report7GenerateValue(durationByRP, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Аналитик, ч. (%)",
                                this.report7GenerateValue(durationByAnalyst, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Разработчик, ч. (%)",
                                this.report7GenerateValue(durationByDev, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Системный инженер, ч. (%)",
                                this.report7GenerateValue(durationBySystem, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Должностям", "Тестирование, ч. (%)",
                                this.report7GenerateValue(durationByTest, durationPeriod)));

                        // Подсчитаем к итоговому периоду
                        periodByAnalyst += durationByAnalyst;
                        periodByRP += durationByRP;
                        periodByDev += durationByDev;
                        periodBySystem += durationBySystem;
                        periodByTest += durationByTest;

                        // Подсчёт по регионам
                        for (Map.Entry<String, Double> region : regions.entrySet()) {
                            final String regionHoursAndPercents = region.getKey().concat(HOURS_WITH_PERCENTS);

                            dataSource.add(this.report7DataSourceRow(period, projectName, "По Регионам", (String) regionHoursAndPercents,
                                    this.report7GenerateValue(region.getValue(), durationPeriod)));
                            // Посчитаем для итого
                            if (periodRegions.get(regionHoursAndPercents) == null) {
                                periodRegions.put(regionHoursAndPercents, region.getValue());
                            } else {
                                periodRegions.put(regionHoursAndPercents, region.getValue() + periodRegions.get(regionHoursAndPercents));
                            }
                        }
                        if (durationPeriod > 0) {
                            dataSource.add(this.report7DataSourceRow(period, projectName, "Трудозатраты", "Общие, ч.", doubleFormat.format(durationPeriod)));
                        }

                        if (periodsDuration.get(period.getNumber().toString()) == null) {
                            HashMap<String, Double> value = new HashMap<String, Double>();
                            value.put(projectName, durationPeriod);
                            periodsDuration.put(period.getNumber().toString(), value);
                        } else {
                            periodsDuration.get(period.getNumber().toString()).put(projectName, durationPeriod);
                        }

                        // Относительные затраты по центрам
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Центрам", "Центр владельца проекта, ч. (%)",
                                this.report7GenerateValue(durationByCenterOwner, durationPeriod)));
                        dataSource.add(this.report7DataSourceRow(period, projectName, "По Центрам", "Другие центры, ч. (%)",
                                this.report7GenerateValue(durationByCenterEtc, durationPeriod)));
                        periodByCenterEtc += durationByCenterEtc;
                        periodByCenterOwner += durationByCenterOwner;
                        regions.clear();
                    }

                    //durations.put(projectName, Double.valueOf(0));
                    if (periodEnd.equals(maxEndOfPeriod)) {
                        periodEnd = DateUtils.addDays(periodEnd, 1);
                    }

                    periodStart = periodEnd;    // Обязательно
                }
                // Вывод итого в dataSource
                if (projectName != null) {
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Руководитель проекта, ч. (%)",
                            this.report7GenerateValue(periodByRP, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Аналитик, ч. (%)",
                            this.report7GenerateValue(periodByAnalyst, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Разработчик, ч. (%)",
                            this.report7GenerateValue(periodByDev, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Системный инженер, ч. (%)",
                            this.report7GenerateValue(periodBySystem, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Должностям", "Тестирование, ч. (%)",
                            this.report7GenerateValue(periodByTest, durations.get(projectName))));

                    // Относительные затраты по центрам
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Центрам", "Центр владельца проекта, ч. (%)",
                            this.report7GenerateValue(periodByCenterOwner, durations.get(projectName))));
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Центрам", "Другие центры, ч. (%)",
                            this.report7GenerateValue(periodByCenterEtc, durations.get(projectName))));

                    // Трудозатраты
                    dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "Трудозатраты", "Общие, ч.", doubleFormat.format(durations.get(projectName))));

                    // По регионам
                    for (Map.Entry<String, Double> region : periodRegions.entrySet()) {
                        dataSource.add(this.report7DataSourceRow(itogoPeriod, projectName, "По Регионам", (String) region.getKey(),
                                this.report7GenerateValue(region.getValue(), durations.get(projectName))));
                    }
                }
            }
            for (Map.Entry<String, HashMap<String, Double>> period : periodsDuration.entrySet()) {
                Double sum = 0D;
                for (Map.Entry<String, Double> project : period.getValue().entrySet()) {
                    sum = sum + project.getValue();
                }
                for (Map.Entry<String, Double> project : period.getValue().entrySet()) {
                    if (sum > 0 && project.getValue() > 0) {
                        temp = project.getValue() / sum * 100;
                        dataSource.add(this.report7DataSourceRow(itogoPeriod, project.getKey(), "Трудозатраты", "Относительные, %",
                                doubleFormat.format(temp).concat("%")));
                    }
                }
            }

            Double sum = 0D;
            for (Map.Entry<String, Double> period : durations.entrySet()) {
                sum += period.getValue();
            }

            for (Map.Entry<String, Double> period : durations.entrySet()) {
                temp = period.getValue() / sum * 100;
                dataSource.add(this.report7DataSourceRow(itogoPeriod, period.getKey(), "Трудозатраты", "Относительные, %",
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

    private Date getEndOfPeriod(Report07PeriodTypeEnum periodType, Date periodEnd, Date maxEndOfPeriod) {
        final Date endOfPeriod = DateUtils.addMonths(periodEnd, periodType.getMonthsCount());

        return (Date) ObjectUtils.min(endOfPeriod, maxEndOfPeriod);
    }

    private Date getMaxEndOfPeriod(Date end, Date periodStart, Report07PeriodTypeEnum periodType) {
        final Calendar calendar = DateUtils.toCalendar(periodStart);

        return (Date) ObjectUtils.min(periodType.getMaxDateOfPartOfYear(calendar), end);
    }

    private String report7GenerateValue(Double projectDuration, Double periodDuration) {
        if (projectDuration == null || projectDuration.isNaN())
            projectDuration = 0D;
        if (projectDuration == null || projectDuration.isNaN())
            periodDuration = 0D;
        Double result;
        if (periodDuration > 0) {
            result = projectDuration / periodDuration * 100;
        } else {
            result = 0D;
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
        for (String role1 : roles) {
            role.add(Integer.parseInt(role1));
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
