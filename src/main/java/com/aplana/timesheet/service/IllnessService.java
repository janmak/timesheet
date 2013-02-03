package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.IllnessDAO;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User: vsergeev
 * Date: 27.01.13
 */
@Service
public class IllnessService {

    @Autowired
    IllnessDAO illnessDAO;

    public List<Illness> getEmployeeIllness(Employee employee){
        return illnessDAO.getEmployeeIllness(employee);
    }

    public void setIllness(Illness ilness){
        illnessDAO.setIllness(ilness);
    }

    public void deleteIllness(Illness illness) {
        illnessDAO.deleteIllness(illness);
    }

    public void deleteIllnessById(Integer reportId) {
        illnessDAO.deleteIllnessById(reportId);
    }

    public Illness find(Integer reportId) {
        return illnessDAO.find(reportId);
    }
}
