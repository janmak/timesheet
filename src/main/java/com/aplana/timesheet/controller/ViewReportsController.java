package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.ViewReportsForm;
import com.aplana.timesheet.form.validator.ViewReportsFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EmployeeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ViewReportsController {
    private static final Logger logger = LoggerFactory.getLogger(ViewReportsController.class);

    @Autowired
    EmployeeHelper employeeHelper;
    @Autowired
    DivisionService divisionService;
    @Autowired
    ViewReportsFormValidator tsFormValidator;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    TimeSheetService timeSheetService;
    @Autowired
    SecurityService securityService;
    @Autowired
    CalendarService calendarService;

    @RequestMapping(value = "/viewreports", method = RequestMethod.GET)
    public String sendViewReports() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("redirect:/viewreports/%s/%s/%s/%s", securityService.getSecurityPrincipal().getEmployee().getDivision().getId(), securityService.getSecurityPrincipal().getEmployee().getId(), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1);
    }

    @RequestMapping(value = "/viewreports/{divisionId}/{employeeId}/{year}/{month}")
    public ModelAndView showDates(
            @PathVariable("divisionId") Integer divisionId, 
            @PathVariable("employeeId") Integer employeeId, 
            @PathVariable("year") Integer year, 
            @PathVariable("month") Integer month, 
            @ModelAttribute("viewReportsForm") ViewReportsForm tsForm, 
            BindingResult result
    ) {
        logger.info("year {}, month {}", year, month);
        tsFormValidator.validate(tsForm, result);

        Employee employee = employeeService.find(employeeId);
        ModelAndView mav = new ModelAndView("viewreports");
        List<Calendar> years = DateTimeUtil.getYearsList(calendarService);
        List<Division> divisionList = divisionService.getDivisions();
        mav.addObject("year", year);
        mav.addObject("month", month);
        mav.addObject("divisionId", divisionId);
        mav.addObject("employeeId", employeeId);
        mav.addObject("yearsList", years);
        mav.addObject("employee", employee);
        mav.addObject("monthList", DateTimeUtil.getMonthListJson(years, calendarService));
        mav.addObject("divisionList", divisionList);
        mav.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisionList));
        mav.addObject("reports", timeSheetService.findDatesAndReportsForEmployee(employee, year, month));
        return mav;
    }

}