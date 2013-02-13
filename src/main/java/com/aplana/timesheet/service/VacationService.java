package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Vacation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class VacationService {

    @Autowired
    private VacationDAO vacationDAO;

    public void store(Vacation vacation) {
        vacationDAO.store(vacation);
    }

    public Boolean isDayVacation(Employee employee, Date date){
        return vacationDAO.isDayVacation(employee, date);
    }

    public List<Vacation> getAllNotApprovedVacations() {
        return vacationDAO.getAllNotApprovedVacations();
    }
}
