<%@ page import="com.aplana.timesheet.controller.BusinessTripsAndIllnessController" %>
<%@ page import="static com.aplana.timesheet.form.BusinessTripsAndIllnessForm.*" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title><fmt:message key="businesstripsandillness"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/businesstripsandillness.css">
    <script type="text/javascript">

//        типы отчетов
        var illnessReportType = 6;
        var businessTripReportType = 7;

        dojo.ready(function () {
            window.focus();
            <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">
                dojo.byId("divisionId").value = ${divisionId};
                divisionChanged(dojo.byId("divisionId").value);
                updateManagerList(dojo.byId('divisionId').value);
                dojo.byId("employeeId").value = ${employeeId};
                dojo.byId("manager").value = ${managerId};
            </sec:authorize>
            initRegionsList();
        });

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass:"dijit.Calendar"
        });

        dojo.require("dijit.form.DateTextBox");
        dojo.require("dojo.NodeList-traverse");

        var employeeList = ${employeeListJson};
        var forAll = ${forAll};

        //устанавливается значение по умолчанию "Все регионы"
        function initRegionsList(){
            var regions = ${regionIds};
            var regionsSelect = dojo.byId("regions");
            if (regions.length == 1) {
                if (regions[0] == <%= ALL_VALUE %>) {
                    regionsSelect[0].selected = true;
                }
            }

        }

        function showBusinessTripsAndIllnessReport() {
            var empId = ${employeeId};
            var divisionId = ${divisionId};
            var regions = dojo.byId("regions").value;
            var manager = dojo.byId("manager").value;

            <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">
                var empId = dojo.byId("employeeId").value;
                var divisionId = dojo.byId("divisionId").value;
            </sec:authorize>

            var dateFrom = dojo.byId("dateFrom").value;
            var dateTo = dojo.byId("dateTo").value

            var datesValid = false;

            var regionsValid = getSelectedIndexes(dojo.byId("regions")).length > 0;

            datesValid = dateFrom != null && dateTo != null && (dateFrom <= dateFrom);

            if (datesValid && divisionId != null && divisionId != 0 && empId != null && empId != 0 && regionsValid) {
                businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillness/"
                        + divisionId + "/" + empId;
                businesstripsandillness.submit();
            } else {
                var error = "";
               if (dateFrom == null) {
                    error += ("Необходимо выбрать дату начало периода!!\n");
                }

                 if (dateTo == null) {
                    error += ("Необходимо выбрать дату окончания периода!\n");
                }

                if (dateFrom > dateTo) {
                    error += ("Дата окончания периода должна быть больше даты начала периода!\n");
                }

                if (divisionId == 0 || divisionId == null) {
                    error += ("Необходимо выбрать подразделение и сотрудника!\n");
                }
                else if (empId == 0 || empId == null) {
                    error += ("Необходимо выбрать сотрудника!\n");
                }
                if (!regionsValid) {
                    error += ("Необходимо выбрать регион или несколько регионов!\n");
                }

                alert(error);

            }
        }

        function createBusinessTripOrIllness() {
            var empId = dojo.byId("employeeId").value;

            if (empId != null && empId != 0) {
                businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/" + empId;
                businesstripsandillness.submit();
            } else {
                alert("Необходимо выбрать сотрудника!\n");
            }
        }

        function deleteReport(parentElement, rep_id, calendarDays, workingDays, workDaysOnIllnessWorked){
            if (!confirm("Подтвердите удаление!")) {
                return;
            }

            var prevHtml = parentElement.innerHTML;

            dojo.addClass(parentElement, "activity-indicator");
            parentElement.innerHTML =
                    "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

            function handleError(error) {
                resetParent();

                alert("Удаление не произошло:\n\n" + error);
            }

            function resetParent() {
                dojo.removeClass(parentElement, "activity-indicator");
                parentElement.innerHTML = prevHtml;
            }

            dojo.xhrGet({
                url: "<%= request.getContextPath()%>/businesstripsandillness/delete/" + rep_id + "/" + ${reportFormed},
                handleAs: "text",

                load: function(data) {
                    if (data.length == 0) {
                        dojo.destroy(dojo.NodeList(parentElement).parents("tr")[0]);
                        recountResults(calendarDays, workingDays, workDaysOnIllnessWorked);
                    } else {
                        handleError(data);
                    }
                },

                error: function(error) {
                    handleError(error.message);
                }
            });
        }
        function recountResults(calendarDays, workingDays, workDaysOnIllnessWorked){
            if (${reportFormed == 6}){
                recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked);
            }
            if (${reportFormed == 7}){
                if(!forAll){
                    decreaseResultDays(document.getElementById("mounthCalendarDaysInBusinessTrip"), calendarDays);
                    decreaseResultDays(document.getElementById("mounthWorkDaysOnBusinessTrip"), workingDays);
                }
            }
        }
        function decreaseResultDays(cellWithResults, daysToDecrease){
            var daysInTable = parseFloat(cellWithResults.innerHTML);
            var recountedDays = daysInTable - daysToDecrease;
            cellWithResults.innerHTML = recountedDays;
        }
        function recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked){
            if(!forAll){
              if (${fn:length(reports.periodicalsList) > 0}){
                  decreaseResultDays(document.getElementById("mounthCalendarDaysOnIllness"), calendarDays);
                  decreaseResultDays(document.getElementById("mounthWorkDaysOnIllness"), workingDays);
                  decreaseResultDays(document.getElementById("mounthWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
              }
             decreaseResultDays(document.getElementById("yearWorkDaysOnIllness"), workingDays);
             decreaseResultDays(document.getElementById("yearWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
            }
        }
        function editReport(reportId){
            businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/" + reportId + "/" + ${reportFormed};
            businesstripsandillness.submit();
        }

        function updateManagerList(id) {
            var managersNode = dojo.byId("manager");
            var emptyOption = managersNode.options[0];
            var manager = managersNode.value;

            managersNode.options.length = 0;
            managersNode.add(emptyOption);

            var managerMapJson = '${managerMapJson}';
            var count = 0;
            if (managerMapJson.length > 0) {
                var managerMap = dojo.fromJson(managerMapJson);
                dojo.filter(managerMap,function (m) {
                    return (m.division == id);
                }).forEach(function (managerData) {
                            var option = document.createElement("option");
                            option.text = managerData.name;
                            option.value = managerData.id;

//                            if (managerData.number == manager) {
//                                option.selected = "selected";
//                            }
                            managersNode.add(option);
                        });
            }
            if (managersNode.options.length == 1 && emptyOption.value == managersNode.options[0].value){
                dojo.byId("manager").disabled = 'disabled';
            } else {
                dojo.byId("manager").disabled = '';
            }
        }

        function updateMultipleForSelect(select) {
            var allOptionIndex;
            var isAllOption = dojo.some(select.options, function (option, idx) {
                if (option.value == <%= ALL_VALUE %> && option.selected) {
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
        }

        function getSelectedIndexes (multiselect)
        {
            var arrIndexes = new Array;
            for (var i=0; i < multiselect.options.length; i++)
            {
                if (multiselect.options[i].selected) arrIndexes.push(i);
            }
            return arrIndexes;
        };

        function managerChange(manager) {
            var employeeSelect = dojo.byId("employeeId");
            var divisionSelectValue = dojo.byId("divisionId").value;
            var selectedEmployee = employeeSelect.value;
            employeeSelect.options.length = 0;
            var allOption = document.createElement("option");
            allOption.text ="Все сотрудники";
            allOption.value = <%= ALL_VALUE %>;
            employeeSelect.add(allOption);
            employeeSelect.options[0].selected= "selected";
            var employeeMapJson = '${employeeListJson}';
            var count = 0;
            if (employeeMapJson.length > 0) {
                var employeeMap = dojo.fromJson(employeeMapJson);
                var filteredEmpMap = dojo.filter(employeeMap,function (m) {
                    return (m.divId == divisionSelectValue);
                }).forEach(function (divEmps) {
                            divEmps.divEmps.forEach(function (empData) {
                                if (empData.manId == manager || manager == 0) {
                                    var option = document.createElement("option");
                                    option.text = empData.value;
                                    option.value = empData.id;
                                    if (empData.number == selectedEmployee) {
                                        option.selected = "selected";
                                    }
                                    employeeSelect.add(option);
                                }
                            })
                        });
            }
        }
        function divisionChanged(division) {
            updateManagerList(division);
            dojo.byId("manager").onchange();
        }
    </script>
</head>
<body>
    <h1><fmt:message key="businesstripsandillness"/></h1>
    <br>
    <form:form method="post" commandName="businesstripsandillness" name="mainForm">
    <table class="no_border" style="margin-bottom: 20px;">
        <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">
            <tr>
                <td>
                    <div class="horizontalBlock labelDiv">
                    <span class="lowspace">Подразделение:</span>
                    </div>
                </td>
                <td>
                    <form:select path="divisionId" id="divisionId" cssClass="date_picker"
                                 onchange="divisionChanged(this.value);updateManagerList(this.value)" class="without_dojo"
                                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                </td>
                <td rowspan="5" style="padding: 5px;vertical-align: top;">
                    <span class="label">Регионы:</span>
                </td>
                <td rowspan="5" style="padding: 5px;vertical-align: top;">
                    <form:select path="regions" onmouseover="showTooltip(this)" size="6"
                                 onmouseout="tooltip.hide()" multiple="true"
                                 onchange="updateMultipleForSelect(this)">
                        <form:option value="<%= ALL_VALUE %>" label="Все регионы"/>
                        <form:options items="${regionList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                </td>
            </tr>
            <tr>
                <td>
                    <span class="lowspace">Руководитель:</span>
                </td>
                <td>
                    <form:select path="manager" class="without_dojo"
                                 onchange="managerChange(this.value)" onmouseover="showTooltip(this);"
                                 onmouseout="tooltip.hide();" multiple="false">
                        <form:option label="Все руководители" value="0"/>
                        <form:options items="${managerList}" itemLabel="name" itemValue="id"/>
                    </form:select>
                </td>

            </tr>
            <tr>
                <td>
                    <span class="lowspace">Сотрудник:</span>
                </td>
                <td>
                    <form:select path="employeeId" id="employeeId" class="without_dojo" cssClass="date_picker"
                                 onmouseover="tooltip.show(getTitle(this));"
                                 onmouseout="tooltip.hide();">
                    </form:select>
                </td>
            </tr>
        </sec:authorize>
        <tr>
            <td colspan="4">

                <div class="horizontalBlock" style="width: 151px; margin-top: 5px;">
                    <span class="lowspace">Начало периода:</span>
                </div>
                <div class="horizontalBlock">
                    <form:input path="dateFrom" id="dateFrom" class="date_picker"
                                data-dojo-type="DateTextBox" required="true"
                                onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>

                </div>
            </td>
        </tr>
        <tr>
            <td colspan="4">
                <div class="horizontalBlock" style="width: 151px; margin-top: 5px;">
                    <span class="lowspace">Окончание периода:</span>
                </div>
                <div class="horizontalBlock">
                    <form:input path="dateTo" id="dateTo" class="date_picker"
                                data-dojo-type="DateTextBox" required="true"
                                onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"/>
                </div>

            </td>
        </tr>
        <tr>
            <td colspan="2">
                <div class="floatleft lowspace" style="width: 50px">
                    <span>Тип:</span>
                </div>
                <div class="floatleft">
                    <form:select path="reportType" id="reportType" onMouseOver="tooltip.show(getTitle(this));"
                                 onMouseOut="tooltip.hide();" multiple="false" cssClass="date_picker">
                        <form:options items="${businesstripsandillness.reportTypes}" itemLabel="name" itemValue="id"
                                      required="true"/>
                    </form:select>
                </div>
            </td>
            <td colspan="3">
                <div class="floatleft lowspace">
                    <button id="show" class="butt block " onclick="showBusinessTripsAndIllnessReport()">
                        Показать
                    </button>
                </div>
            </td>
        </tr>
    </table>

    <%------------------------------TABLE-----------------------------------%>

        <table id="reporttable">
            <thead>
                <tr>
                    <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                    <c:choose>
                         <c:when test="${forAll!=true}">
                          <th width="15" class="iconbutton">
                            <img src="<c:url value="/resources/img/add.gif"/>" title="Создать" onclick="createBusinessTripOrIllness();"/>
                          </th>
                         </c:when>
                         <c:otherwise>
                             <th class="tight"></th>
                         </c:otherwise>
                    </c:choose>
                    <th class="tight"></th>
                    <th class="tight"></th>
                    </sec:authorize>
                    <th width="200">Сотрудник</th>
                    <th width="200">Центр</th>
                    <th width="200">Регион</th>
                    <th width="100">Дата с</th>
                    <th width="100">Дата по</th>
                    <th width="100">Кол-во календарных дней</th>
                    <th width="100">Кол-во <br>рабочих дней</th>
                    <c:choose>
                        <c:when test="${reportFormed == 7}">
                            <th width="160">Проектная/внепроектная</th>
                        </c:when>
                        <c:when test="${reportFormed == 6}">
                            <th width="160">Основание</th>
                        </c:when>
                    </c:choose>
                    <th width="200">Комментарий</th>
                </tr>
            </thead>
            <c:if test="${not empty reportsMap}">
            <c:forEach var="employeeReport" items="${reportsMap}">
                <c:set var="reports" value="${employeeReport.value}"/>
                <c:choose>
                    <c:when test="${fn:length(reports.periodicalsList) > 0}">
                        <tbody>

                            <c:forEach var="report" items="${reports.periodicalsList}">
                                <tr>
                                    <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                                    <td></td>
                                    <td>
                                            <div class="iconbutton">
                                                <img src="<c:url value="/resources/img/edit.png"/>" title="Редактировать"
                                                     onclick="editReport(${report.id});" />
                                            </div>
                                        </td>
                                        <td>
                                            <div class="iconbutton">
                                                <c:choose>
                                                    <c:when test="${reportFormed == 6}">
                                                        <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                                             onclick="deleteReport(this.parentElement, ${report.id}, ${report.calendarDays}, ${report.workingDays}, ${report.workDaysOnIllnessWorked});" />
                                                    </c:when>
                                                    <c:when test="${reportFormed == 7}">
                                                        <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                                             onclick="deleteReport(this.parentElement, ${report.id}, ${report.calendarDays}, ${report.workingDays});" />
                                                    </c:when>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </sec:authorize>
                                    <td class="textcenter">${employeeReport.key.name}</td>
                                    <td class="textcenter">${employeeReport.key.division.name}</td>
                                    <td class="textcenter">${employeeReport.key.region.name}</td>
                                    <td class="textcenter"><fmt:formatDate value="${report.beginDate}" pattern="dd.MM.yyyy"/></td>
                                    <td class="textcenter"><fmt:formatDate value="${report.endDate}" pattern="dd.MM.yyyy"/></td>
                                    <td class="textcenter">${report.calendarDays}</td>
                                    <td class="textcenter">${report.workingDays}</td>
                                    <c:choose>
                                        <c:when test="${reportFormed == 6}">
                                            <td class="textcenter">${report.reason.value}</td>
                                        </c:when>
                                        <c:when test="${reportFormed == 7}">
                                            <td class="textcenter">
                                                ${report.type.value}
                                                    <c:if test="${report.project != null}">
                                                        (${report.project.name})
                                                    </c:if>
                                            </td>
                                        </c:when>
                                    </c:choose>
                                    <td>${report.comment}</td>
                                </tr>
                            </c:forEach>

                        </tbody>
                    </c:when>

                    <c:when test="${fn:length(reports.periodicalsList) == 0}">
                        <c:choose>
                            <c:when test="${reportFormed == 6}">
                                <tbody>
                                <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </sec:authorize>
                                <td class="textcenter">${employeeReport.key.name}</td>
                                <td colspan="8"><b>В выбранном месяце сотрудник на больничном не был!</b></td>
                                </tbody>
                            </c:when>
                            <c:when test="${reportFormed == 7}">
                                <tbody>
                                <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </sec:authorize>
                                <td class="textcenter">${employeeReport.key.name}</td>
                                <td colspan="8"><b>В выбранном месяце сотрудник в командировках не был!</b></td>
                                </tbody>
                            </c:when>
                        </c:choose>
                    </c:when>
                </c:choose>
            </c:forEach>
            </c:if>
            <c:if test="${empty reportsMap}">
                <tbody>
                <td></td>
                <td></td>
                <td></td>
                <td colspan="9">
                    <b>Не найдено данных по заданным параметрам,<br> вы можете создать
                        <c:choose>
                            <c:when test="${reportFormed == 6}">
                                больничный
                            </c:when>
                            <c:when test="${reportFormed == 7}">
                                командировку
                            </c:when>
                        </c:choose>
                        пройдя по <a href="#" onclick="createBusinessTripOrIllness();"> ссылке.</a>
                    </b>
                </td>
                </tbody>
            </c:if>
            </table>
            <c:choose>
                <c:when test="${forAll!=true}">
                    <c:choose>
                        <c:when test="${reportFormed == 6}">
                            <table id="illnessresult">
                                <thead>
                                <tr><td colspan="2" class="bold">Итоги за месяц:</td></tr>
                                <c:choose>
                                    <c:when test="${fn:length(reports.periodicalsList) > 0}">
                                        <tr>
                                            <td class="resultrow">Общее кол-во календарных дней болезни за месяц:</td>
                                            <td class="resultrow" id="mounthCalendarDaysOnIllness">${reports.mounthCalendarDays}</td>
                                        </tr>
                                        <tr>
                                            <td class="resultrow">Общее кол-во рабочих дней болезни за месяц:</td>
                                            <td class="resultrow" id="mounthWorkDaysOnIllness">${reports.mounthWorkDays}</td>
                                        </tr>
                                        <tr>
                                            <td class="resultrow">Общее кол-во рабочих дней болезни за месяц, когда сотрудник работал:</td>
                                            <td class="resultrow" id="mounthWorkDaysOnIllnessWorked"><fmt:formatNumber value="${reports.mounthWorkDaysOnIllnessWorked}" pattern="#.#"/></td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="bold"><tr><td colspan="2" class="bold">В выбранном месяце сотрудник не болел!</td></tr></span>
                                    </c:otherwise>
                                </c:choose>
                                <tr><td colspan="2" class="bold">Итоги за год:</td></tr>
                                <tr>
                                    <td class="resultrow">Общее кол-во рабочих дней болезни за год:</td>
                                    <td class="resultrow" id="yearWorkDaysOnIllness">${reports.yearWorkDaysOnIllness}</td>
                                </tr>
                                <tr>
                                    <td class="resultrow">Общее кол-во рабочих дней болезни за год , когда сотрудник работал:</td>
                                    <td class="resultrow" id="yearWorkDaysOnIllnessWorked"><fmt:formatNumber value="${reports.yearWorkDaysOnIllnessWorked}" pattern="#.#"/></td>
                                </tr>
                                </thead>
                            </table>
                        </c:when>

                        <c:when test="${reportFormed == 7}">
                            <c:if test="${fn:length(reports.periodicalsList) > 0}">
                                <table id="businesstripresult">
                                    <thead>
                                        <tr><td colspan="2" class="bold">Итоги за месяц:</td></tr>
                                        <tr>
                                            <td class="resultrow">Общее кол-во календарных дней в командировке за месяц:</td>
                                            <td class="resultrow" id="mounthCalendarDaysInBusinessTrip">${reports.mounthCalendarDays}</td>
                                        </tr>
                                        <tr>
                                            <td class="resultrow">Общее кол-во рабочих дней в командировке за месяц:</td>
                                            <td class="resultrow" id="mounthWorkDaysOnBusinessTrip">${reports.mounthWorkDays}</td>
                                        </tr>
                                    </thead>
                                </table>
                            </c:if>
                        </c:when>
                    </c:choose>
                </c:when>
            </c:choose>
    </form:form>
</body>
</html>