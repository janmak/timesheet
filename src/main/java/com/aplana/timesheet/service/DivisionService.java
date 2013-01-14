package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.DivisionDAO;
import com.aplana.timesheet.dao.ProjectDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ldap.DivisionLdap;
import java.util.ArrayList;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("divisionService")
public class DivisionService {
	
	private static final Logger logger = LoggerFactory.getLogger(DivisionService.class);
	@Autowired
	DivisionDAO divisionDAO;
	@Autowired
	ProjectDAO projectDAO;
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private SendMailService sendMailService;

	public List<Division> getDivisions() {
		return divisionDAO.getDivisions();
	}

	/**
	 * Ищет активное подразделение с указанным именем.
	 * 
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	public Division find(String title) {
		return divisionDAO.find(title);
	}
	
	/**
	 * Ищет активное подразделение с указанным идентификатором.
	 * 
	 * @param division идентификатор подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	public Division find(Integer division) {
		return divisionDAO.find(division);
	}
	
	public StringBuffer synchronize() {
		List<DivisionLdap> withBadLeader = new ArrayList<DivisionLdap>();
		List<Division> fromDb = divisionDAO.getDivisionsAll();
		List<DivisionLdap> fromLdap = divisionDAO.getDivisionsFromLDAP();
		StringBuffer sb = new StringBuffer("");
		
		List<Division> forUpdate = new ArrayList<Division>();
		List<DivisionLdap> forAppend = new ArrayList<DivisionLdap>();
		
		sb.append("Start synchronization divisions.\n\n");
        for ( DivisionLdap ldapDiv : fromLdap ) {
            if ( ldapDiv.isLeaderVerified() ) {
                boolean exist = false;
                for ( Division dbDiv : fromDb ) {
                    String dbSid = dbDiv.getLdap_objectSid();
                    String ldapSid = ldapDiv.getLdapObjectSid();
                    if ( dbSid != null && dbSid.equals( ldapSid ) ) {
                        if ( dbDiv.isActive() ) {
                            if ( updateName( dbDiv, ldapDiv )|| updateLeader( dbDiv, ldapDiv ) ) {
                                forUpdate.add( dbDiv );
                            }
                        }
                        exist = true;
                        break;
                    }
                }
                if ( ! exist ) {
                    forAppend.add( ldapDiv );
                }
            } else {
                withBadLeader.add( ldapDiv );
            }
        }
		
		sb.append(add(forAppend));
		sb.append(divisionDAO.setDivision(forUpdate));
		
		sendMailService.sendAdminAlert(withBadLeader);
		return sb;
		
	}

	private boolean updateName(Division dbDiv, DivisionLdap ldapDiv) {
		if((dbDiv.getLdapName() == null) || !dbDiv.getLdapName().equals(ldapDiv.getLdap_name())) {
			dbDiv.setLdapName(ldapDiv.getLdap_name());
			return true;
		}
		return false;
	}
	private boolean updateLeader(Division dbDiv, DivisionLdap ldapDiv) {
		String dbObjectSid;
		String ldapObjectSid;
		boolean forceUpdate;
		if (dbDiv.getLeader() != null) {
			dbObjectSid = dbDiv.getLeader().getObjectSid();
			forceUpdate = false;
		} else {
			logger.error("For employee [{}] no objectSid", dbDiv.getLeader());
			forceUpdate = true;
			dbObjectSid = "";
		}

		if (ldapDiv.getLeaderSid() != null) {
			ldapObjectSid = ldapDiv.getLeaderSid();
		} else {
			logger.error("No leader's objectSid for division [{}]", ldapDiv.getLdap_name());
			return false;
		}

		if (forceUpdate || !(dbObjectSid.equals(ldapObjectSid))) {
			Employee employee = employeeService.findByObjectSid(ldapObjectSid);
			dbDiv.setLeader(employee);
			return true;
		}
		return false;
	}

	private StringBuffer add(List<DivisionLdap> forAppend) {
		StringBuffer sb = new StringBuffer();
		for(DivisionLdap ldapDiv : forAppend) {
			Division division = new Division();
			division.setName(ldapDiv.getLdap_name());
			division.setLdapName(ldapDiv.getLdap_name());
			division.setLdap_objectSid(ldapDiv.getLdapObjectSid());
			Employee leader = employeeService.findByObjectSid(ldapDiv.getLeaderSid());
			if(leader == null) {
				logger.debug("div name {}", ldapDiv.getLdap_name());
				logger.debug("not found leader sid {}", ldapDiv.getLeaderSid());
			}
			division.setLeader(leader);
			Set<Employee> employees = getEmpoyees(ldapDiv.getMembers());
			division.setEmployees(employees);
			division.setActive(true);
			sb.append(divisionDAO.setDivision(division));
		}
		return sb;
	}

	private Set<Employee> getEmpoyees(List<String> employeesObjetSid) {
		Set<Employee> result = new HashSet<Employee>();
		for(String objectSid : employeesObjetSid) {
			Employee hold = employeeService.findByObjectSid(objectSid);
			result.add(hold);
		}
		return result;
	}
}