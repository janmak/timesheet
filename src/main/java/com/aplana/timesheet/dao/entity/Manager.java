package com.aplana.timesheet.dao.entity;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "manager")
public class Manager implements Serializable {

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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Region region;
    @ManyToOne(fetch = FetchType.LAZY)
    private Division division;
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;
}
