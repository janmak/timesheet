package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.ViewReportsForm;
import com.aplana.timesheet.form.validator.ViewReportsFormValidator;
import com.aplana.timesheet.service.TimeSheetService;
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
public class ViewReportsController extends AbstractControllerForEmployeeWithYears {

    @Autowired
    ViewReportsFormValidator tsFormValidator;
    @Autowired
    TimeSheetService timeSheetService;

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

        ModelAndView mav = createModelAndViewForEmployee("viewreports", employeeId, divisionId);

        Employee employee = (Employee) mav.getModel().get(EMPLOYEE);

        mav.addObject("year", year);
        mav.addObject("month", month);
        mav.addObject("monthList", getMonthListJson((List<Calendar>) mav.getModel().get(YEARS_LIST)));
        mav.addObject("reports", timeSheetService.findDatesAndReportsForEmployee(employee, year, month));

        return mav;
    }

    /**
     * Возвращает List месяцев, существующих в системе
     *
     * @param years
     * @return String
     */
    private String getMonthListJson(List<Calendar> years) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < years.size(); i++) {
            List<Calendar> months = calendarService.getMonthList(years.get(i).getYear());
            sb.append("{year:'");
            sb.append(years.get(i).getYear());
            sb.append("', months:[");
            if (months.size() > 0) {
                for (int j = 0; j < months.size(); j++) {
                    sb.append("{number:'");
                    sb.append(months.get(j).getMonth());
                    sb.append("', name:'");
                    sb.append(months.get(j).getMonthTxt());
                    sb.append("'}");
                    if (j < (months.size() - 1)) {
                        sb.append(", ");
                    }
                }
                sb.append("]}");
            } else {
                sb.append("{year:'0', value:''}]}");
            }

            if (i < (years.size() - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}