<%@ page import="static com.aplana.timesheet.util.ResourceUtils.getResRealPath" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

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
        require(["dojo/parser", "dijit/TitlePane"]);

        dojo.ready(function() {
            window.focus();

            setCurrentYear(${year});
            divisionChange(dojo.byId("divisionId"));

            dojo.byId("employeeId").value = ${employeeId};
            dojo.byId("vacationId").setAttribute("disabled", "disabled");
        });

        var employeeList = ${employeeListJson};

        function showVacations() {
            var empId = dojo.byId("employeeId").value;
            var divisionId = dojo.byId("divisionId").value;
            var year = dojo.byId("year").value;

            if (isNotNilOrNull(year)) {
                if (checkEmployeeData(divisionId, empId)) {
                    dojo.byId("vacationId").setAttribute("disabled", "disabled");
                    vacationsForm.action =
                            "<%=request.getContextPath()%>/vacations/" + divisionId + "/" + empId + "/" + year;
                    vacationsForm.submit();
                }
            } else {
                var error = "";

                if (isNilOrNull(year)) {
                    error += ("Необходимо выбрать год\n");
                }

                alert(error);
            }
        }

        function createVacation() {
            var divisionId = dojo.byId("divisionId").value;
            var empId = dojo.byId("employeeId").value;

            if (checkEmployeeData(divisionId, empId)) {
                vacationsForm.action =
                        "<%=request.getContextPath()%>/createVacation/" + empId;
                vacationsForm.submit();
            }
        }

        function deleteVacation(parentElement, vac_id) {
            if (!confirm("Удалить заявку?")) {
                return;
            }

            dojo.byId("vacationId").removeAttribute("disabled");
            dojo.byId("vacationId").value = vac_id;
            vacationsForm.submit();
        }
    </script>
</head>
<body>

<h1><fmt:message key="title.vacations"/></h1>
<br/>

<form:form method="post" commandName="vacationsForm" name="mainForm">
    <form:hidden path="vacationId" />

    <span class="label">Подразделение:</span>
    <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:option label="" value="0"/>
        <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
    </form:select>

    <span class="label">Сотрудник:</span>
    <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                 onmouseout="tooltip.hide();" onchange="setDefaultEmployeeJob(-1);">
        <form:option items="${employeeList}" label="" value="0"/>
    </form:select>
    <br><br>

    <span class="label">Год:</span>
    <form:select path="year" id="year" class="without_dojo" onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
        <form:option label="" value="0"/>
        <form:options items="${yearsList}" itemLabel="year" itemValue="year"/>
    </form:select>

    <button id="show" style="width:150px" style="vertical-align: middle" type="button"
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
    </tr>
    </thead>
    <tbody>
    <c:choose>
    <c:when test="${fn:length(vacationsList) == 0}">
        <tr>
            <td colspan="8">За выбранный год нет ни одного заявления на отпуск</td>
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
                <c:if test="${(vacation.status.id eq 57 or vacation.status.id eq 59) and fn:length(vacation.vacationApprovals) > 0}">
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
        </tr>
    </c:forEach>
    </tbody>
    <tfoot>
        <tr class="summary">
            <td colspan="3">Кол-во утвержденных заявлений на отпуск</td>
            <td colspan="5">${summaryApproved}</td>
        </tr>
        <tr class="summary">
            <td colspan="3">Кол-во отклоненных заявлений на отпуск</td>
            <td colspan="5">${summaryRejected}</td>
        </tr>
        <tr class="summary">
            <td colspan="3">Кол-во календарных дней отпуска за год</td>
            <td colspan="5">${summaryCalDays}</td>
        </tr>
        <tr class="summary">
            <td colspan="3">Кол-во рабочих дней отпуска за год</td>
            <td colspan="5">${summaryWorkDays}</td>
        </tr>
    </tfoot>
    </c:otherwise>
    </c:choose>
</table>
</body>
</html>