package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.ReportCheck;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ReportCheckDAO{
	private static final Logger logger = LoggerFactory.getLogger(ReportCheckDAO.class);
	
	@PersistenceContext
	private EntityManager entityManager;

    /**
     * Возвращает отчет по дате и человеку
     * @param date
     * @param employee
     * @return ReportCheck
     */
	@SuppressWarnings("unchecked")
	public ReportCheck getDayEmpReportCheck(String date, Employee employee) {
		Query query = entityManager.createQuery(
                "from ReportCheck as r WHERE r.employee = :emp and r.checkdate =:date"
        ).setParameter("date", date).setParameter("emp", employee);

		List<ReportCheck> result = query.getResultList();
		logger.debug("getDayEmpReportCheck List<ReportCheck> result size = {}", result.size());

        return result.isEmpty() ? null : result.get(0);
	}

    /**
     * Заносит в базу запись по пропущенным отчетам
     * @param reportCheck
     */
	public void setReportCheck(ReportCheck reportCheck) {
        ReportCheck existedReportCheck = getDayEmpReportCheck( reportCheck.getCheckDate(), reportCheck.getEmployee() );
        logger.info("reportcheck {} found in base",existedReportCheck);
		if ( existedReportCheck == null){
			ReportCheck rcheckMerged = entityManager.merge(reportCheck);
			logger.info("reportcheck merged.");
			entityManager.flush();
			logger.info("Persistence context synchronized to the underlying database.");
			logger.debug("Flushed ReportCheck object id = {}", rcheckMerged.getId());
        } else {
            logger.warn( "Record already exist in database!" );
        }
	}
    /**
     * Заносит в базу записи по пропущенным отчетам в виде списка
     * @param reportchecks
     */
	public void setReportChecks(List<ReportCheck> reportchecks) {
		if (reportchecks != null){
			logger.info("reportchecks trying to merge.");
			for (ReportCheck rcheck : reportchecks) {
				setReportCheck(rcheck);
			}
		}
	}


    public ReportCheck store (ReportCheck reportCheck) {
        reportCheck = entityManager.merge(reportCheck);

        entityManager.flush();

        return reportCheck;
    }

    public void delete(ReportCheck reportCheck) {
        Hibernate.initialize(reportCheck);

        entityManager.remove(reportCheck);
    }
}