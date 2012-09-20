package com.aplana.timesheet.dao.entity;

import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Table(name = "time_sheet_detail")
public class TimeSheetDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_sheet_detail_seq")
	@SequenceGenerator(name = "time_sheet_detail_seq", sequenceName = "time_sheet_detail_seq", allocationSize = 10)
	@Column(columnDefinition = "decimal(10,0) not null")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "time_sheet_id")
	@ForeignKey(name = "FK_TIME_SHEET")
	private TimeSheet timeSheet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_type")
	@ForeignKey(name = "FK_TIME_SHEET_TYPE")
	private DictionaryItem actType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "act_cat")
	@ForeignKey(name = "FK_TIME_SHEET_CAT")
	private DictionaryItem actCat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "proj_id")
	@ForeignKey(name = "FK_TIME_SHEET_PROJECT")
	private Project project;

	@Column(name = "cq_id")
	private String cqId;

	@Column(columnDefinition = "text null")
	private String description;

	@Column(columnDefinition = "decimal(2,1) null")
	private Double duration;

	@Column(columnDefinition = "text null")
	private String problem;

	// В это поле попадает название пресейла, вводимое пользователем, если он не
	// нашел нужное ему название в списке пресейлов.
	private String other;

	public TimeSheet getTimeSheet() {
		return timeSheet;
	}

	public DictionaryItem getActType() {
		return actType;
	}

	public DictionaryItem getActCat() {
		return actCat;
	}

	public Project getProject() {
		return project;
	}

	public void setTimeSheet(TimeSheet timeSheet) {
		this.timeSheet = timeSheet;
	}

	public void setActType(DictionaryItem actType) {
		this.actType = actType;
	}

	public void setActCat(DictionaryItem actCat) {
		this.actCat = actCat;
	}

	public void setProject(Project proj) {
		this.project = proj;
	}

	public Double getDuration() {
		return duration;
	}

	public String getCqId() {
		return cqId;
	}

	public String getDescription() {
		return description;
	}

	public String getProblem() {
		return problem;
	}

	public String getOther() {
		return other;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public void setCqId(String cqId) {
		this.cqId = cqId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

	public void setOther(String other) {
		this.other = other;
	}

    public String getDescriptionEscaped()
    {
        return StringEscapeUtils.escapeHtml4(this.description);
    }

    public String getProblemEscaped()
    {
        return StringEscapeUtils.escapeHtml4(this.problem);
    }

	@Override
	public String toString() {
		return new StringBuilder()
			.append(" id=").append(id)
			.append(" timeSheet [").append(timeSheet).append("]")
			.append(" actType [").append(actType).append("]")
			.append(" actCat [").append(actCat).append("]")
			.append(" project [").append(project).append("]")
			.append(" cqId=").append(cqId)
			.append(" description=").append(description)
			.append(" duration=").append(duration)
			.append(" problem=").append(problem)
			.append(" other=").append(other)
		.toString();
	}
}