package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.ldap.EmployeeLdap;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.*;
import org.springframework.ldap.filter.*;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LdapDAO {
	private static final Logger logger = LoggerFactory.getLogger(LdapDAO.class);

	private LdapTemplate ldapTemplate;

    public static final String SID = "objectSid";
    public static final String NAME = "description";
    public static final String LEADER = "managedBy";



    public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

    public EmployeeLdap getEmployeeByEmail(String email) {
        logger.info("Getting Employee {} from LDAP",email);
        EqualsFilter filter = new EqualsFilter("mail", email);
        logger.debug("LDAP Query {}", filter.encode());
        return ( EmployeeLdap ) Iterables.getFirst(ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()), null);
    }

    public EmployeeLdap getEmployeeByLdapName(String name) {
        try {
            EqualsFilter filter = new EqualsFilter("distinguishedName", name.replaceAll("/", ","));
            logger.debug("LDAP Query {}", filter.encode());
            return (EmployeeLdap) Iterables.getFirst(ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()), null);
        } catch (NameNotFoundException e) {
            logger.debug("Not found: " + name);
            return null;
        }
    }

    public EmployeeLdap getEmployeeByDisplayName(String name) {
        try {
            EqualsFilter filter = new EqualsFilter("displayName", name);
            logger.debug("LDAP Query {}", filter.encode());
            return (EmployeeLdap) Iterables.getFirst(ldapTemplate.search("", filter.encode(), new EmployeeAttributeMapper()), null);
        } catch (NameNotFoundException e) {
            logger.debug("Not found: " + name);
            return null;
        }
    }

    public List<EmployeeLdap> getEmployeesByDepartmentNameFromDb(String department) {
        logger.debug("DeparmentName – {}", department);
        String[] split = department.split(",");
        List<EmployeeLdap> result = new ArrayList<EmployeeLdap>();

        for (String s : split) {
            result.addAll(getEmployees(s));
        }
        return result;
    }

	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getEmployees(String department) {
		logger.info("Getting Employees from LDAP.");
        AndFilter andFilter = new AndFilter()
                .and(new EqualsFilter("department", department))
                .and(new EqualsFilter("objectClass", "user"));
        logger.debug("LDAP Query {}", andFilter.encode());
		List<EmployeeLdap> employees = ldapTemplate.search("", andFilter.encode(), new EmployeeAttributeMapper());
		logger.debug("Employees size is {}", employees.size());
		if(!employees.isEmpty())
            logger.debug("Employee {} City is {}", employees.get(0).getDisplayName(), employees.get(0).getCity());
		return employees;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getDisabledEmployyes() {
		logger.info("Getting Disabled Employees from LDAP.");
		DistinguishedName dn = new DistinguishedName();
	    dn.add("ou", "Disabled Users");

        AndFilter andFilter = new AndFilter().and(new EqualsFilter("objectClass", "person"));
		logger.debug("LDAP Query {}", andFilter.encode());
		return ldapTemplate.search(dn, andFilter.encode(), new EmployeeAttributeMapper());
	}
	
	@SuppressWarnings("unchecked")
	public List<EmployeeLdap> getDivisionLeader(String divisionLeaderName, String division) {
		logger.info("Getting Division Leaders from LDAP.");
        AndFilter andFilter = new AndFilter()
                .and( new EqualsFilter( "displayName", divisionLeaderName ) )
                .and( new EqualsFilter( "department", division ) );
        logger.debug("LDAP Query {}", andFilter.encode());
		return ldapTemplate.search("", andFilter.encode(), new EmployeeAttributeMapper());
	}

    @SuppressWarnings("unchecked")
    public List<Map> getDivisions() {
        AndFilter andFilter = new AndFilter()
                .and(new EqualsFilter("objectClass", "group"))
                .and(new LikeFilter("cn", "_Project Center *"));
        SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        return ldapTemplate.search("", andFilter.encode(), ctls, new AttributesMapper() {
            @Override
            public Map mapFromAttributes(Attributes attributes) throws NamingException {
                Map map = new HashMap();
                NamingEnumeration<? extends Attribute> all = attributes.getAll();
                while (all.hasMoreElements()) {
                    Attribute attribute = all.nextElement();
                    map.put(attribute.getID(), attribute.get());
                }
                return map;
            }
        });
    }

    private class EmployeeAttributeMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			EmployeeLdap employee = new EmployeeLdap();

            employee.setObjectSid   (LdapUtils.convertBinarySidToString((byte[]) attributes.get(SID).get()));
            employee.setDepartment  ( getAttributeByName( attributes, "department" ) );
            employee.setDisplayName ( getAttributeByName( attributes, "displayName" ));
            employee.setEmail(getAttributeByName(attributes, "mail"));
		    employee.setManager     ( getAttributeByName( attributes, "manager" ) );
            employee.setTitle       ( getAttributeByName( attributes, "title" ) );
            employee.setWhenCreated ( getAttributeByName( attributes, "whenCreated" ) );
            employee.setCity        ( getAttributeByName( attributes, "l" ) );

            Attribute ldapCn = attributes.get("distinguishedname");
			if(ldapCn != null)
				employee.setLdapCn(ldapCn.get().toString());
			
			return employee;
		}

        private String getAttributeByName( Attributes attributes, String attributeName ) throws NamingException {
            Attribute department = attributes.get( attributeName );

            return department != null ? ( String ) department.get() : null;
        }
    }
}