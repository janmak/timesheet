package com.aplana.timesheet.dao.entity.ldap;

public class EmployeeLdap {
	private String department;
	private String displayName;
	private String email;
	private String manager;
	private String title;
	private String whenCreated;
	private String city;
	private String ldapCn;
    private String objectSid;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String mail) {
        this.email = mail;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWhenCreated() {
        return whenCreated;
    }

    public void setWhenCreated(String whenCreated) {
        this.whenCreated = whenCreated;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public String getObjectSid() {
        return objectSid;
    }

    public void setObjectSid(String objectSid) {
        this.objectSid = objectSid;
    }

    public String getLdapCn() {
		return ldapCn;
	}

	public void setLdapCn(String ldapCn) {
		this.ldapCn = ldapCn;
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(" department=").append(department)
				.append(" displayName=").append(displayName)
				.append(" mail=").append(email)
				.append(" manager=").append(manager)
				.append(" title=").append(title)
				.append(" whenCreated=").append(whenCreated)
				.append(" city=").append(city)
			.toString();
	}
}