package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Entity
@Table(
        name = "employee_project_plan",
        uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "project_id", "year", "month" })
)
public class EmployeeProjectPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empl_prj_plan_seq")
    @SequenceGenerator(name = "empl_prj_plan_seq", sequenceName = "empl_prj_plan_seq", allocationSize = 10)
    @Column(name = "id", columnDefinition = "integer")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @ForeignKey(name = "fk_project")
    private Project project;

    @Column(name = "year", columnDefinition = "integer", nullable = false)
    private Integer year;

    @Column(name = "month", columnDefinition = "integer", nullable = false)
    private Integer month;

    @Column(name = "value", columnDefinition = "double precision", nullable = false)
    private Double value;

    public EmployeeProjectPlan() {
    }

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EmployeeProjectPlan");
        sb.append("{id=").append(id);
        sb.append(", employee=").append(getEmployee());
        sb.append(", project=").append(getProject());
        sb.append(", year=").append(year);
        sb.append(", month=").append(month);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
