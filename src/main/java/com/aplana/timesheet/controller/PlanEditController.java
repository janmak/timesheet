package com.aplana.timesheet.controller;

import argo.jdom.JsonNode;
import argo.jdom.JsonObjectNodeBuilder;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import com.aplana.timesheet.dao.EmployeeDAO;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.EmployeePlanType;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.enums.TypesOfActivityEnum;
import com.aplana.timesheet.form.PlanEditForm;
import com.aplana.timesheet.form.validator.PlanEditFormValidator;
import com.aplana.timesheet.service.*;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.EnumsUtils;
import com.aplana.timesheet.util.JsonUtil;
import com.aplana.timesheet.util.TimeSheetConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.Calendar;

import static argo.jdom.JsonNodeBuilders.aStringBuilder;
import static argo.jdom.JsonNodeBuilders.anObjectBuilder;
import static argo.jdom.JsonNodeFactories.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Controller
public class PlanEditController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanEditController.class);

    public static final String _PLAN = "_plan";
    public static final String _FACT = "_fact";

    public static final String SUMMARY = "summary";
    public static final String CENTER_PROJECTS = "center_projects";
    public static final String CENTER_PRESALES = "center_presales";
    public static final String OTHER_PROJECTS_AND_PRESALES = "other_projects_and_presales";
    public static final String NON_PROJECT = "non_project";
    public static final String ILLNESS = "illness";
    public static final String VACATION = "vacation";
    public static final String EMPLOYEE = "employee";
    public static final String EMPLOYEE_ID = "employee_id";

    public static final String PROJECT_ID = "id";
    public static final String PROJECT_NAME = "name";
    public static final String PROJECTS_PLANS = "projects_plans";
    public static final String SUMMARY_PLAN = SUMMARY + _PLAN;

    public static final String SUMMARY_FACT = SUMMARY + _FACT;
    public static final String CENTER_PROJECTS_PLAN = CENTER_PROJECTS + _PLAN;
    public static final String CENTER_PROJECTS_FACT = CENTER_PROJECTS + _FACT;
    public static final String CENTER_PRESALES_PLAN = CENTER_PRESALES + _PLAN;
    public static final String CENTER_PRESALES_FACT = CENTER_PRESALES + _FACT;
    public static final String OTHER_PROJECTS_AND_PRESALES_PLAN = OTHER_PROJECTS_AND_PRESALES + _PLAN;
    public static final String OTHER_PROJECTS_AND_PRESALES_FACT = OTHER_PROJECTS_AND_PRESALES + _FACT;
    public static final String NON_PROJECT_PLAN = NON_PROJECT + _PLAN;
    public static final String NON_PROJECT_FACT = NON_PROJECT + _FACT;
    public static final String ILLNESS_PLAN = ILLNESS + _PLAN;
    public static final String ILLNESS_FACT = ILLNESS + _FACT;
    public static final String VACATION_PLAN = VACATION + _PLAN;
    public static final String VACATION_FACT = VACATION + _FACT;
    public static final String JSON_DATA_YEAR = "year";
    public static final String JSON_DATA_MONTH = "month";
    public static final String JSON_DATA_ITEMS = "items";

    public static final String PLAN_SAVE = "/planSave";
    private static final String PLAN_EDIT = "/planEdit";
    private static final String COOKIE_DIVISION_ID = "cookie_division_id";

    private static final String COOKIE_REGIONS = "cookie_regions";
    private static final String COOKIE_PROJECT_ROLES = "cookie_project_roles";

    private static final String COOKIE_SHOW_PLANS = "cookie_show_plans";
    private static final String COOKIE_SHOW_FACTS = "cookie_show_facts";
    private static final String COOKIE_SHOW_PROJECTS = "cookie_show_projects";
    private static final String COOKIE_SHOW_PRESALES = "cookie_show_presales";

    private static final String SEPARATOR = "~";

    private static final JsonStringNode PROJECTS_PLANS_FIELD = string(PROJECTS_PLANS);
    private static final JsonStringNode NON_PROJECT_PLAN_FIELD = string(NON_PROJECT_PLAN);
    private static final JsonStringNode ILLNESS_PLAN_FIELD = string(ILLNESS_PLAN);
    private static final JsonStringNode VACATION_PLAN_FIELD = string(VACATION_PLAN);
    private static final JsonStringNode OTHER_PROJECTS_AND_PRESALES_PLAN_FIELD =
            string(OTHER_PROJECTS_AND_PRESALES_PLAN);

    private static final Map<JsonStringNode, TSEnum> PLAN_TYPE_MAP = new HashMap<JsonStringNode, TSEnum>();

    static {
        PLAN_TYPE_MAP.put(NON_PROJECT_PLAN_FIELD, EmployeePlanType.NON_PROJECT);
        PLAN_TYPE_MAP.put(ILLNESS_PLAN_FIELD, EmployeePlanType.ILLNESS);
        PLAN_TYPE_MAP.put(VACATION_PLAN_FIELD, EmployeePlanType.VACATION);
        PLAN_TYPE_MAP.put(OTHER_PROJECTS_AND_PRESALES_PLAN_FIELD, EmployeePlanType.WORK_FOR_OTHER_DIVISIONS);
    }

    ////////////////////////////////////////////////////////////////

    private static double nilIfNull(Double value) {
        return (value == null) ? 0 : value;
    }

    private static boolean isPresale(Project project) {
        return (EnumsUtils.getEnumById(project.getState().getId(), TypesOfActivityEnum.class) == TypesOfActivityEnum.PRESALE);
    }

    private static <T> T defaultValue(T value, T defaultValue) {
        return (value == null) ? defaultValue : value;
    }

    private static Integer tryParseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<Integer> tryParseIntegerListFromString(String string) {
        try {
            final List<Integer> list = new ArrayList<Integer>();

            for (String s : StringUtils.split(string, SEPARATOR)) {
                list.add(Integer.valueOf(s));
            }

            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean tryParseBoolean(String value) {
        try {
            return Boolean.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static void addCookie(HttpServletResponse response, String name, Object value) {
        response.addCookie(new Cookie(name, String.valueOf(value)));
    }

    @Autowired
    private PlanEditFormValidator planEditFormValidator;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRoleService projectRoleService;

    @Autowired
    private RegionService regionregionService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetService timeSheetService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    private EmployeePlanService employeePlanService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @RequestMapping(PLAN_EDIT)
    public ModelAndView showForm(
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        initForm(form, request);

        saveCookie(form, response);

        return createModelAndView(form, bindingResult);
    }

    /**
     *
     * @param form
     * @return true, if form was init not from cookies
     */
    private void initForm(PlanEditForm form, HttpServletRequest request) {
        initDefaultForm(form);

        final Cookie[] cookies = request.getCookies();

        String name, value;

        for (Cookie cookie : cookies) {
            name = cookie.getName();
            value = cookie.getValue();

            if (COOKIE_DIVISION_ID.equals(name)) {
                form.setDivisionId(defaultValue(tryParseInt(value), form.getDivisionId()));
            } else if (COOKIE_REGIONS.equals(name)) {
                form.setRegions(defaultValue(tryParseIntegerListFromString(value), form.getRegions()));
            } else if (COOKIE_PROJECT_ROLES.equals(name)) {
                form.setProjectRoles(defaultValue(tryParseIntegerListFromString(value), form.getProjectRoles()));
            } else if (COOKIE_SHOW_PLANS.equals(name)) {
                form.setShowPlans(defaultValue(tryParseBoolean(value), form.getShowPlans()));
            } else if (COOKIE_SHOW_FACTS.equals(name)) {
                form.setShowFacts(defaultValue(tryParseBoolean(value), form.getShowFacts()));
            } else if (COOKIE_SHOW_PROJECTS.equals(name)) {
                form.setShowProjects(defaultValue(tryParseBoolean(value), form.getShowProjects()));
            } else if (COOKIE_SHOW_PRESALES.equals(name)) {
                form.setShowPresales(defaultValue(tryParseBoolean(value), form.getShowPresales()));
            }
        }
    }

    private void initDefaultForm(PlanEditForm form) {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);

        form.setDivisionId(securityService.getSecurityPrincipal().getEmployee().getDivision().getId());
        form.setYear(year);
        form.setMonth(calendar.get(Calendar.MONTH) + 1);
        form.setRegions(Arrays.asList(PlanEditForm.ALL_VALUE));
        form.setProjectRoles(Arrays.asList(PlanEditForm.ALL_VALUE));
        form.setShowPlans(Boolean.TRUE);
        form.setShowFacts(Boolean.TRUE);
        form.setShowProjects(Boolean.TRUE);
        form.setShowPresales(Boolean.TRUE);
    }

    private List<Region> getRegionList() {
        return regionregionService.getRegions();
    }

    private List<ProjectRole> getProjectRoleList() {
        return projectRoleService.getProjectRoles();
    }

    private List<Division> getDivisionList() {
        return divisionService.getDivisions();
    }

    private List<com.aplana.timesheet.dao.entity.Calendar> getYearList() {
        return DateTimeUtil.getYearsList(calendarService);
    }

    private String getMonthMapAsJson(List<com.aplana.timesheet.dao.entity.Calendar> yearList) {
        return DateTimeUtil.getMonthListJson(yearList, calendarService);
    }

    @RequestMapping(value = PLAN_EDIT, method = RequestMethod.POST)
    public ModelAndView showTable(
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        final ModelAndView modelAndView = createModelAndView(form, bindingResult);

        if (!bindingResult.hasErrors()) {
            saveCookie(form, response);
        }

        return modelAndView;
    }

    private void saveCookie(PlanEditForm form, HttpServletResponse response) {
        addCookie(response, COOKIE_DIVISION_ID, form.getDivisionId());
        addCookie(response, COOKIE_SHOW_PLANS, form.getShowPlans());
        addCookie(response, COOKIE_SHOW_FACTS, form.getShowFacts());
        addCookie(response, COOKIE_SHOW_PROJECTS, form.getShowProjects());
        addCookie(response, COOKIE_SHOW_PRESALES, form.getShowPresales());
        addCookie(response, COOKIE_REGIONS, StringUtils.join(form.getRegions(), SEPARATOR));
        addCookie(response, COOKIE_PROJECT_ROLES, StringUtils.join(form.getProjectRoles(), SEPARATOR));
    }

    private ModelAndView createModelAndView(PlanEditForm form, BindingResult bindingResult) {
        final ModelAndView modelAndView = new ModelAndView("planEdit");

        modelAndView.addObject("regionList", getRegionList());
        modelAndView.addObject("projectRoleList", getProjectRoleList());
        modelAndView.addObject("divisionList", getDivisionList());

        final List<com.aplana.timesheet.dao.entity.Calendar> yearList = getYearList();

        modelAndView.addObject("yearList", yearList);
        modelAndView.addObject("monthMapJson", getMonthMapAsJson(yearList));

        planEditFormValidator.validate(form, bindingResult);

        if (!bindingResult.hasErrors() && (form.getShowPlans() || form.getShowFacts())) {
            fillTableData(modelAndView, form);
            modelAndView.addObject("monthList", calendarService.getMonthList(form.getYear()));
        }

        return modelAndView;
    }

    private void fillTableData(ModelAndView modelAndView, PlanEditForm form) {
        final Date date = DateTimeUtil.createDate(form.getYear(), form.getMonth());

        modelAndView.addObject("jsonDataToShow", getDataAsJson(form, date));
        modelAndView.addObject("projectListJson", getProjectListAsJson(getProjects(form, date)));
    }

    private List<Project> getProjects(PlanEditForm form, Date date) {
        final List<Integer> projectStates = getProjectStates(form);

        return projectStates.isEmpty()
                ? new ArrayList<Project>()
                : projectService.getProjectsByStatesForDate(
                        projectStates,
                        date
                );
    }

    private String getProjectListAsJson(List<Project> projects) {
        final List<JsonNode> nodes = new ArrayList<JsonNode>();

        for (Project project : projects) {
            nodes.add(
                    object(
                            field(PROJECT_ID, number(project.getId())),
                            field(PROJECT_NAME, string(project.getName()))
                    )
            );
        }

        return JsonUtil.format(array(nodes));
    }

    private List<Integer> getProjectStates(PlanEditForm form) {
        final List<Integer> states = new ArrayList<Integer>();

        if (form.getShowProjects()) {
            states.add(TypesOfActivityEnum.PROJECT.getId());
        }

        if (form.getShowPresales()) {
            states.add(TypesOfActivityEnum.PRESALE.getId());
        }

        return states;
    }

    private String getDataAsJson(PlanEditForm form, Date date) {
        final List<Employee> employees = employeeService.getDivisionEmployees(
                form.getDivisionId(),
                date,
                getRegionIds(form),
                getProjectRoleIds(form)
        );
        final ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();
        final Map<Integer, Double> projectsFactMap = new HashMap<Integer, Double>();

        final Integer year = form.getYear();
        final Integer month = form.getMonth();
        final boolean showPlans = form.getShowPlans();
        final boolean showFacts = form.getShowFacts();

        JsonObjectNodeBuilder builder;
        Region region;
        Division division;
        int workDaysCount;

        for (Employee employee : employees) {
            builder = anObjectBuilder().
                    withField(EMPLOYEE_ID, JsonUtil.aNumberBuilder(employee.getId())).
                    withField(EMPLOYEE, aStringBuilder(employee.getName()));

            division = employee.getDivision();
            region = employee.getRegion();

            workDaysCount = calendarService.getWorkDaysCountForRegion(region, year, month, employee.getStartDate());

            if (showPlans) {
                builder.withField(
                        SUMMARY_PLAN,
                        JsonUtil.aNumberBuilder(TimeSheetConstants.WORK_DAY_DURATION * workDaysCount)
                );

                Double centerProjectsPlan = null;
                Double centerPresalesPlan = null;

                for (EmployeeProjectPlan employeeProjectPlan : employeeProjectPlanService.find(employee, year, month)) {
                    final Project project = employeeProjectPlan.getProject();
                    final Employee manager = project.getManager();

                    if (manager != null && division.equals(manager.getDivision())) {
                        final double duration = nilIfNull(employeeProjectPlan.getValue());

                        if (isPresale(project)) {
                            centerPresalesPlan = nilIfNull(centerPresalesPlan) + duration;
                        } else {
                            centerProjectsPlan = nilIfNull(centerProjectsPlan) + duration;
                        }
                    }

                    builder.withField(
                            String.format("%d" + _PLAN, project.getId()),
                            JsonUtil.aNumberBuilder(employeeProjectPlan.getValue())
                    );
                }

                appendNumberField(builder, CENTER_PROJECTS_PLAN, centerProjectsPlan);
                appendNumberField(builder, CENTER_PRESALES_PLAN, centerPresalesPlan);


                for (EmployeePlan employeePlan : employeePlanService.find(employee, year, month)) {
                    appendNumberField(builder, getFieldNameForEmployeePlan(employeePlan), employeePlan.getValue());
                }
            }

            if (showFacts) {
                double summaryFact = 0;
                double centerProjectsFact = 0;
                double centerPresalesFact = 0;
                double otherProjectsFact = 0;
                double nonProjectFact = 0;
                double illnessFact = 0;
                double vacationFact = 0;

                projectsFactMap.clear();

                Integer projectId;

                for (TimeSheet timeSheet : timeSheetService.getTimeSheetsForEmployee(employee, year, month)) {
                    for (TimeSheetDetail timeSheetDetail : timeSheet.getTimeSheetDetails()) {
                        final double duration = nilIfNull(timeSheetDetail.getDuration());

                        summaryFact += duration;

                        final TypesOfActivityEnum actType =
                                EnumsUtils.getEnumById(timeSheetDetail.getActType().getId(), TypesOfActivityEnum.class);

                        switch (actType) {
                            case NON_PROJECT: nonProjectFact += duration; break;
                            case ILLNESS: illnessFact += getDefaultDuration(duration); break;
                            case VACATION: vacationFact += getDefaultDuration(duration); break;
                        }

                        final Project project = timeSheetDetail.getProject();

                        if (project != null && project.isActive()) {
                            projectId = project.getId();

                            final Employee manager = project.getManager();

                            if (manager != null && division.equals(manager.getDivision())) {
                                if (isPresale(project)) {
                                    centerPresalesFact += duration;
                                } else {
                                    centerProjectsFact += duration;
                                }
                            } else {
                                otherProjectsFact += duration;
                            }

                            projectsFactMap.put(projectId, nilIfNull(projectsFactMap.get(projectId)) + duration);
                        }
                    }
                }

                builder.
                    withField(SUMMARY_FACT, JsonUtil.aNumberBuilder(summaryFact)).
                    withField(CENTER_PROJECTS_FACT, JsonUtil.aNumberBuilder(centerProjectsFact)).
                    withField(CENTER_PRESALES_FACT, JsonUtil.aNumberBuilder(centerPresalesFact)).
                    withField(OTHER_PROJECTS_AND_PRESALES_FACT, JsonUtil.aNumberBuilder(otherProjectsFact)).
                    withField(NON_PROJECT_FACT, JsonUtil.aNumberBuilder(nonProjectFact)).
                    withField(ILLNESS_FACT, JsonUtil.aNumberBuilder(illnessFact)).
                    withField(VACATION_FACT, JsonUtil.aNumberBuilder(vacationFact));

                for (Map.Entry<Integer, Double> entry : projectsFactMap.entrySet()) {
                    builder.withField(
                            String.format("%d" + _FACT, entry.getKey()),
                            JsonUtil.aNumberBuilder(entry.getValue())
                    );
                }
            }

            nodes.add(builder.build());
        }

        return JsonUtil.format(array(nodes));
    }

    private String getFieldNameForEmployeePlan(EmployeePlan employeePlan) {
        for (Map.Entry<JsonStringNode, TSEnum> entry : PLAN_TYPE_MAP.entrySet()) {
            if (entry.getValue() == EnumsUtils.getEnumById(employeePlan.getType(), EmployeePlanType.class)) {
                return entry.getKey().getText();
            }
        }

        throw new IllegalArgumentException();
    }

    private void appendNumberField(JsonObjectNodeBuilder builder, String fieldName, Double value) {
        if (value != null) {
            builder.withField(fieldName, JsonUtil.aNumberBuilder(value));
        }
    }

    private double getDefaultDuration(double duration) {
        return (duration == 0) ? TimeSheetConstants.WORK_DAY_DURATION : duration;
    }

    private List<Integer> getRegionIds(PlanEditForm form) {
        final List<Integer> regions = form.getRegions();

        if (regions.contains(PlanEditForm.ALL_VALUE)) {
            return Arrays.asList(EmployeeDAO.ALL_REGIONS);
        }

        return regions;
    }

    private List<Integer> getProjectRoleIds(PlanEditForm form) {
        final List<Integer> projectRoles = form.getProjectRoles();

        if (projectRoles.contains(PlanEditForm.ALL_VALUE)) {
            return Arrays.asList(EmployeeDAO.ALL_PROJECT_ROLES);
        }

        return projectRoles;
    }

    @RequestMapping(value = PLAN_SAVE, method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String save(
            @ModelAttribute(PlanEditForm.FORM) PlanEditForm form
    ) {
        try {
            final List<EmployeePlan> employeePlans = new ArrayList<EmployeePlan>();
            final List<EmployeeProjectPlan> employeeProjectPlans = new ArrayList<EmployeeProjectPlan>();
            final JsonRootNode rootNode = JsonUtil.parse(form.getJsonData());

            final Integer year = JsonUtil.getDecNumberValue(rootNode, JSON_DATA_YEAR);
            final Integer month = JsonUtil.getDecNumberValue(rootNode, JSON_DATA_MONTH);

            processJsonDataItems(employeePlans, employeeProjectPlans, rootNode, year, month);

            employeePlanService.store(employeePlans);
            employeeProjectPlanService.store(employeeProjectPlans);
        } catch (InvalidSyntaxException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

        return "redirect:" + PLAN_EDIT;
    }

    private void processJsonDataItems(
            List<EmployeePlan> employeePlans, List<EmployeeProjectPlan> employeeProjectPlans,
            JsonRootNode rootNode, Integer year, Integer month
    ) {
        Integer employeeId;

        for (JsonNode jsonNode : rootNode.getArrayNode(JSON_DATA_ITEMS)) {
            employeeId = JsonUtil.getDecNumberValue(jsonNode, EMPLOYEE_ID);

            final Employee employee = employeeService.find(employeeId);

            addEmployeePlansAndRemoveIfNeed(employeePlans, year, month, employee, jsonNode);

            addEmployeeProjectPlansAndRemoveOldEntries(employeeProjectPlans, year, month, employee, jsonNode);
        }
    }

    private void addEmployeeProjectPlansAndRemoveOldEntries(List<EmployeeProjectPlan> employeeProjectPlans, Integer year, Integer month, Employee employee, JsonNode jsonNode) {
        employeeProjectPlanService.remove(employee, year, month);

        if (jsonNode.getFields().containsKey(PROJECTS_PLANS_FIELD)) {
            for (JsonNode node : jsonNode.getArrayNode(PROJECTS_PLANS)) {
                employeeProjectPlans.add(createEmployeeProjectPlanIfNeed(year, month, employee, node));
            }
        }
    }

    private void addEmployeePlansAndRemoveIfNeed(List<EmployeePlan> employeePlans, Integer year, Integer month, Employee employee, JsonNode jsonNode) {
        final Map<JsonStringNode, JsonNode> fields = jsonNode.getFields();
        final List<EmployeePlan> employeePlansToDelete = new ArrayList<EmployeePlan>();

        JsonStringNode field;

        for (Map.Entry<JsonStringNode, TSEnum> entry : PLAN_TYPE_MAP.entrySet()) {
            field = entry.getKey();

            final EmployeePlan employeePlan = createEmployeePlanIfNeed(
                    year, month, employee,
                    dictionaryItemService.find(entry.getValue().getId())
            );

            if (fields.containsKey(field)) {
                employeePlan.setValue(JsonUtil.getFloatNumberValue(jsonNode, field.getText()));

                employeePlans.add(employeePlan);
            } else {
                employeePlansToDelete.add(employeePlan);
            }
        }

        employeePlanService.remove(employeePlansToDelete);
    }

    private EmployeePlan createEmployeePlanIfNeed(Integer year, Integer month, Employee employee, DictionaryItem dictionaryItem) {
        EmployeePlan employeePlan = employeePlanService.tryFind(employee, year, month, dictionaryItem);

        if (employeePlan == null) {
            employeePlan = new EmployeePlan();

            employeePlan.setType(dictionaryItem);
            employeePlan.setEmployee(employee);
            employeePlan.setYear(year);
            employeePlan.setMonth(month);
        }

        return employeePlan;
    }

    private EmployeeProjectPlan createEmployeeProjectPlanIfNeed(Integer year, Integer month, Employee employee,
                                                                JsonNode node
    ) {
        final Project project = projectService.find(JsonUtil.getDecNumberValue(node, PROJECT_ID));

        EmployeeProjectPlan employeeProjectPlan = employeeProjectPlanService.tryFind(employee, year, month, project);

        if (employeeProjectPlan == null) {
            employeeProjectPlan = new EmployeeProjectPlan();

            employeeProjectPlan.setEmployee(employee);
            employeeProjectPlan.setProject(project);
            employeeProjectPlan.setYear(year);
            employeeProjectPlan.setMonth(month);
        }

        employeeProjectPlan.setValue(JsonUtil.getFloatNumberValue(node, _PLAN));

        return employeeProjectPlan;
    }
}
