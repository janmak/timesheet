package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.DivisionService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.SecurityService;
import com.aplana.timesheet.util.EmployeeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
public abstract class AbstractControllerForEmployeeWithYears {

    protected static final Logger logger = LoggerFactory.getLogger(ViewReportsController.class);

    protected static final String YEARS_LIST = "yearsList";
    protected static final String EMPLOYEE = "employee";

    @Autowired
    protected EmployeeService employeeService;

    @Autowired
    protected CalendarService calendarService;

    @Autowired
    protected DivisionService divisionService;

    @Autowired
    protected EmployeeHelper employeeHelper;

    @Autowired
    protected SecurityService securityService;

    protected final ModelAndView createModelAndViewForEmployee(String viewName, Integer employeeId, Integer divisionId) {
        final ModelAndView modelAndView = new ModelAndView(viewName);

        Employee employee = employeeService.find(employeeId);

        List<Calendar> years = getYearsList();
        List<Division> divisionList = divisionService.getDivisions();

        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject(YEARS_LIST, years);
        modelAndView.addObject(EMPLOYEE, employee);
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisionList));

        return modelAndView;
    }

    /**
     * Возвращает List годов, существующих в системе
     * @return List<Calendar>
     */
    private List<Calendar> getYearsList() {
        final List<Calendar> yearsList = calendarService.getYearsList();

        logger.info(yearsList.toString());

        return yearsList;
    }

}
