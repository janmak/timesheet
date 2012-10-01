package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeDAO employeeDAO;

    /**
     * Возвращает сотрудника по идентификатору.
     * @param id идентификатор сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным id не найден.
     */
    public Employee find(Integer id) {
        return employeeDAO.find(id);
    }

    public Employee findByEmail(String mail)
    {
        return employeeDAO.findByEmail(mail);
    }

    public Employee findByLdapName(String ldapName) {
        return employeeDAO.findByLdapName(ldapName);
    }

    /**
     * Возвращает сотрудника по имени.
     * @param name имя сотрудника
     * @return объект класса Employee либо null, если сотрудник
     *         с указанным именем не найден.
     */
    public Employee find(String name) {
        return employeeDAO.find(name);
    }

    /**
     * Возвращает список действующих сотрудников.
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @return список действующих сотрудников.
     */
    public List<Employee> getEmployees(Division division) {
        return employeeDAO.getEmployees(division);
    }

    public List<Employee> getAllEmployeesDivision(Division division) {
        return employeeDAO.getAllEmployeesDivision(division);
    }

    /**
     * Возвращает список доступных для синхронизации с ldap сотрудников.
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @return список сотрудников для синхронизации
     */
    public List<Employee> getEmployeesForSync(Division division) {
        return employeeDAO.getEmployeesForSync(division);
    }

    /**
     * Сохраняет в базе нового сотрудника, либо обновляет данные уже
     * существующего сотрудника.
     * @param employee
     */
    public void setEmployee(Employee employee) {
        employeeDAO.setEmployee(employee);
    }

    /**
     * Сохраняет в базе новых сотрудников, либо обновляет данные уже
     * существующих сотрудников.
     * @param employees
     */
    public StringBuffer setEmployees(List<Employee> employees) {
        return employeeDAO.setEmployees(employees);
    }
	
	/**
	 * Ищет сотрудника по objectSid
	 */
	public Employee findByObjectSid(String ObjectSid) {
		return employeeDAO.findByObjectSid(ObjectSid);
	}
}