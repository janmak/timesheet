package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.reports.Report04;
import com.aplana.timesheet.reports.TSJasperReport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report04ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {

    @Override
    protected void fillSpecificProperties( ModelAndView mav ) {
        fillDivisionList( mav );
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report04();
    }

    @Override
    protected String getViewName() {
        return "report04";
    }

    @Override
    public Integer getReportId() {
        return 4;
    }
}
