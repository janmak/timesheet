package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.LdapDAO;
import com.aplana.timesheet.dao.ProjectRolePermissionsDAO;
import com.aplana.timesheet.service.ProjectParticipantService;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import java.sql.Timestamp;
import java.util.*;

import static com.aplana.timesheet.util.ExceptionUtils.getRealLastCause;

@Service("employeeLdapService")
public class EmployeeLdapService extends AbstractServiceWithTransactionManagement {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeLdapService.class);
    private StringBuffer trace = new StringBuffer();

    @Autowired
    private DivisionService divisionService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private ProjectParticipantService projectParticipantService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private LdapDAO ldapDao;

    @Autowired
    private ProjectRolePermissionsDAO projectRolePermissionsDAO;


    private enum EmployeeType {
        EMPLOYEE, DIVISION_MANAGER, NEW_EMPLOYEE
    }

    /**
     * Отображение ProjectRole --> роль в системе списания занятости.
     */
    public String synchronizeOneEmployee(String email) {
        TransactionStatus transactionStatus = null;

        try {
            transactionStatus = getNewTransaction();

            String res = syncOneActiveEmployee(ldapDao, email);

            if (transactionStatus != null) {
                commit(transactionStatus);
            }

            return res;
        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
            return "Error in syncOneActiveEmployee";
        }
    }

    /**
     * Синхронизация одного сотрудника из LDAP
     * Только когда сотрудник есть в LDAP но не было в БД
     */

    private String syncOneActiveEmployee(LdapDAO ldapDao, String email) {
        logger.info("Start synchronize employee.");

        StringBuffer errors = new StringBuffer();
        //пользователь из LDAP по email
        EmployeeLdap employeeLdap = ldapDao.getEmployeeByEmail(email);
        //проверка не нужна, но нАдо
        if (employeeLdap != null) {
            //создаем нового сотрудника
            Employee employee = createAndFillEmployee(employeeLdap, errors, EmployeeType.NEW_EMPLOYEE);
            //добавляем в БД сотрудника
            if (errors.length() == 0) employeeService.setEmployee(employee);

            return errors.toString();
        } else {
            return null;
        }
    }

    public void synchronize() {
        trace.setLength(0);
        logger.info("Synchronization with ldap started.");
        trace.append("Synchronization with ldap started.\n\n");
        final TransactionStatus transactionStatus = getNewTransaction();
        try {

            final Map<Integer, Employee> divisionLeaders = new HashMap<Integer, Employee>();

            //Синхронизируем руководителей подразделений.
            syncDivisionLeaders(ldapDao, divisionLeaders);

            // Синхронизация подразделений
            syncDivisions(divisionLeaders);

            //Синхронизируем активных сотрудников.
            syncActiveEmployees(ldapDao);

            //Синхронизируем уволенных сотрудников
            syncDisabledEmployees(ldapDao);

            if (transactionStatus != null) {
                commit(transactionStatus);
            }
        } catch (Exception e) {
            logger.error("Error occured ", e);
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
            trace.append("Error occured ").append(getRealLastCause(e)).append("\n");
        }
        trace.append("Synchronization with ldap finished.\n\n");
        logger.info("Synchronization with ldap finished.");
    }

    /**
     * Синхронизует неактивных сотрудников из ldap с сотрудниками из базы
     * системы списания занятости.
     *
     * @param ldapDao – дао
     */
    private void syncDisabledEmployees(LdapDAO ldapDao) {
        logger.info("Start synchronize disabled employees.");
        trace.append("Start synchronize disabled employees.\n\n");

        final TransactionStatus transactionStatus = getNewTransaction();
        try {
            //берем удаленных сотрудников из LDAP
            List<EmployeeLdap> disabledEmployeesLdap = ldapDao.getDisabledEmployyes();
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
                        if (empDb.getEmail().equals(empLdap.getEmail())) {
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
                projectParticipantService.deactivateEmployeesRights(empsToSync);
            } else {
                logger.info("Nothing to sync.");
            }

            if (transactionStatus != null) {
                commit(transactionStatus);
            }

        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }
    }

    /**
     * Синхронизует руководителей подразделений из ldap
     * с базой данных системы списания занятости.
     *
     * @param ldapDao         - дао
     * @param divisionLeaders
     */
    private String syncDivisionLeaders(LdapDAO ldapDao, Map<Integer, Employee> divisionLeaders) {
        logger.info("Start synchronize division leaders.");
        trace.append("Start synchronize division leaders.\n\n");

        StringBuffer errors = new StringBuffer();
        final TransactionStatus transactionStatus = getNewTransaction();

        try {

            List<Division> divisions = divisionService.getDivisions();
            List<Employee> managersToSync = new ArrayList<Employee>();

            for (Division division : divisions) {
                List<EmployeeLdap> divLeader = ldapDao
                        .getDivisionLeader(division.getLeader(), division.getLdapName());
                logger.debug("Division '{}' has {} leader", division.getLdapName(), divLeader.size());

                if (divLeader.isEmpty()) {
                    continue;
                }

                EmployeeLdap managerLdap = divLeader.get(0);

                Employee manager = createAndFillEmployee(managerLdap, errors, EmployeeType.DIVISION_MANAGER);

                managersToSync.add(manager);
                divisionLeaders.put(division.getId(), manager);
            }
            if (!managersToSync.isEmpty()) {
                trace.append(employeeService.setEmployees(managersToSync));
            } else {
                logger.info("Nothing to sync.");
            }

            if (transactionStatus != null) {
                commit(transactionStatus);
            }

        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }

        return errors.toString();
    }

    private void syncDivisions(Map<Integer, Employee> divisionLeaders) {
        final String start = "Start synchronize divisions...";
        logger.info(start);
        trace.append(start).append("\n\n");

        final TransactionStatus transactionStatus = getNewTransaction();
        try {

            final List<Division> divisionsToSync = new ArrayList<Division>();

            for (Map.Entry<Integer, Employee> entry : divisionLeaders.entrySet()) {
                final Employee employee = entry.getValue();

                if (employee.getId() != null) {
                    final Division division = divisionService.find(entry.getKey());

                    division.setLeader(employee.getName());
                    division.setLeaderId(employee);

                    divisionsToSync.add(division);
                }
            }

            if (divisionsToSync.isEmpty()) {
                logger.info("Nothing to sync.");
            } else {
                trace.append(divisionService.setDivisions(divisionsToSync)).append("\n\n");
            }

            if (transactionStatus != null) {
                commit(transactionStatus);
            }

        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }
    }

    /**
     * Синхронизует активных сотрудников из ldap
     * с базой данных системы списания занятости.
     *
     * @param ldapDao - дао
     */
    private String syncActiveEmployees(LdapDAO ldapDao) {
        logger.info("Start synchronize active employees.");
        trace.append("Start synchronize active employees.\n\n");

        StringBuffer errors = new StringBuffer();

        final TransactionStatus transactionStatus = getNewTransaction();

        try {

            List<EmployeeLdap> employeesLdap;

            List<Employee> empsToSync = new ArrayList<Employee>();
            for (Division division : divisionService.getDivisions()) {
                employeesLdap = ldapDao.getEmployees(division.getLdapName());
                logger.debug("Ldap division {} has {} employees", division.getLdapName(), employeesLdap.size());

                for (EmployeeLdap employeeLdap : employeesLdap) {
                    Employee employee = createAndFillEmployee(employeeLdap, errors, EmployeeType.EMPLOYEE);

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

            if (transactionStatus != null) {
                commit(transactionStatus);
            }

        } catch (Exception e) {
            if (transactionStatus != null) {
                rollback(transactionStatus);
            }
        }

        return errors.toString();

    }

    private Employee createAndFillEmployee(
            EmployeeLdap employeeLdap,
            StringBuffer errors,
            EmployeeType employeeType
    ) {
        Employee employee = new Employee();

        employee.setName(employeeLdap.getDisplayName());
        employee.setEmail(StringUtils.trim(employeeLdap.getEmail()));
        employee.setLdap(employeeLdap.getLdapCn());

        // Роли из БД по умолчанию ставятся только для новых сотрудников
        if ((employee.getJob() != null) && (employeeType.equals(EmployeeType.NEW_EMPLOYEE))) {
            setEmployeePermission(employee);
        } else {
            employee.setPermissions(new HashSet<Permission>());
        }

        findAndFillJobField(employeeLdap, errors, employee);
        // важно установить Region и Division до установки Manager
        findAndFillRegionField(employeeLdap, errors, employee);
        findAndFillDivisionField(employeeLdap, employee, errors);

        switch (employeeType) {
            case NEW_EMPLOYEE:
                employee.setStartDate(DateTimeUtil.stringToTimestamp(DateTimeUtil.increaseDay(DateTimeUtil.currentDay())));
                break;

            case EMPLOYEE:
            case DIVISION_MANAGER:
                //Employee empInDbByObjectSid = employeeService.findByObjectSid( employeeLdap.getObjectSid() );
                Employee empInDbByMail = employeeService.findByEmail(employeeLdap.getEmail());
                if (empInDbByMail != null) {
                    employee.setId(empInDbByMail.getId());
                    employee.setStartDate(empInDbByMail.getStartDate());
                    employee.getPermissions().addAll(empInDbByMail.getPermissions());
                    employee.setJobRate(empInDbByMail.getJobRate());
                    employee.setManager2(empInDbByMail.getManager2());
                    //есть сотрудник в БД
                    //Миша: для существующих поле манагер не обновлялось, при этом остальные поля должны обновляться
                    //сперва должно сравниваться по полю LDAP, если нет то по полю EMAIL, если нет то считать что сотрудник новый и добавлять
                } else {
                    logger.error(employeeLdap.getEmail() + " not in db");
                    employee.setStartDate(DateTimeUtil.ldapDateToTimestamp(employeeLdap.getWhenCreated()));
                }
                break;
        }

        // У руководителя подразделения manager всегда отсутствует
        if (employeeType != EmployeeType.DIVISION_MANAGER) {
            findAndFillManagerField(employee, employeeLdap, errors);
        }

        return employee;
    }

    private void findAndFillDivisionField(EmployeeLdap employeeLdap, Employee employee, StringBuffer errors) {
        final Division division = divisionService.find(employeeLdap.getDepartment());

        if (division == null) {
            errors.append("division(department), ");
        } else {
            employee.setDivision(division);
        }
    }

    private void findAndFillManagerField(Employee employee, EmployeeLdap employeeLdap, StringBuffer errors) {
        final Division division = employee.getDivision();
        final Employee divisionLeader = (division == null) ? null : employeeService.find(division.getLeader());

        if (divisionLeader != null && employeeLdap.getLdapCn().equalsIgnoreCase(divisionLeader.getLdap())) {
            return;
        }

        // Сначала ищем руководителя из ldap
        Employee manager = employeeService.findByLdapName(employeeLdap.getManager());

        // Если не нашли и известно подразделение - берем руководителя подразделения
        if (manager == null) {
            manager = divisionLeader;
        }

        if (manager != null) {
            employee.setManager(manager);
        } else {
            errors.append("manager, ");
        }
    }

    void setEmployeePermission(Employee employee) {
        Set<Permission> permissions = new HashSet<Permission>();

        permissions.add(
                projectRolePermissionsDAO.getProjectRolePermission(
                        employee.getJob().getId()
                )
        );
        employee.setPermissions(permissions);
    }

    private void findAndFillRegionField(EmployeeLdap employeeLdap, StringBuffer errors, Employee employee) {
        //ищем регион сотруднику
        List<Region> list = regionService.getRegions();
        boolean isSetRegion = false;
        if (employeeLdap.getCity() != null) {
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
        }
        if (!isSetRegion) errors.append("region not found, ");
    }

    private void findAndFillJobField(EmployeeLdap employeeLdap, StringBuffer errors, Employee employee) {
        ProjectRole job = projectRoleService.find(employeeLdap.getTitle());
        if (job != null) {
            employee.setJob(job);
        } else {
            employee.setJob(projectRoleService.getUndefinedRole());
            // TODO fix?
//            errors.append("job not found for employee " + employeeLdap.getDisplayName());
        }
    }

    public String getTrace() {
        return this.trace.toString();
    }
}