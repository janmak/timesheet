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

        dojo.ready(function () {
            window.focus();
            reloadViewReportsState();
            divisionChange(dojo.byId("divisionId"));
            dojo.byId("employeeId").value = ${employeeId};
        });

        dojo.require("dijit.form.DateTextBox");
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

        function showDates() {
            var empId = dojo.byId("employeeId").value;
            var year = dojo.byId("year").value;
            var divisionId = dojo.byId("divisionId").value;
            var month = dojo.byId("month").value;
            if (year != null && year != 0 && month != null && month != 0 && divisionId != null && divisionId != 0 && empId != null && empId != 0) {
                businessTripsAndIllnessForm.action = "<%=request.getContextPath()%>/businesstripsandillness/" + divisionId + "/" + empId + "/" + year + "/" + month;
                businessTripsAndIllnessForm.submit();
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
        function deleteTimeSheet(id) {
            if (confirm("Вы действительно хотите удалить отчет?")) {
                dojo.byId("commandURL").value = window.location;
                mainForm.action = "<%=request.getContextPath()%>/timesheetDel/" + id;
                mainForm.submit();
            }
        }

    </script>
</head>
<body>
    <h1><fmt:message key="businesstripsandillness"/></h1>
    <br/>
    <form:form method="post" commandName="businessTripsAndIllnessForm" name="mainForm">
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

        <form:radiobutton path="reporttype" value="1" cssClass="radiobuttons"/><fmt:message key="businesstripstype"/>
        <br>
        <form:radiobutton path="reporttype" value="2" cssClass="radiobuttons"/><fmt:message key="illnesstype"/>

        <br><br>

        <button id="show" style="width:150px" style="vertical-align: middle" type="button" onclick="showDates()">Сформировать
        </button>

        <br><br>

    </form:form>
    <table id="viewreports">
        <c:choose>
            <c:when test="${reportFormed == -1}">
                <b>${exceptionMessage}</b>
            </c:when>
            <%--отчет по командировкам--%>
            <c:when test="${reportFormed == 1}">
                <thead>
                <tr>
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
                    <tr class="statusNotCome">
                        <td class="date"><fmt:formatDate value="${report.beginDate}" pattern="dd.MM.yyyy"/></td>
                        <td class="date"><fmt:formatDate value="${report.endDate}" pattern="dd.MM.yyyy"/></td>
                        <td>${report.calendarDays}</td>
                        <td>${report.workingDays}</td>
                        <td>${report.type.value}</td>
                        <td>${report.comment}</td>
                    </tr>
                </c:forEach>
                </tbody>
                <thead>
                <tr>
                    <td colspan="3">Общее кол-во календарных дней в командировке за месяц:</td>
                    <td id="mounthCalendarDaysInBusinessTrip">${reports.mounthCalendarDays}</td>
                </tr>
                <tr>
                    <td colspan="3">Общее кол-во рабочих дней в командировке за месяц:</td>
                    <td id="mounthWorkDaysOnBusinessTrip">${reports.mounthWorkDays}</td>
                </tr>
                </thead>
            </c:when>
            <%--отчет по больничным--%>
            <c:when test="${reportFormed == 2}">
                <thead>
                    <tr>
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
                        <tr class="statusNotCome">
                            <td class="date"><fmt:formatDate value="${report.beginDate}" pattern="dd.MM.yyyy"/></td>
                            <td class="date"><fmt:formatDate value="${report.endDate}" pattern="dd.MM.yyyy"/></td>
                            <td>${report.calendarDays}</td>
                            <td>${report.workingDays}</td>
                            <td>${report.reason.value}</td>
                            <td>${report.comment}</td>
                        </tr>
                    </c:forEach>
                </tbody>
                <thead>
                <tr>
                    <td colspan="3">Общее кол-во календарных дней болезни за месяц:</td>
                    <td id="mounthCalendarDaysOnIllness">${reports.mounthCalendarDays}</td>
                </tr>
                <tr>
                    <td colspan="3">Общее кол-во рабочих дней болезни за месяц:</td>
                    <td id="mounthWorkDaysOnIllness">${reports.mounthWorkDays}</td>
                </tr>
                <tr>
                    <td colspan="3">Общее кол-во рабочих дней болезни за месяц, когда сотрудник работал:</td>
                    <td id="mounthWorkDaysOnIllnessWorked"><fmt:formatNumber value="${reports.mounthWorkDaysOnIllnessWorked}" pattern="#.#"/></td>
                </tr>
                <tr>
                    <td colspan="3">Общее кол-во рабочих дней болезни за год:</td>
                    <td id="yearWorkDaysOnIllness">${reports.yearWorkDaysOnIllness}</td>
                </tr>
                <tr>
                    <td colspan="3">Общее кол-во рабочих дней болезни за год , когда сотрудник работал:</td>
                    <td id="yearWorkDaysOnIllnessWorked"><fmt:formatNumber value="${reports.yearWorkDaysOnIllnessWorked}" pattern="#.#"/></td>
                </tr>
                </thead>
            </c:when>
        </c:choose>
    </table>
</body>
</html>