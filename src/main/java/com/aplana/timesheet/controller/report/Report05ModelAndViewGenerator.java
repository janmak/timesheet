package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.reports.Report05;
import com.aplana.timesheet.reports.TSJasperReport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report05ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {

    @Override
    protected void fillSpecificProperties( ModelAndView mav ) {
        fillDivisionList( mav, false, true , false);
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report05();
    }

    @Override
    protected String getViewName() {
        return "report05";
    }

    @Override
    public Integer getReportId() {
        return 5;
    }
}
