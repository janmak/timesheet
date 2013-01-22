package com.aplana.timesheet.dao.entity;

import org.hibernate.annotations.ForeignKey;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "manager")
public class Manager implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manager_seq")
    @SequenceGenerator(name = "manager_seq", sequenceName = "manager_seq", allocationSize = 10)
    @Column(nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    @ForeignKey(name = "fk_region")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    @ForeignKey(name = "fk_division")
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @ForeignKey(name = "fk_employee")
    private Employee employee;

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
