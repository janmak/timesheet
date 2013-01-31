package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.reports.Report07;
import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.service.SecurityService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
@Component
public class Report07ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {
    @Autowired
    private SecurityService securityService;

    @Override
    protected void fillSpecificProperties(ModelAndView mav) {
        fillDivisionList(mav);
        mav.addObject("employeeDivision", securityService.getSecurityPrincipal().getEmployee().getDivision().getId());
        mav.addObject("filterProjects", "checked");
    }

    @Override
    protected TSJasperReport getForm() {
        return new Report07();
    }

    @Override
    protected String getViewName() {
        return "report07";
    }

    @Override
    public Integer getReportId() {
        return 7;
    }
}
