package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.EmployeeLdapDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.dao.entity.Region;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.aplana.timesheet.util.DateTimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service("employeeLdapService")
public class EmployeeLdapService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeLdapService.class);
    private StringBuffer trace = new StringBuffer();

    @Autowired
    private DivisionService divisionService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private EmployeeLdapDAO employeeLdapDao;


    private enum EmployeeType {
        EMPLOYEE, MANAGER, NEW_EMPLOYEE
    }

    /**
     * Отображение ProjectRole --> роль в системе списания занятости.
     */
    public String synchronizeOneEmployee(String email) {
        return syncOneActiveEmployee(employeeLdapDao, email);
    }

    public void synchronize() {
        trace.setLength(0);
        logger.info("Synchronization with ldap started.");
        trace.append("Synchronization with ldap started.\n\n");
        try {
            //Синхронизируем руководителей подразделений.
            syncDivisionLeaders(employeeLdapDao);

            //Синхронизируем активных сотрудников.
            syncActiveEmployees(employeeLdapDao);

            //Синхронизируем уволенных сотрудников
            syncDisabledEmployees(employeeLdapDao);
        } catch (DataAccessException e) {
            logger.error("Error occured " + e.getCause());
            trace.append("Error occured ").append(e.getCause());
        }
        trace.append("Synchronization with ldap finished.\n\n");
        logger.info("Synchronization with ldap finished.");
    }

    /**
     * Синхронизует неактивных сотрудников из ldap с сотрудниками из базы
     * системы списания занятости.
     *
     * @param employeeLdapDao – дао
     */
    private void syncDisabledEmployees(EmployeeLdapDAO employeeLdapDao) {
        logger.info("Start synchronize disabled employees.");
        trace.append("Start synchronize disabled employees.\n\n");

        //берем удаленных сотрудников из LDAP
        List<EmployeeLdap> disabledEmployeesLdap = employeeLdapDao.getDisabledEmployyes();
        logger.debug("disabled employees ldap size = {}", disabledEmployeesLdap.size());

        //берем сотрудников из БД
        List<Employee> employeesDb = employeeService.getEmployeesForSync(null);

        //список сотрудников которые будут синхронизироваться
        List<Employee> empsToSync = new ArrayList<Employee>();

        for (Employee empDb : employeesDb) {
            //если в базе сотрудник помечен как НЕ уволенный(нет даты увольнения)
            //archived больше не используется
            if (empDb.getEndDate() == null) {
                for (EmployeeLdap empLdap : disabledEmployeesLdap) {
                    if (empDb.getEmail().equals(empLdap.getMail())) {
                        logger.debug("Employee {} disabled in ldap, but active in db.", empLdap.getDisplayName());
                        logger.debug("And was marked like archived.");

                        //проставляем дату увольнения
                        //дата проставляется текущей датой синхронизации
                        //тк в LDAP нет точной даты увольнения
                        empDb.setEndDate(new Timestamp((new Date()).getTime()));
                        empsToSync.add(empDb);
                    }
                }
            }
        }

        //синхронизирует сотрудников, если есть что синхронизировать
        if (!empsToSync.isEmpty()) {
            trace.append(employeeService.setEmployees(empsToSync));
        } else {
            logger.info("Nothing to sync.");
        }
    }

    /**
     * Синхронизует руководителей подразделений из ldap
     * с базой данных системы списания занятости.
     *
     * @param employeeLdapDao - дао
     */
    private String syncDivisionLeaders(EmployeeLdapDAO employeeLdapDao) {
        logger.info("Start synchronize division leaders.");
        trace.append("Start synchronize division leaders.\n\n");
        List<Division> divisions = divisionService.getDivisions();
        List<Employee> managersToSync = new ArrayList<Employee>();
        StringBuffer errors = new StringBuffer();
        for (Division division : divisions) {
            List<EmployeeLdap> divLeader = employeeLdapDao
                    .getDivisionLeader(division.getLeader(), division.getLdapName());
            logger.debug("Division '{}' has {} leader", division.getLdapName(), divLeader.size());

            EmployeeLdap managerLdap = divLeader.get(0);

            Employee manager = createAndFillEmployee(managerLdap, division, EmployeeType.MANAGER, errors);

            managersToSync.add(manager);
        }
        if (!managersToSync.isEmpty()) {
            trace.append(employeeService.setEmployees(managersToSync));
        } else {
            logger.info("Nothing to sync.");
        }

        return errors.toString();
    }

    /**
     * Синхронизация одного сотрудника из LDAP
     * Только когда сотрудник есть в LDAP но не было в БД
     */
    private String syncOneActiveEmployee(EmployeeLdapDAO employeeLdapDao, String email) {
        logger.info("Start synchronize employee.");

        StringBuffer errors = new StringBuffer();
        //пользователь из LDAP по email
        EmployeeLdap employeeLdap = employeeLdapDao.getEmployee(email);
        //проверка не нужна, но нАдо
        if (employeeLdap != null) {
            //создаем нового сотрудника
            Employee employee = createAndFillEmployee(employeeLdap, errors);
            //добавляем в БД сотрудника
            if (errors.length() == 0) employeeService.setEmployee(employee);

            return errors.toString();
        } else {
            return null;
        }
    }

    /**
     * Синхронизует активных сотрудников из ldap
     * с базой данных системы списания занятости.
     *
     * @param employeeLdapDao - дао
     */
    private String syncActiveEmployees(EmployeeLdapDAO employeeLdapDao) {
        logger.info("Start synchronize active employees.");
        trace.append("Start synchronize active employees.\n\n");
        List<EmployeeLdap> employeesLdap;

        StringBuffer errors = new StringBuffer();

        List<Employee> empsToSync = new ArrayList<Employee>();
        for (Division division : divisionService.getDivisions()) {
            employeesLdap = employeeLdapDao.getEmployyes(division.getLdapName());
            logger.debug("Ldap division {} has {} employees", division.getLdapName(), employeesLdap.size());

            for (EmployeeLdap employeeLdap : employeesLdap) {
                Employee employee = createAndFillEmployee(employeeLdap, division, EmployeeType.EMPLOYEE, errors);

                if (employee.getManager() != null) {
                    empsToSync.add(employee);
                }
            }
        }
        if (!empsToSync.isEmpty()) {
            logger.debug("There are {} new employees", empsToSync.size());
            trace.append(employeeService.setEmployees(empsToSync));
        } else {
            logger.info("Nothing to sync.");
        }

        return errors.toString();
    }

    private Employee createAndFillEmployee(
            EmployeeLdap employeeLdap,
            Division division,
            EmployeeType employeeType,
            StringBuffer errors
    ) {
        return createAndFillEmployee(employeeLdap, division, errors, employeeType);
    }

    private Employee createAndFillEmployee(
            EmployeeLdap employeeLdap,
            StringBuffer errors
    ) {
        return createAndFillEmployee(employeeLdap, null, errors, EmployeeType.NEW_EMPLOYEE);
    }

    private Employee createAndFillEmployee(
            EmployeeLdap employeeLdap,
            Division division,
            StringBuffer errors,
            EmployeeType employeeType
    ) {
        Employee employee = new Employee();

        employee.setName(employeeLdap.getDisplayName());
        if (employeeLdap.getMail() != null) // APLANATS-433
            employee.setEmail(employeeLdap.getMail().trim());
        employee.setLdap(employeeLdap.getLdapCn());

        ProjectRole role = employee.getJob();
        if (role != null) {
            ProjectRole sysRole = projectRoleService.getSysRole(employee.getJob().getId());
            if (sysRole != null) {
                employee.setRole(sysRole.getSysRoleId());
            }
        }

        findAndFillJobField(employeeLdap, errors, employee);
        findAndFillRegionField(employeeLdap, errors, employee);

        switch (employeeType) {
            case NEW_EMPLOYEE:
                employee.setStartDate(DateTimeUtil.stringToTimestamp(DateTimeUtil.increaseDay(DateTimeUtil.currentDay())));

                // Устанавливаем подразделение для сотрудника
                employee.setDivision(divisionService.find(employeeLdap.getDepartment()));

                //берем подразделение и менеджера по подразделению
                String department = employeeLdap.getDepartment();
                if (department != null && !department.equals("")) {
                    //если не найдет внутри find стоит
                    Division findedDivision = divisionService.find(department);
                    if (findedDivision == null) {
                        errors.append("division(department), ");
                    } else {
                        Employee manager = employeeService.find(findedDivision.getLeader());
                        if (manager == null) {
                            errors.append("manager, ");
                        } else {
                            employee.setManager(manager);
                        }
                    }
                } else {
                    errors.append("division(department), manager,");
                }
                break;
            case EMPLOYEE:
            case MANAGER:
                employee.setDivision(divisionService.find(employeeLdap.getDepartment()));

                //Employee empInDbByObjectSid = employeeService.findByObjectSid( employeeLdap.getObjectSid() );
                Employee empInDbByMail = employeeService.findByEmail(employeeLdap.getMail());
                if (empInDbByMail != null) {
                    employee.setId(empInDbByMail.getId());
                    employee.setStartDate(empInDbByMail.getStartDate());
                    employee.setManager(empInDbByMail.getManager());
                    //есть сотрудник в БД
                    //Миша: для существующих поле манагер не обновлялось, при этом остальные поля должны обновляться
                    //сперва должно сравниваться по полю LDAP, если нет то по полю EMAIL, если нет то считать что сотрудник новый и добавлять
                } else {
                    logger.error(employeeLdap.getMail() + "no in db"); //TODO оповещение админа?
                    Employee manager = employeeService.find(division.getLeader());
                    employee.setStartDate(DateTimeUtil.ldapDateToTimestamp(employeeLdap.getWhenCreated()));
                    employee.setManager(manager);
                }

                if (employeeType == EmployeeType.MANAGER) {
                    employee.setManager(null);
                }
                break;
        }

        return employee;
    }

    private void findAndFillRegionField(EmployeeLdap employeeLdap, StringBuffer errors, Employee employee) {
        //ищем регион сотруднику
        List<Region> list = regionService.getRegions();
        boolean isSetRegion = false;
        loop:
        for (Region reg : list) {
            String[] regs = reg.getLdapCity().split(",");
            for (String region : regs) {
                if (employeeLdap.getCity().contains(region.trim())) {
                    employee.setRegion(regionService.findRegionByCity(region.trim()));
                    isSetRegion = true;
                    continue loop;
                }
            }
        }
        if (!isSetRegion) errors.append("region, ");
    }

    private void findAndFillJobField(EmployeeLdap employeeLdap, StringBuffer errors, Employee employee) {
        ProjectRole job = projectRoleService.find(employeeLdap.getTitle());
        if (job != null) {
            employee.setJob(job);
        } else {
            employee.setJob(projectRoleService.getUndefinedRole());
            errors.append("job, ");
        }
    }

    public String getTrace() {
        return this.trace.toString();
    }
}