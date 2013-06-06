<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ page import="static com.aplana.timesheet.form.VacationsForm.*" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html>
<head>
    <title><fmt:message key="title.vacations"/></title>
    <link rel="stylesheet" type="text/css" href="<%= getResRealPath("/resources/css/vacations.css", application) %>" />
    <script src="<%= getResRealPath("/resources/js/vacations.js", application) %>" type="text/javascript"></script>
    <script type="text/javascript">
        dojo.require("dojo.NodeList-traverse");
        dojo.require("dojox.html.entities");
        dojo.require("dijit.form.DateTextBox");
        dojo.require(CALENDAR_EXT_PATH);
        require(["dojo/parser", "dijit/TitlePane"]);

        function getEmployeeId() {
            return "${employee.id}";
        }

        dojo.addOnLoad(function() {
            updateMultipleForSelect(dojo.byId("<%= REGIONS %>"));
        });

        dojo.declare("Calendar", com.aplana.dijit.ext.SimpleCalendar, {
            getEmployeeId: getEmployeeId
        });

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass: "Calendar"
            <sec:authorize access="not hasRole('ROLE_ADMIN')">
            , isDisabledDate: function(date) {
                return (date <= new Date());
            }
            </sec:authorize>
        });

        dojo.ready(function() {
            window.focus();
            divisionChangeVac(dojo.byId("<%= DIVISION_ID %>").value);
            dojo.byId("<%= REGIONS %>").value = ${regionId};
            if (dojo.byId("<%= REGIONS %>").value != -1){
                sortEmployee();
                selectedAllRegion = false;
            }else{
                sortEmployeeFull();
                selectedAllRegion = true;
            }

            dojo.byId("<%= EMPLOYEE_ID %>").value = ${employeeId};
            dojo.byId("<%= VACATION_ID %>").setAttribute("disabled", "disabled");
        });

        var employeeList = ${employeeListWithRegAndManJson};
        var regionsIdList = ${regionsIdList};
        var managerList = ${managerListJson};
        var selectedAllRegion = null;
        var selectedEmployee = ${employeeId};

        function showVacations() {
            var calFromDate = dojo.byId("<%= CAL_FROM_DATE %>").value;
            var calToDate = dojo.byId("<%= CAL_TO_DATE %>").value;

            if (checkEmployeeData(divisionId, empId)) {

                dojo.byId("<%= VACATION_ID %>").setAttribute("disabled", "disabled");
                vacationsForm.action =
                        "<%=request.getContextPath()%>/vacations";
                vacationsForm.submit();
            }
        }

        function divisionChangeVac(obj) {
            var divisionId = null;

            if (obj.target == null) {
                divisionId = obj.value;
            }
            else {
                divisionId = obj.target.value;
            }
            sortManager();
            if (selectedAllRegion){
                sortEmployeeFull();
            }else{
                sortEmployee();
            }
        }

        function managerChange(obj) {
            var managerId = null;

            if (obj.target == null) {
                managerId = obj.value;
            }
            else {
                managerId = obj.target.value;
            }

            if (selectedAllRegion){
                sortEmployeeFull();
            }else{
                sortEmployee();
            }
        }

        function updateMultipleForSelect(select) {
            var allOptionIndex;

            var isAllOption = dojo.some(select.options, function(option, idx) {
                if (option.value == <%= ALL_VALUE %> && option.selected) {
                    allOptionIndex = idx;
                    return true;
                }

                return false;
            });

            if (isAllOption) {
                select.removeAttribute("multiple");
                select.selectedIndex = allOptionIndex;
                selectedAllRegion = true;
                sortEmployeeFull();
            } else {
                select.setAttribute("multiple", "multiple");
                selectedAllRegion = false;
                sortEmployee();
            }
        }

        function sortManager(){
            var divisionId = dojo.byId("<%= DIVISION_ID %>").value;
            var managerSelect = dojo.byId("<%= MANAGER_ID %>");
            var managerOption = null;

            managerSelect.options.length = 0;
            for (var i = 0; i < managerList.length; i++){
                if (managerList[i].divId == divisionId){
                    managerOption = dojo.doc.createElement("option");
                    dojo.attr(managerOption, {
                        value:managerList[i].id
                    });
                    managerOption.title = managerList[i].value;
                    managerOption.innerHTML = managerList[i].value;
                    managerSelect.appendChild(managerOption);
                }
            }
        }

        function sortEmployee(){
            var employeeSelect = dojo.byId("<%= EMPLOYEE_ID %>");
            var divisionId = dojo.byId("<%= DIVISION_ID %>").value;
            var employeeOption = null;
            var select = dojo.byId("<%= REGIONS %>");
            var managerId = dojo.byId("<%= MANAGER_ID %>").value;

            employeeSelect.options.length = 0;
            for (var i = 0; i < employeeList.length; i++) {
                if ((divisionId == employeeList[i].divId)
                        && ((employeeList[i].manId == managerId)
                        || (managerId == 0))){
                    for (var j = 0; j < regionsIdList.length; j++){
                        var selected = dojo.some(select.options, function(option, idx){
                            if (option.value == regionsIdList[j] && option.selected){
                                if (regionsIdList[j] == employeeList[i].regId){
                                    for (var l = 0; l < employeeList[i].divEmps.length; l++) {
                                        if (employeeList[i].divEmps[l].id != 0) {
                                            if (managerId != 0){
                                                isNullManager(employeeList[i].divEmps[l], employeeOption, employeeSelect);
                                            }else{
                                                employeeOption = dojo.doc.createElement("option");
                                                dojo.attr(employeeOption, {
                                                    value:employeeList[i].divEmps[l].id
                                                });
                                                employeeOption.title = employeeList[i].divEmps[l].value;
                                                employeeOption.innerHTML = employeeList[i].divEmps[l].value;
                                                employeeSelect.appendChild(employeeOption);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
            sortSelect(employeeSelect);
            if (selectCurrentEmployee(employeeSelect)){
                dojo.byId("<%= EMPLOYEE_ID %>").value = selectedEmployee;
            }else{
                dojo.byId("<%= EMPLOYEE_ID %>").value = -1;
            }
        }

        function sortEmployeeFull(){
            var employeeSelect = dojo.byId("<%= EMPLOYEE_ID %>");
            var divisionId = dojo.byId("<%= DIVISION_ID %>").value;
            var employeeOption = null;
            var managerId = dojo.byId("<%= MANAGER_ID %>").value;

            employeeSelect.options.length = 0;
            for (var i = 0; i < employeeList.length; i++) {
                if ((divisionId == employeeList[i].divId)
                        && ((employeeList[i].manId == managerId)
                        || (managerId == 0))){
                    for (var l = 0; l < employeeList[i].divEmps.length; l++) {
                        if (employeeList[i].divEmps[l].id != 0) {
                            if (managerId != 0){
                                isNullManager(employeeList[i].divEmps[l], employeeOption, employeeSelect);
                            }else{
                                employeeOption = dojo.doc.createElement("option");
                                dojo.attr(employeeOption, {
                                    value:employeeList[i].divEmps[l].id
                                });
                                employeeOption.title = employeeList[i].divEmps[l].value;
                                employeeOption.innerHTML = employeeList[i].divEmps[l].value;
                                employeeSelect.appendChild(employeeOption);
                            }
                        }
                    }
                }
            }
            sortSelect(employeeSelect);
            if (selectCurrentEmployee(employeeSelect)){
                dojo.byId("<%= EMPLOYEE_ID %>").value = selectedEmployee;
            }else{
                dojo.byId("<%= EMPLOYEE_ID %>").value = -1;
            }
        }

        function selectCurrentEmployee(employeeSelect){
            for (var i = 0; i < employeeSelect.options.length; i++){
                if (employeeSelect[i].value == selectedEmployee){
                    return true;
                }
            }
            return false;
        }

        function isNullManager(employee, employeeOption, employeeSelect){
            employeeOption = dojo.doc.createElement("option");
            dojo.attr(employeeOption, {
                value:employee.id
            });
            employeeOption.title = employee.value;
            employeeOption.innerHTML = employee.value;
            employeeSelect.appendChild(employeeOption);
            for (var i=0; i < employeeList.length; i++){
                if (employee.id == employeeList[i].manId){
                    for (var l=0; l < employeeList[i].divEmps.length; l++){
                        if (employeeList[i].divEmps[l].id != 0) {
                            isNullManager(employeeList[i].divEmps[l], employeeOption, employeeSelect);
                        }
                    }
                }
            }
        }

        function changeSelectedEmployee(){
            selectedEmployee = dojo.byId("<%= EMPLOYEE_ID %>").value;
        }

        function createVacation() {
            var divisionId = dojo.byId("<%= DIVISION_ID %>").value;
            var empId = dojo.byId("<%= EMPLOYEE_ID %>").value;

            if (checkEmployeeData(divisionId, empId)) {
                vacationsForm.action =
                        "<%=request.getContextPath()%>/createVacation/" + empId;
                vacationsForm.submit();
            }
        }

        function deleteVacation(parentElement, vac_id) {
            var empId = dojo.byId("<%= EMPLOYEE_ID %>").value;
            var divisionId = dojo.byId("<%= DIVISION_ID %>").value;

            if (!confirm("Удалить заявку?")) {
                return;
            }

            dojo.byId("vacationId").removeAttribute("disabled");
            dojo.byId("vacationId").value = vac_id;
            vacationsForm.action =
                    "<%=request.getContextPath()%>/vacations";
            vacationsForm.submit();
        }

        /* Добавляет в указанный select пустой option. */
        function insertAllInclusiveOption(select) {
            var option = dojo.doc.createElement("option");
            dojo.attr(option, {
                value:"-1"
            });
            option.innerHTML = "Все сотрудники";
            select.appendChild(option);
        }

        /* Сортирует по алфавиту содержимое выпадающих списков. */
        function sortSelect(select) {
            var tmpArray = [];
            for (var i = 0; i < select.options.length; i++) {
                tmpArray.push(select.options[i]);
            }
            tmpArray.sort(function (a, b) {
                return (a.text < b.text) ? -1 : 1;
            });
            select.options.length = 0;
            insertAllInclusiveOption(select);
            for (var i = 0; i < tmpArray.length; i++) {
                select.options[i+1] = tmpArray[i];
            }
        }
    </script>
</head>
<body>

<h1><fmt:message key="title.vacations"/></h1>
<br/>

<form:form method="post" commandName="vacationsForm" name="mainForm">
    <form:hidden path="<%= VACATION_ID%>" />
    <table class="without_borders">
        <colgroup>
            <col width="130" />
            <col width="320" />
        </colgroup>
        <tr>
            <td>
                <span class="label">Подразделение:</span>
            </td>
            <td>
                <form:select path="<%= DIVISION_ID %>" id="<%= DIVISION_ID %>" onchange="divisionChangeVac(this)" class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:option label="" value="0"/>
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <span class="label">Руководитель:</span>
            </td>
            <td>
                <form:select path="<%= MANAGER_ID %>" id="<%= MANAGER_ID %>" onChange="managerChange(this)" class="without_dojo"
                             onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                    <form:option label="Не выбран" value="0"/>
                    <form:options items="${managerList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Регионы:</span>
            </td>
            <td>
                <form:select path="<%= REGIONS %>" onmouseover="showTooltip(this)" size="5"
                             onmouseout="tooltip.hide()" multiple="true" onchange="updateMultipleForSelect(this)">
                    <form:option value="<%= ALL_VALUE %>" label="Все регионы" />
                    <form:options items="${regionList}" itemLabel="name" itemValue="id" />
                </form:select>
            </td>
            <td>
                <span class="label">Сотрудник:</span>
            </td>
            <td>
                <form:select path="<%= EMPLOYEE_ID %>" id="<%= EMPLOYEE_ID %>" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                             onmouseout="tooltip.hide();" onChange="changeSelectedEmployee()">
                    <form:option items="${employeeList}" label="" value="0"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>
                <span class="label">Начало периода</span>
            </td>
            <td>
                <form:input path="<%= CAL_FROM_DATE %>" id="<%= CAL_FROM_DATE %>" class="date_picker" data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();" />
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Окончание периода</span>
            </td>
            <td>
                <form:input path="<%= CAL_TO_DATE %>" id="<%= CAL_TO_DATE %>" class="date_picker" data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"  />
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Тип отпуска:</span>
            </td>
            <td>
                <form:select path="<%= VACATION_TYPE %>" id="<%= VACATION_TYPE %>" onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false" size="1">
                    <form:option value="0" label="Все" />
                    <form:options items="${vacationTypes}" itemLabel="value" itemValue="id" />
                </form:select>
            </td>
        </tr>
    </table>
    <button id="show" style="width:150px" style="vertical-align: middle" type="submit"
            onclick="showVacations()">Показать</button>

    <br/><br/>

    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" />
</form:form>
<br/>
<table id="vacations">
    <thead>
    <tr>
        <th width="15" class="create-button">
            <img src="<c:url value="/resources/img/add.gif"/>" title="Создать" onclick="createVacation();"/>
        </th>
        <th width="160">Статус</th>
        <th width="220">Тип отпуска</th>
        <th width="120">Дата с</th>
        <th width="120">Дата по</th>
        <th width="130">Кол-во календарных дней</th>
        <th width="130">Кол-во рабочих дней</th>
        <th width="270">Комментарий</th>
        <th width="270">Сотрудник</th>
        <th width="200">Центр</th>
        <th width="120">Регион</th>
    </tr>
    </thead>
    <tbody>
    <c:choose>
    <c:when test="${fn:length(vacationsList) == 0}">
        <tr>
            <td colspan="11">Нет ни одного заявления на отпуск, удовлетворяющего выбранным параметрам</td>
        </tr>
    </tbody>
    </c:when>
    <c:otherwise>
    <c:forEach var="vacation" items="${vacationsList}" varStatus="lp">
        <tr>
            <td>
                <sec:authorize access="
                    hasRole('ROLE_ADMIN') or
                    ${
                        (vacation.employee.id eq curEmployee.id) or
                        (vacation.author.id eq curEmployee.id)
                    }
                ">
                    <div class="delete-button">
                        <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                             onclick="deleteVacation(this.parentElement, ${vacation.id});" />
                    </div>
                </sec:authorize>
            </td>
            <td class="centered">
                ${vacation.status.value}
                <c:if test="${fn:length(vacation.vacationApprovals) > 0}">
                <div data-dojo-type="dijit/TitlePane" data-dojo-props="title: 'Согласующие', open: false"
                     style="margin: 3px; padding: 0;">
                    <table class="centered">
                        <c:forEach var="va" items="${vacation.vacationApprovals}">
                        <tr>
                            <td>${va.manager.name}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${va.result}">
                                        Согласовано
                                        <br>
                                        <fmt:formatDate value="${va.responseDate}" pattern="dd.MM.yyyy" />
                                    </c:when>
                                    <c:when test="${!va.result && va.result != null}">
                                        Отклонено
                                        <br>
                                        <fmt:formatDate value="${va.responseDate}" pattern="dd.MM.yyyy" />
                                    </c:when>
                                    <c:when test="${va.manager.id == curEmployee.id}">
                                        <a href="<%= request.getContextPath() %>/vacation_approval?uid=${va.uid}" target="blank">
                                                Ожидается Ваше согласование</a>
                                    </c:when>
                                    <c:otherwise>
                                        Запрос отправлен<br>
                                           <fmt:formatDate value="${va.requestDate}" pattern="dd.MM.yyyy" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        </c:forEach>
                    </table>
                </div>
                </c:if>
            </td>
            <td class="centered">${vacation.type.value}</td>
            <td class="date"><fmt:formatDate value="${vacation.beginDate}" pattern="dd.MM.yyyy"/></td>
            <td class="date"><fmt:formatDate value="${vacation.endDate}" pattern="dd.MM.yyyy"/></td>
            <td class="centered">${calDays[lp.index]}</td>
            <td class="centered">${workDays[lp.index]}</td>
            <td>
                ${vacation.comment}
                <c:if test="${vacation.author.id ne vacation.employee.id}">
                    <c:if test="${fn:length(vacation.comment) != 0}"><br/></br></c:if>
                    Заявка создана сотрудником ${vacation.author.name}
                </c:if>
            </td>
            <td class="centered">${vacation.employee.name}</td>
            <td class="centered">${vacation.employee.division.name}</td>
            <td class="centered">${vacation.employee.region.name}</td>
        </tr>
    </c:forEach>
    </tbody>
    <tfoot>
        <tr class="summary">
            <td colspan="3">Кол-во утвержденных заявлений на отпуск</td>
            <td colspan="1">${summaryApproved}</td>
        </tr>
        <tr class="summary">
            <td colspan="3">Кол-во отклоненных заявлений на отпуск</td>
            <td colspan="1">${summaryRejected}</td>
        </tr>
        <tr>
            <td colspan="4" class="centered">
                <c:choose>
                    <c:when test="${employeeId != -1}">
                        <div data-dojo-type="dijit/TitlePane" data-dojo-props="title: 'Кол-во дней отпуска за период', open: false"
                             style="margin: 3px; padding: 0;">
                            <table class="centered">
                                <thead>
                                <tr>
                                    <th width="170">Год</th>
                                    <th width="170">Календарные дни</th>
                                    <th width="170">Рабочие дни</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="cal" items="${calDaysCount}">
                                        <tr>
                                            <td>${cal.year}</td>
                                            <td>${cal.summaryCalDays}</td>
                                            <td>${cal.summaryWorkDays}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                </c:choose>
            </td>
        </tr>
    </tfoot>
    </c:otherwise>
    </c:choose>
</table>
</body>
</html>