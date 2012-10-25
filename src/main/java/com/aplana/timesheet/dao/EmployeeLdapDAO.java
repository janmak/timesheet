package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.aplana.timesheet.dao.entity.ldap.LdapAplanaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.PresentFilter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

public class EmployeeLdapDAO {
	private static final Logger logger = LoggerFactory.getLogger(EmployeeLdapDAO.class);

	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

    public EmployeeLdap getEmployee(String email) {
        logger.info("Getting Employee {} from LDAP",email);

        AndFilter andFilter = new AndFilter();
        andFilter.and(new EqualsFilter("mail",email));
        logger.debug("LDAP Query {}", andFilter.encode());
        List<EmployeeLdap> employees =ldapTemplate.search("",andFilter.encode(),new EmployeeAttributeMapper());
        return employees.get(0);
    }



	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getEmployyes(String department) {
		logger.info("Getting Employees from LDAP.");
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("department", department));
		andFilter.and(new PresentFilter("memberOf"));
		logger.debug("LDAP Query {}", andFilter.encode());
		List<EmployeeLdap> employees = ldapTemplate.search("", andFilter.encode(), new EmployeeAttributeMapper());
		logger.debug("Employees size is {}", employees.size());
		if(employees.size()>0)logger.debug("Employee {} City is {}", employees.get(0).getDisplayName(), employees.get(0).getCity());
		return employees;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getDisabledEmployyes() {
		logger.info("Getting Disabled Employees from LDAP.");
		DistinguishedName dn = new DistinguishedName();
	    dn.add("ou", "Disabled Users");
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectClass", "person"));
		logger.debug("LDAP Query {}", andFilter.encode());
		return ldapTemplate.search(dn, andFilter.encode(), new EmployeeAttributeMapper());
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getDivisionLeader(String name, String division) {
		logger.info("Getting Division Leaders from LDAP.");
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("displayName", name));
		andFilter.and(new EqualsFilter("department", division));
		logger.debug("LDAP Query {}", andFilter.encode());
		return ldapTemplate.search("", andFilter.encode(), new EmployeeAttributeMapper());
	}

	public EmployeeLdap getEmployeeByName(String name) {
		try {
			AndFilter andFilter = new AndFilter();
            name = name.replaceAll("/", ",");
			andFilter.and(new EqualsFilter("distinguishedName", name));
				List<EmployeeLdap> employees = (List<EmployeeLdap>)ldapTemplate.search("" , andFilter.encode(), new EmployeeAttributeMapper());
			if((employees != null) && !employees.isEmpty())
				return employees.get(0);
		}
		catch (NameNotFoundException e) { logger.debug("Not found: " + name); }
		return null;
	}



    private class EmployeeAttributeMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			EmployeeLdap employee = new EmployeeLdap();
			Attribute department = attributes.get("department");
			if (department != null) {
				employee.setDepartment(((String) department.get()));
			}			
			Attribute displayName = attributes.get("displayName");
			if (displayName != null) {
				employee.setDisplayName(((String) displayName.get()));
			}
			Attribute mail = attributes.get("mail");
			if (mail != null) {
				employee.setMail(((String) mail.get()));
			}
			Attribute manager  = attributes.get("manager");
			if (manager != null) {
				employee.setManager(((String) manager.get()));
			}
			Attribute title = attributes.get("title");
			if (title != null) {
				employee.setTitle(((String) title.get()));
			}
			Attribute whenCreated = attributes.get("whenCreated");
			if (whenCreated != null) {
				employee.setWhenCreated((String) whenCreated.get());
			}
			Attribute city = attributes.get("l");
			if (city != null) {
				employee.setCity((String) city.get());
			}
			Attribute ldapCn = attributes.get("distinguishedname");
			if(ldapCn != null)
				employee.setLdapCn(ldapCn.get().toString());
			
			Attribute objectSid = attributes.get("objectSid");
            if (objectSid != null)
			    employee.setObjectSid(LdapAplanaUtils.getSidAttribute(objectSid));

			return employee;
		}
	}
}