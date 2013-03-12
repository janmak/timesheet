package com.aplana.timesheet.controller;

import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.util.JsonUtil;
import com.aplana.timesheet.util.ViewReportHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

import static argo.jdom.JsonNodeFactories.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class CalendarController extends AbstractController {

    public static final String IS_HOLIDAY_FIELD = "isHoliday";
    public static final String CALENDAR_ISHOLIDAY_URL = "/calendar/isholiday";
    public static final String ISHOLIDAY_DATE = "date";
    public static final String ISHOLIDAY_EMPLOYEE_ID = "employeeId";

    @Autowired
    private ViewReportHelper viewReportHelper;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private EmployeeService employeeService;

    @RequestMapping(value = "/calendar/dates", headers = "Accept=application/json")
    @ResponseBody
    public String reportDates(
            @RequestParam("queryYear") Integer queryYear,
            @RequestParam("queryMonth") Integer queryMonth,
            @RequestParam("employeeId") Integer employeeId
    ) {
        return viewReportHelper.getDateReportsListJson(queryYear, queryMonth, employeeId);
    }

    @RequestMapping(value = CALENDAR_ISHOLIDAY_URL, headers = "Accept=application/json")
    @ResponseBody
    public String isHoliday(
            @RequestParam(ISHOLIDAY_DATE) Date date,
            @RequestParam(ISHOLIDAY_EMPLOYEE_ID) Integer employeeId
    ) {
        return JsonUtil.format(
                object(
                        field(
                                IS_HOLIDAY_FIELD,
                                calendarService.isHoliday(date, employeeService.find(employeeId)) ? trueNode() : falseNode())
                )
        );
    }

}
