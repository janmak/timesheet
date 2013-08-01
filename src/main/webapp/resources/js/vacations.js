/**
 * Created with IntelliJ IDEA.
 * User: rshamsutdinov
 * Date: 22.01.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */


dojo.addOnLoad(function () {
    updateMultipleForSelect(dojo.byId(REGIONS));
});

function showGraphic() {
    var g = new Gantt(dojo.byId("graphic_div"), holidayList);

    for (var i = 0; i < vacationListJSON.length; i++){
        var vacation = new RegionEmployees(vacationListJSON[i].region_name, vacationListJSON[i].employeeList);
        g.AddRegionEmployeeList(vacation);
    }

    g.Draw();
}

function showVacations() {
    dojo.byId(VACATION_ID).setAttribute("disabled", "disabled");
    vacationsForm.action = contextPath + "/vacations";
    vacationsForm.submit();
}

function divisionChangeVac(division) {
    var divisionId = division.value;
    if (divisionId == undefined) {
        divisionId = division;
    }
    fillProjectListByDivChange(divisionId);
    sortManager();
    fillEmployeeSelect();
}

function selectCurrentEmployee(employeeSelect) {
    for (var i = 0; i < employeeSelect.options.length; i++) {
        if (employeeSelect[i].value == selectedEmployee) {
            return true;
        }
    }
    return false;
}

function isNilOrNull(obj) {
    return !(obj != null && obj != 0);
}

function checkEmployeeData(empId) {
    if (isNilOrNull(empId)){
        alert("Необходимо выбрать сотрудника!\n");
        return false;
    }
    return true;
}

function getVacationsNeedsApprovalCountString(){
    dojo.xhrGet({
        url: getContextPath() + "/vacations/count",
        timeout:10000,
        load:function (data) {
            if (data) {
                dojo.byId("vacationCount").innerHTML = data;
            } else {
                dojo.byId("vacationCount").innerHTML = "";
            }
        }
    });
}

function updateMultipleForSelect(select) {
    var allOptionIndex;

    var isAllOption = dojo.some(select.options, function (option, idx) {
        if (option.value == ALL_VALUE && option.selected) {
            allOptionIndex = idx;
            return true;
        }

        return false;
    });

    if (isAllOption) {
        select.removeAttribute("multiple");
        select.selectedIndex = allOptionIndex;
    } else {
        select.setAttribute("multiple", "multiple");
    }
    fillEmployeeSelect();
}

function sortManager() {
    var divisionId = dojo.byId(DIVISION_ID).value;
    var managerSelect = dojo.byId(MANAGER_ID);
    managerSelect.options.length = 0;
    insertEmptyOptionWithCaptionInHead(managerSelect, "Все");

    for (var i = 0; i < managerList.length; i++) {
        if (managerList[i].divId == divisionId) {
            var managerOption = dojo.doc.createElement("option");
            dojo.attr(managerOption, {
                value:managerList[i].id
            });
            managerOption.title = managerList[i].value;
            managerOption.innerHTML = managerList[i].value;
            managerSelect.appendChild(managerOption);
        }
    }
}

function fillEmployeeSelect(){
    dojo.xhrGet({
        url: getContextPath() + "/vacations/getEmployeeList",
        form: "vacationsForm",
        handleAs:"json",
        timeout:10000,
        load:function (employeeList) {
            var employeeSelect = dojo.byId(EMPLOYEE_ID);
            employeeSelect.options.length = 0;
            for (var i in employeeList){
                insertEmployeeToSelect(employeeSelect, employeeList[i]);
            }
            sortSelectOptions(employeeSelect);
            insertEmptyOptionWithCaptionInHead(employeeSelect, "Все сотрудники");
            if (selectCurrentEmployee(employeeSelect)) {
                employeeSelect.value = selectedEmployee;
            } else {
                employeeSelect.value = 0;
            }

        }
    });
}

function insertEmployeeToSelect(employeeSelect, employee){
    var employeeOption = dojo.doc.createElement("option");
    dojo.attr(employeeOption, {
        value: employee.id
    });
    employeeOption.title = employee.value;
    employeeOption.innerHTML = employee.value;
    employeeSelect.appendChild(employeeOption);
}


function changeSelectedEmployee() {
    selectedEmployee = dojo.byId(EMPLOYEE_ID).value;
}

function createVacation() {
    var empId = dojo.byId(EMPLOYEE_ID).value;

    if (checkEmployeeData(empId)) {
        vacationsForm.action =
            contextPath + "/createVacation/" + empId;
        vacationsForm.submit();
    }
}

function deleteVacation(parentElement, vac_id) {
    var empId = dojo.byId(EMPLOYEE_ID).value;
    var divisionId = dojo.byId(DIVISION_ID).value;

    if (!confirm("Удалить заявку?")) {
        return;
    }

    dojo.byId(VACATION_ID).removeAttribute("disabled");
    dojo.byId(VACATION_ID).value = vac_id;
    vacationsForm.action = contextPath + "/vacations";
    vacationsForm.submit();
}

function deleteApprover(apr_id) {
    if (!confirm("Удалить утверждающего?")) {
        return;
    } else {
        console.log("apr_id = " + apr_id);
        dojo.byId(APPROVAL_ID).value = apr_id;
        vacationsForm.action = contextPath + "/vacations";
        vacationsForm.submit();
    }
}

/* Заполняет список доступных проектов/пресейлов */
function fillProjectListByDivChange(division) {

    var projectSelect = dojo.byId(PROJECT_ID);
    dojo.removeAttr(projectSelect, "disabled");
    //Очищаем список проектов.
    projectSelect.options.length = 0;
    var hasAny = false;
    for (var i = 0; i < fullProjectList.length; i++) {
        if (division == 0 || fullProjectList[i].divId == division) {
            var divProjs = fullProjectList[i].divProjs;
            for (var j = 0; j < divProjs.length; j++){
                projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value: divProjs[j].id
                });
                projectOption.title = divProjs[j].value;
                projectOption.innerHTML = divProjs[j].value;
                projectSelect.appendChild(projectOption);
                hasAny = true;
            }
            break;
        }
    }

    sortSelectOptions(projectSelect);
    validateAndAddNewOption(hasAny, division, projectSelect);
    projectSelect.value = 0;
}

function validateAndAddNewOption(hasAny, divisionId, select){
    if (hasAny || divisionId == 0){
        insertEmptyOptionWithCaptionInHead(select, "Все");
    }else{
        insertEmptyOptionWithCaptionInHead(select, "Пусто");
        dojo.attr(select, {disabled:"disabled"});
    }
}
