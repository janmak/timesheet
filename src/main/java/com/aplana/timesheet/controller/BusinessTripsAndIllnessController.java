package com.aplana.timesheet.controller;

import com.aplana.timesheet.controller.quickreport.BusinessTripsQuickReport;
import com.aplana.timesheet.controller.quickreport.IllnessesQuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReport;
import com.aplana.timesheet.controller.quickreport.QuickReportGenerator;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.dao.entity.Division;
import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Permission;
import com.aplana.timesheet.enums.PermissionsEnum;
import com.aplana.timesheet.enums.QuickReportTypesEnum;
import com.aplana.timesheet.enums.RegionsEnum;
import com.aplana.timesheet.exception.controller.BusinessTripsAndIllnessControllerException;
import com.aplana.timesheet.form.BusinessTripsAndIllnessForm;
import com.aplana.timesheet.properties.TSPropertyProvider;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.aplana.timesheet.enums.PermissionsEnum.CHANGE_ILLNESS_BUSINESS_TRIP;
import static com.aplana.timesheet.enums.PermissionsEnum.VIEW_ILLNESS_BUSINESS_TRIP;

/**
 * User: vsergeev
 * Date: 17.01.13
 */
@Controller
public class BusinessTripsAndIllnessController extends AbstractController{

    private static final String UNCNOWN_PRINTTYPE_ERROR_MESSAGE = "Ошибка: запрашивается неизвестный тип отчета!";
    private static final String INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: в настройках указана неверная дата начала отчетного года! Установлен год по умолчанию.";
    private static final String INVALID_MOUNTH_BEGIN_DATE_ERROR_MESSAGE = "Ошибка: не удается определить отчетный месяц!";
    private static final String NO_PRINTTYPE_FINDED_ERROR_MESSAGE = "Ошибка: не удалось получить тип отчета!";
    private static final String ACCESS_ERROR_MESSAGE = "Не удается считать права на формирование отчетов!";
    private static final String ACCESS_DENIED_ERROR_MESSAGE = "У Вас недостаточно прав для просмотра отчетов других сотрудников!";
    private static final String UNCNOWN_REGION_EXCEPTION_MESSAGE = "Сотрудник имеет неизвестный регион!";
    private static final String INVALID_DATES_IN_SETTINGS_EXCEPTION_MESSAGE = "В файле настроек указаны неверные даты для региона!";

    //дефолтные дни начала года для регионов и Москвы
    private static final int DEFAULT_MOSCOW_YEAR_BEGIN_MONTH = java.util.Calendar.APRIL;
    private static final int DEFAULT_MOSCOW_YEAR_BEGIN_DAY = 1;
    private static final Integer DEFAULT_REGION_YEAR_BEGIN_MONTH = java.util.Calendar.SEPTEMBER;
    private static final Integer DEFAULT_REGION_YEAR_BEGIN_DAY = 1;

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
    DictionaryItemService dictionaryItemService;
    @Autowired
    BusinessTripService businessTripService;
    @Autowired
    IllnessService illnessService;
    @Autowired
    @Qualifier("illnessesQuickReportGenerator")
    QuickReportGenerator<IllnessesQuickReport> illnessesQuickReportGenerator;
    @Autowired
    @Qualifier("businessTripsQuickReportGenerator")
    QuickReportGenerator<BusinessTripsQuickReport> businessTripsQuickReportGenerator;
    @Autowired
    TSPropertyProvider propertyProvider;

    @RequestMapping(value = "/businesstripsandillness/delete/{reportId}/{reportType}", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteReport(@PathVariable("reportId") Integer reportId,
                               @PathVariable("reportType") Integer reportType) {
        try {
            final QuickReportTypesEnum reportTypeAsEnum = EnumsUtils.getEnumById(reportType, QuickReportTypesEnum.class);
            switch (reportTypeAsEnum) {
                case BUSINESS_TRIP:
                    return deleteBusinessTrip(reportId);
                case ILLNESS:
                    return deleteIllness(reportId);
                default:
                    return "Удаление такого типа отчетовеще не реализовано!";
            }
        } catch (NoSuchElementException ex) {
            logger.error("Неизвестный тип отчета!", ex);
            return ("Ошибка при удалении: неизвестный тип отчета!");
        } catch (BusinessTripsAndIllnessControllerException ex) {
            return ex.getMessage();
        }
    }

    @RequestMapping(value = "/businesstripsandillness")
    public String showBusinessTripsAndIllnessDefaultCall() {
        Employee currentUser = securityService.getSecurityPrincipal().getEmployee();
        Integer divisionId = currentUser.getDivision().getId();
        Integer employeeId = currentUser.getId();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH) + 1;

        return String.format("redirect:/businesstripsandillness/%s/%s/%s/%s", divisionId, employeeId, year, month);
    }

    @RequestMapping(value = "/businesstripsandillness/{divisionId}/{employeeId}/{year}/{month}")
    public ModelAndView showBusinessTripsAndIllness(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm tsForm,
            BindingResult result) throws BusinessTripsAndIllnessControllerException {
        Integer printtype = tsForm.getReportType();
        Employee employee = employeeService.find(employeeId);
        List<Calendar> years = DateTimeUtil.getYearsList(calendarService);
        List<Division> divisionList = divisionService.getDivisions();
        PermissionsEnum recipientPermission = getRecipientPermission(employee);
        if (recipientPermission == null) { //сотрудник запрашивает отчет другого сотрудника (не свой), но нет прав на просмотр чужих отчетов
            employee = getTimeSheetUser();    //формируем отчет для него
        }
        QuickReport report = getReport(printtype, employee, month, year);
        return  fillResponseModel(divisionId, year, month, printtype, employee, years, divisionList, report, recipientPermission);
    }

    @RequestMapping(value = "/businesstripsandillness/{divisionId}/{employeeId}/{year}/{month}/{reportTypeId}")
    public ModelAndView showBusinessTripsAndIllnessWithResult(
            @PathVariable("divisionId") Integer divisionId,
            @PathVariable("employeeId") Integer employeeId,
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @PathVariable("reportTypeId") Integer reportTypeId,
            @ModelAttribute("businesstripsandillness") BusinessTripsAndIllnessForm tsForm,
            BindingResult result) throws BusinessTripsAndIllnessControllerException {
        tsForm.setReportType(reportTypeId);

        return showBusinessTripsAndIllness(divisionId, employeeId, year, month, tsForm, result);
    }

    /**
     * Удаляем больничный. Если удаление прошло нормально, то возвращаем пустую строку.
     */
    private String deleteIllness(Integer reportId) throws BusinessTripsAndIllnessControllerException {
        try {
            illnessService.deleteIllnessById(reportId);
            return StringUtils.EMPTY;
        } catch (Throwable th) {
            throw new BusinessTripsAndIllnessControllerException("Ошибка при удалении больничного из БД!");
        }
    }

    /**
     * Удаляем командировку. Если удаление прошло нормально, то возвращаем пустую строку.
     */
    private String deleteBusinessTrip(Integer reportId) throws BusinessTripsAndIllnessControllerException {
        try {
            businessTripService.deleteBusinessTripById(reportId);
            return StringUtils.EMPTY;
        } catch (Throwable th) {
            throw new BusinessTripsAndIllnessControllerException("Ошибка при удалении командировки из БД!");
        }
    }

    /**
     * Получаем права сотрудника для просмотреа отчетов. Если у сотрудника прав несколько, то возвращается разрешение с
     * наивысшим приоритетом.
     */
    private PermissionsEnum getRecipientPermission(Employee employee) throws BusinessTripsAndIllnessControllerException {
        Employee reportRecipient = getTimeSheetUser();
        Set<Permission> recipientPermissions = reportRecipient.getPermissions();
        if (recipientCanChangeReports(recipientPermissions)) {
            return CHANGE_ILLNESS_BUSINESS_TRIP;
        } else if (recipientRequiresOwnReports(employee, reportRecipient) || recipientCanViewAllReports(recipientPermissions)) {
            return VIEW_ILLNESS_BUSINESS_TRIP;
        } else {
            logger.error(ACCESS_DENIED_ERROR_MESSAGE, new BusinessTripsAndIllnessControllerException(ACCESS_DENIED_ERROR_MESSAGE));
            return null;
        }
    }

    /**
     * Получаем пользователя из спринга
     */
    private Employee getTimeSheetUser() throws BusinessTripsAndIllnessControllerException {
        TimeSheetUser securityUser = securityService.getSecurityPrincipal();
        if (securityUser == null) {
            throw new BusinessTripsAndIllnessControllerException(ACCESS_ERROR_MESSAGE);
        }
        Employee employee = employeeService.find(securityUser.getEmployee().getId());  //из-за lazy loading приходится занова получать сотрудника для начала транзакции

        return employee;
    }

    /**
     * проверяем, может ли получатель редактировать отчеты
     */
    private boolean recipientCanChangeReports(Set<Permission> recipientPermissions) {
        return checkRecipientForPermission(recipientPermissions, CHANGE_ILLNESS_BUSINESS_TRIP);
    }

    /**
     * проверяем, может ли получатель видеть отчеты всех сотрудников
     */
    private boolean recipientCanViewAllReports(Set<Permission> recipientPermissions) {
        return checkRecipientForPermission(recipientPermissions, VIEW_ILLNESS_BUSINESS_TRIP);
    }

    /**
     * проверяем, не хочет ли сотрудник посмотреть свой собственный отчет
     */
    private boolean recipientRequiresOwnReports(Employee employee, Employee reportRecipient) {
        return reportRecipient.getId().equals(employee.getId());
    }

    /**
     * находим среди разрешений сотрудника нужное
     */
    private boolean checkRecipientForPermission(Set<Permission> recipientPermissions, final PermissionsEnum permission) {
        try {
            Iterables.find(recipientPermissions, new Predicate<Permission>() {
                @Override
                public boolean apply(@Nullable Permission perm) {
                    return perm.getId().equals(permission.getId());
                }
            });
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    /**
     * заполняем данные об отчетах сотрудников и возвращаем формочку с табличкой по нужному типу отчетов
     */
    private ModelAndView fillResponseModel(Integer divisionId, Integer year, Integer month, Integer printtype,
                                           Employee employee, List<Calendar> years, List<Division> divisionList, QuickReport report, PermissionsEnum recipientPermission) {
        ModelAndView modelAndView = new ModelAndView("businesstripsandillness");
        modelAndView.addObject("year", year);
        modelAndView.addObject("month", month);
        modelAndView.addObject("divisionId", divisionId);
        modelAndView.addObject("employeeId", employee.getId());
        modelAndView.addObject("yearsList", years);
        modelAndView.addObject("employeeName", employee.getName());
        modelAndView.addObject("monthList", DateTimeUtil.getMonthListJson(years, calendarService));
        modelAndView.addObject("divisionList", divisionList);
        modelAndView.addObject("employeeListJson", employeeHelper.getEmployeeListJson(divisionList, employeeService.isShowAll(request)));
        modelAndView.addObject("reports", report);
        modelAndView.addObject("reportFormed", printtype);
        modelAndView.addObject("recipientPermission", recipientPermission);

        return modelAndView;
    }

    /**
     * В зависимости от типа запрашиваемого отчета, формируем сам отчет
     */
    private QuickReport getReport(Integer printtype, Employee employee, Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        Date periodBeginDate = getMonthBeginDate(month, year);
        Date followingMonthBeginDate = DateUtils.addMonths(periodBeginDate, 1);
        Date periodEndDate = DateUtils.addDays(followingMonthBeginDate, -1);
        Date yearBeginDate = getYearBeginDate(employee, month, year);
        Date yearEndDate = DateUtils.addDays(DateUtils.addYears(yearBeginDate, 1), -1);
        if (printtype == null) {
            throw new BusinessTripsAndIllnessControllerException(NO_PRINTTYPE_FINDED_ERROR_MESSAGE);
        }
        QuickReportGenerator generator = getQuickReportGenerator(printtype);

        return generator.generate(employee, periodBeginDate, periodEndDate, yearBeginDate, yearEndDate);
    }

    /**
     * возвращает дату (первое число по указанным месяцу и году)
     */
    private Date getMonthBeginDate(Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        try {
            return format.parse(String.format("01.%s.%s", month, year));
        } catch (ParseException ex) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_MOUNTH_BEGIN_DATE_ERROR_MESSAGE);
        }
    }

    /**
     * Возвращает дату начала ОТЧЕТНОГО года, в который входит выбранный месяц выбранного КАЛЕНДАРНОГО года.
     * даты начала/конца ОТЧЕТНЫХ годов берутся либо из дефолтных значений, либо из файла настроек таймшита
     */
    private Date getYearBeginDate(Employee employee, Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        DateNumbers dateNumbers = getYearPeriodForEmployyesRegion(employee);
        return generateYearBeginDate(dateNumbers, month, year);
    }

    /**
     * Превращаем DateNumbers в Date попутно проверяя, в какой год попадает отчетный месяц.
     * Если отчетный месяц меньше месяца начала периода - значит период начался в предыдущем году относительно года отчетного месяца.
     * Год нужно уменьшить.
     */
    private Date generateYearBeginDate(DateNumbers dateNumbers, Integer month, Integer year) throws BusinessTripsAndIllnessControllerException {
        try {
            if (month < dateNumbers.getDatabaseMonth()) {
                year -= 1;
            }

            return format.parse(String.format("%s.%s.%s", dateNumbers.getDay(), dateNumbers.getDatabaseMonth(), year));
        } catch (ParseException e) {
            throw new BusinessTripsAndIllnessControllerException(INVALID_DATES_IN_SETTINGS_EXCEPTION_MESSAGE);
        }
    }

    /**
     * Получаем начало ОТЧЕТНОГО года для региона, в котором работает данный сотрудник.
     * Дата начала либо считывается из файла настроек таймшита, либо выставляется по умолчанию
     * (1.04 для Москвы или 1.09 для регионов)
     */
    private DateNumbers getYearPeriodForEmployyesRegion(Employee employee) throws BusinessTripsAndIllnessControllerException {
        RegionsEnum regionEnum = EnumsUtils.getEnumById(employee.getRegion().getId(), RegionsEnum.class);

        switch (regionEnum) {
            case MOSCOW: {
                try {
                    return getMoskowYearBeginDates();
                } catch (NumberFormatException ex) {
                    logger.error(INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE);
                    return getDefaultMoskowYearBeginDates();
                } catch (NullPointerException ex) {
                    return getDefaultMoskowYearBeginDates();
                }
            }
            case OTHERS:
            case UFA:
            case NIJNIY_NOVGOROD:
            case PERM: {
                try {
                    return getRegionsYearBeginDates();
                } catch (NumberFormatException ex) {
                    logger.error(INVALID_YEAR_BEGIN_DATE_ERROR_MESSAGE);
                    return getDefaultRegionsYearBeginDates();
                } catch (NullPointerException ex) {
                    return getDefaultMoskowYearBeginDates();
                }
            }
            default:
                throw new BusinessTripsAndIllnessControllerException(UNCNOWN_REGION_EXCEPTION_MESSAGE);
        }
    }

    /**
     * возвращает даты начала ОТЧЕТНОГО года для Москвы
     */
    private DateNumbers getMoskowYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setDay(propertyProvider.getQuickreportMoskowBeginDay());
        dateNumbers.setMonth(propertyProvider.getQuickreportMoskowBeginMonth());

        return dateNumbers;
    }


    /**
     * возвращает даты начала ОТЧЕТНОГО года для регионов
     */
    private DateNumbers getRegionsYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setDay(propertyProvider.getQuickreportRegionsBeginDay());
        dateNumbers.setMonth(propertyProvider.getQuickreportRegionsBeginMonth());

        return dateNumbers;
    }

    /**
     * Возвращает дефолтные даты начала ОТЧЕТНОГО года для регионов
     */
    private DateNumbers getDefaultRegionsYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setMonth(DEFAULT_REGION_YEAR_BEGIN_MONTH);
        dateNumbers.setDay(DEFAULT_REGION_YEAR_BEGIN_DAY);

        return dateNumbers;
    }

    /**
     * возвращает дефолтные даты начала ОТЧЕТНОГО года для Москвы
     */
    private DateNumbers getDefaultMoskowYearBeginDates() {
        DateNumbers dateNumbers = new DateNumbers();
        dateNumbers.setMonth(DEFAULT_MOSCOW_YEAR_BEGIN_MONTH);
        dateNumbers.setDay(DEFAULT_MOSCOW_YEAR_BEGIN_DAY);

        return dateNumbers;
    }

    /**
     * В зависимости от типа отчета ввозвращает нужный генератор
     */
    private QuickReportGenerator getQuickReportGenerator(Integer printtype) throws BusinessTripsAndIllnessControllerException {
        QuickReportTypesEnum quickReportType = EnumsUtils.getEnumById(printtype, QuickReportTypesEnum.class);
        switch (quickReportType) {
            case ILLNESS:
                return illnessesQuickReportGenerator;
            case BUSINESS_TRIP:
                return businessTripsQuickReportGenerator;
            default:
                throw new BusinessTripsAndIllnessControllerException(UNCNOWN_PRINTTYPE_ERROR_MESSAGE);
        }
    }

}