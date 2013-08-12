package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "project_task", uniqueConstraints = @UniqueConstraint(columnNames = {"cq_id"}))
public class ProjectTask {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "proj_task_seq")
    @SequenceGenerator(name = "proj_task_seq", sequenceName = "proj_task_seq", allocationSize = 10)
    @Column(nullable = false)
    Integer id;

    @Column(name = "cq_id", nullable = false)
    String cqId;
    
    @Column(columnDefinition = "text not null")
    String description;
    
    @Column(columnDefinition = "bool not null default true")
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project")
    @ForeignKey(name = "FK_PROJECT")
    Project project;

    public Integer getId() {
	return id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public String getCqId() {
        return cqId;
    }

    public void setCqId(String cqId) {
        this.cqId = cqId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Project getProject() {
	return project;
    }

    public void setProject(Project project) {
	this.project = project;
    }
}