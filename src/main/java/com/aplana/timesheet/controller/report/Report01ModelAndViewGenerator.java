package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.enums.OvertimeCategory;
import com.aplana.timesheet.reports.Report01;
import com.aplana.timesheet.reports.TSJasperReport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report01ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {

    @Override
    protected void fillSpecificProperties( ModelAndView mav ) {
        fillDivisionList( mav );

        List<OvertimeCategory> ls = new ArrayList<OvertimeCategory>();
        ls.add(OvertimeCategory.ALL);
        ls.add(OvertimeCategory.HOLIDAY);
        ls.add(OvertimeCategory.SIMPLE);
        mav.addObject("categoryList", ls);
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report01();
    }

    @Override
    protected String getViewName() {
        return "report01";
    }

    @Override
    public Integer getReportId() {
        return 1;
    }
}
