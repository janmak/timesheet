package com.aplana.timesheet.form.validator;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.dao.entity.TimeSheet;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TimeSheetFormValidator implements Validator {
    private static final Logger logger = LoggerFactory.getLogger(TimeSheetFormValidator.class);

    @Autowired
    private TimeSheetService timeSheetService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRoleService projectRoleService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private DivisionService divisionService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private ProjectTaskService projectTaskService;

    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(TimeSheetForm.class);
    }

    public boolean isProjectActType(Integer actTypeId) {
        return actTypeId.equals(new Integer(12)) || actTypeId.equals(new Integer(13)) || actTypeId.equals(new Integer(14));
    }

    public void validate(Object target, Errors errors) {
        TimeSheetForm tsForm = (TimeSheetForm) target;
        String selectedDate = tsForm.getCalDate();
        Integer selectedEmployeeId = tsForm.getEmployeeId();
        String plan = tsForm.getPlan();
        boolean longVacation = tsForm.isLongVacation();
        boolean longIllness = tsForm.isLongIllness();
        logger.debug("longVacation = {} and longillness = {}.", longVacation, longIllness);
        boolean planNecessary = true;
        Employee employee = employeeService.find(selectedEmployeeId);
        ProjectRole emplJob = (employee != null) ? employee.getJob() : projectRoleService.getUndefinedRole();
        if (emplJob == null) {
            logger.warn("emplJob is null");
        } else if (emplJob.getCode() == null) {
            logger.warn("emplJob.getCode() is null");
        }
        double totalDuration = 0;

        // Подразделение не выбрано.
        Integer division = tsForm.getDivisionId();
        if (division == null || division == 0) {
            errors.rejectValue("divisionId",
                    "error.tsform.division.required",
                    "Подразделение не выбрано.");
        }
        // Неверное подразделение.
        else if (!isDivisionValid(division)) {
            errors.rejectValue("divisionId",
                    "error.tsform.division.invalid",
                    "Выбрано неверное подразделение.");
        }
        // Сотрудник не выбран.
        if (selectedEmployeeId == null || selectedEmployeeId == 0) {
            errors.rejectValue("employeeId",
                    "error.tsform.employee.required",
                    "Сотрудник не выбран.");
        }
        // Неверный сотрудник
        else if (!isEmployeeValid(selectedEmployeeId)) {
            errors.rejectValue("employeeId",
                    "error.tsform.employee.invalid",
                    "Неверные данные сотрудника.");
        }
        // Дата не выбрана.
        if (selectedDate != null) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                    "calDate",
                    "error.tsform.caldate.required",
                    "Необходимо выбрать дату.");

            // Эти проверки не проводится, если дата не выбрана.
            if (!"".equals(selectedDate)) {
                // Выбрана недопустимая дата (если сотрудник выбрал дату из
                // диапазона дат, которые еще не внесены в таблицу calendar.
                if (!isCaldateValid(tsForm.getCalDate())) {
                    errors.rejectValue("calDate",
                            "error.tsform.caldate.invalid",
                            "Выбрана недопустимая дата.");
                }
                // Сотрудник уже отправлял отчёт за выбранную дату.
                else if (!isCaldateUniqueForCurrentEmployee(selectedDate, selectedEmployeeId)) {
                    Object[] errorMessageArgs = {DateTimeUtil.formatDateString(selectedDate)};
                    errors.rejectValue("calDate",
                            "error.tsform.caldate.notuniq",
                            errorMessageArgs,
                            "Вы уже списывали занятость за " + DateTimeUtil.formatDateString(selectedDate));
                }
            }
        }
        //Если выбран долгий отпуск или долгая болезнь
        if (longVacation || longIllness) {
            planNecessary = false;
            String beginDate = tsForm.getBeginLongDate();
            String endDate = tsForm.getEndLongDate();
            //Не указана дата начала
            ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                    "beginLongDate",
                    "error.tsform.beginlongdate.required",
                    "Необходимо выбрать дату начала отпуска\\болезни.");
            //Не указана дата окончания
            ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                    "endLongDate",
                    "error.tsform.endlongdate.required",
                    "Необходимо выбрать дату окончания отпуска\\болезни.");
            //Дата окончания не может быть раньше даты начала
            Timestamp beginTs = DateTimeUtil.stringToTimestamp(beginDate, DateTimeUtil.DATE_PATTERN);
            Timestamp endTs = DateTimeUtil.stringToTimestamp(endDate, DateTimeUtil.DATE_PATTERN);
            if (beginTs.getTime() > endTs.getTime()) {
                errors.rejectValue("beginLongDate",
                        "error.tsform.datesegment.notvalid",
                        "Дата окончания не может быть раньше даты начала.");
            }
            //Недопустимя дата начала
            if (!"".equals(beginDate) && !isCaldateValid(beginDate)) {
                errors.rejectValue("beginLongDate",
                        "error.tsform.beginlongdate.invalid",
                        "Выбрана недопустимая дата начала отпуска\\болезни.");
            }
            //Недопустимая дата окончания
            if (!"".equals(endDate) && !isCaldateValid(endDate)) {
                errors.rejectValue("endLongDate",
                        "error.tsform.endlongdate.invalid",
                        "Выбрана недопустимая дата окончания отпуска\\болезни.");
            }
            //Сотрудник уже отправлял отчёт за выбранную дату.
            List<String> splittedDateRange = DateTimeUtil.splitDateRangeOnDays(beginDate, endDate);
            for (String dateInStr : splittedDateRange) {
                    if (!isCaldateUniqueForCurrentEmployee(dateInStr, selectedEmployeeId)) {
                        if(!("".equals(beginDate) || "".equals(endDate)))
                        {
                            Object[] errorMessageArgs = {DateTimeUtil.formatDateString(dateInStr)};
                            errors.rejectValue("calDate",
                                    "error.tsform.caldate.notuniq",
                                    errorMessageArgs,
                                    "Вы уже списывали занятость за " + DateTimeUtil.formatDateString(dateInStr));
                        }
                }
            }
        }
        // Для табличной части (по строчно).
        List<TimeSheetTableRowForm> tsTablePart = tsForm.getTimeSheetTablePart();
        List<TimeSheetTableRowForm> listToRemove = new ArrayList<TimeSheetTableRowForm>();
        if (tsTablePart != null) {
            logger.debug("TimeSheetForm table has {} lines.", tsTablePart.size());
            int notNullRowNumber = 0;
            for (TimeSheetTableRowForm formRow : tsTablePart) {
                Integer actTypeId = formRow.getActivityTypeId();
                Integer projectId = formRow.getProjectId();
                Integer projectRoleId = formRow.getProjectRoleId();
                Integer actCatId = formRow.getActivityCategoryId();
                String cqId = formRow.getCqId();
                String durationStr = formRow.getDuration();
                String description = formRow.getDescription();
                Object[] errorMessageArgs = {"в строке №" + (notNullRowNumber + 1)}; // Номер строки, где произошла ошибка валидации.
                // Если хоть в одной строке таблицы есть тип активности
                // отгул(с отработкой или за переработки), отпуск или болезнь
                // то планы на следующий рабочий день можно не указывать.
                if ((actTypeId != null && (actTypeId == 15 || actTypeId == 16 || actTypeId == 17 || actTypeId == 43))) {
                    planNecessary = false;
                }
                // По каким-то неведомым причинам при нажатии на кнопку веб интерфейса
                // "Удалить выбранные строки" (если выбраны промежуточные строки) они удаляются с формы, но
                // в объект формы вместо них попадают null`ы. Мы эти строки удаляем из объекта формы. Если
                // удалять последние строки (с конца табличной части формы), то все работает корректно.
                // Также, если тип активности не выбран значит вся строка пустая, валидацию ее не проводим и удаляем.
                if (actTypeId == null || actTypeId == 0) {
                    listToRemove.add(formRow);
                    continue;
                }
                // Неверный тип активности
                if (!isActTypeValid(actTypeId)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityTypeId",
                            "error.tsform.activity.type.invalid", errorMessageArgs,
                            "Неверный тип активности в строке " + (notNullRowNumber + 1) + ".");
                }
                // Не указано название проекта
                if (actTypeId == 12 && (projectId == 0 || projectId == null)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                            "error.tsform.project.required", errorMessageArgs,
                            "Необходимо указать название проекта в строке " + (notNullRowNumber + 1) + ".");
                }
                // Не указано название пресейла
                else if (actTypeId == 13 && (projectId == 0 || projectId == null)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                            "error.tsform.presale.required", errorMessageArgs,
                            "Необходимо указать название пресейла в строке " + (notNullRowNumber + 1) + ".");
                }
                // Неверный проект\пресейл
                else if (!isProjectValid(projectId)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                            "error.tsform.project.presale.invalid", errorMessageArgs,
                            "Неверный проект\\пресейл в строке " + (notNullRowNumber + 1) + ".");
                }
                // Не указана проектная роль
                if ((isProjectActType(actTypeId)) && // APLANATS-276 Роль нужно указывать только для проектных видов деятельности
                        ((projectRoleId == null) || (projectRoleId != null && projectRoleId == 0))) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectRoleId",
                            "error.tsform.projectrole.required", errorMessageArgs,
                            "Необходимо указать проектную роль в строке " + (notNullRowNumber + 1) + ".");
                }
                // Неверная проектная роль
                else if (!isProjectRoleValid(projectRoleId)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectRoleId",
                            "error.tsform.projectrole.invalid", errorMessageArgs,
                            "Неверная проектная роль в строке " + (notNullRowNumber + 1) + ".");
                }
                // Не указана категория активности
                if (actCatId != null && (!emplJob.getCode().equals("DR") && actCatId == 0)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                            "error.tsform.activity.category.required", errorMessageArgs,
                            "Необходимо указать категорию активности в строке " + (notNullRowNumber + 1) + ".");
                }
                // Неверная категория активности
                else if (!isActCatValid(actCatId, emplJob)) {
                    errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                            "error.tsform.activity.category.invalid", errorMessageArgs,
                            "Неверная категория активности в строке " + (notNullRowNumber + 1) + ".");
                }

                logger.debug("cqId = {}", cqId);
                if (projectId != null) {
                    Project project = projectService.find(projectId);
                    // Необходимо указать проектную задачу
                    if (project != null && project.isCqRequired()) {
                        if (cqId == null || cqId.equals("0")) {
                            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].cqId",
                                    "error.tsform.cqid.required", errorMessageArgs,
                                    "Необходимо выбрать проектную задачу в строке " + (notNullRowNumber + 1) + ".");
                        }
                        // Неверная проектная задача
                        else if (!isProjectTaskValid(projectId, cqId)) {
                            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].cqId",
                                    "error.tsform.cqid.invalid", errorMessageArgs,
                                    "Неверная проектная задача в строке " + (notNullRowNumber + 1) + ".");
                        }
                    }
                }
                double duration;
                // Необходимо указать часы
                if (durationStr != null) {
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors,
                            "timeSheetTablePart[" + notNullRowNumber + "].duration",
                            "error.tsform.duration.required", errorMessageArgs,
                            "Необходимо указать часы в строке " + (notNullRowNumber + 1) + ".");
                    // Часы должны быть указаны в правильном формате (1, 1.2, 5.5 и т.п.)
					// and may be 1,2; 2,3
                    if (!durationStr.equals("")) {
                        Pattern p1 = Pattern.compile("([0-9]*)(\\.|,)[0-9]");
                        Pattern p2 = Pattern.compile("([0-9]*)");
                        Matcher m1 = p1.matcher(durationStr);
                        Matcher m2 = p2.matcher(durationStr);
                        if (!m1.matches() && !m2.matches()) {
                            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].duration",
                                    "error.tsform.duration.format", errorMessageArgs,
                                    "Количество часов указано не верно в строке " + (notNullRowNumber + 1) + ". Примеры правильных значений (5, 3.5, 2.0 и т.п.).");
                        } else {
                            duration = Double.parseDouble(durationStr.replace(",","."));
                            // Количество часов должно быть больше нуля
                            if (duration <= 0) {
                                errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].duration",
                                        "error.tsform.duration.length", errorMessageArgs,
                                        "Количество часов должно быть больше нуля в строке " + (notNullRowNumber + 1) + ".");
                            }
                            // Считаем общее количество часов
                            totalDuration += duration;
                        }
                    }
                }
                // Необходимо указать комментарии
                if (description != null) {
                    logger.debug("Employee Job: {}", emplJob.getCode());
                    if (description.equals("") && !emplJob.getCode().equals("MN") && !emplJob.getCode().equals("DR")) {
                        errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].description",
                                "error.tsform.description.required", errorMessageArgs,
                                "Необходимо указать комментарии в строке " + (notNullRowNumber + 1) + ".");
                    }
                }
                notNullRowNumber++;
            }
            tsTablePart.removeAll(listToRemove);
            if ((!longVacation && !longIllness) && tsTablePart.size() == 0) {
                errors.reject("error.tsform.tablepart.required",
                        "В отчёте должны быть записи.");
            }
        } else if (tsTablePart == null && (!longVacation && !longIllness)) {
            errors.reject("error.tsform.tablepart.required",
                    "В отчёте должны быть записи.");
        }
        // Сумма часов превышает 24.
        if (totalDuration > 24) {
            errors.rejectValue("totalDuration",
                    "error.tsform.total.duration.max",
                    "Сумма часов не должна превышать 24.");
        }
        logger.debug("Total duration is {}", totalDuration);
        // Планы на следующий рабочий день.
        if (planNecessary && ((plan == null) || plan.equals("")) && !emplJob.getCode().equals("MN") && !emplJob.getCode().equals("DR")) {
            errors.rejectValue("plan",
                    "error.tsform.plan.required",
                    "Необходимо указать планы на следующий рабочий день.");
        }
    }

    /*
    * Возвращает false, если сотрудник уже отправлял отчет за выбранную дату.
    */

    private boolean isCaldateUniqueForCurrentEmployee(String calDate, Integer employeeId) {
        return timeSheetService.findForDateAndEmployee(calDate, employeeId) == null;
    }

    /*
      * Возвращает true, если введённая дата присутствует в таблице calendar и false, если нет.
      */
    private boolean isCaldateValid(String date) {
        return calendarService.find( date ) != null;
    }

    private boolean isDivisionValid(Integer division) {
        return divisionService.find( division ) != null;
    }

    private boolean isEmployeeValid(Integer employee) {
        return employeeService.find( employee ) != null;
    }

    private boolean isActTypeValid(Integer actType) {
        return dictionaryItemService.find( actType ) != null;
    }

    private boolean isActCatValid(Integer actCat, ProjectRole emplJob) {
        if (actCat == null ||
                //У проектной роли "Руководитель центра" нет доступных категорий активности.
                ( emplJob.getCode().equals("DR") && actCat == 0 )
        ) {
            return true;
        }
        return dictionaryItemService.find(actCat) != null;
    }

    private boolean isProjectValid(Integer project) {
        return project == null || projectService.findActive( project ) != null;
    }

    private boolean isProjectRoleValid(Integer projectRole) {
        return projectRole == null || projectRoleService.findActive( projectRole ) != null;
    }

    private boolean isProjectTaskValid(Integer project, String task) {
        if (project == null && task == null) {
            return true;
        }
        return projectTaskService.find( project, task ) != null;
    }
}