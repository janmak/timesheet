package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.*;
import com.aplana.timesheet.dao.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: bsirazetdinov
 * Date: 22.07.13
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = DataAccessException.class)
public class PlannedVacationService {
    private static final Logger logger = LoggerFactory.getLogger(PlannedVacationService.class);

    @Autowired
    private VacationDAO vacationDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    private SendMailService sendMailService;

    @Autowired
    protected ProjectService projectService;

    final static Date dateCurrent;
    final static Date dateAfter;
    final static Date dateBefore;
    static {
        final Calendar calendar2 = Calendar.getInstance();
        dateCurrent = calendar2.getTime();
        calendar2.add(Calendar.WEEK_OF_YEAR, 2);
        dateAfter = calendar2.getTime();
        calendar2.add(Calendar.WEEK_OF_YEAR, -4);
        dateBefore = calendar2.getTime();
    }

    public PlannedVacationService() {};




    /**
     * Получаем руководителей сотрудников
     */

    private  Map<Employee, List<Employee>> getEmployeeManagers(List<Employee> employees) {
        Map<Employee, List<Project>> employeeProjects = new HashMap<Employee, List<Project>>();
        for(Employee employee:employees) {
            List<Project> projects = new ArrayList<Project>();
            projects.addAll(projectService.getEmployeeProjectsFromTimeSheetByDates(dateBefore, dateCurrent, employee));
            projects.addAll(projectService.getEmployeeProjectPlanByDates(dateCurrent, dateAfter, employee));

            employeeProjects.put(employee, projects);
        }

        Map<Employee, List<Employee>> employeeManagers = new HashMap<Employee, List<Employee>>();

        for(Map.Entry<Employee, List<Project>> entry : employeeProjects.entrySet()) {
            List<Employee> manager = new ArrayList<Employee>();
            for (Project project:entry.getValue()) {
                manager.addAll(employeeDAO.getProjectManagers(project));
            }
            employeeManagers.put(entry.getKey(), manager);
        }
        return employeeManagers;
    }

    /**
     * Переворачиваем мапу
     */
    private  Map<Employee, List<Employee>> reverseEmployeeManagersToManagerEmployees(Map<Employee, List<Employee>> employeeManagers) {
        Map<Employee, List<Employee>> managerEmployees = new HashMap<Employee, List<Employee>>();

        for(Map.Entry<Employee, List<Employee>> entry : employeeManagers.entrySet()) {  // проходим по сотрудник - его менеджеры
            for (Employee manager:entry.getValue()) {                                   // просматриваем его менеджеров
                if(manager.equals(entry.getKey()))continue;
                if(managerEmployees.containsKey(manager)) {                             // если в мапе менеджер - сотрудники есть такой менеджер
                    if(!managerEmployees.get(manager).contains(entry.getKey()))         // если у этого менеджера нет этого сотрудника
                        managerEmployees.get(manager).add(entry.getKey());              // то добавляем
                } else {
                    List<Employee> employee = new ArrayList<Employee>();                // если в мапе менеджер - сотрудники нет такого менеджера
                    employee.add(entry.getKey());                                       // добавляем нового с единственным сотрудником
                    managerEmployees.put(manager, employee);
                }
            }
        }
        return managerEmployees;
    }


    private Employee getManager(Employee e) {
        return e.getManager();
    }

    /**
    * Получаем руководителей чьи "близкие" подчиненые планируют отпуска в ближайшие 2 недели
    */

    private Map<Employee, Set<Vacation>> getManagerEmployeesVacation() {
        final List<Employee> employees = employeeDAO.getEmployeeWithPlannedVacation(dateCurrent, dateAfter);

        Map<Employee, List<Employee>> employeeManagers = getEmployeeManagers(employees);

        Map<Employee, List<Employee>> managerEmployees = reverseEmployeeManagersToManagerEmployees(employeeManagers);



        Map<Employee, Set<Vacation>> managerEmployeesVacation = new HashMap<Employee, Set<Vacation>>();
        for(Map.Entry<Employee, List<Employee>> entry : managerEmployees.entrySet()) {
            Set<Vacation> vacations = new TreeSet<Vacation>();

            for (Employee employee:entry.getValue()) {
                vacations.addAll(vacationDAO.findVacations(employee.getId(), dateCurrent, dateAfter, null));
            }

            managerEmployeesVacation.put(entry.getKey(), new TreeSet<Vacation>(vacations));
            Employee manager = entry.getKey();
            while((manager = getManager(manager))!=null) {
                if(managerEmployeesVacation.containsKey(manager)) {
                    managerEmployeesVacation.get(manager).addAll(vacations);
                } else {
                    managerEmployeesVacation.put(manager, new TreeSet<Vacation>(vacations));
                }
            }
        }



        return managerEmployeesVacation;
    }

    @Transactional
    public void service() {
        Map<Employee, Set<Vacation>>   gf = getManagerEmployeesVacation();

        sendMailService.plannedVacationInfoMailing(gf);
    }


    public VacationDAO getVacationDAO() {
        return vacationDAO;
    }

    public void setVacationDAO(VacationDAO vacationDAO) {
        this.vacationDAO = vacationDAO;
    }

    public EmployeeDAO getEmployeeDAO() {
        return employeeDAO;
    }

    public void setEmployeeDAO(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }


}
