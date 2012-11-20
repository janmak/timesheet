package com.aplana.timesheet.service;

import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.dao.entity.Calendar;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.TimeSheetUser;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TimeSheetService {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetService.class);

    @Autowired
    private TimeSheetDAO timeSheetDAO;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    public VelocityEngine velocityEngine;

    @Autowired
    public ProjectRoleService projectRoleService;
    
    @Autowired
    public SecurityService securityService;

    public void storeTimeSheet(TimeSheetForm tsForm) {
        TimeSheet timeSheet = new TimeSheet();
        logger.debug("Selected employee id = {}", tsForm.getEmployeeId());
        logger.debug("Selected calDate = {}", tsForm.getCalDate());
        timeSheet.setEmployee(employeeService.find(tsForm.getEmployeeId()));
        timeSheet.setCalDate(calendarService.find(tsForm.getCalDate()));
        timeSheet.setPlan(tsForm.getPlan());

        List<TimeSheetTableRowForm> tsTablePart = tsForm.getTimeSheetTablePart();
        Set<TimeSheetDetail> timeSheetDetails = new LinkedHashSet<TimeSheetDetail>();
        for (TimeSheetTableRowForm formRow : tsTablePart) {
            // По каким-то неведомым причинам при нажатии на кнопку веб
            // интерфейса "Удалить выбранные строки"
            // (если выбраны промежуточные строки) они удаляются с формы, но в
            // объект формы вместо них
            // попадают null`ы. Мы эти строки удаляем из объекта формы. Если
            // удалять последние строки (с конца
            // табличной части формы), то все работает корректно.
            if (formRow.getActivityTypeId() == null) {
                tsTablePart.remove(formRow);
                continue;
            }

            TimeSheetDetail timeSheetDetail = new TimeSheetDetail();
            timeSheetDetail.setTimeSheet(timeSheet);
            timeSheetDetail.setActType(dictionaryItemService.find(formRow.getActivityTypeId()));
            if (formRow.getActivityCategoryId() != null) {
                timeSheetDetail.setActCat(dictionaryItemService.find(formRow.getActivityCategoryId()));
            } else {
                timeSheetDetail.setActCat(null);
            }
            Integer projectId = formRow.getProjectId();
            Double duration = null;
            String durationStr = formRow.getDuration();
            if (projectId != null) {
                timeSheetDetail.setProject(projectService.find(projectId));
            }
            // Сохраняем часы только для тех полей, которые не disabled
            if (durationStr != null) {
                duration = Double.parseDouble(durationStr.replace(",","."));
            }
            timeSheetDetail.setCqId(formRow.getCqId());
            timeSheetDetail.setDuration(duration);
            timeSheetDetail.setDescription(formRow.getDescription());
            timeSheetDetail.setProblem(formRow.getProblem());
            timeSheetDetail.setProjectRole(projectRoleService.find(formRow.getProjectRoleId()));
            timeSheetDetails.add(timeSheetDetail);
        }
        timeSheet.setTimeSheetDetails(timeSheetDetails);
        timeSheetDAO.storeTimeSheet(timeSheet);
        logger.info("TimeSheet object for employee {} ({}) saved.", tsForm.getEmployeeId(), timeSheet.getCalDate());
    }

    public void storeTimeSheetLong(TimeSheetForm tsForm) {
        logger.debug("Selected employee id = {}", tsForm.getEmployeeId());
        logger.debug("Selected calDate = {}", tsForm.getCalDate());
        List<String> splittedDateRange = DateTimeUtil.splitDateRangeOnDays(tsForm.getBeginLongDate(), tsForm.getEndLongDate());
        for (String dateInStr : splittedDateRange) {
            TimeSheet timeSheet = new TimeSheet();
            timeSheet.setEmployee(employeeService.find(tsForm.getEmployeeId()));
            timeSheet.setCalDate(calendarService.find(dateInStr));
            Set<TimeSheetDetail> timeSheetDetails = new LinkedHashSet<TimeSheetDetail>();
            TimeSheetDetail timeSheetDetail = new TimeSheetDetail();
            if (tsForm.isLongVacation()) {
                timeSheetDetail.setActType(dictionaryItemService.find(DictionaryItemService.VACATION_ID));
                timeSheetDetail.setDescription("Отпуск");
            } else if (tsForm.isLongIllness()) {
                timeSheetDetail.setActType(dictionaryItemService.find(DictionaryItemService.ILLNESS_ID));
                timeSheetDetail.setDescription("Болезнь");
            }
            timeSheetDetail.setTimeSheet(timeSheet);
            timeSheetDetails.add(timeSheetDetail);
            timeSheet.setTimeSheetDetails(timeSheetDetails);
            timeSheetDAO.storeTimeSheet(timeSheet);
            logger.info("TimeSheet object for employee {} ({}) saved (long).", tsForm.getEmployeeId(), timeSheet.getCalDate());
        }
    }

    /**
     * Ищет в таблице timesheet запись соответсвующую date для сотрудника с
     * идентификатором employeeId и возвращает объект типа Timesheet.
     *
     * @param calDate     Дата в виде строки.
     * @param employeeId Идентификатор сотрудника в базе данных.
     * @return объект типа Timesheet, либо null, если объект не найден.
     */
    public TimeSheet findForDateAndEmployee(String calDate, Integer employeeId) {
        return timeSheetDAO
                .findForDateAndEmployee(calendarService.find(calDate), employeeId); // Котов. Убрал вызов employeeService.find, здесь достаточно Id, а работать будет на порядок быстрее
    }

    /**
     * Собирает из таблиц timesheet и calendar список дат и работ по сотруднику за месяц.
     *
     * @param year
     * @param month
     * @param employee
     * @return List DayTimeSheet объектов. Первое поле - дата, второе - рабочий/нерабочий день, третье - id работы, если есть.
     */
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Integer year, Integer month, Integer region, Employee employee) {
        return timeSheetDAO
                .findDatesAndReportsForEmployee(year, month, region, employee);
    }

    /**
     * Переопределение. (Используется для последующей расскраски календаря)
     * Собирает из таблиц timesheet и calendar список дат и работ по сотруднику за месяц.
     *
     * @param year
     * @param month
     * @param employeeId
     * @return List DayTimeSheet объектов. Первое поле - дата, второе - рабочий/нерабочий день, третье - id работы, если есть.
     */
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Integer year, Integer month, Integer employeeId) {
        Employee emp = employeeService.find(employeeId);
        return timeSheetDAO
                .findDatesAndReportsForEmployee(year, month, emp.getRegion().getId(), emp);
    }

    public TimeSheet find(Integer id) {
        return timeSheetDAO.find(id);
    }

    public void delete(TimeSheet timeSheet) {
        timeSheetDAO.delete(timeSheet);
    }

    /**
     * Формирует JSON планов предыдущего дня и на следующего дня
     * @param date
     * @param employeeId
     * @return jsonString
     */
    public String getPlansJson(String date, Integer employeeId) {
        StringBuilder json = new StringBuilder();

        TimeSheet lastTimeSheet = timeSheetDAO.findLastTimeSheetBefore(calendarService.find(date), employeeId);
        Calendar nextWorkDay = calendarService.getNextWorkDay(calendarService.find(date), employeeService.find(employeeId).getRegion());
        TimeSheet nextTimeSheet = timeSheetDAO.findNextTimeSheetAfter(nextWorkDay, employeeId);

        json.append("{");
        if (lastTimeSheet != null) {
            json.append("\"prev\":{");
            json.append("\"dateStr\":");
            json.append("\"" + DateTimeUtil.formatDate(lastTimeSheet.getCalDate().getCalDate()) + "\",");   //преобразование к  yyyy-MM-dd
            json.append("\"plan\":\"");
            String lastPlan = lastTimeSheet.getPlanEscaped();
            if (lastPlan != null)
                json.append("" + lastPlan.replace("\r\n","\\n"));
            json.append("\"}");
        }

        if (lastTimeSheet != null && nextTimeSheet != null)
            json.append(",");

        if (nextTimeSheet != null) {
            json.append("\"next\":{");
            json.append("\"dateStr\":");
            json.append("\"" + DateTimeUtil.formatDate(nextTimeSheet.getCalDate().getCalDate()) + "\",");   //преобразование к  yyyy-MM-dd
            json.append("\"plan\":\"");
            String nextPlan = getStringTimeSheetDetails(nextTimeSheet);
            if (nextPlan != null)
                json.append(nextPlan.replace("\r\n","\\n"));
            json.append("\"}");
        }
        json.append("}");
        return json.toString();

    }
    /**
     * Формирует строку на подобие поля "Что было сделано" из отчета
     * @param timeSheet
     * @return String
     */
    private String getStringTimeSheetDetails(TimeSheet timeSheet){
        Set<TimeSheetDetail> timeSheetDetails = timeSheet.getTimeSheetDetails();
        StringBuilder sb;
        StringBuilder rezult = new StringBuilder();
        int i = 1;
        for(TimeSheetDetail detail: timeSheetDetails){
            sb = new StringBuilder();
            sb.append(i + ". ");
            sb.append(detail.getActType().getValue() + " - ");
            if (detail.getProject() != null)
                sb.append(detail.getProject().getName() + " : ");
            sb.append(detail.getDescriptionEscaped());
            rezult.append(sb.toString() + "\\n");
            i++;
        }
        return rezult.toString();
    }
}