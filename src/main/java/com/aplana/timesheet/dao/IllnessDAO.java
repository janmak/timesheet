package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Illness;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 27.01.13
 */
@Service
public class IllnessDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional (readOnly = true)
    public List<Illness> getEmployeeIllness(Employee employee) {
        Query query = entityManager.createQuery("from Illness as i where i.employee = :employee order by i.beginDate");
        query.setParameter("employee", employee);

        return (List<Illness>) query.getResultList();
    }

    @Transactional
    public void setIllness(Illness illness) {
        Illness illnessMerged = entityManager.merge(illness);
        entityManager.flush();
    }

    @Transactional
    public void deleteIllness(Illness illness) {
        entityManager.remove(illness);
    }

    @Transactional
    public void deleteIllnessById(Integer reportId) {
        Query query = entityManager.createQuery("delete from Illness as i where i.id = :id");
        query.setParameter("id", reportId);

        query.executeUpdate();
    }

    @Transactional
    public Illness find(Integer reportId) {
        Query query = entityManager.createQuery(
                "select i from Illness as i where i.id = :id"
        ).setParameter( "id", reportId );

        return (Illness) query.getSingleResult();
    }

    @Transactional
    public Boolean isDayIllness(Employee employee, Date date){
        Query query = entityManager.createQuery(
                "SELECT i FROM Illness AS i WHERE i.employee = :employee AND :date BETWEEN i.beginDate AND i.endDate"
        ).setParameter("employee", employee).setParameter("date", date);
        if (query.getResultList().isEmpty()) {
            return false;
        }
        return true;
    }

}
