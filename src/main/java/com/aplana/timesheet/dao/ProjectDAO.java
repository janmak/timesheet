package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;

@Repository
public class ProjectDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);
    private StringBuffer trace;

    public static final Integer ANACCOUNTED_PRESALE_ID = 18;
    public static final String ANACCOUNTED_PRESALE_NAME = "Неучтённый пресейл";

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
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Project> getAll() {
        Query query = entityManager
                .createQuery("from Project as p where p.active=:active");
        query.setParameter("active", true);
        return query.getResultList();
    }

    /**
     * Возвращает активные проекты без разделения по подразделениям.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Project> getProjects() {
        Query query = entityManager
                .createQuery("from Project as p where p.state=:state and p.active=:active");
        query.setParameter("state", dictionaryItemDAO.find(DictionaryItemDAO.PROJECTS_ID));
        query.setParameter("active", true);
        return query.getResultList();
    }

    /**
     * Возвращает активные пресейлы без разделения по подразделениям.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Project> getPresales() {
        Query query = entityManager
                .createQuery("from Project as p where p.state=:state and p.active=:active and p.id<>:anaccounted_presale");
        query.setParameter("state", dictionaryItemDAO.find(DictionaryItemDAO.PRESALES_ID));
        query.setParameter("active", true);
        query.setParameter("anaccounted_presale", ANACCOUNTED_PRESALE_ID);
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
        Project result;
        Query query = entityManager.createQuery("from Project as p where p.id=:id and p.active=:active");
        query.setParameter("active", true);
        query.setParameter("id", id);
        try {
            result = (Project) query.getSingleResult();
        } catch (NoResultException e) {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает все активные проекты\пресейлы для которых в CQ заведены
     * проектные задачи. (cq_required=true)
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Project> getProjectsWithCq() {
        Query query = entityManager
                .createQuery("from Project as p where p.cqRequired='true' and p.active=:active");
        query.setParameter("active", true);
        return query.getResultList();
    }

    /**
     * Возвращает список всех участников указанного проекта.
     *
     * @param project
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<ProjectParticipant> getParticipants(Project project) {
        Query query = entityManager
                .createQuery("from ProjectParticipant as pp where pp.active=:active and pp.project=:project");
        query.setParameter("active", true);
        query.setParameter("project", project);
        return query.getResultList();
    }

    /**
     * Возвращает список всех проектных ролей указанного сотруднка.
     *
     * @param project, employee
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<ProjectParticipant> getEmployeeProjectRoles(Project project, Employee employee) {
        Query query = entityManager
                .createQuery("from ProjectParticipant as pp where pp.active=:active and pp.project=:project and pp.employee=:employee");
        query.setParameter("active", true);
        query.setParameter("project", project);
        query.setParameter("employee", employee);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Project findByName(String name) {
        Query query = entityManager.createQuery("from Project as p where p.name=:name");
        query.setParameter("name", name);
        if (query.getResultList().size() == 0)
            return null;
        return (Project) query.getResultList().get(0);
    }

    @Transactional(readOnly = true)
    public Project findByProjectId(String projectId) {
        Query query = entityManager.createQuery("select p from Project p where p.projectId=:projectId");
        query.setParameter("projectId", projectId);
        query.setMaxResults(1);
        List result = query.getResultList();
        if (result.size() > 0)
            return (Project) result.get(0);
        return null;
    }

    @Transactional
    public void store(Project project) {
        DictionaryItem item = dictionaryItemDAO.find(12); // Проект
        Project exProject = findByProjectId(project.getProjectId());
        if (exProject != null) {
            exProject.setActive(project.isActive());
            exProject.setManager(project.getManager());
            if (project.getDivisions() != null)
                exProject.getDivisions().addAll(project.getDivisions());
            exProject.setState(item);
            entityManager.merge(exProject);
        } else {
            Project existingProject = findByName(project.getName());
            if (existingProject != null) {
                existingProject.setProjectId(project.getProjectId());
                existingProject.setActive(project.isActive());
                existingProject.setManager(project.getManager());
                if (project.getDivisions() != null)
                    existingProject.getDivisions().addAll(project.getDivisions());
                existingProject.setState(item);
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
}