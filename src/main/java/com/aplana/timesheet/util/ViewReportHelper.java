package com.aplana.timesheet.util;

import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.enums.DictionaryEnum;
import com.aplana.timesheet.enums.VacationStatusEnum;
import com.aplana.timesheet.enums.VacationTypesEnum;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.service.*;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Temporal;
import java.text.ParseException;
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
    final private Integer PREVIOUS_DAY_MARK = 1;
    final private Integer HOLIDAY_MARK = 2;
    final private Integer TYPICAL_DAY_MARK = 0;

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

        checkVacationDay(year, month, vacations, vacationDates, VACATION_MARK);

        for (Map.Entry date : vacationDates.entrySet()) {
            final String sdate = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(date.getKey());
            builder.withField(sdate, aStringBuilder(date.getValue().toString()));
        }

        String format = JsonUtil.format(builder.build());
        logger.debug(format);
        return format;
    }

    /**
     * В мапу добавляются все дни месяца с учетом прошедших дней, выходных и праздников
     * @param year
     * @param month
     * @param employeeId
     * @param vacationDates
     * @param currentDate
     */
    private void addMonthDays(Integer year, Integer month, Integer employeeId, Map<Date, Integer> vacationDates, Date currentDate) {
        Employee emp = employeeService.find(employeeId);
        List<Calendar> monthDays = calendarService.getDateList(year, month);

        for (Calendar day : monthDays) {
            if (!holidayDAO.isWorkDay(day.getCalDate().toString(), emp.getRegion())) {
                vacationDates.put(day.getCalDate(), HOLIDAY_MARK);  //если выходной или праздничный день
            } else if (day.getCalDate().before(currentDate)) {
                vacationDates.put(day.getCalDate(), PREVIOUS_DAY_MARK);  //если это прошедший день
            } else {
                vacationDates.put(day.getCalDate(), TYPICAL_DAY_MARK);
            }
        }
    }

    /**
     * Метод отмечает дни обычного отпуска, полнового и их пересечения
     *
     * @param year
     * @param month
     * @param vacations
     * @param vacationDates мапа с днями и отмеченными выходными и праздниками
     * @param markValue     метка дня (обычный, плановый)
     */
    private void checkVacationDay(Integer year, Integer month, List<Vacation> vacations, Map<Date, Integer> vacationDates, Integer markValue) {
        Date lastDayofMonth = calendarService.getMaxDateMonth(year, month);
        Date firstDayofMonth = calendarService.getMinDateMonth(year, month);
        if (vacationDates != null) {
            for (Vacation vacation : vacations) {
                Long cnt = DateTimeUtil.getAllDaysCount(vacation.getBeginDate(), vacation.getEndDate()) - 1;//количество дней в отпуске
                for (Long i = 0L; i <= cnt; i++) {
                    Date vacationDay = DateUtils.addDays(vacation.getBeginDate(), i.intValue());
                    if (!vacationDay.after(lastDayofMonth) && !vacationDay.before(firstDayofMonth)) {
                        if (vacationDates.get(vacationDay) != HOLIDAY_MARK) {
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
        }
    }

    @Transactional
    public String getDateVacationWithPlannedListJson(Integer year, Integer month, Integer employeeId) {
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        Map<Date, Integer> vacationDates = getVacationWithPlannedMap(year, month, employeeId, false);

        for (Map.Entry date : vacationDates.entrySet()) {
            final String sdate = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(date.getKey());
            builder.withField(sdate, aStringBuilder(date.getValue().toString()));
        }

        String format = JsonUtil.format(builder.build());
        logger.debug(format);
        return format;
    }

    private Map<Date, Integer> getVacationWithPlannedMap(Integer year, Integer month, Integer employeeId, Boolean needForCalcCount) {
        Map<Date, Integer> vacationDates = new HashMap<Date, Integer>();
        Date currentDate = new Date((new Date()).getTime());

        addMonthDays(year, month, employeeId, vacationDates, currentDate);

        List<DictionaryItem> typesVac = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_TYPE.getId());
        DictionaryItem planned = dictionaryItemService.find(VacationTypesEnum.PLANNED.getId());
        typesVac.remove(planned);

        final List<Vacation> vacations;

        // Если необходимо посчитать дни отпуска сотрудника в getCountVacationAndPlannedVacationDays за выбранный месяц
        // то считаем дни «Отпуска с сохранением содержания» ( утвержденные заявление и заявления на согласовании) +
        // дни «Отпуска без сохранения содержания»( утвержденные заявление и заявления на согласовании), +
        // дни «Планируемого отпуска»
        if (needForCalcCount) {
            //без учета отпусков с отработкой
            typesVac.remove(dictionaryItemService.find(VacationTypesEnum.WITH_NEXT_WORKING.getId()));

            List<DictionaryItem> statusVac = dictionaryItemService.getItemsByDictionaryId(DictionaryEnum.VACATION_STATUS.getId());
            //нужны только утвержденные отпуска
            statusVac.remove(dictionaryItemService.find(VacationStatusEnum.REJECTED.getId()));

            vacations = vacationService.findVacationsByTypesAndStatuses(year, month, employeeId, typesVac, statusVac);
        } else {
            vacations = vacationService.findVacationsByTypes(year, month, employeeId, typesVac);
        }

        checkVacationDay(year, month, vacations, vacationDates, VACATION_MARK);

        final List<Vacation> vacationsPlanned = vacationService.findVacationsByType(year, month, employeeId, planned);

        //Отмечаем плановые отпуска
        checkVacationDay(year, month, vacationsPlanned, vacationDates, PLANNED_VACATION_MARK);
        return vacationDates;
    }

    /**
     * Возвращает количество дней утвержденных отпусков (+плановых), без учета отпусков с отработкой
     *
     * @param year
     * @param month
     * @param employeeId
     * @return
     */
    public Integer getCountVacationAndPlannedVacationDays(Integer year, Integer month, Integer employeeId) {
        Integer count = 0;
        Map<Date, Integer> vacationDates = getVacationWithPlannedMap(year, month, employeeId, true);
        for (Map.Entry date : vacationDates.entrySet()) {
            if (date.getValue() == PLANNED_VACATION_MARK || date.getValue() == VACATION_MARK || date.getValue() == CROSS_VACATION_MARK) {
                count++;
            }
        }
        return count;
    }
}
