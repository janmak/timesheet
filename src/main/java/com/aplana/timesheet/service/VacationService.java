package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.VacationDAO;
import com.aplana.timesheet.dao.entity.Vacation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
