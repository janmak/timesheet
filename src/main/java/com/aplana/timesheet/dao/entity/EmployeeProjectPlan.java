package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * User: vsergeev
 * Date: 04.02.13
 */
@Entity
@Table (name = "employee_project_plan")
public class EmployeeProjectPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empl_prj_plan_seq")
    @SequenceGenerator(name = "empl_prj_plan_seq", sequenceName = "empl_prj_plan_seq", allocationSize = 10)
    @Column(nullable = false)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @ForeignKey(name = "fk_employee")
    Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @ForeignKey(name = "fk_project")
    Project project;

    @Column(name = "year")
    Integer year;

    @Column(name = "month")
    Integer month;

    @Column(name = "value")
    Double value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
