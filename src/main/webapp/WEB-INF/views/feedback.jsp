<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="feedback"/></title>
    <script type="text/javascript">

        dojo.ready(function () {
            window.focus();
        });

        var employeeList = ${employeeListJson};

        // Устанавливаем подразделение и сотрудника по умолчанию
        //из аргументов переданных через контроллер
        window.onload = function setDefaultEmployee() {
            dojo.attr("name", {disabled:"disabled"});
            dojo.attr("email", {disabled:"disabled"});
            name_email.style.display = 'none';

            var divVal = ${divId};
            var empVal = ${empId};

            //устанавливаем подразделение
            /* var divOptions = dojo.query('.divOption');
             for (var i = 0; i < divOptions.length; i++) {
             if (divOptions[i].value == divVal) {
             dojo.attr(divOptions[i], {selected:"selected"});
             }
             }*/
            mainForm.divisionId.value = divVal;

            //устанавливаем сотрудника
            var employeeSelect = dojo.byId("employeeId");
            var employeeOption = null;
            //Очищаем список сотрудников.
            employeeSelect.options.length = 0;
            for (var i = 0; i < employeeList.length; i++) {
                if (divVal == employeeList[i].divId) {
                    insertEmptyOption(employeeSelect);
                    for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                        if (employeeList[i].divEmps[j].id != 0 && employeeList[i].divEmps[j].id != 27) {
                            employeeOption = dojo.doc.createElement("option");
                            if (employeeList[i].divEmps[j].id == empVal) {
                                dojo.attr(employeeOption, {
                                    value:employeeList[i].divEmps[j].id,
                                    selected:"selected"
                                });
                            } else {
                                dojo.attr(employeeOption, {
                                    value:employeeList[i].divEmps[j].id
                                });
                            }
                            employeeOption.title = employeeList[i].divEmps[j].value;
                            employeeOption.innerHTML = employeeList[i].divEmps[j].value;
                            employeeSelect.appendChild(employeeOption);
                        }
                    }
                }
            }
            sortSelectOptions(employeeSelect);
        };

        //проверяем и отсылаем форму
        function submitform() {
            var division = dojo.byId('divisionId');
            var employee = dojo.byId('employeeId');
            var description = dojo.byId('feedbackDescription');
            var type = dojo.byId('feedback_type');
            var typeIndex = type.selectedIndex;
            var divIndex = division.selectedIndex;
            var empIndex = employee.selectedIndex;
            var name = dojo.byId('name').value;
            var email = dojo.byId('email').value;

            if (description != null && description.value != "") {
                if (divIndex != null && divIndex != 0 && empIndex != 0) {
                    feedbackForm.action = "feedback";
                    feedbackForm.submit();
                } else {
                    if (name == "" || email == "") {
                        alert("Введите, пожалуйста, ваше имя и email.");
                    } else {
                        feedbackForm.action = "feedback";
                        feedbackForm.submit();
                    }
                }
            } else {
                alert("Поле 'Текст сообщения' не определено.");
            }
        }

        //закрываем окно
        function cancel() {
            if (confirmCancelWindow()) {
                self.close();
            }
        }

        //очищаем форму
        function clearForm(obj) {
            if (confirmClearWindow()) {
                document.forms.mainForm.reset();
                feedbackForm.action = "newFeedbackMessage";
                feedbackForm.submit();
            }
        }

        //Показать скрытый input для дополнительного файла-вложения
        function showAdditionalInput(id) {
            document.getElementById(id).style.display = '';
        }
    </script>

</head>
<body>
	<c:if test="${jiraIssueCreateUrl != null}">
		<h2><a target="_blank" href=${jiraIssueCreateUrl}>Перейти к созданию запроса в Jira</a></h2>
	</c:if>
<h1><fmt:message key="feedback"/></h1>


<form:form method="post" commandName="feedbackForm" name="mainForm" enctype="multipart/form-data" cssClass="noborder">


    <c:if test="${fn:length(errors) > 0}">
        <div class="errors_box">
            <c:forEach items="${errors}" var="error">
                <fmt:message key="${error.code}">
                    <fmt:param value="${error.arguments[0]}"/>
                </fmt:message><br/>
            </c:forEach>
        </div>
    </c:if>

    <div id="form_table">
        <table id="time_sheet_table">
            <tr id="time_sheet_header">
                <th align="center" width="120px">Тип сообщения</th>
                <th align="center" width="210px">Текст сообщения</th>
                <th align="center" width="270px">Вложения</th>
            </tr>

            <tr class="time_sheet_row" id="ts_row">
                <td width="38" class="top_align"> <!-- Тип проблемы -->
                    <form:select path="feedbackType" cssClass="activityType" style="width:200px" id="feedback_type"
                                 name="feedback_type" onchange="feedbackTypeChange(this);"
                                 onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                        <fmt:message key="feedback.type.newproposal" var="problemNewProposal"/>
                        <form:option value="1" title="${problemNewProposal}" label="${problemNewProposal}"/>
                        <fmt:message key="feedback.type.incorrectdata" var="problemIncorrectMessage"/>
                        <form:option value="2" title="${problemIncorrectMessage}" label="${problemIncorrectMessage}"/>
                        <fmt:message key="feedback.type.cantsendreport" var="problemCantReport"/>
                        <form:option value="3" title="${problemCantReport}" label="${problemCantReport}"/>
                        <fmt:message key="feedback.type.notfoundproject" var="problemNotFoundProject"/>
                        <form:option value="4" title="${problemNotFoundProject}" label="${problemNotFoundProject}"/>
                        <fmt:message key="feedback.type.other" var="problemOther"/>
                        <form:option value="5" title="${problemOther}" label="${problemOther}"/>
                    </form:select>
                </td>
                <td class="top_align"> <!-- Текст сообщения -->
                    <form:textarea path="feedbackDescription" id="feedbackDescription" name="feedbackDescription" rows="8"
                                   cols="40"></form:textarea>
                </td>
                <td class="top_align"><!-- Вложения -->
                    <input id="file1Path" name="file1Path" type="file" size="30" onchange="showAdditionalInput('file2Path')"/><br/>
                    <input style="display:none" id="file2Path" name="file2Path" type="file" size="30" /><br/>
                    <span>Суммарный размер вложений - не более 8МБ</span>
                </td>

            </tr>
        </table>
    </div>

    <div id="form_header">
        <span class="label">Подразделение</span>
        <form:select path="divisionId" id="divisionId" name="divisionId" onchange="divisionChange(this)"
                     class="without_dojo" onload="setSelect()" onmouseover="tooltip.show(getTitle(this));"
                     onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options class="divOption" items="${divisionList}" itemLabel="name" itemValue="id"/>
        </form:select>

        <span class="label">Сотрудник</span>
        <form:select path="employeeId" id="employeeId" name="employeeId" class="without_dojo" onchange="setIDs()"
                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options items="${employeeList}" itemLabel="name" itemValue="id"/>
        </form:select>
    </div>

    <div id="name_email">
        <span id="nameLabel" class="label">Имя и фамилия:</span>
        <input id="name" name="name" class="without_dojo" disabled="disabled"/>
        <span id="emailLabel" class="label">Ваш email:</span>
        <input id="email" name="email" class="without_dojo" disabled="disabled"/>
    </div>

    <div id="marg_buttons" style="margin-top:10px; margin-bottom:10px ">
        <button id="send_button" name="send_button" style="width:150px" type="button"
                onclick="submitform()">Отправить
        </button>

        <button id="clear_button" name="clear_button" style="width:150px" type="button"
                onclick="clearForm(this)">Очистить
        </button>
    </div>
</form:form>
</body>
</html>