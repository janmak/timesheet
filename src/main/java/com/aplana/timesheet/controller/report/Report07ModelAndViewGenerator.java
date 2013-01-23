package com.aplana.timesheet.controller.report;

import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.reports.Report07;
import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.service.SecurityService;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author eshangareev
 * @version 1.0
 */
public class Report07ModelAndViewGenerator extends AbstractJasperReportModelAndViewGenerator {
    @Autowired
    private SecurityService securityService;

    @Override
    protected void fillSpecificProperties(ModelAndView mav) {
        fillDivisionList(mav);
        List<Division> divisions = (List<Division>) mav.getModelMap().get("divisionList");

        final Integer defaultId = securityService.getSecurityPrincipal().getEmployee().getDivision().getId();

        Division division = Iterables.find(divisions, new Predicate<Division>() {
            @Override
            public boolean apply(@Nullable Division input) {
                return defaultId == input.getId();
            } });
        divisions.remove(division);
        divisions.add(divisions.set(0, division));

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
