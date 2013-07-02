package com.aplana.timesheet.service;

import com.aplana.timesheet.constants.TimeSheetConstants;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.PermissionsEnum;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

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
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Employee find(String name) {
        return employeeDAO.find(name);
    }

    /**
     * Возвращает список сотрудников
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @param filterFired Отоброжать ли уволенных сотрудников
     * @return список действующих сотрудников.
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployees(Division division, Boolean filterFired) {
        List<Employee> result;
        if (filterFired == true) {
            result = employeeDAO.getAllEmployeesDivision(division);
        } else {
            result = employeeDAO.getEmployees(division);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployeesDivision(Division division) {
        return employeeDAO.getAllEmployeesDivision(division);
    }

    /**
     * Возвращает список доступных для синхронизации с ldap сотрудников.
     * @param division Если null, то поиск осуществляется без учета подразделения,
     *                 иначе с учётом подразделения
     * @return список сотрудников для синхронизации
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesForSync(Division division) {
        return employeeDAO.getEmployeesForSync(division);
    }

    /**
     * Сохраняет в базе нового сотрудника, либо обновляет данные уже
     * существующего сотрудника.
     * @param employee
     */
    @Transactional
    public void setEmployee(Employee employee) {
        employeeDAO.save(employee);
    }

    /**
     * Сохраняет в базе новых сотрудников, либо обновляет данные уже
     * существующих сотрудников.
     * @param employees
     */
    @Transactional
    public StringBuffer setEmployees(List<Employee> employees) {
        return employeeDAO.setEmployees(employees);
    }

    @Transactional(readOnly = true)
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

    public List<Employee> getEmployees() {
        return employeeDAO.getEmployees();
    }

    public Employee getEmployeeFromBusinessTrip(Integer reportId) {
        return employeeDAO.tryGetEmployeeFromBusinessTrip(reportId);
    }

    public Employee getEmployeeFromIllness(Integer reportId) {
        return employeeDAO.tryGetEmployeeFromIllness(reportId);
    }

    public Boolean isLineManager(Employee employee) {
        return employeeDAO.isLineManager(employee);
    }

    /**
     * Получаем список менеджеров, которые еще не приняли решение по отпуску
     */
    public List<Employee> getProjectManagersThatDoesntApproveVacation(Project project, Vacation vacation) {
        return employeeDAO.getProjectManagersThatDoesntApproveVacation(project, vacation);
    }

    /**
     * получаем список младших (тимлиды, ведущие аналитики) руководителей проектов, на которых сотрудник планирует свою занятость в даты отпуска.
     */
    public Map<Employee, List<Project>> getJuniorProjectManagersAndProjects(List<Project> employeeProjects, final Vacation vacation) {
        Map<Employee, List<Project>> managersAndProjects = new HashMap<Employee, List<Project>>();
        for (Project project : employeeProjects) {
            if (! vacation.getEmployee().getId().equals(project.getManager().getId())) {        //если оформляющий отпуск - руководитель этого проекта, то по этому проекту писем не рассылаем
                List<Employee> managers = getProjectManagersThatDoesntApproveVacation(project, vacation);
                for (Employee manager : managers) {
                    if (! manager.getId().equals(vacation.getEmployee().getId())) {       //отсеиваем сотрудника, если он сам руководитель
                        if (managersAndProjects.containsKey(manager)) {
                            List<Project> projects = managersAndProjects.get(manager);
                            projects.add(project);
                        } else {
                            ArrayList<Project> projectArrayList = new ArrayList<Project>(1);
                            projectArrayList.add(project);
                            managersAndProjects.put(manager, projectArrayList);
                        }
                    }
                }
            }
        }

        return managersAndProjects;
    }

    public List<Employee> getEmployeesForSync() {
        return employeeDAO.getEmployeesForSync();
    }

    public List<Employee> getManagerListForAllEmployee(){
        return employeeDAO.getManagerListForAllEmployee();
    }

    public List<Integer> getEmployeesIdByDivisionManagerRegion(Integer divisionId, Integer managerId, Integer regionId){
        return employeeDAO.getEmployeesIdByDivisionManagerRegion(divisionId, managerId, regionId);
    }

    public List<Integer> getEmployeesIdByDivisionRegion(Integer divisionId, Integer regionId){
        return employeeDAO.getEmployeesIdByDivisionRegion(divisionId, regionId);
    }

    public List<Integer> getEmployeesIdByDivisionManager(Integer divisionId, Integer managerId){
        return employeeDAO.getEmployeesIdByDivisionManager(divisionId, managerId);
    }

    /**
     * Получаем список линейных руководителей с дублями
     * @param employee
     * @return
     */
    public List<Employee> getLinearEmployees(Employee employee) {
        List<Employee> employees = new ArrayList<Employee>();
        Employee manager = employee.getManager();
        if(manager !=null){
            employees.add(manager);
            employees.addAll(getLinearEmployees(manager));
        }
        /* APLANATS-865
        Employee manager2 = employee.getManager2();
        if(manager2 !=null){
            employees.add(manager2);
            employees.addAll(getLinearEmployees(manager2));
        }*/
        return employees;
    }

    /**
     * Ищет сотрудника по LdapCN
     */
    public Employee findByLdapCN(String LdapCN) {
        return employeeDAO.findByLdapCN(LdapCN);
    }
}