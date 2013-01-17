<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.timesheet"/></title>

    <script type="text/javascript">
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.Calendar");
        var workplaceList = ${workplaceJson};
        var actTypeList = ${actTypeJson};
        var projectList = ${projectListJson};
        var actCategoryList = ${actCategoryListJson};
        var availableActCategoryList = ${availableActCategoriesJson};
        var employeeList = ${employeeListJson};
        var projectRoleList = ${projectRoleListJson};
        var projectTaskList = ${projectTaskListJson};
        var selectedProjects = ${selectedProjectsJson};
        var selectedProjectTasks = ${selectedProjectTasksJson};
        var selectedProjectRoles = ${selectedProjectRolesJson};
        var selectedActCategories = ${selectedActCategoriesJson};
        var selectedWorkplace = ${selectedWorkplaceJson};
        var selectedLongVacationIllness = ${selectedLongVacationIllnessJson};
        var selectedCalDate = ${selectedCalDateJson};
        var root = window.addEventListener || window.attachEvent ? window : document.addEventListener ? document : null;
        var dateInfoHolder = [];
        var month = correctLength(new Date().getMonth() + 1);

        // Первоначальная установка дат
        colorDayWithReportFromThreeMonth(dateInfoHolder, new Date().getFullYear(), month, ${timeSheetForm.employeeId});

        dojo.declare("Calendar", dijit.Calendar, {
            getClassForDate:function (date) {
                var employeeId = dojo.byId("employeeId").value;
                var month = correctLength(date.getMonth() + 1);
                var year = date.getFullYear();
                var day = correctLength(date.getDate());

                var info = dateInfoHolder[year + "-" + month + ":" + employeeId];
                var dateInfo;
                if (info == null) {
                    dateInfoHolder[year + "-" + month + ":" + employeeId] = {}; //Создаем пустой объект, чтобы показать, что за этот месяц запрос уже отправлен
                    colorDayWithReportFromThreeMonth(dateInfoHolder, year, month, employeeId)
                }
                info = dateInfoHolder[year + "-" + month  + ":" + employeeId];
                dateInfo = info[year + "-" + month + "-" + day];

                switch (dateInfo) {
                    case "1":// в этот день имеется отчет
                        if (date <= new Date())
                            return 'classDateGreen';
                        else return '';
                        break;
                    case "2":   //выходной или праздничный день
                        return 'classDateRedText';
                        break;
                    case "0":   //день без отчета
                        if (date <= new Date())
                            return 'classDateRedBack';
                        else return '';
                    default: // Никаких классов не назначаем, если нет информации
                        return '';
                        break;
                }

            }
        });

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass:"Calendar"
        });

        dojo.ready(function () {
           if (typeof(root.onbeforeunload) != "undefined") root.onbeforeunload = confirmTimeSheetCloseWindow;

            //dojo.connect(dojo.byId("add_row_button"), "onclick", "addNewRow");
            dojo.connect(dojo.byId("plan"), "onkeyup", dojo.byId("plan"), textareaAutoGrow);

            <sec:authorize access="!hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')">
            // Выбор сотрудника доступен только руководителю и администратору
            // пока не будем блокировать
            //dojo.attr("divisionId", {disabled:"disabled"});
            //dojo.attr("employeeId", {disabled:"disabled"});
            </sec:authorize>

            timeSheetForm.divisionId.value = ${timeSheetForm.divisionId};

            divisionChange(timeSheetForm.divisionId);

            timeSheetForm.employeeId.value = ${timeSheetForm.employeeId};

            /* После реализации LDAP аутентификации в этом нет необходимости
             if (existsCookie('aplanaDivision')) {
             timeSheetForm.divisionId.value = CookieValue('aplanaDivision');
             if (existsCookie('aplanaEmployee')) {
             divisionChange(timeSheetForm.divisionId);
             timeSheetForm.employeeId.value = CookieValue('aplanaEmployee');
             dojo.removeAttr("view_reports_button", "disabled");
             }
             } */

            /*смотрим, поддерживаются ли куки и рисуем индикатор*/
            showCookieIndicator();
            /*смотрим, сколько строк отправляли в предыдущем отчете
             и задаем в новом отчете столько же*/
            if (existsCookie('aplanaRowsCount')) {
                if (CookieValue('aplanaRowsCount') > 0) {
                    /* нафига это тут так было написано --- неизвестно
                     addNewRows(CookieValue('aplanaRowsCount') > 1 ? CookieValue('aplanaRowsCount') : 2);
                     */
                    // Fix APLANATS-346
                    addNewRows(CookieValue('aplanaRowsCount'));
                } else {
                    addNewRows(2);
                }
            } else {
                addNewRows(2);
            }
            setDuringDate();
            reloadTimeSheetState();
            recalculateDuration();
            refreshPlans(dijit.byId('calDate').value, dojo.byId('employeeId').value);
        });

        function colorDayWithReportFromThreeMonth(dateInfoHolder, year, month, employeeId){
            loadCalendarColors(dateInfoHolder, year, month, employeeId);
            var monthPrev =  parseInt(month, 10) - 1;
            var yearPrev = year;
            if (monthPrev <= 0){
                monthPrev = 12;
                yearPrev = parseInt(year, 10) - 1;
            }
            if (dateInfoHolder[yearPrev + "-" + correctLength(monthPrev) + ":" + employeeId] == null)
                loadCalendarColors(dateInfoHolder, yearPrev, correctLength(monthPrev), employeeId);
            var monthNext =  parseInt(month, 10) + 1;
            var yearNext = year;
            if (monthNext > 12){
                monthNext = 1;
                yearNext = parseInt(year, 10) + 1;
            }
            if (dateInfoHolder[yearNext + "-" + correctLength(monthNext) + ":" + employeeId] == null)
                loadCalendarColors(dateInfoHolder, yearNext, correctLength(monthNext), employeeId);
        }

        //загружает список дней с раскраской календаря за месяц
        function loadCalendarColors(dateInfoHolder, year, month, employeeId){
            dojo.xhrGet({
                url: "${pageContext.request.contextPath}" + "/timesheet/dates",
				headers: {
					"If-Modified-Since":"Sat, 1 Jan 2000 00:00:00 GMT"
				},
                handleAs:"json",
                timeout:1000,
                content:{queryYear:year, queryMonth:month, employeeId:employeeId},
                load:function (data, ioArgs) {
                    if (data && ioArgs && ioArgs.args && ioArgs.args.content) {
                        dateInfoHolder[ioArgs.args.content.queryYear + "-" + ioArgs.args.content.queryMonth  + ":" + ioArgs.args.content.employeeId] = data;
                    }
                },
                error:function (err, ioArgs) {
                    if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                        // Если ошибка - не будем ничего рисовать. При следующем запросе на отрисовку - снова будет сделана попытка получения данных за этот месяц
                        dateInfoHolder[ioArgs.args.content.queryYear + "-" + ioArgs.args.content.queryMonth  + ":" + ioArgs.args.content.employeeId] = null;
                    }
                }
            });
        }

        function correctLength(dayOrMonth){
            if (dayOrMonth < 10)
                return '0' + dayOrMonth;
            return dayOrMonth;
        }

        function refreshPlans(date, employeeId){
            var month = correctLength(date.getMonth() + 1);
            var year = date.getFullYear();
            var day = correctLength(date.getDate());

            var corretDate = year + "-" + month + "-" + day;
            dojo.xhrGet({
                url: "${pageContext.request.contextPath}" + "/timesheet/plans",
                handleAs:"json",
                timeout:1000,
                content:{date:corretDate, employeeId:employeeId},
                load:function (data, ioArgs) {
                    if (data && ioArgs && ioArgs.args && ioArgs.args.content) {

                        if (data.prev != null){
                            var prev = data.prev;
                            var dateString = prev.dateStr;

                            dojo.byId("lbPrevPlan").innerHTML = "Планы предущего рабочего дня (" + timestampStrToDisplayStr(dateString.toString()) + "):";
                            var planText = prev.plan;
                            if (planText.length != 0 ){
                                var text = prev.plan;
                                text = text.replace(/\n/g, '<br>');
                                dojo.byId("plan_textarea").innerHTML = text;
                            }
                            else
                                dojo.byId("plan_textarea").innerHTML = "План последнего рабочего не был определен";
                        }
                        else{
                            dojo.byId("lbPrevPlan").innerHTML = "Ничего не запланировано.";
                            dojo.byId("plan_textarea").innerHTML = "";
                        }

                        if (data.next != null){
                            var next =  data.next;
                            var dateString = next.dateStr;
                            dojo.byId("lbNextPlan").innerHTML = "Планы на следующий(" + timestampStrToDisplayStr(dateString.toString()) + ") рабочий день:";
                            dojo.byId("plan").value = next.plan;
                        }
                        else {
                            dojo.byId("lbNextPlan").innerHTML = "Планы на следующий рабочий день:";
                            dojo.byId("plan").value = "";
                        }

                    }
                },
                error:function (err, ioArgs) {
                    if (err && ioArgs && ioArgs.args && ioArgs.args.content) {
                        dojo.byId("lbPrevPlan").innerHTML = "Ничего не запланировано.";
                        dojo.byId("plan_textarea").innerHTML = "";
                        dojo.byId("plan").value = "";
                    }
                }
            });
        }

    </script>

    <script type="text/javascript">
        function submitform(s) {
            if (typeof(root.onbeforeunload) != "undefined") {
                root.onbeforeunload = null;
            }

            var longVacation = dojo.byId("long_vacation");
            var longIllness = dojo.byId("long_illness");
            if (s == 'send' && confirmSendReport()) {
                if (longVacation.checked == true || longIllness.checked == true) {
                    timeSheetForm.action = "timesheetLong";
                    timeSheetForm.submit();
                } else {
                    var division = dojo.byId('divisionId');
                    var employee = dojo.byId('employeeId');
                    var rowsCount = dojo.query(".time_sheet_row").length;
                    var projectId;
                    var projectComponent;
                    var diff = false;
                    for (var i = 0; i < rowsCount; i++) {
                        projectComponent = dojo.query("#project_id_" + i)
                        if (!diff && projectComponent.length > 0)
                            if (projectComponent[0].value) {
                                if (projectId && (projectId != projectComponent[0].value)) {
                                    if (projectComponent[0].value != 0)
                                        diff = true;
                                }
                                else
                                    projectId = projectComponent[0].value;
                            }
                    }
                    setCookie('aplanaDivision', division.value, TimeAfter(7, 0, 0));
                    setCookie('aplanaEmployee', employee.value, TimeAfter(7, 0, 0));
                    setCookie('aplanaRowsCount', rowsCount, TimeAfter(7, 0, 0));
                    if (diff)
                        deleteCookie("aplanaProject");
                    else
                        setCookie('aplanaProject', projectId, TimeAfter(7, 0, 0));
                    timeSheetForm.action = "timesheet";
                    maskBody();

                    // disabled не включается в submit. поэтому снимем аттрибут.
                    dojo.removeAttr("divisionId", "disabled");
                    dojo.removeAttr("employeeId", "disabled");
                    timeSheetForm.submit();
                }
            }
            if (s == 'newReport' && confirmCreateNewReport()) {
                timeSheetForm.action = "newReport";
                timeSheetForm.submit();
            }
//            if (s == 'problem') {
//                var empId = dojo.byId("employeeId").value;
//                var divId = dojo.byId("divisionId").value;
//                window.open('problem/' + divId + '/' + empId, 'problem_window');
//            }
        }
        function validateReportDate(value) {
            if (value != null && dateNotBetweenMonth(value)) {
                dojo.style("date_warning", {"display":"inline", "color":"red"});
                if (invalidReportDate(value) > 0)
                    dojo.byId("date_warning").innerHTML = "Разница текущей и указанной дат больше 27 дней";
                else
                    dojo.byId("date_warning").innerHTML = "Разница текущей и указанной дат больше 27 дней";
            }
            else {
                dojo.style("date_warning", {"display":"none"});
            }
        }
        function CopyPlan() {
            var plan_text = dojo.byId("plan_textarea").innerHTML;
            plan_text = plan_text.replace(/<br>/g, '\n');
            dojo.byId("description_id_" + GetFirstIdDescription()).value = plan_text;
        }
    </script>
    <style type="text/css">
        #date_warning {
            display: none;
            padding-left: 15px;
        }

        .classDateGreen {
            background-color: #97e68d !important;
        }

        .classDateRedText {
            color: #f58383;
        }
        .classDateRedBack{
            background-color: #f58383 !important;
        }
    </style>
</head>
<body>
<div class="maskable" id="maskDiv"></div>

<h1><fmt:message key="title.timesheet"/></h1>


<form:form method="post" commandName="timeSheetForm" cssClass="noborder">

    <div id="form_header" style="margin-bottom: 15px;">
        <span class="label">Подразделение</span>
        <form:select path="divisionId" id="divisionId" onchange="divisionChange(this)" class="without_dojo"
                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
        </form:select>

        <span class="label">Отчет сотрудника</span>
        <form:select path="employeeId" id="employeeId" class="without_dojo" onmouseover="tooltip.show(getTitle(this));"
                     onmouseout="tooltip.hide();" onchange="setDefaultEmployeeJob(-1);
                     colorDayWithReportFromThreeMonth(dateInfoHolder, new Date().getFullYear(), correctLength(new Date().getMonth() + 1), this.value);
                     refreshPlans(dijit.byId('calDate').value, this.value);">
            <form:option label="" value="0"/>
        </form:select>
        <span class="label">за дату</span>
        <form:input path="calDate" id="calDate" class="date_picker" dojoType="DateTextBox" required="true"
                     onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();"
                     onChange="validateReportDate(this.value);refreshPlans(this.value, dojo.byId('employeeId').value);"/>
        <span id="date_warning"></span>
    </div>

    <div style="width: 500px;">
        <span id="lbPrevPlan">Планы предущего рабочего дня:</span>
        <div id="plan_textarea" style="margin: 2px 0px 2px 0px; padding:2px 2px 2px 2px;border: solid 1px silver;"></div>
        <button id="add_in_comments" type="button" style="width:300px" onclick="CopyPlan()">Скопировать в первый
            комментарий
        </button>
    </div>
    <div id="marg_buttons" style="margin-top:15px;">

        <%--перенесен в шапку таблицы как картинка--%>
        <%--<button id="add_row_button" style="width:150px" type="button">Добавить строку</button>--%>

        <%--убрано, но есть отдельная кнопка для каждой строки таблицы--%>
        <%--<button id="del_row_button" style="width:210px" type="button" onclick="delSelectedRows()">Удалить выбранные--%>
        <%--строки--%>

        <%--</button>--%>
        <c:if test="${fn:length(errors) > 0}">
            <div class="errors_box">
                <c:forEach items="${errors}" var="error">
                    <fmt:message key="${error.code}">
                        <fmt:param value="${error.arguments[0]}"/>
                    </fmt:message><br/>
                </c:forEach>
            </div>
        </c:if>
        <!--<button id="report_problem_button" style="width:200px" type="button" onclick="submitform('feedback')">Сообщить о
            проблеме
        </button>-->
    </div>
    <div id="form_table">
        <table id="time_sheet_table">
            <tr id="time_sheet_header">
                <th width="30px">
                    <a onclick="addNewRow()">
                        <img style="cursor: pointer;" src="<c:url value="/resources/img/add.gif"/>" width="15px" title="Добавить строку"/>
                    </a>
                </th>
                <th width="20px">№</th>
                <th width="100px">Место работы</th>
                <th width="120px">Тип активности</th>
                <th width="200px">Название проекта/пресейла</th>
                <th width="130px">Проектная роль</th>
                <th width="170px">Активность</th>
                <th width="130px">Задача</th>
                <th width="30px">ч.</th>
                <th width="200px">Комментарии</th>
                <th width="200px">Проблемы</th>
            </tr>

            <c:if test="${fn:length(timeSheetForm.timeSheetTablePart) > 0}">
                <c:forEach items="${timeSheetForm.timeSheetTablePart}" varStatus="row">
                    <tr class="time_sheet_row" id="ts_row_${row.index}">

                        <%--чекбоксики для кнопки удаления выбранных строк--%>
                        <%--<td class="text_center_align"><input class="selectedRow" type="checkbox"--%>
                                                             <%--name="selectedRow[${row.index}]"--%>
                                                             <%--id="selected_row_id_${row.index}"/></td>--%>

                        <td class="text_center_align" id="delete_button_id_${row.index}">

                        </td>
                        <td class="text_center_align row_number"><c:out value="${row.index + 1}"/></td>
                        <td class="top_align"> <!-- Место работы -->
                            <form:select path="timeSheetTablePart[${row.index}].workplaceId"
                                         id="workplace_id_${row.index}" onchange="workplaceChange(this)"
                                         cssClass="workplaceType" onmouseover="tooltip.show(getTitle(this));"
                                         onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                                <form:options items="${workplaceList}" itemLabel="value" itemValue="id"/>
                            </form:select>
                        </td>
                        <td class="top_align"> <!-- Тип активности -->
                            <form:select path="timeSheetTablePart[${row.index}].activityTypeId"
                                         id="activity_type_id_${row.index}" onchange="typeActivityChange(this)"
                                         cssClass="activityType" onmouseover="tooltip.show(getTitle(this));"
                                         onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                                <form:options items="${actTypeList}" itemLabel="value" itemValue="id"/>
                            </form:select>
                        </td>
                        <td class="top_align"> <!-- Название проекта/пресейла -->
                            <form:select path="timeSheetTablePart[${row.index}].projectId" id="project_id_${row.index}"
                                         onchange="projectChange(this)" cssClass="project"
                                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                            </form:select>
                        </td>
                        <td class="top_align"> <!-- Проектная роль -->
                            <form:select path="timeSheetTablePart[${row.index}].projectRoleId"
                                         id="project_role_id_${row.index}" onchange="projectRoleChange(this)"
                                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                                <form:options items="${projectRoleList}" itemLabel="name" itemValue="id"/>
                            </form:select>
                        </td>
                        <td class="top_align"> <!-- Категория активности/название работы -->
                            <form:select path="timeSheetTablePart[${row.index}].activityCategoryId"
                                         id="activity_category_id_${row.index}"
                                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                            </form:select>
                        </td>
                        <td class="top_align"> <!-- Проектная задача -->
                            <form:select path="timeSheetTablePart[${row.index}].cqId" id="cqId_id_${row.index}"
                                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                            </form:select>
                        </td>
                        <td class="top_align"><form:input cssClass="text_right_align duration" type="text"
                                                          path="timeSheetTablePart[${row.index}].duration"
                                                          id="duration_id_${row.index}"
                                                          onchange="checkDuration(this);"/></td>
                        <td class="top_align"><form:textarea wrap="soft"
                                                             path="timeSheetTablePart[${row.index}].description"
                                                             rows="4" cols="30" id="description_id_${row.index}"/></td>
                        <td class="top_align"><form:textarea wrap="soft" path="timeSheetTablePart[${row.index}].problem"
                                                             rows="4" cols="30" id="problem_id_${row.index}"/></td>
                    </tr>
                </c:forEach>
            </c:if>

            <tr style="height : 20px;" id="total_duration_row">
                <td></td>
                <td></td>
                <td>&nbsp;ИТОГО</td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td id="total_duration" class="text_right_align">0</td>
                <td></td>
                <td></td>
            </tr>
        </table>
    </div>

    <div id="plan_box" style="margin-bottom: 10px; margin-top: 10px">
        <span id="lbNextPlan" class="label">Планы на следующий рабочий день:</span>
        <div id="box_margin" style="margin-top :6px; margin-bottom: 8px;">
            <form:textarea wrap="soft" path="plan" id="plan" rows="7" cols="92"/>
            <br/>
        </div>
    </div>
    <div id="vacation_ill"><h2 style="margin-top :5px;"><fmt:message key="vacation_illness"/></h2></div>
    <div id="otpusk_illeness" style="margin-bottom: 10px">
        <table>
            <tr id="">
                <td class="no_border" width="20px"><input type="checkbox" name="longVacation" id="long_vacation"
                                                          onclick="checkLongVacation();"></td>
                <td class="no_border" width="50px">Отпуск /</td>
                <td class="no_border" width="20px"><input type="checkbox" name="longIllness" id="long_illness"
                                                          onclick="checkLongIllness();"></td>
                <td class="no_border" width="60px">Болезнь</td>
                <td class="no_border" width="20px"> с</td>
                <td class="no_border" width="210px">
                    <input id="begin_long_date" name="beginLongDate" data-dojo-id="fromDate" dojoType="dijit.form.DateTextBox"
                           class="date_picker" disabled="disabled" onmouseover="tooltip.show(getTitle(this));"
                           onmouseout="tooltip.hide();" onChange="toDate.constraints.min = arguments[0];"/>
                </td>
                <td class="no_border" width="20px"> по</td>
                <td class="no_border" width="210px">
                    <input id="end_long_date" name="endLongDate" data-dojo-id="toDate"  dojoType="dijit.form.DateTextBox" class="date_picker"
                           disabled="disabled" onmouseover="tooltip.show(getTitle(this));"
                           onmouseout="tooltip.hide();" onChange="fromDate.constraints.max = arguments[0];"/>
                </td>
            </tr>
        </table>

    </div>
    <div>
        <table>
            <tr>
                <td class="no_border" width="155px">
                    <button id="submit_button" style="width:210px" onclick="submitform('send')" type="button">Отправить
                        отчёт
                    </button>
                </td>
                <td class="no_border" width="220px">
                    <button id="new_report_button" style="width:210px; display:none;" type="button"
                            onclick="submitform('newReport')">Очистить все поля
                    </button>
                </td>
            </tr>
        </table>
    </div>
</form:form>
</body>
</html>