package com.aplana.timesheet.service;

import argo.jdom.JsonArrayNodeBuilder;
import argo.jdom.JsonObjectNodeBuilder;
import com.aplana.timesheet.dao.AvailableActivityCategoryDAO;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.TimeSheetDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.form.entity.DayTimeSheet;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static argo.jdom.JsonNodeBuilders.*;
import static argo.jdom.JsonNodeFactories.string;
import static com.aplana.timesheet.enums.TypesOfActivityEnum.ILLNESS;
import static com.aplana.timesheet.enums.TypesOfActivityEnum.getById;

@Service
public class TimeSheetService {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetService.class);

    @Autowired
    private TimeSheetDAO timeSheetDAO;

    @Autowired
    private EmployeeDAO employeeDAO;

    @Autowired
    AvailableActivityCategoryDAO availableActivityCategoryDAO;

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
    private ProjectTaskService projectTaskService;
    
    @Autowired
    public SecurityService securityService;

    public TimeSheet storeTimeSheet(TimeSheetForm tsForm) {
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
            timeSheetDetail.setWorkplace(dictionaryItemService.find(formRow.getWorkplaceId()));
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
                timeSheetDetail.setProjectTask(projectTaskService.find(projectId, formRow.getCqId()));
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
        return timeSheet;
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
     * @param employee
     * @param year
     * @param month
     * @return List DayTimeSheet объектов. Первое поле - дата, второе - рабочий/нерабочий день, третье - id работы, если есть.
     */
    public List<DayTimeSheet> findDatesAndReportsForEmployee(Employee employee, Integer year, Integer month) {
        return timeSheetDAO
                .findDatesAndReportsForEmployee(year, month, employee.getRegion().getId(), employee);
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

        if (emp == null) {
            emp = new Employee();
            final Region region = new Region();

            region.setId(-1);

            emp.setId(employeeId);
            emp.setRegion(region);
        }

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
        final JsonObjectNodeBuilder builder = anObjectBuilder();

        final TimeSheet lastTimeSheet = timeSheetDAO.findLastTimeSheetBefore(calendarService.find(date), employeeId);
        final Calendar nextWorkDay = calendarService.getNextWorkDay(
                calendarService.find(date),
                employeeService.find(employeeId).getRegion()
        );
        final TimeSheet nextTimeSheet = timeSheetDAO.findNextTimeSheetAfter(nextWorkDay, employeeId);

        if (lastTimeSheet != null) {
            builder.withField("prev", getPlanBuilder(lastTimeSheet));
        }

        if (nextTimeSheet != null &&
            !( ILLNESS == getById(
                    Lists.newArrayList(
                            nextTimeSheet.getTimeSheetDetails()).get(0).getActType().getId()))
        ){ // <APLANATS-458>
            builder.withField("next", getPlanBuilder(nextTimeSheet));
        }

        return JsonUtil.format(builder);
    }

    private JsonObjectNodeBuilder getPlanBuilder(TimeSheet timeSheet) {
        return anObjectBuilder().
                withField("dateStr", aStringBuilder(DateTimeUtil.formatDate(timeSheet.getCalDate().getCalDate()))).
                withField("plan", aStringBuilder(getPlan(timeSheet)));
    }

    private String getPlan(TimeSheet timeSheet) {
        String lastPlan = timeSheet.getPlanEscaped();
        if (lastPlan != null) {
            lastPlan = lastPlan.replace("\r\n", "\\n");
        } else {
            lastPlan = StringUtils.EMPTY;
        }
        return lastPlan;
    }

    /**
     * Формирует строку на подобие поля "Что было сделано" из отчета
     * @param timeSheet
     * @return String
     */
    public String getStringTimeSheetDetails(TimeSheet timeSheet){
        Set<TimeSheetDetail> timeSheetDetails = timeSheet.getTimeSheetDetails();
        StringBuilder sb;
        StringBuilder rezult = new StringBuilder();
        int i = 1;
        for(TimeSheetDetail detail: timeSheetDetails){
            sb = new StringBuilder();
            sb.append( i ).append( ". " );
            sb.append( detail.getActType().getValue() ).append( " - " );
            if (detail.getProject() != null)
                sb.append( detail.getProject().getName() ).append( " : " );
            sb.append(detail.getDescriptionEscaped());
            rezult.append( sb.toString() ).append( "\\n" );
            i++;
        }
        return rezult.toString();
    }

    public Date getLastWorkdayWithoutTimesheet(Integer employeeId){
        Employee employee = employeeDAO.find(employeeId);
        Calendar calendar = timeSheetDAO.getDateNextAfterLastDayWithTS(employee);
        Date result = new Date();
        if (calendar == null){
            return result;
        } else{
            result.setTime(calendar.getCalDate().getTime());
            return result;
        }
    }

    public Date getEmployeeFirstWorkDay(Integer employeeId){
         return employeeDAO.getEmployeeFirstWorkDay(employeeId);
    }

    public List<TimeSheet> getTimeSheetsForEmployee(Employee employee, Integer year, Integer month) {
        return timeSheetDAO.getTimeSheetsForEmployee(employee, year, month);
    }

    public String getListOfActDescriptoin(){
        List<AvailableActivityCategory> availableActivityCategories = availableActivityCategoryDAO.getAllAvailableActivityCategories();
        final JsonArrayNodeBuilder result = anArrayBuilder();
        for (AvailableActivityCategory activityCategory : availableActivityCategories){
            result.withElement(
                    anObjectBuilder().
                            withField("actCat", JsonUtil.aNumberBuilder(activityCategory.getActCat().getId())).
                            withField("actType", JsonUtil.aNumberBuilder(activityCategory.getActType().getId())).
                            withField("projectRole", JsonUtil.aNumberBuilder(activityCategory.getProjectRole().getId())).
                            withField("description",
                                    activityCategory.getDescription() != null ?
                                    aStringBuilder(activityCategory.getDescription()) :
                                    string(StringUtils.EMPTY)
                            )
            );
        }
        result.withElement(
                anObjectBuilder().
                            withField("actCat", JsonUtil.aNumberBuilder(0)).
                            withField("actType", JsonUtil.aNumberBuilder(0)).
                            withField("projectRole", JsonUtil.aNumberBuilder(0)).
                            withField("description", string(StringUtils.EMPTY))
        );

        return JsonUtil.format(result);
    }

    public String getSelectedCalDateJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        String date = "";
        sb.append("'");
        if (DateTimeUtil.isDateValid(tsForm.getCalDate())){
            date = DateTimeUtil.formatDateString(tsForm.getCalDate());
            sb.append(date);
        }
        sb.append("'");
        logger.debug("SelectedCalDateJson = {}", date);
        return sb.toString();
    }

    public String getSelectedActCategoriesJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{row:'").append(i).append("', ");
                sb.append("actCat:'").append(tablePart.get(i).getActivityCategoryId()).append("'}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', actCat:''}]");
        }
        return sb.toString();
    }

    public String getSelectedWorkplaceJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{row:'").append(i).append("', ");
                sb.append("workplace:'").append(tablePart.get(i).getWorkplaceId()).append("'}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', workplace:''}]");
        }
        return sb.toString();
    }

    public String getSelectedProjectTasksJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                if (!"".equals(tablePart.get(i).getCqId())) {
                    sb.append("{row:'").append(i).append("', ");
                    sb.append("task:'").append(tablePart.get(i).getCqId()).append("'}");
                    if (i < (tablePart.size() - 1)) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', task:''}]");
        }
        return sb.toString();
    }

    public String getSelectedProjectRolesJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                if (!"".equals(tablePart.get(i).getCqId())) {
                    sb.append("{row:'").append(i).append("', ");
                    sb.append("role:'").append(tablePart.get(i).getProjectRoleId()).append("'}");
                    if (i < (tablePart.size() - 1)) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', role:''}]");
        }
        return sb.toString();
    }

    public String getSelectedProjectsJson(TimeSheetForm tsForm) {
        StringBuilder sb = new StringBuilder();
        List<TimeSheetTableRowForm> tablePart = tsForm.getTimeSheetTablePart();
        if (tablePart != null) {
            sb.append("[");
            for (int i = 0; i < tablePart.size(); i++) {
                sb.append("{row:'").append(i).append("', ");
                sb.append("project:'").append(tablePart.get(i).getProjectId()).append("'}");
                if (i < (tablePart.size() - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        } else {
            sb.append("[{row:'0', project:''}]");
        }
        return sb.toString();
    }
}