package com.aplana.timesheet.service;

import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import com.aplana.timesheet.dao.entity.*;
import com.aplana.timesheet.enums.TSEnum;
import com.aplana.timesheet.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public void savePlans(JsonRootNode rootNode, Integer year, Integer month) {
        final List<EmployeePlan> employeePlans = new ArrayList<EmployeePlan>();
        final List<EmployeeProjectPlan> employeeProjectPlans = new ArrayList<EmployeeProjectPlan>();

        processJsonDataItems(employeePlans, employeeProjectPlans, rootNode, year, month);

        employeePlanService.store(employeePlans);
        employeeProjectPlanService.store(employeeProjectPlans);
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
