package com.aplana.timesheet.util;

import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.service.*;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;

@Service
public class ViewReportHelper {

    @Autowired
    TimeSheetService timeSheetService;

    @Autowired
    VacationService vacationService;

    @Autowired
    CalendarService calendarService;

    @Autowired
    HolidayDAO holidayDAO;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    private static final Logger logger = LoggerFactory.getLogger(ViewReportHelper.class);

    final private Integer PLANNED_VACATION_MARK = 4;
    final private Integer VACATION_MARK = 3;
    final private Integer CROSS_VACATION_MARK = 5;

    @Transactional
    public String getDateReportsListJson(Integer year, Integer month, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();
        final List<DayTimeSheet> calTSList = timeSheetService.findDatesAndReportsForEmployee(year, month, employeeId);

        for (DayTimeSheet queryResult : calTSList) {
            final String day = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(queryResult.getCalDate());

            Integer value = 0; //если нет отчета

            if ((queryResult.getId() != null) || (queryResult.getVacationDay()) || (queryResult.getIllnessDay()))
                value = 1;   //если есть отчет
            else if (!queryResult.getWorkDay())
                value = 2;   //если выходной или праздничный день

            builder.withField(day, aStringBuilder(value.toString()));
        }

        return JsonUtil.format(builder.build());
    }

    @Transactional
    public String getDateVacationListJson(Integer year, Integer month, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();
        final List<Vacation> vacations = vacationService.findVacations(year, month, employeeId);
        Map<Date, Integer> vacationDates = new HashMap<Date, Integer>();
        Date currentDate = new Date((new java.util.Date()).getTime());

        addMonthDays(year, month, employeeId, vacationDates, currentDate);

        checkVacationDay(vacations, vacationDates, VACATION_MARK);

        for (Map.Entry date: vacationDates.entrySet()) {
            final String sdate = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(date.getKey());
            builder.withField(sdate, aStringBuilder(date.getValue().toString()));
        }

        String format = JsonUtil.format(builder.build());
        logger.debug(format);
        return format;
    }

    private void addMonthDays(Integer year, Integer month, Integer employeeId, Map<Date, Integer> vacationDates, Date currentDate) {
        Employee emp = employeeService.find(employeeId);
        List<Calendar> monthDays = calendarService.getDateList(year, month);

        //Добавляем в мапу все дни месяца
        for (Calendar day : monthDays) {
            if (!holidayDAO.isWorkDay(day.getCalDate().toString(), emp.getRegion())) {
                    vacationDates.put(day.getCalDate(), 2);  //если выходной или праздничный день
            } else if (day.getCalDate().before(currentDate)) {
                vacationDates.put(day.getCalDate(), 1);  //если это прошедший день
            } else {
                vacationDates.put(day.getCalDate(), 0);
            }
        }
    }

    private void checkVacationDay(List<Vacation> vacations, Map<Date, Integer> vacationDates,/* Date currentDate,*/ Integer markValue) {
        //отмечаем дни отпуска
        for (Vacation vacation : vacations) {
            Long cnt = DateTimeUtil.getAllDaysCount(vacation.getBeginDate(), vacation.getEndDate()) - 1;//количество дней в отпуске
            for (Long i = 0L; i <= cnt; i++) {
                Date vacationDay = DateUtils.addDays(vacation.getBeginDate(), i.intValue());
                if (!(vacationDates.get(vacationDay) == 2)) {
                    if (vacationDates.get(vacationDay) != null && (markValue != PLANNED_VACATION_MARK)) {
                        vacationDates.put(vacationDay, markValue);
                    } else {
                        if (vacationDates.get(vacationDay) == VACATION_MARK) {
                            vacationDates.put(vacationDay, CROSS_VACATION_MARK);
                        } else {
                            vacationDates.put(vacationDay, markValue);
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public String getDateVacationWithPlannedListJson(Integer year, Integer month, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        Map<Date, Integer> vacationDates = new HashMap<Date, Integer>();
        Date currentDate = new Date((new java.util.Date()).getTime());

        addMonthDays(year, month, employeeId, vacationDates, currentDate);

        List<DictionaryItem> typesVac = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId());
        DictionaryItem planned = dictionaryItemService.find(VacationTypesEnum.PLANNED.getId());
        typesVac.remove(planned);

        final List<Vacation> vacations = vacationService.findVacationsByTypes(year, month, employeeId, typesVac);

        checkVacationDay(vacations, vacationDates, VACATION_MARK);


        final List<Vacation> vacationsPlanned = vacationService.findVacationsByType(year, month, employeeId, planned);

        //Отмечаем плановые отпуска
        checkVacationDay(vacationsPlanned, vacationDates, PLANNED_VACATION_MARK);

        for (Map.Entry date: vacationDates.entrySet()) {
            final String sdate = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(date.getKey());
            builder.withField(sdate, aStringBuilder(date.getValue().toString()));
        }

        String format = JsonUtil.format(builder.build());
        return format;
    }
}
