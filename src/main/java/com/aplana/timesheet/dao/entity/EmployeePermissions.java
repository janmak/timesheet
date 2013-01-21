package com.aplana.timesheet.dao.entity;


import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.Date;
import java.lang.Integer;

/**
 * User: iziyangirov
 * Date: 21.01.13
 */
@Entity
@Table(name = "employee_permissions")
public class EmployeePermissions {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empl_permissions_seq")
    @SequenceGenerator(name = "empl_permissions_seq", sequenceName = "empl_permissions_seq", allocationSize = 10)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    @ForeignKey(name = "fk_permission")
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}