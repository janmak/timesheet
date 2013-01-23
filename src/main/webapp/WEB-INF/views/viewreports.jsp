<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html>
    <head>
        <title><fmt:message key="viewreports"/></title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/viewreports.css">
        <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/viewreports.js"></script>
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
                    viewReportsForm.action = "<%=request.getContextPath()%>/viewreports/" + divisionId + "/" + empId + "/" + year + "/" + month;
                    viewReportsForm.submit();
                } else {
                    var error = "";
                    if (year == 0 || year == null) {
                        error += ("Необходимо выбрать год и месяц\n");
                    }
                    else if (month == 0 || month == null) {
                        error += ("Необходимо выбрать месяц!\n");
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
        <style type="text/css">
            .colortext {
                color: brown;
            }
            .center {
                text-align:center;
                text-valign:middle;
            }
            tr.b {
                font-weight: bold;
            }
        </style>
    </head>
    <body>

        <h1><fmt:message key="viewreports"/></h1>
        <br/>

        <form:form method="post" commandName="viewReportsForm" name="mainForm">
            <c:if test="${fn:length(errors) > 0}">
                <div class="errors_box">
                    <c:forEach items="${errors}" var="error">
                        <fmt:message key="${error.code}">
                            <fmt:param value="${error.arguments[0]}"/>
                        </fmt:message><br/>
                    </c:forEach>
                </div>
            </c:if>

            <span class="label">Подразделение</span>
            <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                <form:option label="" value="0"/>
                <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
            </form:select>

            <span class="label">Отчет сотрудника</span>
            <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                         onmouseout="tooltip.hide();" onchange="setDefaultEmployeeJob(-1);" cssStyle="width: autoAPLANATS-498">
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
            <button id="show" style="width:150px" style="vertical-align: middle" type="button" onclick="showDates()">Показать
            </button>
            <p/>
            <input type="hidden" name="commandURL" id="commandURL"/>
        </form:form>
        <table id="viewreports">
            <thead>
                <tr>
                    <th width="120">Дата</th>
                    <th width="160">Статус</th>
                    <th width="130">Часы</th>
                </tr>
            </thead>
            <tbody>
                <%-- <% for(DayTimeSheet report : request.getAttribute("reports")) { %> --%>
                <c:forEach var="report" items="${reports}">
                    <c:if test="${report.statusHoliday}">
                        <tr class="statusHoliday">
                            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="yyyy.MM.dd"/></td>
                            <td>Выходной</td>
                            <td></td>
                        </tr>
                    </c:if>
                    <c:if test="${report.statusNotStart}">
                        <tr class="statusNotStart">
                            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="yyyy.MM.dd"/></td>
                            <td>Ещё не принят на работу</td>
                            <td></td>
                        </tr>
                    </c:if>
                    <c:if test="${report.statusNormalDay}">
                        <tr class="statusNormalDay toplan">
                            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="yyyy.MM.dd"/></td>
                            <td>
                                <a target="_blank" href="<%=request.getContextPath()%>/report/<fmt:formatDate value="${report.calDate}" pattern="/yyyy/MM/dd/"/>${report.timeSheet.employee.id}">Посмотреть отчёт</a>
                                <sec:authorize access="hasRole('ROLE_ADMIN')">
                                    <a href="#" onclick="deleteTimeSheet(${report.timeSheet.id})"><img src="<c:url value="/resources/img/delete.png"/>" width="15px" title="Удалить"/></a>
                                    </sec:authorize>
                            </td>
                            <td class="duration">${report.duration}</td>
                        </tr>
                    </c:if>
                    <c:if test="${report.statusWorkOnHoliday}">
                        <tr class="statusWorkOnHoliday">
                            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="yyyy.MM.dd"/></td>
                            <td>
                                Работа в выходной день <a target="_blank" href="<%=request.getContextPath()%>/report/<fmt:formatDate value="${report.calDate}" pattern="/yyyy/MM/dd/"/>${report.timeSheet.employee.id}">Посмотреть отчёт</a>
                                <sec:authorize access="hasRole('ROLE_ADMIN')">
                                    <a href="#" onclick="deleteTimeSheet(${report.timeSheet.id})"><img src="<c:url value="/resources/img/delete.png"/>" width="15px" title="Удалить"/></a>
                                    </sec:authorize>
                            </td>
                            <td class="duration">${report.duration}</td>
                        </tr>
                    </c:if>
                    <c:if test="${report.statusNoReport}">
                        <tr class="statusNoReport toplan">
                            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="yyyy.MM.dd"/></td>
                            <td>Отчёта нет <a href="<%=request.getContextPath()%>/timesheet?date=<fmt:formatDate value="${report.calDate}" pattern="yyyy-MM-dd"/>&id=${employeeId}">(Создать)</a></td>
                            <td class="duration">${report.duration}</td>
                        </tr>
                    </c:if>
                    <c:if test="${report.statusNotCome}">
                        <tr class="statusNotCome">
                            <td class="date"><fmt:formatDate value="${report.calDate}" pattern="yyyy.MM.dd"/></td>
                            <td></td>
                            <td></td>
                        </tr>
                    </c:if>
                </c:forEach>
            </tbody>
            <thead>
                <tr>
                    <td colspan="2">Всего(факт):</td>
                    <td id="durationall">JavaScript is disabled</td>
                </tr>
                <tr>
                    <td colspan="2">Всего(план):</td>
                    <td id="durationplan">JavaScript is disabled</td>
                </tr>
                <tr>
                    <td colspan="2">Норма (часов) в неделю:</td>
                    <td><input type="text" id="normainweak"></td>
                </tr>
            </thead>
        </table>
    </body>
</html>