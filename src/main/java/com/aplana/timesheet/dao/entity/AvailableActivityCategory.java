package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Доступная категория активности.
 * @author astepanov
 */
@Entity
@Table(name = "available_activity_category", uniqueConstraints = @UniqueConstraint(columnNames = {"act_type", "project_role", "act_cat"}))
public class AvailableActivityCategory {
	@Id
	@Column(nullable = false)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_type", nullable = false)
    @ForeignKey(name = "FK_ACTIVITY_TYPE")
    private DictionaryItem actType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project")
	@ForeignKey(name = "FK_PROJECT")
	private Project project;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_role", nullable = false)
	@ForeignKey(name = "FK_PROJECT_ROLE")
	private ProjectRole projectRole;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_cat", nullable = false)
    @ForeignKey(name = "FK_ACTIVITY_CATEGORY")
	private DictionaryItem actCat;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public ProjectRole getProjectRole() {
		return projectRole;
	}

	public void setProjectRole(ProjectRole projectRole) {
		this.projectRole = projectRole;
	}

	public DictionaryItem getActType() {
		return actType;
	}

	public void setActType(DictionaryItem actType) {
		this.actType = actType;
	}

	public DictionaryItem getActCat() {
		return actCat;
	}

	public void setActCat(DictionaryItem actCat) {
		this.actCat = actCat;
	}
}