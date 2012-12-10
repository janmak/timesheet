package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.EmployeeToken;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class EmployeeTokenDAO {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EmployeeDAO employeeDAO;

    public EmployeeToken find(String key) {
        EmployeeToken token = this.entityManager.find(EmployeeToken.class, key);
        Hibernate.initialize(token.getEmployee());
        Hibernate.initialize(token.getEmployee().getDivision());
        Hibernate.initialize(token.getEmployee().getManager());
        return token;
    }

    public Employee getEmployee(String key) {
        return this.entityManager.find(EmployeeToken.class, key).getEmployee();
    }

    @Transactional
    public EmployeeToken create(int empId) {
        EmployeeToken token = new EmployeeToken();
        token.setEmployee(employeeDAO.find(empId));
        this.entityManager.merge(token);
        return token;
    }

    @Transactional
    public EmployeeToken create(String name) {
        EmployeeToken token = new EmployeeToken();
        token.setEmployee(employeeDAO.find(name));
        this.entityManager.merge(token);
        entityManager.flush();
        return token;
    }
    
    @Transactional
    public void delete(EmployeeToken token) {
        entityManager.remove(token);
    }
    
    public void delete(String key) {
        delete(find(key));
    }
}
