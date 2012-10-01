package com.aplana.timesheet.dao.entity.ldap;

import java.util.List;

/**
 *
 * @author aimamutdinov
 */
public class DivisionLdap {
	private String ldap_name;
	private String ldapObjectSid;
	private String leaderSid;
	private boolean leaderVerified;
	private List<String> members;

	public String getLdap_name() {
		return ldap_name;
	}

	public String getLdapObjectSid() {
		return ldapObjectSid;
	}

	public String getLeaderSid() {
		return leaderSid;
	}

	public boolean isLeaderVerified() {
		return leaderVerified;
	}

	public List<String> getMembers() {
		return members;
	}

	public void setLdap_name(String ldap_name) {
		this.ldap_name = ldap_name;
	}

	public void setLdapObjectSid(String ldapObjectSid) {
		this.ldapObjectSid = ldapObjectSid;
	}

	public void setLeaderSid(String leaderSid) {
		this.leaderSid = leaderSid;
	}

	public void setLeaderVerified(boolean leaderVerified) {
		this.leaderVerified = leaderVerified;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}
	
}
