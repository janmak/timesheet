package com.aplana.timesheet.util;

import com.aplana.timesheet.dao.entity.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class TimeSheetUser extends User {

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    private Employee employee;

    public TimeSheetUser(Employee user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getName(), "[PROTECTED]", authorities);
        this.employee = user;
    }
}
