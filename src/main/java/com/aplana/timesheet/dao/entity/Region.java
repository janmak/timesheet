package com.aplana.timesheet.dao.entity;

import com.aplana.timesheet.dao.Identifiable;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "region", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Region implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ldap_city", nullable = false)
    private String ldapCity;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Holiday> holidays;

    @Column(name = "additional_emails")
    private String additionalEmails;

    public String getAdditionalEmails() {
        return additionalEmails;
    }

    public void setAdditionalEmails(String additionalEmails) {
        this.additionalEmails = additionalEmails;
    }

    public Set<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(Set<Holiday> holidays) {
        this.holidays = holidays;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLdapCity(String ldapCity) {
        this.ldapCity = ldapCity;
    }

    public String getLdapCity() {
        return ldapCity;
    }
}