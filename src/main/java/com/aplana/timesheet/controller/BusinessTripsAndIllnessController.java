package com.aplana.timesheet.controller;

import com.aplana.timesheet.controller.quickreport.BusinessTripsQuickReport;
import com.aplana.timesheet.controller.quickreport.IllnessesQuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReportGenerator;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.enums.Region;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EmployeeHelper;
import com.aplana.timesheet.util.EnumsUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * User: vsergeev
 * Date: 17.01.13
 */
@Controller
public class BusinessTripsAndIllnessController {

    public static final String UNCNOWN_PRINTTYPE_ERROR_MESSAGE = "Ошибка: запрашивается неизвестный тип отчета!";
    public static final String INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: в настройках указана неверная дата начала отчетного года!";
    public static final String INVALID_MOUNTH_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: не удается определить отчетный месяц!";
    public static final String NO_PRINTTYPE_FINDED_ERROR_MESSAGE = "Ошибка: не удалось получить тип отчета!";

    private static class YearPeriod {
        private Integer yearBeginDay;
        private Integer yearBeginMounth;
    }

    private static final int PRINT_TYPE_ERROR = -1;
    private static final int PRINT_TYPE_BUSINESS_TRIP = 1;
    private static final int PRINT_TYPE_ILLNESS = 2;

    private static final Logger logger = LoggerFactory.getLogger(BusinessTripsAndIllnessController.class);

    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    SecurityService securityService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    EmployeeHelper employeeHelper;
    @Autowired
    DivisionService divisionService;
    @Autowired
    TimeSheetService timeSheetService;
    @Autowired
    CalendarService calendarService;
    @Autowired
    @Qualifier("illnessesQuickReportGenerator")
    QuickReportGenerator<IllnessesQuickReport> illnessesQuickReportGenerator;
    @Autowired
    @Qualifier("businessTripsQuickReportGenerator")
    QuickReportGenerator<BusinessTripsQuickReport> businessTripsQuickReportGenerator;
    @Autowired
    TSPropertyProvider propertyProvider;

    @RequestMapping(value = "/businesstripsandillness")
    public String showBusinessTripsAndIllnessDefaultCall(){
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return String.format("redirect:/businesstripsandillness/%s/%s/%s/%s", securityService.getSecurityPrincipal().getEmployee().getDivision().getId(), securityService.getSecurityPrincipal().getEmployee().getId(), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1);
    }

    @RequestMapping(value = "/businesstripsandillness/{divisionId}/{employeeId}/{year}/{month}")
    public ModelAndView showBusinessTripsAndIllness(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @ModelAttribute("businessTripsAndIllnessForm") BusinessTripsAndIllnessForm tsForm,
            BindingResult result) {

        final Employee employee = employeeService.find(employeeId);
        List<Calendar> years = DateTimeUtil.getYearsList(calendarService);
        List<Division> divisionList = divisionService.getDivisions();
        Integer printtype = tsForm.getReporttype();
        QuickReport report = null;
        String exceptionMessage = null;

        try {
            report = getReport(printtype, employee, month, year);
        } catch (BusinessTripsAndIllnessControllerException e) {
            logger.error(e.getMessage(), e);
            printtype = PRINT_TYPE_ERROR;
            exceptionMessage = e.getMessage();
        }
        ModelAndView modelAndView = fillResponseModel(employee.getId(), employeeId, year, month, printtype, employee, years, divisionList, report, exceptionMessage);
        return modelAndView;
    }

    /**
     * Заполняем данные для ответа
     * @return
     */
    private ModelAndView fillResponseModel(Integer divisionId, Integer employeeId, Integer year, Integer mounth, Integer printtype,
                                           Employee employee, List<Calendar> years, List<Division> divisionList, QuickReport report, String exceptionMessage) {
        ModelAndView modelAndView = new ModelAndView("businesstripsandillness");
        modelAndView.addObject("year", year);
        modelAndView.addObject("month", mounth);
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employeeId);
        modelAndView.addObject("yearsList", years);
        modelAndView.addObject("employee", employee);
        modelAndView.addObject("monthList", DateTimeUtil.getMonthListJson(years, calendarService));
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisionList));
        modelAndView.addObject("reports", report);
        modelAndView.addObject("reportFormed", printtype);
        modelAndView.addObject("exceptionMessage", exceptionMessage);

        return modelAndView;
    }

    /**
     * В зависимости от типа запрашиваемого отчета формируем сам отчет
     * @return
     */
    private QuickReport getReport(Integer printtype, Employee employee, Integer mounth, Integer year) throws BusinessTripsAndIllnessControllerException {
        Date periodBeginDate = getMounthBeginDate(mounth, year);
        Date followingMounthBeginDate = DateUtils.addMonths(periodBeginDate, 1);
        Date periodEndDate = DateUtils.addDays(followingMounthBeginDate, -1);
        Date yearBeginDate = getYearBeginDate(employee, mounth, year);
        Date yearEndDate = DateUtils.addDays(DateUtils.addYears(yearBeginDate, 1), -1);
        if (printtype == null){
            throw new BusinessTripsAndIllnessControllerException(NO_PRINTTYPE_FINDED_ERROR_MESSAGE);
        }
        QuickReportGenerator generator = getQuickReportGenerator(printtype);

        return generator.generate(employee, periodBeginDate, periodEndDate, yearBeginDate, yearEndDate);
    }

    private Date getMounthBeginDate(Integer mounth, Integer year) throws BusinessTripsAndIllnessControllerException {
        try {
            return format.parse(String.format("01.%s.%s", mounth, year));
        } catch (ParseException ex) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_MOUNTH_BEGIN_DATE_ERROR_MESSAGE);
        }
    }

    private Date getYearBeginDate(Employee employee, Integer mounth, Integer year) throws BusinessTripsAndIllnessControllerException {
        try {
            YearPeriod yearPeriod = getYearPeriodForEmployyesRegion(employee);

            return generateYearBeginDate(yearPeriod, mounth, year);
        } catch (ParseException ex) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE);
        }
    }

    private Date generateYearBeginDate(YearPeriod yearPeriod, Integer mounth, Integer year) throws ParseException {
        if (mounth < yearPeriod.yearBeginMounth){    //если месяц меньше - значит период начался в предыдущем году
            year -= 1;
        }
        return format.parse(String.format("%s.%s.%s", yearPeriod.yearBeginDay, yearPeriod.yearBeginMounth, year));
    }

    private YearPeriod getYearPeriodForEmployyesRegion(Employee employee) {
        YearPeriod yearPeriod = new YearPeriod();

        Region regionEnum = EnumsUtils.getEnumById(Region.values(), employee.getRegion().getId());

        switch (regionEnum) {
            case MOSCOW: {
                yearPeriod.yearBeginDay = propertyProvider.getQuickreportMoskowBegindate();
                yearPeriod.yearBeginMounth = propertyProvider.getQuickreportMoskowBeginmounth();
                break;
            }
            case OTHERS:
            case UFA:
            case NIJNIY_NOVGOROD:
            case PERM: {
                yearPeriod.yearBeginDay = propertyProvider.getQuickreportRegionsBegindate();
                yearPeriod.yearBeginMounth = propertyProvider.getQuickreportRegionsBeginmounth();
            }
        }

        return yearPeriod;
    }

    private QuickReportGenerator getQuickReportGenerator(Integer printtype) throws BusinessTripsAndIllnessControllerException {
        switch (printtype){
            case PRINT_TYPE_ILLNESS: return illnessesQuickReportGenerator;
            case PRINT_TYPE_BUSINESS_TRIP: return businessTripsQuickReportGenerator;
            default: throw new BusinessTripsAndIllnessControllerException(UNCNOWN_PRINTTYPE_ERROR_MESSAGE);
        }
    }

}