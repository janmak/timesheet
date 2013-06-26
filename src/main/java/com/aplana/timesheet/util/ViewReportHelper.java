package com.aplana.timesheet.util;

import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.HolidayDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.service.CalendarService;
import com.aplana.timesheet.service.EmployeeService;
import com.aplana.timesheet.service.TimeSheetService;
import com.aplana.timesheet.service.VacationService;
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

    private static final Logger logger = LoggerFactory.getLogger(ViewReportHelper.class);

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
        Employee emp = employeeService.find(employeeId);
        List<Calendar> monthDays = calendarService.getDateList(year, month);
        Date currentDate = new Date((new java.util.Date()).getTime());
        //Добавляем в мапу все дни месяца
        for (Calendar day : monthDays) {
            if (!holidayDAO.isWorkDay(day.getCalDate().toString(), emp.getRegion())) {
                if (!day.getCalDate().before(currentDate)) {
                    vacationDates.put(day.getCalDate(), 2);  //если выходной или праздничный день
                } else {
                    vacationDates.put(day.getCalDate(), 1);  //если это прошедший день
                }
            } else if (day.getCalDate().before(currentDate)) {
                vacationDates.put(day.getCalDate(), 1);  //если это прошедший день
            } else {
                vacationDates.put(day.getCalDate(), 0);
            }
        }
        //отмечаем дни отпуска
        for (Vacation vacation : vacations) {
            if (!vacation.getEndDate().before(currentDate)) {
                //количество дней в отпуске
                Long cnt = DateTimeUtil.getAllDaysCount(vacation.getBeginDate(), vacation.getEndDate()) - 1;
                for (Long i = 0L; i <= cnt; i++) {
                    Date vacationDay = DateUtils.addDays(vacation.getBeginDate(), i.intValue());
                    if (vacationDay.after(currentDate)) {
                        if (vacationDates.get(vacationDay) != null) {
                            vacationDates.put(vacationDay, 3);
                        }
                        ;
                    }
                }
            }
        }
        for (Map.Entry date: vacationDates.entrySet()) {
            final String sdate = new SimpleDateFormat(DateTimeUtil.DATE_PATTERN).format(date.getKey());
            builder.withField(sdate, aStringBuilder(date.getValue().toString()));
        }

        String format = JsonUtil.format(builder.build());
        return format;
    }
}
