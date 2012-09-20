package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "project_participant", uniqueConstraints = @UniqueConstraint(columnNames = {"employee", "project_role", "project"}))
public class ProjectParticipant {
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "proj_part_seq")
    @SequenceGenerator(name = "proj_part_seq", sequenceName = "proj_part_seq", allocationSize = 10)
    @Column(nullable = false)
    private Integer id;
	
	@Column(columnDefinition = "bool not null default true")
	private boolean active;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee", nullable = false)
    @ForeignKey(name = "FK_EMPLOYEE")
	private Employee employee;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_role", nullable = false)
	@ForeignKey(name = "FK_PROJECT_ROLE")
	private ProjectRole projectRole;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project", nullable = false)
	private Project project;

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public ProjectRole getProjectRole() {
		return projectRole;
	}

	public void setProjectRole(ProjectRole projectRole) {
		this.projectRole = projectRole;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append(" id=").append(id)
			.append(" active=").append(active)
			.append(" employee=").append(employee.toString())
			.append(" projectRole=").append(projectRole.toString())
			.append(" project=").append(project.toString())
		.toString();
	}
}