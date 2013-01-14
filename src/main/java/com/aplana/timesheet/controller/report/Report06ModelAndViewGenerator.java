package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.reports.Report06;
import com.aplana.timesheet.reports.TSJasperReport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report06ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {

    @Override
    protected void fillSpecificProperties( ModelAndView mav ) {
        fillWithProjects( mav );
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report06();
    }

    @Override
    protected String getViewName() {
        return "report06";
    }

    @Override
    public Integer getReportId() {
        return 6;
    }
}
