package com.aplana.timesheet.dao;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.enums.Region;
import com.aplana.timesheet.reports.BaseReport;
import com.aplana.timesheet.reports.OverTimeCategory;
import com.aplana.timesheet.reports.Report01;
import com.aplana.timesheet.reports.Report06;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;

/**
 * @author eshangareev
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:WEB-INF/spring/testApplicationContext.xml"})
public class JasperReportDAOTest {

    @Autowired
    JasperReportDAO dao;

    @Test
    public void testGetResultList() {
        Report01 report = getPrefilledBaseReport(new Report01());
        report.setCategory(OverTimeCategory.All);
        report.setDivisionOwnerId(1);
        dao.getResultList(report);

        Report06 report06 = getPrefilledBaseReport(new Report06());

        report06.setProjectId(1);
        dao.getResultList(report06);
    }


    private <T extends BaseReport> T getPrefilledBaseReport(final T report) {
        report.setBeginDate(DateTimeUtil.dateToString(DateUtils.addDays(new Date(), -120)));
        report.setEndDate(DateTimeUtil.dateToString(DateUtils.addDays(new Date(), -90)));
        report.setRegionIds(Arrays.asList(Region.UFA.getId()));
        report.setRegionNames(Arrays.asList(Region.UFA.getName()));
        report.setAllRegions(false);

        return report;
    }
}
