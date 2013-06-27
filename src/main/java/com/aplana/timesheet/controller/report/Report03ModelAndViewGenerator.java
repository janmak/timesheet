package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.reports.Report03;
import com.aplana.timesheet.reports.TSJasperReport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report03ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {

    @Override
    protected void fillSpecificProperties( ModelAndView mav ) {

        fillWithAllProjects( mav );
        fillDivisionList( mav, true, true, true);
        fillFullProjectListJson( mav );
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report03();
    }

    @Override
    protected String getViewName() {
        return "report03";
    }

    @Override
    public Integer getReportId() {
        return 3;
    }
}
