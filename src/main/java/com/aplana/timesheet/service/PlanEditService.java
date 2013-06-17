package com.aplana.timesheet.service;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.util.DateTimeUtil;
import com.aplana.timesheet.util.JsonUtil;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Calendar;

import static com.aplana.timesheet.controller.PlanEditController.*;

/**
 * @author rshamsutdinov
 * @version 1.0
 */
@Service
public class PlanEditService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeProjectPlanService employeeProjectPlanService;

    @Autowired
    private EmployeePlanService employeePlanService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DictionaryItemService dictionaryItemService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePlans(JsonRootNode rootNode, Integer year, Integer month) {
        final Calendar calendar = DateTimeUtil.getCalendar(year, month);

        if (DateUtils.truncatedCompareTo(new Date(), calendar.getTime(), Calendar.MONTH) > 0) {
            throw new IllegalArgumentException("Редактирование планов за предыдущий месяц запрещено");
        }

        final List<EmployeePlan> employeePlans = new ArrayList<EmployeePlan>();
        final List<EmployeeProjectPlan> employeeProjectPlans = new ArrayList<EmployeeProjectPlan>();

        processJsonDataItems(employeePlans, employeeProjectPlans, rootNode, year, month);

        employeePlanService.mergeProjectPlans(employeePlans);
        employeeProjectPlanService.mergeEmployeeProjectPlans(employeeProjectPlans);
    }

    private void processJsonDataItems(
            List<EmployeePlan> employeePlans, List<EmployeeProjectPlan> employeeProjectPlans,
            JsonRootNode rootNode, Integer year, Integer month
    ) {
        Integer employeeId;

        for (JsonNode jsonNode : rootNode.getArrayNode(JSON_DATA_ITEMS)) {
            employeeId = JsonUtil.getDecNumberValue(jsonNode, EMPLOYEE_ID);

            final Employee employee = employeeService.find(employeeId);

            addEmployeePlans(employeePlans, year, month, employee, jsonNode);

            addEmployeeProjectPlans(employeeProjectPlans, year, month, employee, jsonNode);
        }
    }

    private void addEmployeeProjectPlans(List<EmployeeProjectPlan> employeeProjectPlans, Integer year, Integer month, Employee employee, JsonNode jsonNode) {
        //employeeProjectPlanService.remove(employee, year, month); KSS APLANATS-850 Удалять будем только существующие записи с value=0. Удаление будет вызвано в методе com.aplana.timesheet.service.EmployeeProjectPlanService.mergeEmployeeProjectPlans

        if (jsonNode.getFields().containsKey(PROJECTS_PLANS_FIELD)) {
            for (JsonNode node : jsonNode.getArrayNode(PROJECTS_PLANS)) {
                employeeProjectPlans.add(createEmployeeProjectPlanIfNeed(year, month, employee, node));
            }
        }
    }

    private void addEmployeePlans(List<EmployeePlan> employeePlans, Integer year, Integer month, Employee employee, JsonNode jsonNode) {
        final Map<JsonStringNode, JsonNode> fields = jsonNode.getFields();

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
            }
        }

        // KSS APLANATS-850 Удалять будем только существующие записи с value=0.
        // Удаление будет вызвано в методе com.aplana.timesheet.service.EmployeePlanService.mergeProjectPlans

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
