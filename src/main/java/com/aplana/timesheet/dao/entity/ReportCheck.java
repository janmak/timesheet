package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reportcheck", uniqueConstraints = @UniqueConstraint(columnNames = {"checkdate", "employee"}))

public class ReportCheck{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reportcheck_seq")
    @SequenceGenerator(name = "reportcheck_seq", sequenceName = "reportcheck_seq", allocationSize = 10)
	@Column(nullable = false)
	private Integer id;
	
	@Column(name = "checkdate", nullable = false)
	private String checkdate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "employee")
	@ForeignKey(name = "FK_REP_EMPLOYEE")
	private Employee employee;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "division")
	@ForeignKey(name = "FK_REP_DIVISION")
	private Division division;

	@Column(name = "rnotsendnum")
	private Integer reportsNotSendNumber;
	
	@Column(name = "sundaycheck", columnDefinition = "bool not null default true")
	private boolean sundayCheck;

	@Transient
	List<String> passedDays = new ArrayList<String>();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCheckDate() {
		return checkdate;
	}

	public void setCheckDate(String checkDate) {
		this.checkdate = checkDate;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Division getDivision() {
		return division;
	}

	public void setDivision(Division division) {
		this.division = division;
	}

	public Integer getReportsNotSendNumber() {
		return reportsNotSendNumber;
	}

	public void setReportsNotSendNumber(Integer reportsNotSendNumber) {
		this.reportsNotSendNumber = reportsNotSendNumber;
	}

	public boolean isSundayCheck() {
		return sundayCheck;
	}

	public void setSundayCheck(boolean sundayCheck) {
		this.sundayCheck = sundayCheck;
	}
	
	public List<String> getPassedDays() {
		return passedDays;
	}

	public void setPassedDays(List<String> passedDays) {
		this.passedDays = passedDays;
	}

	@Override
	public String toString() {
		return "ReportCheck [checkdate=" + checkdate + ", employee="
				+ employee + ", reportsNotSendNumber="
				+ reportsNotSendNumber + ", sundayCheck=" + sundayCheck + "]";
	}
}