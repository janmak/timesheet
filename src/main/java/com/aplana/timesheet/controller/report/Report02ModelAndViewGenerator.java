package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.reports.Report02;
import com.aplana.timesheet.reports.TSJasperReport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report02ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {

    @Override
    protected void fillSpecificProperties( ModelAndView mav ) {

        fillWithAllProjects( mav );
        fillDivisionList( mav, true, true, true );
        fillFullProjectListJson( mav );

        mav.addObject("filterProjects", "checked");
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report02();
    }

    @Override
    protected String getViewName() {
        return "report02";
    }

    @Override
    public Integer getReportId() {
        return 2;
    }
}
