package com.aplana.timesheet.controller;

import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.DayTimeSheet;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.ViewReportsForm;
import com.aplana.timesheet.form.validator.ViewReportsFormValidator;
import com.aplana.timesheet.service.*;
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

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    CalendarService calendarService;
    @Autowired
    TimeSheetService timeSheetService;
    @Autowired
    SecurityService securityService;

    @RequestMapping(value = "/viewreports", method = RequestMethod.GET)
    public /*ModelAndView*/ String sendViewReports(/*, @ModelAttribute("viewReportsForm") ViewReportsForm tsForm, BindingResult result*/) {
        /* logger.info("employeeId {}.", employeeId);
       ModelAndView mav = new ModelAndView("viewreports");
       mav.addObject("year", 0);
       mav.addObject("month", 0);
       mav.addObject("viewReportsForm", tsForm);
       mav.addObject("employeeId", employeeId);
       mav.addObject("employeeName", employeeService.find(employeeId).getName());
       mav.addObject("yearsList", getYearsList());
       List<Calendar> years = calendarService.getYearsList();
       mav.addObject("monthList", getMonthListJson(years));
       java.util.Calendar date = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
       date.setTime(new java.util.Date());
       logger.info("<<<<<<<<< End of RequestMapping <<<<<<<<<<<<<<<<<<<<<<");
       return mav;*/
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("redirect:/viewreports/%s/%s/%s/%s", securityService.getSecurityPrincipal().getEmployee().getDivision().getId(), securityService.getSecurityPrincipal().getEmployee().getId(), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1);
    }

    @RequestMapping(value = "/viewreports/{divisionId}/{employeeId}/{year}/{month}")
    public ModelAndView showDates(@PathVariable("divisionId") Integer divisionId, @PathVariable("employeeId") Integer employeeId, @PathVariable("year") Integer year, @PathVariable("month") Integer month, @ModelAttribute("viewReportsForm") ViewReportsForm tsForm, BindingResult result) {
        logger.info("year {}, month {}", year, month);
        tsFormValidator.validate(tsForm, result);

        Employee employee = employeeService.find(employeeId);
        ModelAndView mav = new ModelAndView("viewreports");
        List<Calendar> years = calendarService.getYearsList();
        List<Division> divisionList = divisionService.getDivisions();

        mav.addObject("year", year);
        mav.addObject("month", month);
        mav.addObject("divisionId", divisionId);
        mav.addObject("viewReportsForm", tsForm);
        mav.addObject("employeeId", employeeId);
        mav.addObject("employeeName", employee.getName());
        mav.addObject("yearsList", years);
        mav.addObject("monthList", getMonthListJson(years));
        mav.addObject("divisionList", divisionList);
        mav.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisionList));

        java.util.Calendar date = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
        date.setTime(new java.util.Date());
        //List<Calendar> calList = calendarService.getDateList(year, month);
        ArrayList<String[]> dateList = new ArrayList<String[]>();

        // + Лубянов, 28.12.2011
        // получаем id региона сотрудника
        Integer regionId = employee.getRegion().getId();
        // - Лубянов

        List<DayTimeSheet> calTSList = timeSheetService.findDatesAndReportsForEmployee(year, month, regionId, employee);

        //берем текущий месяц
        java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
        calendar.setTime(new java.util.Date());
        int monthInt = calendar.get(java.util.Calendar.MONTH) + 1;
        int yearInt = calendar.get(java.util.Calendar.YEAR);
        int currentDayInt = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        Boolean isCurMonth = (month == monthInt) && (year == yearInt);

        Integer countMonth = 0;
        Integer plus = 0;
        BigDecimal duration = new BigDecimal("0.0");
        for (int i = 0; i < calTSList.size(); i++) {
            DayTimeSheet queryResult = calTSList.get(i);

            String calDate = queryResult.getCalDate().toString();

            String[] oneDay = new String[10];
            oneDay[0] = calDate.substring(8, 10);

            if (queryResult.getId() != null) {
                oneDay[1] = String.valueOf(queryResult.getId());
            } else {
                oneDay[1] = null;
            }
            oneDay[2] = String.valueOf(year);
            oneDay[3] = String.valueOf(month);

            //является ли день рабочим
            if (!queryResult.getWorkDay()) {
                oneDay[4] = "true";

            } else {
                oneDay[4] = null;
            }

            oneDay[5] = oneDay[0];//String.valueOf(curCal.getCalDate().toString().substring(8, 10));
            switch (month) {
                case 1:
                    oneDay[6] = "января";
                    break;
                case 2:
                    oneDay[6] = "февраля";
                    break;
                case 3:
                    oneDay[6] = "марта";
                    break;
                case 4:
                    oneDay[6] = "апреля";
                    break;
                case 5:
                    oneDay[6] = "мая";
                    break;
                case 6:
                    oneDay[6] = "июня";
                    break;
                case 7:
                    oneDay[6] = "июля";
                    break;
                case 8:
                    oneDay[6] = "августа";
                    break;
                case 9:
                    oneDay[6] = "сентября";
                    break;
                case 10:
                    oneDay[6] = "октября";
                    break;
                case 11:
                    oneDay[6] = "ноября";
                    break;
                case 12:
                    oneDay[6] = "декабря";
                    break;
                default:
                    oneDay[6] = "";//curCal.getMonthTxt();
                    logger.error("Неверный номер месяца: " + month);
                    break;
            }

            oneDay[6] = oneDay[6] + " " + year + "г. ";
            oneDay[7] = new SimpleDateFormat("yyyy-MM-dd").format(queryResult.getCalDate()); // полная дата для передачи параметра в /timesheet
            //if ((month <= monthInt) && (year <= yearInt)) {
            if (currentDayInt >= Integer.parseInt(oneDay[5]) || !isCurMonth) {
                oneDay[9] = "false";
                if (queryResult.getWorkDay()) countMonth++;
                if (queryResult.getAct_type() == null) {
                    //если не списано за этот день
                    //?
                } else {
                    //если списана занятость
                    Integer act = queryResult.getAct_type();
                    switch (act) {
                        case 15:
                        case 24:
                            oneDay[8] = "Отгул (" + queryResult.getDuration() + ")";
                            //duration = duration.subtract(queryResult.getDuration());
                            break;
                        case 16:
                            oneDay[8] = "Отпуск";
                            if (queryResult.getWorkDay()) //minus++;
                                plus++;
                            break;
                        case 17:
                            oneDay[8] = "Болезнь";
                            if (queryResult.getWorkDay()) //minus++;
                                plus++;
                            break;
                        case 18:
                            oneDay[8] = "Нерабочий день";
                            if (queryResult.getWorkDay()) //minus++;
                                plus++;
                            break;
                        default:
                            duration = duration.add(queryResult.getDuration());
                            oneDay[8] = queryResult.getDuration().toString();
                            break;
                    }
                }
            } else {
                oneDay[9] = "true";
            }
            dateList.add(oneDay);

        }
        mav.addObject("timeFact", duration);
        //если год меньше текущего
        if (year == yearInt) {
            if (month > monthInt) mav.addObject("timePlan", "0");
            else
                mav.addObject("timePlan", (countMonth - plus) * 8 + (month < monthInt ? "" : (" (до " + (currentDayInt) + "-го) ")));
        } else if (year > yearInt) {
            mav.addObject("timePlan", "0");
        } else {
            mav.addObject("timePlan", (countMonth - plus) * 8);
        }
        mav.addObject("dateList", dateList);
        return mav;
    }

    /**
     * Возвращает List годов, существующих в системе
     */
    private List<Calendar> getYearsList() {
        List<Calendar> yearsList = calendarService.getYearsList();
        logger.info(yearsList.toString());
        return yearsList;
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
            //List<Calendar> months = calendarService.getMonthList();
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