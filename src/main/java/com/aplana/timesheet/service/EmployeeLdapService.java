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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	
	/**
	 * Отображение ProjectRole --> роль в системе списания занятости.
	 */
	//private static final Map<Integer, Integer> ROLES_MAP = new HashMap<Integer, Integer>();

    public String synchronizeOneEmployee(String email) {
        return syncOneActiveEmployee(employeeLdapDao,email);
    }

	public void synchronize() {
		trace.setLength(0);
		logger.info("Synchronization with ldap started.");
		trace.append("Synchronization with ldap started.\n\n");
		try {
			//Resource resource = new ClassPathResource("WEB-INF/springldap.xml");
			//BeanFactory factory = new XmlBeanFactory(resource);
			//EmployeeLdapDAO employeeLdapDao = (EmployeeLdapDAO) factory.getBean("ldapEmployee");

            //Синхронизируем руководителей подразделений.
			syncDivisionLeaders(employeeLdapDao);

			//Синхронизируем активных сотрудников.
		    syncActiveEmployees(employeeLdapDao);

//			//Синхронизируем уволенных сотрудников
			syncDisabledEmployees(employeeLdapDao);
		} catch (DataAccessException e) {
			logger.error("Error occured " + e.getCause());
            trace.append("Error occured " + e.getCause());
		}
		trace.append("Synchronization with ldap finished.\n\n");
		logger.info("Synchronization with ldap finished.");
	}
	
	/**
	 * Синхронизует неактивных сотрудников из ldap с сотрудниками из базы
	 * системы списания занятости.
	 * @param disabledEmps
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
            if (empDb.getEndDate()==null)
            {
                for (EmployeeLdap empLdap : disabledEmployeesLdap) {
                    //если совпадают имена и подразделения
                    if (empDb.getName().equals(empLdap.getDisplayName())
                            && empDb.getDivision().getLdapName().equals(empLdap.getDepartment())) {
                        logger.debug("Employee {} disabled in ldap, but active in db.", empLdap.getDisplayName());
                        logger.debug("And was marked like archived.");

                        //archived больше не используется
                        //empDb.setArchived(true);

                        //проставляем дату увольнения
                        //дата проставляется текущей датой синхронизации
                        //тк в LDAP нет точной даты увольнения
                        empDb.setEndDate(new Timestamp((new Date()).getTime()));
                        empsToSync.add(empDb);
                        //trace.append("Add disable user:").append(empDb.getName()+" "+empDb.getEmail()).append("\n\n");
                    }
                }
            }
		}

        //синхронизирует сотрудников, если есть что синхронизировать
		if (empsToSync.size() > 0) { trace.append(employeeService.setEmployees(empsToSync)); }
		else { logger.info("Nothing to sync."); }
	}
	
	/**
	 * Синхронизует руководителей подразделений из ldap 
	 * с базой данных системы списания занятости.
	 * @param employeeLdapDao
	 */
	private void syncDivisionLeaders(EmployeeLdapDAO employeeLdapDao) {
		logger.info("Start synchronize division leaders.");
		trace.append("Start synchronize division leaders.\n\n");
		List<Division> divisions = divisionService.getDivisions();
		List<Employee> managersToSync = new ArrayList<Employee>();
		for (Division division : divisions) {
            //Division division=divisions.get(0);
			List<EmployeeLdap> divLeader = employeeLdapDao
				.getDivisionLeader(division.getLeader(), division.getLdapName());
			logger.debug("Division '{}' has {} leader", division.getLdapName(), divLeader.size());
            Employee manager = new Employee();
            EmployeeLdap managerLdap = divLeader.get(0);

            manager.setName(managerLdap.getDisplayName());
            manager.setDivision(divisionService.find(managerLdap.getDepartment()));
            manager.setStartDate(DateTimeUtil.ldapDateToTimestamp(managerLdap.getWhenCreated()));
            ProjectRole job = projectRoleService.find(managerLdap.getTitle());

            if (job != null) {
                manager.setJob(job);
            } else {
                manager.setJob(projectRoleService.getUndefinedRole());
            }

            manager.setManager(null);
            manager.setEmail(managerLdap.getMail());
            manager.setLdap(managerLdap.getLdapCn());
            List<Region> list =  regionService.getRegions();
            loop:
            for(Region reg: list){
                String[] regs = reg.getLdapCity().split(",");
                for(String region: regs){
                    if(managerLdap.getCity().contains(region.trim())){
                        manager.setRegion(regionService.findRegionByCity(region.trim()));
                        continue loop;
                    }
                }
            }
            Employee emp = employeeService.findByEmail(managerLdap.getMail());
            manager.setRole(projectRoleService.getSysRole(manager.getJob().getId()).getSysRoleId());

            //есть лидер в БД
			if (emp != null) {
                manager.setId(employeeService.findByEmail(manager.getEmail()).getId());
                if(manager.getJob()==null) manager.setJob(projectRoleService.getUndefinedRole());
            }
            managersToSync.add(manager);
		}
		if (managersToSync.size() > 0) { trace.append(employeeService.setEmployees(managersToSync)); }
		else { logger.info("Nothing to sync."); }
	}

    /**
     * Синхронизация одного сотрудника из LDAP
     * Только когда сотрудник есть в LDAP но не было в БД
     */
    private  String syncOneActiveEmployee(EmployeeLdapDAO employeeLdapDao,String email) {
        logger.info("Start synchronize employee.");
        String errors="";
        //пользователь из LDAP по email
        EmployeeLdap employeeLdap = employeeLdapDao.getEmployee(email);
        //проверка не нужна, но нАдо
        if(employeeLdap!=null) {
            //создаем нового сотрудника
            Employee employee = new Employee();
            employee.setName(employeeLdap.getDisplayName());
            employee.setEmail(employeeLdap.getMail().trim());
            employee.setDivision(divisionService.find(employeeLdap.getDepartment()));
            employee.setLdap(employeeLdap.getLdapCn());
            employee.setStartDate(DateTimeUtil.stringToTimestamp(DateTimeUtil.increaseDay(DateTimeUtil.currentDay())));

            ProjectRole job = projectRoleService.find(employeeLdap.getTitle());
            if (job != null) {
                employee.setJob(job);
            } else {
                employee.setJob(projectRoleService.getUndefinedRole());
                errors+="job, ";
            }
            //employee.setRole(projectJobService.find(employee.getJob().getId()).getIdJob());
            employee.setRole(projectRoleService.getSysRole(employee.getJob().getId()).getSysRoleId());
            //ищем регион сотруднику
            List<Region> list =  regionService.getRegions();
            boolean isSetRegion=false;
            loop:
            for(Region reg: list){
                String[] regs = reg.getLdapCity().split(",");
                for(String region: regs){
                    if(employeeLdap.getCity().contains(region.trim())){
                        employee.setRegion(regionService.findRegionByCity(region.trim()));
                        isSetRegion=true;
                        continue loop;
                    }
                }
            }
            if(!isSetRegion) errors+="region, ";

            //берем подразделение и менеджера по подразделению
            String department=employeeLdap.getDepartment();
            if(department!=null && department!="")
            {
                //если не найдет внутри find стоит
                Division division=divisionService.find(department);
                if(division==null) errors+="division(department), ";
                Employee manager = employeeService.find(division.getLeader());
                if(manager==null) errors+="manager, ";
                employee.setManager(manager);
            }
            else {
                errors+="division(department), manager,";
            }

            //добавляем в БД сотрудника
            if(errors=="") employeeService.setEmployee(employee);
            return errors;
        }
        else {
            return null;
        }
    }


	/**
	 * Синхронизует активных сотрудников из ldap 
	 * с базой данных системы списания занятости.
	 * @param employeeLdapDao
	 */
    private void syncActiveEmployees(EmployeeLdapDAO employeeLdapDao) {
        logger.info("Start synchronize active employees.");
        trace.append("Start synchronize active employees.\n\n");
        List<EmployeeLdap> employeesLdap = null;
        List<Employee> empsToSync = new ArrayList<Employee>();
        for (Division division : divisionService.getDivisions()) {
            employeesLdap = employeeLdapDao.getEmployyes(division.getLdapName());
            logger.debug("Ldap division {} has {} employees", division.getLdapName(), employeesLdap.size());

            for(EmployeeLdap employeeLdap:employeesLdap) {

                Employee employee=new Employee();

                employee.setName(employeeLdap.getDisplayName());
                employee.setDivision(divisionService.find(employeeLdap.getDepartment()));
                employee.setStartDate(DateTimeUtil.ldapDateToTimestamp(employeeLdap.getWhenCreated()));
                ProjectRole job = projectRoleService.find(employeeLdap.getTitle());

                if (job != null)
                    employee.setJob(job);
                 else
                    employee.setJob(projectRoleService.getUndefinedRole());

                employee.setEmail(employeeLdap.getMail());
                employee.setLdap(employeeLdap.getLdapCn());
                List<Region> list =  regionService.getRegions();
                loop:
                for(Region reg: list){
                    String[] regs = reg.getLdapCity().split(",");
                    for(String region: regs){
                        if(employeeLdap.getCity().contains(region.trim())){
                            employee.setRegion(regionService.findRegionByCity(region.trim()));
                            continue loop;
                        }
                    }
                }
                employee.setRole(projectRoleService.getSysRole(employee.getJob().getId()).getSysRoleId());
                Employee empInDbByLdapName=employeeService.findByLdapName(employeeLdap.getLdapCn());
                Employee empInDbByName = employeeService.findByEmail(employeeLdap.getMail());

                //есть сотрудник в БД
                //Миша: для существующих поле манагер не обновлялось, при этом остальные поля должны обновляться
                //сперва должно сравниваться по полю LDAP, если нет то по полю EMAIL, если нет то считать что сотрудник новый и добавлять
                if(empInDbByLdapName != null) {
                    employee.setId(empInDbByLdapName.getId());
                    employee.setManager(empInDbByLdapName.getManager());
                }
                else if (empInDbByName != null) {
                    employee.setId(empInDbByName.getId());
                    employee.setManager(empInDbByName.getManager());
                }
                //если сотрудник не в БД
                else {
                    logger.info(employeeLdap.getMail()+"no in db");
                    Employee manager = employeeService.find(division.getLeader());
                    employee.setManager(manager);
                }
                if(employee.getManager()!=null)
                empsToSync.add(employee);
            }
        }
        if (empsToSync.size() > 0) {
            logger.debug("There are {} new employees", empsToSync.size());
            trace.append(employeeService.setEmployees(empsToSync));
        } else { logger.info("Nothing to sync."); }
    }

	public String getTrace() {
		return this.trace.toString();
	}
	
	// Role mapping according to APLANATS-341
    //удалено APLANATS-374
//	static {
//		ROLES_MAP.put(ProjectRoleService.PROJECT_MANAGER,     Employee.EMPLOYEE_ROLE_MANAGER);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_ANALYST,     Employee.EMPLOYEE_ROLE_USER);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_LEADER,      Employee.EMPLOYEE_ROLE_MANAGER);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_DESIGNER,    Employee.EMPLOYEE_ROLE_USER);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_DEVELOPER,   Employee.EMPLOYEE_ROLE_USER);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_SYSENGINEER, Employee.EMPLOYEE_ROLE_ADMIN);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_TESTER,      Employee.EMPLOYEE_ROLE_USER);
//		ROLES_MAP.put(ProjectRoleService.PROJECT_TECH_WRITER, Employee.EMPLOYEE_ROLE_USER);
//		ROLES_MAP.put(ProjectRoleService.CENTER_MANAGER,      Employee.EMPLOYEE_ROLE_MANAGER);
//		ROLES_MAP.put(ProjectRoleService.NOT_DEFINED_JOB,     Employee.EMPLOYEE_ROLE_USER);
//		ROLES_MAP.put(ProjectRoleService.QUALITY_MANAGER,     Employee.EMPLOYEE_ROLE_MANAGER);
//		ROLES_MAP.put(ProjectRoleService.GENERAL_MANAGER,     Employee.EMPLOYEE_ROLE_MANAGER);
//	}
	
//	private static boolean mapRole(Employee employee) {
//		if (employee.getJob() != null && employee.getJob().getId() != null) {
//		    employee.setRole(ROLES_MAP.get(employee.getJob().getId()));
//            return true;
//		} else {
//			employee.setRole(ROLES_MAP.get(ProjectRoleService.NOT_DEFINED_JOB));
//            return false;
//		}
//        return false;
//	}

}