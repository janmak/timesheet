package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.Calendar;

import static com.aplana.timesheet.enums.TypesOfActivityEnum.PRESALE;

@Repository
@SuppressWarnings("unchecked")
public class ProjectDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);

    private StringBuffer trace;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DictionaryItemDAO dictionaryItemDAO;

    public void setTrace(StringBuffer trace) {
        this.trace = trace;
    }

    /**
     * Возвращает все активные проекты\пресейлы.
     */
    public List<Project> getAll() {
        Query query = entityManager.createQuery(
                "from Project as p where p.active=:active"
        ).setParameter( "active", true );

        return query.getResultList();
    }

    /**
     * Возвращает активные проекты без разделения по подразделениям.
     */
    @SuppressWarnings("unchecked")
    public List<Project> getProjects() {
        Query query = entityManager.createQuery(
                "from Project as p where p.active=:active ORDER BY name"
        ).setParameter("active", true);

        return query.getResultList();
    }

    /**
     * Возвращает объект класса Project по указанному идентификатору
     */
    public Project find(Integer id) {
        if (id == null) {
            logger.warn("Project ID is null.");
            return null;
        }
        return entityManager.find(Project.class, id);
    }

    /**
     * Возвращает объект класса Project по указанному идентификатору,
     * соответсвующий активному проекту, либо null.
     */
    public Project findActive(Integer id) {
        Query query = entityManager.createQuery(
                "from Project as p where p.id=:id and p.active=:active"
        ).setParameter("active", true).setParameter("id", id);

        try {
            return  (Project) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Возвращает все активные проекты\пресейлы для которых в CQ заведены
     * проектные задачи. (cq_required=true)
     */
    public List<Project> getProjectsWithCq() {
        Query query = entityManager.createQuery(
                "from Project as p where p.cqRequired=true and p.active=:active"
        ).setParameter( "active", true );

        return query.getResultList();
    }

    /**
     * Возвращает список всех участников указанного проекта.
     *
     * @param project
     * @return
     */
    public List<ProjectParticipant> getParticipants(Project project) {
        Query query = entityManager.createQuery(
                "from ProjectParticipant as pp where pp.active=:active and pp.project=:project"
        ).setParameter( "active", true ).setParameter( "project", project );

        return query.getResultList();
    }

    /**
     * Возвращает список всех проектных ролей указанного сотруднка.
     *
     * @param project, employee
     * @return
     */
    public List<ProjectParticipant> getEmployeeProjectRoles(Project project, Employee employee) {
        Query query = entityManager.createQuery(
                "from ProjectParticipant as pp where pp.active=:active and pp.project=:project and pp.employee=:employee"
        ).setParameter( "active", true ).setParameter("project", project).setParameter( "employee", employee );

        return query.getResultList();
    }

    public Project findByProjectId(String projectId) {
        Query query = entityManager.createQuery(
                "select p from Project p where p.projectId=:projectId"
        ).setParameter("projectId", projectId).setMaxResults(1);

        List result = query.getResultList();
        return result.isEmpty() ? null : (Project) result.get(0);
    }

    public void store(Project project) {
        final DictionaryItem item = dictionaryItemDAO.find(TypesOfActivityEnum.PROJECT.getId()); // Проект
        final Project existingProject = findByProjectId(project.getProjectId());

        if (existingProject != null) {
            project.setId(existingProject.getId());

            entityManager.merge(project);

            if (trace != null) {
                trace.append("Обновлен проект: ").append(project).append("\n");
            }
        } else if (project.isActive()) {
            project.setCqRequired(false);
            project.setState(item);

            entityManager.merge(project);

            if (trace != null) {
                trace.append("Создан новый проект: ").append(project).append("\n");
            }
        }
    }

    public List<Project> getProjectsByDates(Date beginDate, Date endDate) {
        Query query = entityManager.createQuery("from Project p where p.startDate <= :endDate and p.endDate >= :startDate");
        query.setParameter("startDate", beginDate);
        query.setParameter("endDate", endDate);

        return query.getResultList();
    }

    public List<Project> getEmployeeProjectPlanByDates(Employee employee, HashMap<Integer, Set<Integer>> dates) {
        List<Project> projects = new ArrayList<Project>();
        for (Integer year : dates.keySet()) {
            Query query = entityManager.createQuery("select epp.project from EmployeeProjectPlan as epp where epp.employee = :employee and epp.year = :year and epp.month in :monthList")
                    .setParameter("employee", employee)
                    .setParameter("year", year)
                    .setParameter("monthList", dates.get(year));
            projects.addAll(query.getResultList());
        }

        return projects;
    }

    public List<Project> getEmployeeProjectsFromTimeSheetByDates(Date beginDate, Date endDate, Employee employee) {
        Query query = entityManager.createQuery("select tsd.project from TimeSheetDetail as tsd " +
                "where tsd.timeSheet.employee = :employee and tsd.timeSheet.calDate.calDate between :beginDate and :endDate")
                .setParameter("employee", employee)
                .setParameter("beginDate", beginDate)
                .setParameter("endDate", endDate);

        return query.getResultList();
    }

    /**
     * получаем список проектов пользователя, руководители которых должны подтвердить заявление на отпуск
     */
    public List<Project> getProjectsAssignedToVacation(Vacation vacation) {
        Query query = entityManager.createQuery("select distinct var.project from VacationApprovalResult as var " +
                "where var.vacationApproval.vacation = :vacation")
                .setParameter("vacation", vacation);

        return query.getResultList();
    }

    public List<Project> getProjectsByStatesForDateAndDivisionId(List<Integer> projectStates, Date date,
                                                                 Integer divisionId) {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        final Query query = entityManager.createQuery(
                "select p" +
                " from Project p" +
                " inner join p.divisions d" +
                " where p.state.id in :states" +
                "   and ((:date_month >= MONTH(p.startDate) and  :date_year = YEAR(p.startDate) or :date_year > YEAR(p.startDate))" +
                "   and (p.endDate is null or :date_month <= MONTH(p.endDate) and :date_year = YEAR(p.endDate) or :date_year < YEAR(p.endDate)))" +
                "   and (d.id = :div_id)" +
                " order by p.name"
        ).setParameter("states", projectStates).setParameter("date_month", calendar.get(Calendar.MONTH) + 1).
                setParameter("date_year", calendar.get(Calendar.YEAR)).setParameter("div_id", divisionId);

        return query.getResultList();
    }

    public List<Project> getProjectsForPeriod(Date fromDate, Date toDate) {
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery query = criteriaBuilder.createQuery();
        final Root<Project> from = query.from(Project.class);
        final CriteriaQuery select = query.select(from);
        final List<Predicate> predicates = new ArrayList<Predicate>();

        if (fromDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(from.<Date>get("startDate"), fromDate));
        }

        if (toDate != null) {
            final Path<Date> endDatePath = from.get("endDate");

            predicates.add(
                    criteriaBuilder.or(
                            endDatePath.isNull(),
                            criteriaBuilder.greaterThanOrEqualTo(endDatePath, toDate)
                    )
            );
        }

        predicates.add(criteriaBuilder.isTrue(from.<Boolean>get("active")));

        select.where(predicates.toArray(new Predicate[predicates.size()]));
        select.orderBy(criteriaBuilder.asc(from.get("name")));

        return entityManager.createQuery(query).getResultList();
    }
}