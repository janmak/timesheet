package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.util.DateTimeUtil;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class EmployeeDAO {
	private static final Logger logger = LoggerFactory.getLogger(EmployeeDAO.class);

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Возвращает сотрудника по идентификатору.
	 * @param id идентификатор сотрудника
	 * @return объект класса Employee либо null, если сотрудник
	 *         с указанным id не найден.
	 */
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
	public Employee find(String name) {
		if ("".equals(name)) { return null; }
		Employee result = null;
		Query query = entityManager
			.createQuery("from Employee as e where e.name=:name");
		query.setParameter("name", name);
		try {
			result = (Employee) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Employee with name '{}' not found.", name);
			return null;
		} catch (NonUniqueResultException e) {
			logger.warn("More than one employee with name '{}' was found.", name);
			return null;
		}
		return result;
	}

    /**
     * Возвращает сотрудника по email
     * @param email сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным именем не найден.
     */

    @Transactional(readOnly = true)
    public Employee findByEmail(String email) {
        if ("".equals(email)) { return null; }
        Employee result = null;
        Query query = entityManager
                .createQuery("select e from Employee as e where e.email=:email");
        query.setParameter("email", email);
        try {
            result = (Employee) query.getSingleResult();
        } catch (NoResultException e) {
            logger.warn("Employee with email '{}' not found.", email);
            return null;
        } catch (NonUniqueResultException e) {
            logger.warn("More than one employee with email '{}' was found.", email);
            return null;
        }

        // Загружаем детализации, чтобы не было проблем из-за lazy загрузки.
        Hibernate.initialize(result);
        Hibernate.initialize(result.getDivision());
        Hibernate.initialize(result.getManager());

/*        Division division = result.getDivision();
        Employee manager = result.getManager();
        Region region = result.getRegion();
        ProjectRole role = result.getJob();*/

        return result;
    }


	public Employee findByLdapName(String ldap) {
		Query query = this.entityManager.createQuery("select e from Employee e where e.ldap like :ldap");
		query.setMaxResults(1);
		query.setParameter("ldap", "%"+ldap+"%");
		List employees = query.getResultList();
		if(employees != null && employees.size() > 0)
			return (Employee)employees.get(0);
		return null;
	}
	
	/**
	 * Возвращает список доступных для синхронизации с ldap сотрудников.
	 * @param division Если null, то поиск осуществляется без учета подразделения,
	 * 				   иначе с учётом подразделения
	 * @return список сотрудников для синхронизации
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Employee> getEmployeesForSync(Division division) {
		Query query = null;
		if (division == null) {
			query = entityManager
				.createQuery("from Employee as e where e.notToSync=:notToSync");
		} else {
			query = entityManager
				.createQuery("from Employee as e where e.notToSync=:notToSync and e.division=:division");
			query.setParameter("division", division);
		}
		query.setParameter("notToSync", false);
		List<Employee> result = query.getResultList();

		return result;
	}

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Employee> getAllEmployeesDivision(Division division) {
        Query query = null;
        if (division == null) {
            query = entityManager
                    .createQuery("FROM Employee");
        } else {
            query = entityManager
                    .createQuery("FROM Employee AS e WHERE e.division=:division");
            query.setParameter("division", division);
        }
        List<Employee> result = query.getResultList();

        return result;
    }
    
    /**
     * Возвращает список менеджеров для конкретного работника
     * @param employeeId
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> getRegionManager (Integer employeeId) {
        Query query = this.entityManager.createQuery(
                "select m.employee from  Employee e, Manager m " +
                "where e.id = :emp_id AND m.division.id = e.division.id " +
                "AND m.region.id = e.region.id");
        query.setParameter("emp_id", employeeId);
        List<Employee> result = query.getResultList();
        return result;
    }

	/**
	 * Возвращает список действующих сотрудников указанного подразделения
	 * @param division подразделениe
	 * @return список сотрудников подразделения с идентификатором division
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Employee> getEmployees(Division division) {
		Query query = null;

        Calendar cal = Calendar.getInstance();
        Date maxModDate = cal.getTime();

		if (division == null) {
			query = entityManager
//				.createQuery("from Employee as e where e.archived=:archived order by e.name");
                //действующий сотрудник-который на текущий момент либо не имеет endDate, либо endDate<=cuDate
                .createQuery(
                        "FROM Employee AS emp " +
                                "WHERE (emp.endDate IS NOT NULL AND emp.endDate >= :curDate) " +
                                "OR (emp.endDate IS NULL) " +
                        "ORDER BY emp.name"
                );
            query.setParameter("curDate", maxModDate);
		} else {
			query = entityManager
//				.createQuery("from Employee as e where e.division=:division and e.archived=:archived order by e.name");
                .createQuery(
                        "FROM Employee AS emp " +
                                "WHERE (emp.division=:division AND emp.endDate IS NOT NULL AND emp.endDate >= :curDate) " +
                                "OR (emp.division=:division AND emp.endDate IS NULL) " +
                                "ORDER BY emp.name"
                );

            query.setParameter("curDate", maxModDate);
            query.setParameter("division", division);

		}
		//query.setParameter("archived", false);

		List<Employee> result = query.getResultList();

		return result;
	}

	/**
	 * Сохраняет в базе нового сотрудника, либо обновляет данные уже
	 * существующего сотрудника.
	 * @param employee
	 */
	@Transactional
	public void setEmployee(Employee employee) {
		Employee empMerged = (Employee) entityManager.merge(employee);
		//logger.info("employee {} merged.", employee);
		entityManager.flush();
		logger.info("Persistence context synchronized to the underlying database.");
		logger.debug("Flushed Employee object id = {}", empMerged.getId());
	}

    public boolean isNotToSync(Employee employee) {
        Query query = null;
        query = entityManager
                .createQuery("FROM Employee AS e WHERE e.email=:email");
        query.setParameter("email", employee.getEmail().trim());
        List<Employee> result = query.getResultList();
        if((result!=null) && (!result.isEmpty()))
        {
            return result.get(0).isNotToSync();
        }
        return false;
    }


	/**
	 * Сохраняет в базе новых сотрудников, либо обновляет данные уже
	 * существующих сотрудников.
	 * @param employee
	 */
	@Transactional
	public StringBuffer setEmployees(List<Employee> employees) {
        StringBuffer trace = new StringBuffer();
        trace.append("");
		for (Employee emp : employees) {
            if(!isNotToSync(emp)) {
                if(emp.getId()!=null)   trace.append("Updated user: "+emp.getEmail()+" "+emp.getName()+"\n");
                    else trace.append("Added user: "+emp.getEmail()+" "+emp.getName()+"\n");

			    setEmployee(emp);
            }
            else
            {
                trace.append("\nUser: "+emp.getEmail()+" "+emp.getName()+" marked not_to_sync.(Need update)\n"+emp.toString()+"\n\n");
            }
		}
        trace.append("\n\n");
        return trace;
	}
	
	/**
	 * Ищет сотрудника по objectSid
	 */
	@Transactional(readOnly = true)
	public Employee findByObjectSid(String ObjectSid) {
		if ("".equals(ObjectSid)) { return null; }
		Employee result = null;
		Query query = entityManager
			.createQuery("from Employee as e where e.objectSid=:objectSid");
		query.setParameter("objectSid", ObjectSid);
		try {
			result = (Employee) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Employee with objectSid '{}' not found.", ObjectSid);
			return null;
		} catch (NonUniqueResultException e) {
			logger.warn("More than one employee with objectSid '{}' was found.", ObjectSid);
			return null;
		}
		return result;
	}
}