package com.aplana.timesheet.form;

public class TimeSheetTableRowForm {
	private Integer taskName;
	private String description;
	private String duration;
	private String problem;
	private String other;
	private Integer projectId;
	private Integer activityTypeId;
	private Integer activityCategoryId;
	private Integer projectRoleId;
    private Integer workplaceId;

	public Integer getProjectRoleId() {
		return projectRoleId;
	}

	public void setProjectRoleId(Integer projectRoleId) {
		this.projectRoleId = projectRoleId;
	}

	public Integer getTaskName() {
		return taskName;
	}

	public String getDescription() {
		return description;
	}

	public String getDuration() {
		return duration;
	}

	public String getProblem() {
		return problem;
	}

	public String getOther() {
		return other;
	}

	public void setTaskName(Integer taskName) {
		this.taskName = taskName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public Integer getActivityTypeId() {
		return activityTypeId;
	}

	public Integer getActivityCategoryId() {
		return activityCategoryId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public void setActivityTypeId(Integer activityTypeId) {
		this.activityTypeId = activityTypeId;
	}

	public void setActivityCategoryId(Integer activityCategoryId) {
		this.activityCategoryId = activityCategoryId;
	}

    public Integer getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(Integer workplaceId) {
        this.workplaceId = workplaceId;
    }
}