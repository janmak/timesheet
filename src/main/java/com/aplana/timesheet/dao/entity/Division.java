package com.aplana.timesheet.dao.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "division", uniqueConstraints = @UniqueConstraint(columnNames = { "ldap_name", "name" }))
public class Division {
	@Id
	@SequenceGenerator(name = "division_id_seq", sequenceName = "division_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "division_id_seq")    
	@Column(nullable = false)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column(name = "ldap_name", nullable = false)
	private String ldapName;

	@Column(columnDefinition = "bool not null default true")
	private boolean active;

	@OneToOne(cascade = CascadeType.ALL)
	private Employee leader;

	@OneToMany(mappedBy = "division", cascade = CascadeType.ALL)
	private Set<Employee> employees;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "division_project", joinColumns = { @JoinColumn(name = "division_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "project_id", nullable = false) })
	private Set<Project> projects;
	
	/*
	 * пока не nullable. Когда поле будет заполнено, тогда и изменим свойство
	 */
	@Column
	private String ldapObjectSid;

	public Division() {	}

	public String getLdapName() { return ldapName; }
	public void setLdapName(String ldapName) { this.ldapName = ldapName; }
	public Employee getLeader() { return leader; }
	public void setLeader(Employee leader) { this.leader = leader; }
	public Division(Integer id) { this.id = id; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public Set<Project> getProjects() { return projects; }
	public void setProjects(Set<Project> projects) { this.projects = projects; }
	public Set<Employee> getEmployees() { return employees; }
	public void setEmployees(Set<Employee> employees) { this.employees = employees; }
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getLdap_objectSid() {	return ldapObjectSid; }
	public void setLdap_objectSid(String ldap_objectSid) { this.ldapObjectSid = ldap_objectSid; }
	
	public String toString() {
		StringBuilder sb = new StringBuilder()
			.append(" id=").append(id)
			.append(" name=").append(name)
			.append(" ldapName=").append(ldapName)
			.append(" active=").append(active)
			.append(" ldap_objectSid=").append(ldapObjectSid);
		if(leader != null) {
			sb.append(" leader=").append(leader.getName());
		} else {
			sb.append(" leader=null");
		}
					
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		Division other = (Division) obj;
		if (ldapName == null) {
			if (other.ldapName != null) { return false; }
		} else if (!ldapName.equals(other.ldapName)) { return false; }
		if (ldapObjectSid == null) {
			if (other.ldapObjectSid != null) {
				return false;
			}
		} else if (!ldapObjectSid.equals(other.ldapObjectSid)) {
			return false;
		}
		return true;
	}

}