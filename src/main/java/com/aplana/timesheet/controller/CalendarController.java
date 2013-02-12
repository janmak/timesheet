package com.aplana.timesheet.controller;

import com.aplana.timesheet.util.ViewReportHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class CalendarController {

    @Autowired
    private ViewReportHelper viewReportHelper;

    @RequestMapping(value = "/calendar/dates", headers = "Accept=application/json")
    @ResponseBody
    public String reportDates(
            @RequestParam("queryYear") Integer queryYear,
            @RequestParam("queryMonth") Integer queryMonth,
            @RequestParam("employeeId") Integer employeeId
    ) {
        return viewReportHelper.getDateReportsListJson(queryYear, queryMonth, employeeId);
    }

}
