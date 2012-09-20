package com.aplana.timesheet.dao.entity;

import javax.persistence.*;

@Entity
@Table(name = "project_role", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "code"}))
public class ProjectRole {
	@Id
	@Column(nullable = false)
	private Integer id;
	
	@Column(nullable = false)
	private String name;
	
	@Column(length = 3, nullable = false)
	private String code;
	
	@Column(columnDefinition = "bool not null default true")
	private boolean active;
	
	@Column(name = "ldap_title", length = 100, nullable = false)
	public String ldapTitle;

    public Integer getSysRoleId() {
        return sysRoleId;
    }

    public void setSysRoleId(Integer sysRoleId) {
        this.sysRoleId = sysRoleId;
    }

    @Column(name = "sysroleid")
    public Integer sysRoleId;
	
	/** Конструктор по умолчанию */
	public ProjectRole() {}

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }
	public String getLdapTitle() { return ldapTitle; }
	public void setLdapTitle(String ldapTitle) { this.ldapTitle = ldapTitle; }

	@Override
	public String toString() {
		return new StringBuilder()
			.append(" id=").append(id)
			.append(" active=").append(active)
			.append(" name=").append(name)
			.append(" code=").append(code)
			.append(" ldapTitle=").append(ldapTitle)
		.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		ProjectRole other = (ProjectRole) obj;
		if (code == null) {
			if (other.code != null) { return false; }
		} else if (!code.equals(other.code)) { return false; }
		return true;
	}
}