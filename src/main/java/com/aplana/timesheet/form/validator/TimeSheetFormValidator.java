package com.aplana.timesheet.form.validator;

import static com.aplana.timesheet.util.TimeSheetConstans.*;

import com.aplana.timesheet.dao.entity.Employee;
import com.aplana.timesheet.dao.entity.Project;
import com.aplana.timesheet.dao.entity.ProjectRole;
import com.aplana.timesheet.form.TimeSheetForm;
import com.aplana.timesheet.form.TimeSheetTableRowForm;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
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

    public void validate(Object target, Errors errors) {
        // получаем данные с формы
        TimeSheetForm tsForm = (TimeSheetForm) target;
        String selectedDate = tsForm.getCalDate();
        Integer selectedEmployeeId = tsForm.getEmployeeId();
        String plan = tsForm.getPlan();
        boolean longVacation = tsForm.isLongVacation();
        boolean longIllness = tsForm.isLongIllness();
        String beginDate = tsForm.getBeginLongDate();
        String endDate = tsForm.getEndLongDate();
        Integer division = tsForm.getDivisionId();
        List<TimeSheetTableRowForm> tsTablePart = tsForm.getTimeSheetTablePart();

        logger.debug("longVacation = {} and longIllness = {}.", longVacation, longIllness);
        Boolean planNecessary = true;
        ProjectRole employeeJob = getEmployeeJob(selectedEmployeeId);
        double totalDuration = 0;

        // Проверка подразделения
        divisionValidation(division, errors);
        // Проверка сотрудника
        employeeValidation(selectedEmployeeId, errors);
        // Проверка даты
        dateValidation(selectedDate, selectedEmployeeId, errors);
        // Проверка, если выбран долгий отпуск или долгая болезнь
        longVacationOrIllnessValidation(longVacation, longIllness,
                beginDate, endDate,
                selectedEmployeeId, planNecessary,
                errors);

        if ((longVacation || longIllness)) {
            planNecessary = false;
        }

        // Для табличной части (по строчно).
        List<TimeSheetTableRowForm> listToRemove = new ArrayList<TimeSheetTableRowForm>();
        if (tsTablePart != null && tsTablePart.size() != 0) {

            for (TimeSheetTableRowForm formRow : tsTablePart) {
                int notNullRowNumber = 0;
                if (!currentRowValidation(formRow,            // вернет false если требуется перейти на след. строку
                        planNecessary,
                        listToRemove,
                        employeeJob,
                        totalDuration,
                        notNullRowNumber,
                        errors)) {
                    continue;
                }
            }
            tsTablePart.removeAll(listToRemove);
        } else if (!(longVacation || longIllness)) {
            errors.reject("error.tsform.tablepart.required",
                    "В отчёте должны быть записи.");
        }

        if (planNecessary) {
            // Проверка на длину рабочего дня
            totalDurationValidation(totalDuration, errors);
            // Проверка планов на следующий день
            planValidation(planNecessary, employeeJob, plan, errors);
        }
    }

    public ProjectRole getEmployeeJob(Integer employeeId) {
        Employee employee = employeeService.find(employeeId);
        ProjectRole emplJob = (employee != null) ? employee.getJob() : projectRoleService.getUndefinedRole();
        if (emplJob == null) {
            logger.warn("emplJob is null");
        } else if (emplJob.getCode() == null) {
            logger.warn("emplJob.getCode() is null");
        }
        return emplJob;
    }

    public void dateValidation(String date, Integer employeeId, Errors errors) {
        // Дата не выбрана.
        if (date == null) {
            return;
        }

        if (StringUtils.isBlank(date)) {
            errors.rejectValue("calDate",
                    "error.tsform.caldate.required",
                    "Необходимо выбрать дату.");
            return;
        }

        // Выбрана недопустимая дата (если сотрудник выбрал дату из
        // диапазона дат, которые еще не внесены в таблицу calendar.
        if (calendarService.find(date) == null) {
            errors.rejectValue("calDate",
                    "error.tsform.caldate.invalid",
                    "Выбрана недопустимая дата.");
            return;
        }

        // Сотрудник уже отправлял отчёт за выбранную дату.
        else if (timeSheetService.findForDateAndEmployee(date, employeeId) != null) {
            Object[] errorMessageArgs = {DateTimeUtil.formatDateString(date)};
            errors.rejectValue("calDate",
                    "error.tsform.caldate.notuniq",
                    errorMessageArgs,
                    "Вы уже списывали занятость за " + DateTimeUtil.formatDateString(date));
        }
    }

    public void divisionValidation(Integer division, Errors errors) {
        // Подразделение не выбрано.
        if (division == null || division == 0) {
            errors.rejectValue("divisionId",
                    "error.tsform.division.required",
                    "Подразделение не выбрано.");
            return;
        }
        // Неверное подразделение - нет в БД
        else if (divisionService.find(division) == null) {
            errors.rejectValue("divisionId",
                    "error.tsform.division.invalid",
                    "Выбрано неверное подразделение.");
        }
    }

    public void employeeValidation(Integer employeeId, Errors errors) {
        // Сотрудник не выбран.
        if (employeeId == null || employeeId == 0) {
            errors.rejectValue("employeeId",
                    "error.tsform.employee.required",
                    "Сотрудник не выбран.");
            return;
        }
        // Неверный сотрудник - нет в БД
        else if (employeeService.find(employeeId) == null) {
            errors.rejectValue("employeeId",
                    "error.tsform.employee.invalid",
                    "Неверные данные сотрудника.");
        }
    }

    public void longVacationOrIllnessValidation(boolean longVacation, boolean longIllness,
                                                String beginDate, String endDate,
                                                Integer employeeId, Boolean planNecessary, Errors errors) {
        // если это не долгая болезнь или долгий отпуск - выход
        if (!(longVacation || longIllness)) {
            return;
        } else { // для болезни и отпуска продолжаем, план на завтра не требуется
            planNecessary = false;
        }

        // Не указана дата начала
        if (beginDate == null || StringUtils.isBlank(beginDate)) {
            errors.rejectValue("beginLongDate",
                    "error.tsform.beginlongdate.required",
                    "Необходимо выбрать дату начала отпуска\\болезни.");
            return;
        }
        // Не указана дата окончания
        if (endDate == null || StringUtils.isBlank(endDate)) {
            errors.rejectValue("endLongDate",
                    "error.tsform.endlongdate.required",
                    "Необходимо выбрать дату окончания отпуска\\болезни.");
            return;
        }
        // Дата окончания не может быть раньше даты начала
        Timestamp beginTs = DateTimeUtil.stringToTimestamp(beginDate, DateTimeUtil.DATE_PATTERN);
        Timestamp endTs = DateTimeUtil.stringToTimestamp(endDate, DateTimeUtil.DATE_PATTERN);
        if (beginTs.getTime() > endTs.getTime()) {
            errors.rejectValue("beginLongDate",
                    "error.tsform.datesegment.notvalid",
                    "Дата окончания не может быть раньше даты начала.");
            return;
        }
        // Недопустимя дата начала
        if (calendarService.find(beginDate) == null) {
            errors.rejectValue("beginLongDate",
                    "error.tsform.beginlongdate.invalid",
                    "Выбрана недопустимая дата начала отпуска\\болезни.");
            return;
        }
        // Недопустимая дата окончания
        if (calendarService.find(endDate) == null) {
            errors.rejectValue("endLongDate",
                    "error.tsform.endlongdate.invalid",
                    "Выбрана недопустимая дата окончания отпуска\\болезни.");
            return;
        }
        // Сотрудник уже отправлял отчёт за выбранную дату (проход по всему диапазону дат).
        List<String> splittedDateRange = DateTimeUtil.splitDateRangeOnDays(beginDate, endDate);
        for (String dateInStr : splittedDateRange) {
            if (timeSheetService.findForDateAndEmployee(dateInStr, employeeId) != null) {// есть отчет
                Object[] errorMessageArgs = {DateTimeUtil.formatDateString(dateInStr)};
                errors.rejectValue("calDate",
                        "error.tsform.caldate.notuniq",
                        errorMessageArgs,
                        "Вы уже списывали занятость за " + DateTimeUtil.formatDateString(dateInStr));
                break;
            }
        }
    }

    public void totalDurationValidation(Double totalDuration, Errors errors) {
        // Сумма часов превышает 24.
        if (totalDuration > 24) {
            errors.rejectValue("totalDuration",
                    "error.tsform.total.duration.max",
                    "Сумма часов не должна превышать 24.");
        }
    }

    public void planValidation(Boolean planNecessary, ProjectRole employeeJob, String plan, Errors errors) {
        if (!planNecessary || // если планов на след. день не требуется
                employeeJob.getCode().equals("MN") || // это менеджер
                employeeJob.getCode().equals("DR"))   // или руководитель
        {
            return; // то план можно не указывать
        }

        if (plan == null || StringUtils.isBlank(plan)) {
            errors.rejectValue("plan",
                    "error.tsform.plan.required",
                    "Необходимо указать планы на следующий рабочий день.");
            return;
        }
        // <APLANATS-441> не менее 2х слов
        String regexp = "([^-\\p{LD}]+)?([-\\p{LD}]++([^-\\p{LD}]+)?+){2,}";
        if (!plan.matches(regexp)) {
            errors.rejectValue("plan",
                    "error.tsform.plan.invalid",
                    "Планы на следующий день не могут быть менее 2х слов.");
        }
    }

    private boolean currentRowValidation(TimeSheetTableRowForm formRow,
                                         Boolean planNecessary,
                                         List<TimeSheetTableRowForm> listToRemove,
                                         ProjectRole employeeJob,
                                         Double totalDuration,
                                         Integer notNullRowNumber,
                                         Errors errors
    ) {
        // получаем значения из строки таблицы
        Integer actTypeId = formRow.getActivityTypeId();
        Integer projectId = formRow.getProjectId();
        Integer projectRoleId = formRow.getProjectRoleId();
        Integer actCatId = formRow.getActivityCategoryId();
        String cqId = formRow.getCqId();
        String durationStr = formRow.getDuration();
        String description = formRow.getDescription();

        // Номер строки, где произошла ошибка валидации
        Object[] errorMessageArgs = {"в строке №" + (notNullRowNumber + 1)};

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
            return false;
        }

        // Проверка типа активности
        actTypeValidation(actTypeId, notNullRowNumber, errorMessageArgs, errors);

        // Проверка названия проекта
        projectValidation(actTypeId, projectId, notNullRowNumber, errorMessageArgs, errors);

        // Проверка проектной роли
        projectRoleValidation(actTypeId, projectRoleId, notNullRowNumber, errorMessageArgs, errors);

        // Проверка категории активности
        actCatValidation(actCatId, employeeJob, notNullRowNumber, errorMessageArgs, errors);

        // Проверка проектной задачи
        projectTaskValidation(projectId, cqId, notNullRowNumber, errorMessageArgs, errors);

        // Проверка отработанных часов
        durationValidation(durationStr, totalDuration, notNullRowNumber, errorMessageArgs, errors);

        // Проверка комментариев
        discriptionValidation(description, employeeJob, notNullRowNumber, errorMessageArgs, errors);

        notNullRowNumber++;
        return true;
    }

    public void actTypeValidation(Integer actTypeId,
                                  Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        // Неверный тип активности
        if (dictionaryItemService.find(actTypeId) == null) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityTypeId",
                    "error.tsform.activity.type.invalid", errorMessageArgs,
                    "Неверный тип активности в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    public void projectValidation(Integer actTypeId, Integer projectId,
                                  Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        // Не указано название проекта
        if (actTypeId == DETAIL_TYPE_PROJECT && (projectId == null || projectId == 0)) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].projectId",
                    "error.tsform.project.required", errorMessageArgs,
                    "Необходимо указать название проекта в строке " + (notNullRowNumber + 1) + ".");
        }
        // Не указано название пресейла
        else if (actTypeId == DETAIL_TYPE_PRESALE && (projectId == null || projectId == 0)) {
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
    }

    public void projectRoleValidation(Integer actTypeId, Integer projectRoleId,
                                      Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        // APLANATS-276 Роль нужно указывать только для проектных видов
        if (!isProjectActType(actTypeId)) {
            return;
        }

        // Не указана проектная роль
        if (projectRoleId == null || projectRoleId == 0) {
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
    }

    public void actCatValidation(Integer actCatId, ProjectRole employeeJob,
                                 Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        //У проектной роли "Руководитель центра" нет доступных категорий активности.
        if (employeeJob.getCode().equals("DR")) {
            return;
        }

        // Не указана категория активности
        if (actCatId == null || actCatId == 0) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                    "error.tsform.activity.category.required", errorMessageArgs,
                    "Необходимо указать категорию активности в строке " + (notNullRowNumber + 1) + ".");
        }
        // Неверная категория активности
        else if (!isActCatValid(actCatId)) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].activityCategoryId",
                    "error.tsform.activity.category.invalid", errorMessageArgs,
                    "Неверная категория активности в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    public void projectTaskValidation(Integer projectId, String cqId,
                                      Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        if (projectId == null || projectId == 0) {
            return;
        }

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

    public void discriptionValidation(String description, ProjectRole employeeJob,
                                      Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        // Эти категории пользователей могут не указывать комментарии
        if (employeeJob.getCode().equals("MN") && !employeeJob.getCode().equals("DR")) {
            return;
        }
        // Необходимо указать комментарии
        if (description == null || StringUtils.isBlank(description)) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].description",
                    "error.tsform.description.required", errorMessageArgs,
                    "Необходимо указать комментарии в строке " + (notNullRowNumber + 1) + ".");
        }
    }

    public void durationValidation(String durationStr, Double totalDuration,
                                   Integer notNullRowNumber, Object[] errorMessageArgs, Errors errors) {
        double duration = 0;
        // Необходимо указать часы
        if (durationStr == null || StringUtils.isBlank(durationStr)) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].duration",
                    "error.tsform.duration.required", errorMessageArgs,
                    "Необходимо указать часы в строке " + (notNullRowNumber + 1) + ".");
            return;
        }

        // Часы должны быть указаны в правильном формате (1, 1.2, 5.5 и т.п.)
        // and may be 1,2; 2,3
        Pattern p1 = Pattern.compile("([0-9]*)(\\.|\\,)[0-9]");
        Pattern p2 = Pattern.compile("([0-9]*)");
        Matcher m1 = p1.matcher(durationStr);
        Matcher m2 = p2.matcher(durationStr);
        if (!m1.matches() && !m2.matches()) {
            errors.rejectValue("timeSheetTablePart[" + notNullRowNumber + "].duration",
                    "error.tsform.duration.format", errorMessageArgs,
                    "Количество часов указано не верно в строке " + (notNullRowNumber + 1) + ". Примеры правильных значений (5, 3.5, 2.0 и т.п.).");
        } else {
            duration = Double.parseDouble(durationStr.replace(",", "."));
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

    private boolean isProjectValid(Integer project) {
        return project == null || projectService.findActive(project) != null;
    }

    private boolean isActCatValid(Integer actCat) {
        return actCat == null || dictionaryItemService.find(actCat) != null;
    }

    private boolean isProjectRoleValid(Integer projectRole) {
        return projectRole == null || projectRoleService.findActive(projectRole) != null;
    }

    private boolean isProjectTaskValid(Integer project, String task) {
        return project == null && task == null ||
                projectTaskService.find(project, task) != null;
    }

    public boolean isProjectActType(Integer actTypeId) {
        return actTypeId.equals(new Integer(DETAIL_TYPE_PROJECT)) ||
                actTypeId.equals(new Integer(DETAIL_TYPE_PRESALE)) ||
                actTypeId.equals(new Integer(DETAIL_TYPE_OUTPROJECT));
    }
}