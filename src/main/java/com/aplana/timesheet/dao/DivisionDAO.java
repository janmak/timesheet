package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.ldap.DivisionLdap;
import com.aplana.timesheet.dao.entity.ldap.LdapAplanaUtils;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DivisionDAO {
	private static final Logger logger = LoggerFactory.getLogger(DivisionDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/**
	 * Возвращает список подразделений
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Division> getDivisions() {
		Query query = entityManager.createQuery(
                "from Division as d where d.active=:active order by d.name asc"
        ).setParameter( "active", true );

		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public Division find(Integer id) {
		return entityManager.find(Division.class, id);
	}
	
	/**
	 * Ищет активное подразделение с указанным именем.
	 * @param title название подразделение
	 * @return объект типа Division или null, если подразделение не найдено.
	 */
	@Transactional(readOnly = true)
	public Division find(String title) {
		Query query = entityManager.createQuery(
                "from Division as d where d.active=:active and d.ldapName=:title"
        ).setParameter( "active", true ).setParameter( "title", title );

        try {
			return  (Division) query.getSingleResult();
		} catch (NoResultException e) {
			logger.warn("Department with title '{}' not found.", title);
            return null;
		}
	}
	
	/**
	 * Возвращает список подразделений
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Division> getDivisionsAll() {
		Query query = entityManager.createQuery(
                "from Division as d order by d.name asc"
        );
		return query.getResultList();
	}
	
	public List<DivisionLdap> getDivisionsFromLDAP() {
		AndFilter filter = new AndFilter()
                .and( new EqualsFilter( "objectClass", "group" ) )
		        .and( new LikeFilter( "cn", "_Project Center *" ) );
		return getMembersObjectSid(ldapTemplate.search("",filter.encode(),new DivisionAttributeMapper()));
	}
	
	@Transactional(readOnly = false)
	@SuppressWarnings("unchecked")
	public String setDivision( Division division ) {
        String message = String.format( "%s division %s \n", division.getId() == null ? "Add" : "Update", division.getName() );
        logger.info( message );
        entityManager.merge( division );

        return message;
    }

    @Transactional(readOnly = false)
	@SuppressWarnings("unchecked")
	public StringBuffer setDivision(List<Division> divisions) {
		StringBuffer sb = new StringBuffer("");
		for(Division division : divisions) {
			sb.append(setDivision(division));
		}
		return sb;
	}

	private List<DivisionLdap> getMembersObjectSid(List<DivisionLdap> divisions) {
		for(DivisionLdap div : divisions){
			div.setLeaderVerified(false);
			if (div.getMembers() != null) {
				List<String> membersObjectSid = new ArrayList<String>(div.getMembers().size());
				for (String distinguishedName : div.getMembers()) {
					EqualsFilter filter = new EqualsFilter("distinguishedName", distinguishedName);
					List<String> employeesSid = ldapTemplate.search("", filter.encode(), new MemberMapper());
					if (!employeesSid.isEmpty()) {
						membersObjectSid.add(employeesSid.get(0));
						if (div.getLeaderSid() != null && div.getLeaderSid().equals(distinguishedName)) {
							logger.debug("!!!Set Leader sid dsn={} sid={}", div.getLeaderSid(), employeesSid.get(0));
							div.setLeaderSid(employeesSid.get(0));
							div.setLeaderVerified(true);
						}
					}

				}
				div.setMembers(membersObjectSid);
			}
		}
        return divisions;
    }
	
	
	private static class DivisionAttributeMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			DivisionLdap div = new DivisionLdap();
			
			Attribute ldapObjectSid = attributes.get("ObjectSid");
			div.setLdapObjectSid(LdapAplanaUtils.getSidAttribute(ldapObjectSid));
			
			Attribute leaderAttr = attributes.get("managedBy");
			String leader = checkAttribute(leaderAttr);
			
			checkAttribute(attributes.get("displayName"));
			
			Attribute members = attributes.get("member");
			div.setLeaderSid(leader);
			fillMembers(leader, members, div);			
			
			Attribute name = attributes.get("description");
			div.setLdap_name(checkAttribute(name));
			
			return div;			
		}

		private void fillMembers(String leader, Attribute membersAttribute, DivisionLdap div) throws NamingException {
			ArrayList<String> memberList = new ArrayList<String>();
			if (membersAttribute == null) {
				if (leader != null || !leader.isEmpty()) {
					div.setLeaderVerified(false);
					return;
				}
			}
			NamingEnumeration<Object> members = (NamingEnumeration<Object>) membersAttribute.getAll();
			while(members.hasMore()) {
				Object hold = members.next();
				if(hold instanceof String) {
					String member = (String) hold;
					
					if(member.equals(leader)) {
						div.setLeaderVerified(true);
					}
					memberList.add(member);
				} else {
					// error
					throw new NamingException("Тип одного из атрибутов member не строка");
				}
			}
			div.setMembers(memberList);
		}
	}

	private static class MemberMapper implements AttributesMapper {
		@Override
		public Object mapFromAttributes(Attributes attributes) throws NamingException {
			return LdapAplanaUtils.getSidAttribute(attributes.get("objectSid"));
		}
	}
	
	private static String checkAttribute(Attribute attribute) throws NamingException {
		if (attribute != null) {
			Object value = attribute.get();
			if (value instanceof String) {
				return (String) value;
			} else {
				// error
				throw new NamingException("[" + attribute.getID() + "] is not String");
			}
		}
		throw new NamingException("Attribute is null");
	}
}