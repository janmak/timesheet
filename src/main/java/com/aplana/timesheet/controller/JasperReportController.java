package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.JasperReportDAO;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.form.validator.ReportFormValidator;
import com.aplana.timesheet.reports.*;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.EmployeeHelper;
import com.aplana.timesheet.util.JReportBuildError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
public class JasperReportController {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportController.class);

    @Autowired
    private JasperReportService jasperReportService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private JasperReportDAO reportDAO;

    @Autowired
    private ReportFormValidator reportValidator;

    @Autowired
    private EmployeeHelper employeeHelper;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ServletContext context;

	@Autowired
	SecurityService securityService;

    public ModelAndView createReportMAV(Integer number) throws JReportBuildError {
        return createReportMAV(number, null);
    }

    public ModelAndView createReportMAV(Integer number, TSJasperReport form) throws JReportBuildError {

        if (form == null) {
            switch (number) {
                case 6:
                    form = new Report06();
                    break;
                case 5:
                    form = new Report05();
                    break;
                case 2:
                    form = new Report02();
                    break;
                case 4:
                    form = new Report04();
                    break;
                case 1:
                    form = new Report01();
                    break;
                case 3:
                    form = new Report03();
                    break;
            }
        }

        ModelAndView mav;
        switch (number) {
            case 6:
                mav = new ModelAndView("report06");
                mav.addObject("reportForm", form);
                mav.addObject("projectList", projectService.getProjects());
                mav.addObject("regionList", regionService.getRegions());
                return mav;
            case 5:
                mav = new ModelAndView("report05");
                mav.addObject("reportForm", form);
				List<Division> divisions =  divisionService.getDivisions();
                mav.addObject("divisionList", divisions);
                mav.addObject("regionList", regionService.getRegions());
                mav.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisions));
                return mav;
            case 2:
				mav = new ModelAndView("report02");
				mav.addObject("reportForm", form);
				mav.addObject("projectList", projectService.getAll());
				divisions = divisionService.getDivisions();
				mav.addObject("divisionList", divisions);
                mav.addObject("regionList", regionService.getRegions());
				mav.addObject("projectListJson", projectService.getProjectListJson(divisions));
				mav.addObject("fullProjectListJson", projectService.getProjectListJson());
				mav.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisions));

				mav.addObject("filterProjects", "checked");
				return mav;
            case 4:
                mav = new ModelAndView("report04");
                mav.addObject("reportForm", form);
                mav.addObject("divisionList", divisionService.getDivisions());
                mav.addObject("regionList", regionService.getRegions());
                return mav;
            case 1:
                mav = new ModelAndView("report01");
                mav.addObject("reportForm", form);
                mav.addObject("divisionList", divisionService.getDivisions());
                mav.addObject("regionList", regionService.getRegions());
                return mav;
            case 3:
                mav = new ModelAndView("report03");
                mav.addObject("reportForm", form);
                mav.addObject("projectList", projectService.getAll());
                divisions = divisionService.getDivisions();
                mav.addObject("divisionList", divisions);
                mav.addObject("regionList", regionService.getRegions());
                mav.addObject("projectListJson", projectService.getProjectListJson(divisions));
                mav.addObject("fullProjectListJson", projectService.getProjectListJson());
                mav.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisions));

                mav.addObject("filterProjects", "checked");
                return mav;
            default:
                throw new JReportBuildError("Error forming report: number " + number.toString() + " not found.");
        }
    }

    private ModelAndView showReport(TSJasperReport report, BindingResult result, Integer printtype, int numberReport, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        report.setReportDAO(reportDAO);
        reportValidator.validate(report, result);

        ModelAndView mav;

        if (result.hasErrors()) {
            mav = createReportMAV(numberReport, report);
            mav.addObject("errors", result.getAllErrors());

            return mav;
        }

        if (jasperReportService.makeReport(report, printtype, response, request)) {
	        if (printtype == JasperReportService.REPORT_PRINTTYPE_HTML) {
	            mav = new ModelAndView("empty");
	            mav.addObject("NoPageFormat", "true");
	            return mav;
	        } else {
	        	return null;
	        }
        } else {
            mav = createReportMAV(numberReport, report);
            List<ObjectError> errors = new ArrayList<ObjectError>(1);
            //Если необходимо конкретизировать ошибку для пустого отчета то можно сделать это здесь
            //для всех отчетов выводится error.reportform.nodata
            errors.add(new ObjectError(report.getJRName(), new String[]{"error.reportform.nodata"}, null, null));
			mav.addObject("errors", errors);
            return mav;
        }
    }

    @RequestMapping(value = "/managertools/report/{number}", method = RequestMethod.GET)
    public ModelAndView reportGet(@PathVariable("number") Integer number) throws JReportBuildError {
        return createReportMAV(number);
    }

    @RequestMapping(value = "/managertools/report/1", method = RequestMethod.POST)
    public ModelAndView showReport01(@ModelAttribute("reportForm") Report01 report, BindingResult result,
                                     @RequestParam("printtype") Integer printtype, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        fillRegionName(report);
        return showReport(report, result, printtype, 1, response, request);
    }

    @RequestMapping(value = "/managertools/report/2", method = RequestMethod.POST)
    public ModelAndView showReport02(@ModelAttribute("reportForm") Report02 report, BindingResult result,
                                     @RequestParam("printtype") Integer printtype, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        fillRegionName(report);
        return showReport(report, result, printtype, 2, response, request);
    }

    @RequestMapping(value = "/managertools/report/3", method = RequestMethod.POST)
    public ModelAndView showReport03(@ModelAttribute("reportForm") Report03 report, BindingResult result,
                                     @RequestParam("printtype") Integer printtype, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        fillRegionName(report);
        return showReport(report, result, printtype, 3, response, request);
    }

    @RequestMapping(value = "/managertools/report/4", method = RequestMethod.POST)
    public ModelAndView showReport04(@ModelAttribute("reportForm") Report04 report, BindingResult result,
                                     @RequestParam("printtype") Integer printtype, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        fillRegionName(report);
        return showReport(report, result, printtype, 4, response, request);
    }

    @RequestMapping(value = "/managertools/report/5", method = RequestMethod.POST)
    public ModelAndView showReport05(@ModelAttribute("reportForm") Report05 report, BindingResult result,
                                     @RequestParam("printtype") Integer printtype, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        fillRegionName(report);
        return showReport(report, result, printtype, 5, response, request);
    }

    @RequestMapping(value = "/managertools/report/6", method = RequestMethod.POST)
    public ModelAndView showReport06(@ModelAttribute("reportForm") Report06 report, BindingResult result,
                                     @RequestParam("printtype") Integer printtype, HttpServletResponse response, HttpServletRequest request) throws JReportBuildError {
        fillRegionName(report);
        return showReport(report, result, printtype, 6, response, request);
    }

    // Нужно для отображения названия региона в сформированном отчете
    private void fillRegionName(BaseReport report) {
        if (report.getRegionId().equals(0))
            report.setRegionName("Все");
        else
            report.setRegionName(regionService.find(report.getRegionId()).getName());
    }
}
