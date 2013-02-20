package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.dao.Identifiable;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "division", uniqueConstraints = @UniqueConstraint(columnNames = { "ldap_name", "name" }))
public class Division implements Identifiable {
	@Id
	@Column(nullable = false)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column(name = "ldap_name", nullable = false)
	private String ldapName;

	@Column(columnDefinition = "bool not null default true")
    private boolean active;

    @Column(columnDefinition = "bool not null default true")
    private boolean isCheck;

	@Column(length = 100, nullable = false)
	private String leader;

	@OneToMany(mappedBy = "division", cascade = CascadeType.ALL)
	private Set<Employee> employees;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "division_project",
            joinColumns = {
                    @JoinColumn(name = "division_id", nullable = false) },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_id", nullable = false) })
	private Set<Project> projects;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = true)
    @ForeignKey(name = "fk_employee")
    private Employee leaderId;

    @Column(length = 50, name = "ldap_object_sid")
    private String objectSid;

    @Column(name = "not_to_sync")
    private Boolean notToSyncWithLdap;

    @Column(length = 255, name = "department_name")
    private String departmentName;

    @Column(name = "sync_employee")
    private Boolean syncEmployye;

    public Division() {	}

    public Employee getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Employee leaderId) {
        this.leaderId = leaderId;
    }

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

    public String getObjectSid() {
        return objectSid;
    }

    public void setObjectSid(String objectSid) {
        this.objectSid = objectSid;
    }

    public Boolean getNotToSyncWithLdap() {
        return notToSyncWithLdap;
    }

    public void setNotToSyncWithLdap(Boolean notToSyncWithLdap) {
        this.notToSyncWithLdap = notToSyncWithLdap;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Boolean getSyncEmployye() {
        return syncEmployye;
    }

    public void setSyncEmployye(Boolean syncEmployye) {
        this.syncEmployye = syncEmployye;
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

        Division other = (Division) obj;

        final Integer thisId = getId();

        if (thisId == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!thisId.equals(other.getId())) {
            return false;
        }

        final String thisLdapName = getLdapName();

        if (thisLdapName == null) {
			if (other.getLdapName() != null) {
                return false;
            }
		} else if (!thisLdapName.equals(other.getLdapName())) {
            return false;
        }

        return true;
	}

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}