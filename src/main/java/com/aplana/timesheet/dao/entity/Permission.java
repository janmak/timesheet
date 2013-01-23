package com.aplana.timesheet.dao.entity;

import javax.persistence.*;
import java.lang.Integer;
import java.util.Set;

/**
 * User: iziyangirov
 * Date: 21.01.13
 */
@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name")
    private String beginDate;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "employee_permissions",
            joinColumns = {
                    @JoinColumn(name = "permission_id", nullable = false) },
            inverseJoinColumns = {
                    @JoinColumn(name = "employee_id", nullable = false) })
    private Set<Employee> employees;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "project_role_permissions",
            joinColumns = {
                    @JoinColumn(name = "permission_id", nullable = false) },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_role_id", nullable = false) })
    private Set<ProjectRole> projectRoles;

    public Set<ProjectRole> getProjectRoles() {
        return projectRoles;
    }

    public void setProjectRoles(Set<ProjectRole> projectRoles) {
        this.projectRoles = projectRoles;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }
}