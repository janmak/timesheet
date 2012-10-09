<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html>
<head>
    <title><fmt:message key="viewreports"/></title>

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
                    error += ("Вы не уточнили год и месяц!\n");
                }
                else if (month == 0 || month == null) {
                    error += ("Вы не уточнили месяц!\n");
                }
                if (divisionId == 0 || divisionId == null) {
                    error += ("Вы не уточнили подразделение и сотрудника!\n");
                }
                else if (empId == 0 || empId == null) {
                    error += ("Вы не уточнили сотрудника!\n");
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
    <button id="show" style="width:150px" style="vertical-align: middle" type="button" onclick="showDates()">Показать
    </button>
    <p/>

    <table>
        <tr class="b center">
            <td>Дата</td>
            <td>Статус</td>
            <td width="130">Часы</td>
            <sec:authorize access="hasRole('ROLE_ADMIN')">
                <td width="15px"></td>
            </sec:authorize>
        </tr>
        <c:forEach var="row" items="${dateList}">
            <c:choose>
                <c:when test="${row[1] eq null}">
                    <tr>
                        <c:choose>
                            <c:when test="${row[4] ne null}">
                                <td width="150" bordercolor="brown"><span
                                        class="colortext">&nbsp <b>${row[0]}</b> ${row[6]}</span></td>
                                <td width="160" bordercolor="brown"></td>
                                <td></td>
                            </c:when>
                            <c:when test="${row[4] eq null}">
                                <td width="120" bordercolor="grey">&nbsp <b>${row[0]}</b> ${row[6]}</td>
                                <td width="160" bordercolor="grey">
                                    <c:if test="${row[9]==false}">Отчета нет <a
                                        href="<%=request.getContextPath()%>/timesheet?date=${row[7]}&id=${employeeId}">(Создать)</a>
                                    </c:if>
                                </td>
                                <td></td>
                            </c:when>

                        </c:choose>
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                            <td width="15px"></td>
                        </sec:authorize>
                    </tr>
                </c:when>
                <c:when test="${row[1] ne null}">
                    <tr>
                        <c:choose>
                            <c:when test="${row[4] ne null}">
                                <td width="150" bordercolor="red"><span
                                        class="colortext">&nbsp <b>${row[0]} ${row[6]}</b></span></td>
                                <td width="160" bordercolor="red">
                                    <c:if test="${row[9]==false}">
                                    <a href="<%=request.getContextPath()%>/report/${row[2]}/${row[3]}/${row[5]}/${employeeId}"
                                       target="report+${row[2]}/${row[3]}/${row[5]}/${employeeId}"><span
                                            class="colortext"> Просмотреть отчет</span></a>

                                    </c:if>
                                </td>
                            </c:when>
                            <c:when test="${row[4] eq null}">
                                <td width="150" bordercolor="grey">&nbsp <b>${row[0]}</b> ${row[6]}</td>
                                <td width="160" bordercolor="grey">
                                    <c:if test="${row[9]==false}">
                                    <a href="<%=request.getContextPath()%>/report/${row[2]}/${row[3]}/${row[5]}/${employeeId}"
                                       target="report+${row[2]}/${row[3]}/${row[5]}/${employeeId}"> Просмотреть
                                        отчет</a>
                                    </c:if>
                                </td>
                            </c:when>
                        </c:choose>
                        <td class="center">
                            <c:if test="${row[9]==false}">
                                ${row[8]}
                            </c:if>
                        </td>
                        <sec:authorize access="hasRole('ROLE_ADMIN')">
                            <td width="15px"><a href="#" onclick="deleteTimeSheet(${row[1]})">
                                <c:if test="${row[9]==false}">
                                <img
                                    src="<c:url value="/resources/img/delete.png"/>" width="15px" title="Удалить"/></a>
                                </c:if>
                            </td>
                        </sec:authorize>
                    </tr>
                </c:when>
            </c:choose>
        </c:forEach>
        <tr class="b">
            <td colspan="2">Всего(факт)</td>
            <td class="center">${timeFact}</td>
            <td></td>
        </tr>
        <tr class="b">
            <td colspan="2">Всего(план)</td>
            <td class="center">${timePlan}</td>
            <td></td>
        </tr>
    </table>
    <br>
    <%--не нужна--%>
    <%--<button id="close" style="width:210px" type="button" onclick="window.close()">Закрыть</button>--%>
    <input type="hidden" name="commandURL" id="commandURL"/>
</form:form>
</body>
</html>