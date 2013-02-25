<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<html>
<head>
    <title><fmt:message key="title.createVacation"/></title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/vacations.css" />
    <script type="text/javascript" src="<%= request.getContextPath()%>/resources/js/vacations.js"></script>
    <script type="text/javascript">
        dojo.require("dijit.form.DateTextBox");
        dojo.require(CALENDAR_EXT_PATH);

        function getEmployeeId() {
            return "${employee.id}";
        }

        initCurrentDateInfo(getEmployeeId());

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

        function setDate(date_picker, date) {
            date_picker.set("displayedValue", date);
        }

        function createVacation(approved) {
            if (validate()) {
                createVacationForm.action =
                        "<%=request.getContextPath()%>/validateAndCreateVacation/${employee.id}/"
                                + (approved ? "1" : "0");
                createVacationForm.submit();
            }
        }

        function validate() {
            var fromDate = dijit.byId("calFromDate").get('value');
            var toDate = dijit.byId("calToDate").get('value');
            var type = dojo.byId("types").value;
            var comment = dojo.byId("comment").value;

            var error = "";

            if (isNilOrNull(fromDate)) {
                error += "Необходимо указать дату начала отпуска\n";
            }

            if (isNilOrNull(toDate)) {
                error += "Необходимо указать дату окончания отпуска\n";
            }

            if (fromDate > toDate) {
                error += "Дата начала отпуска не может быть больше даты окончания\n";
            }

            if (isNilOrNull(type)) {
                error += "Необходимо указать тип отпуска\n";
            }

            if (type == ${typeWithRequiredComment} && comment.length == 0) {
                error += "Необходимо написать комментарий\n";
            }

            if (error.length == 0) {
                return true;
            }

            alert(error);

            return false;
        }

        function updateExitToWork() {
            var date = dojo.byId("calToDate").value;
            var exitToWorkElement = dojo.byId("exitToWork");

            if (typeof date == typeof undefined || date == null || date.length == 0) {
                exitToWorkElement.innerHTML = '';
            } else {
                exitToWorkElement.innerHTML =
                        "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

                dojo.xhrGet({
                    url: "<%= request.getContextPath()%>/getExitToWork/${employee.id}/" + date + "/",
                    handleAs: "text",

                    load: function(data) {
                        if (data.size != 0) {
                            exitToWorkElement.innerHTML = data;
                        } else {
                            exitToWorkElement.innerHTML = "Не удалось получить дату выхода из отпуска!";
                        }
                    },

                    error: function(error) {
                        exitToWorkElement.setAttribute("class", "error");
                        exitToWorkElement.innerHTML = error;
                    }
                });
            }
        }

        function cancel() {
            window.location = "<%= request.getContextPath() %>/vacations";
        }
    </script>
</head>
<body>

<h1><fmt:message key="title.createVacation"/></h1>
<br/>

<h2>${employee.name} (${employee.division.name})</h2>

<form:form method="post" commandName="createVacationForm" name="mainForm">
    <form:errors path="*" cssClass="errors_box" delimiter="<br/><br/>" />

    <form:hidden path="employeeId" />

    <table class="without_borders">
        <colgroup>
            <col width="150" />
            <col width="320" />
        </colgroup>

        <tr>
            <td>
                <span class="label">Дата с</span>
            </td>
            <td>
                <form:input path="calFromDate" id="calFromDate" class="date_picker" data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();" />
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Дата по</span>
            </td>
            <td>
                <form:input path="calToDate" id="calToDate" class="date_picker" data-dojo-type="DateTextBox" required="true"
                            onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();" onChange="updateExitToWork();" />
                <div id="exitToWork"></div>
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Тип отпуска</span>
            </td>
            <td>
                <form:select path="vacationType" id="types" onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false" size="1">
                    <form:option value="0" label="" />
                    <form:options items="${vacationTypes}" itemLabel="value" itemValue="id" />
                </form:select>
            </td>
        </tr>

        <tr>
            <td>
                <span class="label">Комментарий</span>
            </td>
            <td>
                <form:textarea path="comment" id="comment" maxlength="400" rows="5" cssStyle="width: 100%" />
            </td>
        </tr>
    </table>

    <button type="button" onclick="createVacation(false)">Создать</button>
    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <button type="button" onclick="createVacation(true)">Добавить утвержденное заявление на отпуск</button>
    </sec:authorize>
    <button type="button" onclick="cancel()">Отмена</button>
</form:form>
</body>
</html>