package com.aplana.timesheet.dao.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "division", uniqueConstraints = @UniqueConstraint(columnNames = { "ldap_name", "name" }))
public class Division {
	@Id
	@Column(nullable = false)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column(name = "ldap_name", nullable = false)
	private String ldapName;

	@Column(columnDefinition = "bool not null default true")
	private boolean active;

	@Column(length = 100, nullable = false)
	private String leader;

	@OneToMany(mappedBy = "division", cascade = CascadeType.ALL)
	private Set<Employee> employees;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "division_project", joinColumns = { @JoinColumn(name = "division_id", nullable = false) }, inverseJoinColumns = { @JoinColumn(name = "project_id", nullable = false) })
	private Set<Project> projects;

	public Division() {	}

    public String getLdapName() {
        return ldapName;
    }

    public void setLdapName( String ldapName ) {
        this.ldapName = ldapName;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader( String leader ) {
        this.leader = leader;
    }

    public Division( Integer id ) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive( boolean active ) {
        this.active = active;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects( Set<Project> projects ) {
        this.projects = projects;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees( Set<Employee> employees ) {
        this.employees = employees;
    }

    public Integer getId() {
        return id;
    }

    public void setId( Integer id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String toString() {
		StringBuilder sb = new StringBuilder()
			.append(" id=").append(id)
			.append(" name=").append(name)
			.append(" ldapName=").append(ldapName)
			.append(" active=").append(active);
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
		return true;
	}

}