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
            reloadViewReportsState();
            <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">
                dojo.byId("divisionId").value = ${divisionId};
                divisionChange(dojo.byId("divisionId"));
                dojo.byId("employeeId").value = ${employeeId};
            </sec:authorize>
        });

        dojo.require("dijit.form.DateTextBox");
        dojo.require("dojo.NodeList-traverse");

        var monthList = ${monthList};
        var employeeList = ${employeeListJson};

        /* По умолчанию отображается текущий год и месяц. */
        function reloadViewReportsState() {
            var temp_date = new Date();
            var lastYear = ${year};
            var lastMonth = ${month};
            if (lastYear == 0 && lastMonth == 0) {
                dojo.byId("year").value = temp_date.getFullYear();
                dojo.byId("year").onchange();
                dojo.byId("month").value = temp_date.getMonth() + 1;
            }
            else {
                dojo.byId("year").value = lastYear;
                dojo.byId("year").onchange();
                dojo.byId("month").value = lastMonth;
            }
        }

        function showBusinessTripsAndIllnessReport() {
            var empId = ${employeeId};
            var divisionId = ${divisionId};

            <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">
                var empId = dojo.byId("employeeId").value;
                var divisionId = dojo.byId("divisionId").value;
            </sec:authorize>


            var year = dojo.byId("year").value;
            var month = dojo.byId("month").value;
            if (year != null && year != 0 && month != null && month != 0 && divisionId != null && divisionId != 0 && empId != null && empId != 0) {
                businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillness/" + divisionId + "/" + empId + "/" + year + "/" + month;
                businesstripsandillness.submit();
            } else {
                var error = "";
                if (year == 0 || year == null) {
                    error += ("Необходимо выбрать год и месяц\n");
                }
                else if (month == 0 || month == null) {
                    error += ("Необходимо увыбрать месяц!\n");
                }
                if (divisionId == 0 || divisionId == null) {
                    error += ("Необходимо выбрать подразделение и сотрудника!\n");
                }
                else if (empId == 0 || empId == null) {
                    error += ("Необходимо выбрать сотрудника!\n");
                }
                alert(error);
            }
        }

        function createBusinessTripOrIllness() {
            var empId = ${employeeId};
            <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">
                var empId = dojo.byId("employeeId").value;
            </sec:authorize>
            if (empId != null && empId != 0) {
                businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/" + empId;
                businesstripsandillness.submit();
            } else {
                error += ("Необходимо выбрать сотрудника!\n");
            }
        }

        function yearChange(obj) {
            var year = null;
            var monthSelect = dojo.byId("month");
            var monthOption = null;
            if (obj.target == null) {
                year = obj.value;
            }
            else {
                year = obj.target.value;
            }
            //Очищаем список месяцев.
            monthSelect.options.length = 0;
            for (var i = 0; i < monthList.length; i++) {
                if (year == monthList[i].year) {
                    insertEmptyOption(monthSelect);
                    for (var j = 0; j < monthList[i].months.length; j++) {
                        if (monthList[i].months[j].number != 0 && monthList[i].months[j].number != 27) {
                            monthOption = dojo.doc.createElement("option");
                            dojo.attr(monthOption, {value:monthList[i].months[j].number});
                            monthOption.title = monthList[i].months[j].name;
                            monthOption.innerHTML = monthList[i].months[j].name;
                            monthSelect.appendChild(monthOption);
                        }
                    }
                }
            }
            if (year == 0) {
                insertEmptyOption(monthSelect);
            }
        }

        function deleteReport(parentElement, rep_id, calendarDays, workingDays, workDaysOnIllnessWorked){
            if (!confirm("Удалить заявку?")) {
                return;
            }

            var prevHtml = parentElement.innerHTML;

            dojo.addClass(parentElement, "activity-indicator");
            parentElement.innerHTML =
                    "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

            function handleError(error) {
                resetParent();

                alert("Не удалось удалить заявку:\n\n" + error);
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
                decreaseResultDays(document.getElementById("mounthCalendarDaysInBusinessTrip"), calendarDays);
                decreaseResultDays(document.getElementById("mounthWorkDaysOnBusinessTrip"), workingDays);
            }
        }
        function decreaseResultDays(cellWithResults, daysToDecrease){
            var daysInTable = parseFloat(cellWithResults.innerHTML);
            var recountedDays = daysInTable - daysToDecrease;
            cellWithResults.innerHTML = recountedDays;
        }
        function recountIllness(calendarDays, workingDays, workDaysOnIllnessWorked){
            if (${fn:length(reports.periodicalsList) > 0}){
                decreaseResultDays(document.getElementById("mounthCalendarDaysOnIllness"), calendarDays);
                decreaseResultDays(document.getElementById("mounthWorkDaysOnIllness"), workingDays);
                decreaseResultDays(document.getElementById("mounthWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
            }
            decreaseResultDays(document.getElementById("yearWorkDaysOnIllness"), workingDays);
            decreaseResultDays(document.getElementById("yearWorkDaysOnIllnessWorked"), workDaysOnIllnessWorked);
        }
        function editReport(reportId){
            businesstripsandillness.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/" + reportId + "/" + ${reportFormed};
            businesstripsandillness.submit();
        }
    </script>
</head>
<body>
    <h1><fmt:message key="businesstripsandillness"/></h1>
    <br/>
    <form:form method="post" commandName="businesstripsandillness" name="mainForm">

        <sec:authorize access="hasAnyRole('VIEW_ILLNESS_BUSINESS_TRIP', 'CHANGE_ILLNESS_BUSINESS_TRIP')">

            <span class="label">Подразделение</span>
            <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                <form:option label="" value="0"/>
                <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
            </form:select>

            <span class="label">Отчет сотрудника</span>
            <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                         onmouseout="tooltip.hide();" onchange="setDefaultEmployeeJob(-1);">
                <form:option items="${employeeList}" label="" value="0"/>
            </form:select>
            <br><br>

       </sec:authorize>
        Год:
        <form:select path="year" id="year" class="without_dojo" onchange="yearChange(this)"
                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options items="${yearsList}" itemLabel="year" itemValue="year"/>
        </form:select>
        Месяц:
        <form:select path="month" id="month" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                     onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
        </form:select>

        <br><br>

        Тип отчета:
        <div class="radiobuttons">
            <form:select path="reportType" id="reportType" onMouseOver="tooltip.show(getTitle(this));"
                         onMouseOut="tooltip.hide();" multiple="false">
                <form:options items="${businesstripsandillness.reportTypes}" itemLabel="name" itemValue="id" required="true"/>
            </form:select>
        </div>

        <br><br>

        <button id="show" class="button" type="button" onclick="showBusinessTripsAndIllnessReport()">
            Сформировать
        </button>
        <button id="create" class="button" type="button" onclick="createBusinessTripOrIllness()">
            Создать
        </button>

        <br><br>

    </form:form>
        <c:choose>
            <c:when test="${reportFormed == -1}">
                <b>${exceptionMessage}</b>
            </c:when>
            <%--отчет по командировкам--%>
            <c:when test="${reportFormed == 7}">
                <c:choose>
                    <c:when test="${fn:length(reports.periodicalsList) > 0}">
                        <table id="businesstripreport">
                            <thead>
                            <tr>
                                <c:if test="${recipientPermission.id == 5}">
                                    <th class="tight"/>
                                    <th class="tight"/>
                                </c:if>
                                <th width="120">Дата с</th>
                                <th width="120">Дата по</th>
                                <th width="160">Кол-во календарных дней</th>
                                <th width="160">Кол-во рабочих дней</th>
                                <th width="160">Проектная/внепроектная</th>
                                <th width="200">Комментарий</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="report" items="${reports.periodicalsList}">
                                <tr>
                                    <c:if test="${recipientPermission.id == 5}">
                                        <td>
                                            <div class="iconbutton">
                                                <img src="<c:url value="/resources/img/edit.png"/>" title="Редактировать"
                                                     onclick="editReport(${report.id});" />
                                            </div>
                                        </td>
                                        <td>
                                            <div class="iconbutton">
                                                <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                                     onclick="deleteReport(this.parentElement, ${report.id}, ${report.calendarDays}, ${report.workingDays});" />
                                            </div>
                                        </td>
                                    </c:if>
                                    <td><fmt:formatDate value="${report.beginDate}" pattern="dd.MM.yyyy"/></td>
                                    <td><fmt:formatDate value="${report.endDate}" pattern="dd.MM.yyyy"/></td>
                                    <td>${report.calendarDays}</td>
                                    <td>${report.workingDays}</td>
                                    <td>${report.type.value}</td>
                                    <td>${report.comment}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
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
                    </c:when>
                    <c:otherwise>
                        <span class="bold">В выбранном месяце сотрудник в командировках небыл!</span>
                    </c:otherwise>
                </c:choose>

            </c:when>
            <%--отчет по больничным--%>
            <c:when test="${reportFormed == 6}">
                <c:choose>
                    <c:when test="${fn:length(reports.periodicalsList) > 0}">
                        <table id="illness">
                            <thead>
                                <tr>
                                    <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                                        <th class="tight"/>
                                        <th class="tight"/>
                                    </sec:authorize>
                                    <th width="120">Дата с</th>
                                    <th width="120">Дата по</th>
                                    <th width="160">Кол-во календарных дней</th>
                                    <th width="160">Кол-во рабочих дней</th>
                                    <th width="160">Основание</th>
                                    <th width="200">Комментарий</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="report" items="${reports.periodicalsList}">
                                    <tr>
                                        <sec:authorize access="hasRole('CHANGE_ILLNESS_BUSINESS_TRIP')">
                                            <td>
                                                <div class="iconbutton">
                                                    <img src="<c:url value="/resources/img/edit.png"/>" title="Редактировать"
                                                         onclick="editReport(${report.id});" />
                                                </div>
                                            </td>
                                            <td>
                                                <div class="iconbutton">
                                                    <img src="<c:url value="/resources/img/delete.png"/>" title="Удалить"
                                                         onclick="deleteReport(this.parentElement, ${report.id}, ${report.calendarDays}, ${report.workingDays}, ${report.workDaysOnIllnessWorked});" />
                                                </div>
                                            </td>
                                        </sec:authorize>
                                        <td><fmt:formatDate value="${report.beginDate}" pattern="dd.MM.yyyy"/></td>
                                        <td><fmt:formatDate value="${report.endDate}" pattern="dd.MM.yyyy"/></td>
                                        <td>${report.calendarDays}</td>
                                        <td>${report.workingDays}</td>
                                        <td>${report.reason.value}</td>
                                        <td>${report.comment}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                </c:choose>
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

        </c:choose>
    </table>
</body>
</html>