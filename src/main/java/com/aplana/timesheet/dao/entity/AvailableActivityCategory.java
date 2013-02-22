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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "avail_act_cat_seq")
    @SequenceGenerator(name = "avail_act_cat_seq", sequenceName = "avail_act_cat_seq", allocationSize = 10)
	@Column(nullable = false)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_type", nullable = false)
    @ForeignKey(name = "FK_ACTIVITY_TYPE")
    private DictionaryItem actType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_role", nullable = false)
	@ForeignKey(name = "FK_PROJECT_ROLE")
	private ProjectRole projectRole;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_cat", nullable = false)
    @ForeignKey(name = "FK_ACTIVITY_CATEGORY")
	private DictionaryItem actCat;

    @Column(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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