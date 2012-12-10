package com.aplana.timesheet.dao.entity;

import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name="employee_token")
public class EmployeeToken {
    
    @Id
    @Column(columnDefinition="TEXT", length = 36, unique=true, nullable=false)
    private String key = UUID.randomUUID().toString();
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @ForeignKey(name = "FK_EMPLOYEE")
    private Employee employee;
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

}
