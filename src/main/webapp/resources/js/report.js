/**
 * User: iziyangirov
 * Date: 29.01.13
 * Здесь собраны общие скрипты для отчетов
 */

dojo.addOnLoad(function() {
    setDefaultRegion();
});

function allRegionsCheckBoxChange(checked){
    var regionIds = "regionIds";
    if (checked) {
        dojo.attr(regionIds, {disabled:"disabled"});
    } else {
        dojo.removeAttr(regionIds, "disabled");
    }
}

function setDefaultRegion(){
    var allRegions = dojo.byId("allRegions");
    if (allRegions != null){
        allRegions.checked = true;
        allRegionsCheckBoxChange(true);
    }
}

function fillEmployeeListByDivision(division) {
    var employeeSelect = dojo.byId("employeeId");
    var employeeOption = null;
    var divisionId = division.value;
    dojo.removeAttr(employeeSelect, "disabled");
    //Очищаем список сотрудников.
    employeeSelect.options.length = 0;
    var hasAny = false;
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id != 0) {
                    employeeOption = dojo.doc.createElement("option");
                    dojo.attr(employeeOption, {
                        value:employeeList[i].divEmps[j].id
                    });
                    employeeOption.title = employeeList[i].divEmps[j].value;
                    employeeOption.innerHTML = employeeList[i].divEmps[j].value;
                    employeeSelect.appendChild(employeeOption);
                    hasAny = true;
                }
            }
        }
    }
    sortSelectOptions(employeeSelect);
    validateAndAddNewOption(hasAny, divisionId, employeeSelect);

    var rows = dojo.query(".row_number");
    for (var i = 0; i < rows.length; i++) {
        fillProjectList(i, dojo.byId("activity_type_id_" + i).value);
    }
}

/* Заполняет список доступных проектов/пресейлов */
function fillProjectListByDivision(division) {

    var checkBox = dojo.byId("filterProjects");
    if (division == null) {
        division = dojo.byId("divisionId");

        if ((checkBox.checked) && (division.value == 0))
            division.value = defaultDivisionId;
    }
    var divisionId = division.value;
    var projectSelect = dojo.byId("projectId");
    dojo.removeAttr(projectSelect, "disabled");
    //Очищаем список проектов.
    projectSelect.options.length = 0;
    var hasAny = false;
    if (divisionId == 0) {
        dojo.attr("filterProjects", {disabled:"disabled", checked:false});
        dojo.attr("projectId", {disabled:"disabled"});
    } else {
        dojo.removeAttr("filterProjects", "disabled");
        dojo.removeAttr("projectId", "disabled");
        if (checkBox.checked) {
            dojo.removeAttr("divisionId", "disabled");
            for (var i = 0; i < projectListWithOwnerDivision.length; i++) {
                if (divisionId == projectListWithOwnerDivision[i].ownerDivisionId) {
                    projectOption = dojo.doc.createElement("option");
                    dojo.attr(projectOption, {
                        value:projectListWithOwnerDivision[i].id
                    });
                    projectOption.title = projectListWithOwnerDivision[i].value;
                    projectOption.innerHTML = projectListWithOwnerDivision[i].value;
                    projectSelect.appendChild(projectOption);
                    hasAny = true;
                }
            }
        }
        else {
            for (var i = 0; i < fullProjectList.length; i++) {
                projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value:fullProjectList[i].id
                });
                projectOption.title = fullProjectList[i].value;
                projectOption.innerHTML = fullProjectList[i].value;
                projectSelect.appendChild(projectOption);
                hasAny = true;
            }
        }
        sortSelectOptions(projectSelect);
        validateAndAddNewOption(hasAny, divisionId, projectSelect);
    }
}

function validateAndAddNewOption(hasAny, divisionId, select){
    if (hasAny || divisionId == 0){
        insertEmptyOptionWithCaptionInHead(select, "Все");
    }else{
        insertEmptyOptionWithCaptionInHead(select, "Пусто");
        dojo.attr(select, {disabled:"disabled"});
    }
}

function setDefaultValuesForReport2And3(){
    reportForm.emplDivisionId.value = 0;
    reportForm.employeeId.value = 0;
    fillProjectListByDivision(reportForm.divisionId);
    fillEmployeeListByDivision(reportForm.emplDivisionId);
    reportForm.projectId.value = 0;
}