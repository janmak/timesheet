package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.enums.PermissionsEnum;
import com.aplana.timesheet.util.TimeSheetConstants;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    public VelocityEngine velocityEngine;
    @Autowired
    TimeSheetService timeSheetService;
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

    public Boolean isShowAll(HttpServletRequest request) {
        Boolean isShowAll = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TimeSheetConstants.COOKIE_SHOW_ALLUSER)) {
                    isShowAll = true;
                    break;
                }
            }
        }
        return isShowAll;
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
     * Возвращает список сотрудников
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @param filterFired Отоброжать ли уволленных сотрудников
     * @return список действующих сотрудников.
     */
    public List<Employee> getEmployees(Division division, Boolean filterFired) {
        List<Employee> result;
        if (filterFired == true) {
            result = employeeDAO.getAllEmployeesDivision(division);
        } else {
            result = employeeDAO.getEmployees(division);
        }
        return result;
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
        employeeDAO.save(employee);
    }

    /**
     * Сохраняет в базе новых сотрудников, либо обновляет данные уже
     * существующих сотрудников.
     * @param employees
     */
    public StringBuffer setEmployees(List<Employee> employees) {
        return employeeDAO.setEmployees(employees);
    }
    
    public List<Employee> getRegionManager(Integer employeeId) {
        return this.employeeDAO.getRegionManager(employeeId);
    }

    public List<Employee> getRegionManager(Integer regionId, Integer divisionId) {
        return employeeDAO.getRegionManager(regionId, divisionId);
    }

    public Double getWorkDaysOnIllnessWorked(Employee employee, Date beginDate, Date endDate){
        return employeeDAO.getWorkDaysOnIllnessWorked(employee, beginDate, endDate);
    }

    public boolean isEmployeeAdmin(Integer employeeId) {
        return isEmployeeHasPermissions(employeeId, PermissionsEnum.ADMIN_PERMISSION);
    }

    public boolean isEmployeeHasPermissions(Integer employeeId, final PermissionsEnum permissions) {
        final Employee employee = find(employeeId);

        return Iterables.any(employee.getPermissions(), new Predicate<Permission>() {
            @Override
            public boolean apply(@Nullable Permission permission) {
                return permission.getId().equals(permissions.getId());
            }
        });
    }

    public List<Employee> getDivisionEmployees(Integer divisionId, Date date, List<Integer> regionIds, List<Integer> projectRoleIds) {
        return employeeDAO.getDivisionEmployees(divisionId, date, regionIds, projectRoleIds);
    }
}