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
@Table(name = "project_role_permissions")
public class ProjectRolePermissions {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "proj_role_permissions_seq")
    @SequenceGenerator(name = "proj_role_permissions_seq", sequenceName = "proj_role_permissions_seq", allocationSize = 10)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id")
    @ForeignKey(name = "fk_permission")
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_role_id")
    @ForeignKey(name = "fk_project_role")
    private ProjectRole projectRole;

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

    public ProjectRole getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(ProjectRole projectRole) {
        this.projectRole = projectRole;
    }
}