package com.aplana.timesheet.controller;

import com.aplana.timesheet.reports.TSJasperReport;
import com.aplana.timesheet.util.JReportBuildError;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author eshangareev
 * @version 1.0
 */
public interface JasperReportModelAndViewGenerator {

    ModelAndView getModelAndViewForReport( TSJasperReport form ) throws JReportBuildError;

    Integer getReportId();
}
