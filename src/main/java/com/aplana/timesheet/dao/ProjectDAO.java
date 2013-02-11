package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.DictionaryItem;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectParticipant;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

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
    @Transactional(readOnly = true)
    public List<Project> getAll() {
        Query query = entityManager.createQuery(
                "from Project as p where p.active=:active"
        ).setParameter( "active", true );

        return query.getResultList();
    }

    /**
     * Возвращает активные проекты без разделения по подразделениям.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Project> getProjects() {
        Query query = entityManager.createQuery(
                "from Project as p where p.state=:state and p.active=:active ORDER BY name"
        ).setParameter("state", dictionaryItemDAO.find(TypesOfActivityEnum.PROJECT.getId())).setParameter("active", true);

        return query.getResultList();
    }

    /**
     * Возвращает активные пресейлы без разделения по подразделениям.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Project> getPresales() {
        final Integer ANACCOUNTED_PRESALE_ID = 18;
        Query query = entityManager.createQuery(
                "from Project as p where p.state=:state and p.active=:active and p.id<>:anaccounted_presale" )
                .setParameter("state", dictionaryItemDAO.find(PRESALE.getId()))
                .setParameter("active", true)
                .setParameter("anaccounted_presale", ANACCOUNTED_PRESALE_ID);

        return query.getResultList();
    }

    /**
     * Возвращает объект класса Project по указанному идентификатору
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<ProjectParticipant> getEmployeeProjectRoles(Project project, Employee employee) {
        Query query = entityManager.createQuery(
                "from ProjectParticipant as pp where pp.active=:active and pp.project=:project and pp.employee=:employee"
        ).setParameter( "active", true ).setParameter("project", project).setParameter( "employee", employee );

        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Project findByName(String name) {
        Query query = entityManager.createQuery(
                "from Project as p where p.name=:name"
        ).setParameter( "name", name );

        List resultList = query.getResultList();
        return resultList.isEmpty()? null : (Project) resultList.get( 0 );
    }

    @Transactional(readOnly = true)
    public Project findByProjectId(String projectId) {
        Query query = entityManager.createQuery(
                "select p from Project p where p.projectId=:projectId"
        ).setParameter("projectId", projectId).setMaxResults(1);

        List result = query.getResultList();
        return result.isEmpty() ? null : (Project) result.get(0);
    }

    @Transactional
    public void store(Project project) {
        DictionaryItem item = dictionaryItemDAO.find(12); // Проект
        Project exProject = findByProjectId(project.getProjectId());
        if (exProject != null) {
            fillProject( exProject, project, item );
            entityManager.merge(exProject);
        } else {
            Project existingProject = findByName(project.getName());
            if (existingProject != null) {
                fillProject( existingProject, project, item );
                existingProject.setProjectId( project.getProjectId() );
                if (trace != null) trace.append("Обновлен проект: ").append(project).append("\n");
                entityManager.merge(existingProject);
            } else {
                if (project.isActive()) {
                    project.setCqRequired(false);
                    project.setState(item);
                    entityManager.merge(project);
                    if (trace != null) trace.append("Создан новый проект: ").append(project).append("\n");
                }
            }
        }
    }

    private void fillProject( Project exProject, Project project, DictionaryItem item ) {
        exProject.setActive(project.isActive());
        exProject.setManager(project.getManager());
        if (project.getDivisions() != null)
            exProject.getDivisions().addAll(project.getDivisions());
        exProject.setState(item);
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

    public List<Project> getEmployeeProjectsByDates(Date beginDate, Date endDate, Employee employee) {
        Query query = entityManager.createQuery("select tsd.project from TimeSheetDetail as tsd " +
                "where tsd.timeSheet.employee = :employee and tsd.timeSheet.creationDate between :beginDate and :endDate")
                .setParameter("employee", employee)
                .setParameter("endDate", beginDate)
                .setParameter("endDate", endDate);

        return query.getResultList();
    }

    public List<Project> getProjectsByStatesForDate(List<Integer> projectStates, Date date) {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        final Query query = entityManager.createQuery(
                "from Project p" +
                " where p.state.id in :states" +
                " and ((:date_month >= MONTH(p.startDate) and :date_year = YEAR(p.startDate) or :date_year > YEAR(p.startDate))" +
                        " and (p.endDate is null or :date_month <= MONTH(p.endDate) and :date_year = YEAR(p.endDate) or :date_year < YEAR(p.endDate)))" +
                " order by p.name"
        ).setParameter("states", projectStates).setParameter("date_month", calendar.get(Calendar.MONTH) + 1).
                setParameter("date_year", calendar.get(Calendar.YEAR));

        return query.getResultList();
    }
}