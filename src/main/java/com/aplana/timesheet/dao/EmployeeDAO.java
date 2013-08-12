package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.*;
import com.google.common.collect.Iterables;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Repository
public class EmployeeDAO {

    public static final int ALL_REGIONS = -1;
    public static final int ALL_PROJECT_ROLES = -1;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Возвращает сотрудника по идентификатору.
	 * @param id идентификатор сотрудника
	 * @return объект класса Employee либо null, если сотрудник
	 *         с указанным id не найден.
	 */
	public Employee find(Integer id) {
		if (id == null) {
			logger.warn("For unknown reasons, the Employee ID is null.");
			return null;
		}
		return entityManager.find(Employee.class, id);
	}
	
	/**
	 * Возвращает сотрудника по имени.
	 * @param name имя сотрудника
	 * @return объект класса Employee либо null, если сотрудник
	 *         с указанным именем не найден.
	 */
	public Employee find(String name) {
		if (name == null || "".equals(name)) { return null; }

		Query query = entityManager.createQuery(
                "from Employee as e where e.name=:name"
        ).setParameter( "name", name );
		try {
            return (Employee) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Employee with name '{}' not found.", name);
		} catch (NonUniqueResultException e) {
			logger.warn("More than one employee with name '{}' was found.", name);
		}
        return null;
	}

    /**
     * Возвращает сотрудника по email
     * @param email сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным именем не найден.
     */

    public Employee findByEmail(String email) {
        if ("".equals(email)) { return null; }

        Query query = entityManager.createQuery(
                "select e from Employee as e where e.email=:email"
        ).setParameter( "email", email );
        try {
            Employee result = (Employee) query.getSingleResult();
            // Загружаем детализации, чтобы не было проблем из-за lazy загрузки.
            Hibernate.initialize(result);
            Hibernate.initialize(result.getDivision());
            Hibernate.initialize(result.getManager());
            return result;
        } catch (NoResultException e) {
            logger.warn("Employee with email '{}' not found.", email);
        } catch (NonUniqueResultException e) {
            logger.warn("More than one employee with email '{}' was found.", email);
        }
        return null;
    }


	public Employee findByLdapName(String ldap) {
		Query query = this.entityManager.createQuery(
                "select e from Employee e where e.ldap like :ldap"
        ).setMaxResults( 1 ).setParameter("ldap", "%" + ldap + "%");

		return (Employee) Iterables.getFirst(query.getResultList(), null);
	}

    public List<Employee> getEmployeesForSync() {
        return getEmployeesForSync(null);
    }

    /**
	 * Возвращает список доступных для синхронизации с ldap сотрудников.
	 * @param division Если null, то поиск осуществляется без учета подразделения,
	 * 				   иначе с учётом подразделения
	 * @return список сотрудников для синхронизации
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getEmployeesForSync(Division division) {
		Query query;
		if (division == null) {
			query = entityManager.createQuery(
                    "from Employee as e where e.notToSync=:notToSync"
            );
		} else {
			query = entityManager.createQuery(
                    "from Employee as e where e.notToSync=:notToSync and e.division=:division"
            );
			query.setParameter("division", division);
		}
		query.setParameter("notToSync", false);

        return query.getResultList();
	}

    @SuppressWarnings("unchecked")
    public List<Employee> getAllEmployeesDivision(Division division) {
        Query query;
        if (division == null) {
            query = entityManager.createQuery("FROM Employee");
        } else {
            query = entityManager.createQuery("FROM Employee AS e WHERE e.division=:division");
            query.setParameter("division", division);
        }

        return query.getResultList();
    }
    
    /**
     * Возвращает список менеджеров для конкретного работника
     * @param employeeId
     * @return List<Employee>
     */
    public List<Employee> getRegionManager (Integer employeeId) {
        Query query = this.entityManager.createQuery(
                "select m.employee from  Employee e, Manager m " +
                "where e.id = :emp_id " +
                    "AND m.division.id = e.division.id " +
                    "AND m.region.id = e.region.id"
        ).setParameter("emp_id", employeeId);

        return query.getResultList();
    }

    /**
     * Возвращает список менеджеров для конкретного региона и подразделения
     * @param regionId
     * @param divisionId
     * @return
     */
    public List<Employee> getRegionManager(Integer regionId, Integer divisionId) {
        Query query = this.entityManager.createQuery(
                "select m.employee from  Manager m " +
                        "WHERE m.division.id = :div_id " +
                        "AND m.region.id = :region_id"
        ).setParameter("region_id", regionId).setParameter("div_id", divisionId);

        return query.getResultList();
    }

	/**
	 * Возвращает список действующих сотрудников указанного подразделения
	 * @param division подразделениe
	 * @return список сотрудников подразделения с идентификатором division
	 */
	@SuppressWarnings("unchecked")
	public List<Employee> getEmployees(Division division) {
		Query query;

        Date maxModDate = new Date();

		if (division == null) {
			query = entityManager.createQuery(
                    //действующий сотрудник-который на текущий момент либо не имеет endDate, либо endDate<=cuDate
                    "FROM Employee AS emp " +
                            "WHERE (emp.endDate IS NOT NULL " +
                                "AND emp.endDate >= :curDate) " +
                                "OR (emp.endDate IS NULL) " +
                            "ORDER BY emp.name"
            ).setParameter("curDate", maxModDate);
		} else {
			query = entityManager.createQuery(
                    "FROM Employee AS emp " +
                            "WHERE (emp.division=:division " +
                                "AND emp.endDate IS NOT NULL " +
                                "AND emp.endDate >= :curDate) " +
                                "OR (emp.division=:division " +
                                "AND emp.endDate IS NULL) " +
                            "ORDER BY emp.name"
            ).setParameter("curDate", maxModDate).setParameter("division", division);
		}

        return query.getResultList();
	}

	/**
	 * Сохраняет в базе нового сотрудника, либо обновляет данные уже
	 * существующего сотрудника.
	 * @param employee
	 */
    public Employee save(Employee employee) {
        Employee empMerged = entityManager.merge(employee);
        entityManager.flush();
        logger.info("Persistence context synchronized to the underlying database.");
        logger.debug("Flushed Employee object id = {}", empMerged.getId());

        employee.setId(empMerged.getId());

        return empMerged;
    }

    public Employee getEmployee(String email) {
        if(email!=null && !email.isEmpty()){
            Employee employee = (Employee) Iterables.getFirst(entityManager.createQuery(
                    "FROM Employee emp WHERE email = :email"
            ).setParameter("email", email).getResultList(), null);

            return employee;
        }
        return null;
    }

    public boolean isNotToSync(Employee employee) {
        final String email = employee.getEmail();

        if (email == null) {
            return true;
        }

        Query query;
        query = entityManager.createQuery(
                "FROM Employee AS e WHERE e.email=:email"
        ).setParameter("email", email.trim());

        List<Employee> result = query.getResultList();

        return result != null &&  ! result.isEmpty() && result.get( 0 ).isNotToSync();
    }

    public Double getWorkDaysOnIllnessWorked(Employee employee, Date beginDate, Date endDate) {
        Query query = entityManager.createQuery(
                "select sum(tsd.duration) from TimeSheetDetail tsd " +
                        "inner join tsd.timeSheet ts " +
                        "where (tsd.actType.id = 14 or tsd.actType.id = 12 or tsd.actType.id = 13 or tsd.actType.id = 42) " +
                        "and (ts.calDate.calDate between :beginDate and :endDate) " +
                        "and (ts.employee = :employee)");
        query.setParameter("beginDate", beginDate);
        query.setParameter("endDate", endDate);
        query.setParameter("employee", employee);

        if (query.getResultList().get(0) != null){
            return ((Double) query.getResultList().get(0));
        } else {
            return 0d;
        }

    }

    public Employee findByObjectSid(String objectSid) {
        Employee employee = (Employee) Iterables.getFirst(entityManager.createQuery(
                "FROM Employee emp WHERE objectSid = :objectSid"
        ).setParameter("objectSid", objectSid).getResultList(), null);

        if(employee == null) return null;

        Hibernate.initialize(employee.getDivision());
        Hibernate.initialize(employee.getDivision().getLeaderId());
        Hibernate.initialize(employee.getDivision().getLeader());
        Hibernate.initialize(employee.getJob());
        Hibernate.initialize(employee.getManager());
        Hibernate.initialize(employee.getRegion());

        return employee;
    }

    public List<Employee> getActiveEmployeesNotInList(List<Integer> syncedEmployees) {
        return entityManager.createQuery(
                "FROM Employee AS emp " +
                        "WHERE (emp.endDate IS NOT NULL " +
                        "AND emp.endDate >= :curDate) " +
                        "OR (emp.endDate IS NULL) AND emp.id NOT IN :ids"
        ).setParameter("curDate", new Date()).setParameter("ids", syncedEmployees).getResultList();
    }

    public List<Employee> getDivisionEmployees(Integer divisionId, Date date, List<Integer> regionIds, List<Integer> projectRoleIds) {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        final Query query = entityManager.createQuery(
                "from Employee e where e.division.id = :div_id" +
                        " and ((:date_month >= MONTH(e.startDate) and :date_year = YEAR(e.startDate) or :date_year > YEAR(e.startDate))" +
                        "       and (e.endDate is null or :date_month <= MONTH(e.endDate) and :date_year = YEAR(e.endDate) or :date_year < YEAR(e.endDate)))" +
                        " and (e.region.id in :region_ids or " + ALL_REGIONS + " in (:region_ids))" +
                        " and (e.job.id in :project_role_ids or " + ALL_PROJECT_ROLES + " in (:project_role_ids))" +
                        " order by e.name"
        ).setParameter("div_id", divisionId).setParameter("date_month", calendar.get(Calendar.MONTH) + 1).
                setParameter("date_year", calendar.get(Calendar.YEAR)).
                setParameter("region_ids", regionIds).setParameter("project_role_ids", projectRoleIds);

        return query.getResultList();
    }

    public List<Employee> getEmployees() {
        final Query query = entityManager.createQuery("from Employee e where e.endDate is null order by e.name");

        return query.getResultList();
    }

    public Employee tryGetEmployeeFromBusinessTrip(Integer reportId) {
        try {
            return (Employee) entityManager.createQuery("select bt.employee from BusinessTrip as bt " +
                    "where bt.id = :id")
                    .setParameter("id", reportId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Employee tryGetEmployeeFromIllness(Integer reportId) {
        try {
            return (Employee) entityManager.createQuery("select  i.employee from Illness as i " +
                    "where i.id = :id")
                    .setParameter("id", reportId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Boolean isLineManager(Employee employee) {
        Long slavesCount = (Long) entityManager.createQuery("select count (*) from Employee as e " +
                "where e.manager = :employee")
                .setParameter("employee", employee)
                .getSingleResult();
        return slavesCount > 0;
    }

    /**
     * Получаем младших менеджеров проекта (тимлиды, ведущие аналитики), которые еще не ответили на письмо о согласовании отпуска
     */
    public List<Employee> getProjectManagersThatDoesntApproveVacation(Project project, Vacation vacation) {
        Query query = entityManager.createQuery("select pp.employee from ProjectParticipant as pp " +
                "where pp.project = :project and pp.active=:active and " +
                "pp.projectRole.id = :roleId and pp.employee not in " +
                "(select va.manager from VacationApproval as va where va.vacation = :vacation and va.result is not null)")
                .setParameter("project", project)
                .setParameter("active", true)
                .setParameter("roleId", vacation.getEmployee().getJob().getId())
                .setParameter("vacation", vacation);

        return query.getResultList();
    }

    public List<Employee> getManagerListForAllEmployee(){
        Query query= entityManager.createQuery("select distinct emp.manager as manager from Employee as emp where emp.endDate is null order by 1");
        return query.getResultList();
    }

    public List<Integer> getEmployeesIdByDivisionManagerRegion(Integer divisionId, Integer managerId, Integer regionId){
        Query query = entityManager.createQuery("select emp.id from Employee as emp where " +
                "emp.manager.id = :managerId and " +
                "emp.region.id = :regionId and " +
                "emp.division.id = :divisionId")
                .setParameter("divisionId", divisionId)
                .setParameter("managerId", managerId)
                .setParameter("regionId", regionId);
        return query.getResultList();
    }

    public List<Integer> getEmployeesIdByDivisionRegion(Integer divisionId, Integer regionId){
        Query query = entityManager.createQuery("select emp.id from Employee as emp where " +
                "emp.region.id = :regionId and " +
                "emp.division.id = :divisionId")
                .setParameter("divisionId", divisionId)
                .setParameter("regionId", regionId);
        return query.getResultList();
    }

    public List<Integer> getEmployeesIdByDivisionManager(Integer divisionId, Integer managerId){
        Query query = entityManager.createQuery("select emp.id from Employee as emp where " +
                "emp.manager.id = :managerId and " +
                "emp.division.id = :divisionId")
                .setParameter("divisionId", divisionId)
                .setParameter("managerId", managerId);
        return query.getResultList();
    }

    public Employee findRandomEmployee() {
        final  Query query =
                entityManager.createQuery("from Employee as emp" + " where emp.manager is not null");
        List resultList = query.getResultList();
        Integer size = resultList.size();
        Random random = new Random();
        Integer number = random.nextInt(size);
        return (Employee) resultList.get(number);
    }

    public Employee findRandomEmployeeForVacationApprovedWithNotNullLeaders() {
        final  Query query =
                entityManager.createQuery("from Employee as emp" + " where emp.manager is not null and " + "emp.division is not null and " + "emp.division.email is not null and " + "emp.division.leaderId is not null and " + "emp.manager2 is not null");
        List resultList = query.getResultList();
        Integer size = resultList.size();
        Random random = new Random();
        Integer number = random.nextInt(size);
        return (Employee) resultList.get(number);
    }

    public Employee findRandomEmployeeForVacationWithNotNullLeaders() {
        final  Query query =
                entityManager.createQuery("from Employee as emp" + " where emp.manager is not null and " + "emp.division is not null and " + "emp.division.email is not null");
        List resultList = query.getResultList();
        Integer size = resultList.size();
        Random random = new Random();
        Integer number = random.nextInt(size);
        return (Employee) resultList.get(number);
    }

    public Employee findRandomEmployeeForManager(Employee manager) {
        final  Query query =
                entityManager.createQuery("from Employee as emp " + "where emp.manager = :manager and " + "emp.manager is not null and " + "emp.division is not null and " + "emp.division.email is not null").setParameter("manager", manager);
        List resultList = query.getResultList();
        logger.info("lalala = {}", resultList);
        Integer size = resultList.size();
        Random random = new Random();
        Integer number = random.nextInt(size);
        return (Employee) resultList.get(number);
    }

    public List<Employee> getEmployeeListForManager (Employee manager) {
        Query query =
                entityManager.createQuery("from Employee as emp " + "where emp.manager = :manager and " + "emp.manager is not null and " + "emp.division is not null and " + "emp.division.email is not null").setParameter("manager", manager);
        List resultList = query.getResultList();
        Integer size = resultList.size();
        return (List<Employee>) resultList;
    }

    public Employee findRandomEmployeeWithNotNullParams() {
        Query query =
                entityManager.createQuery("from Employee as emp" + " where emp.manager is not null and " + "emp.division is not null and " + "emp.division.vacationEmail is not null");
        List resultList = query.getResultList();
        Integer size = resultList.size();
        Random random;
        Integer number;
        if (size != 0) {
            random = new Random();
            number = random.nextInt(size);
        }
        else {
            Query query2 =
                    entityManager.createQuery("from Employee as emp" + " where emp.manager is not null and " + "emp.division is not null");
            resultList = query2.getResultList();
            size = resultList.size();
            random = new Random();
            number = random.nextInt(size);
        }
        return (Employee) resultList.get(number);
    }

}
