<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<html>
<head>
    <title><fmt:message key="businesstripsandillness"/>employee.name</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/resources/css/businesstripsandillnessadd.css">
    <script type="text/javascript">

        dojo.require("dijit.form.Select");
        dojo.require("dijit.form.DateTextBox");
        dojo.require("dijit.form.TextBox");

        var illnessReportType = 6;
        var businessTripReportType = 7;

        var businesstrip_project = 55;
        var businesstrip_notproject = 56;

        var projectUndefined = -1;

        var errors;

        dojo.declare("Calendar", dijit.Calendar, {
            getClassForDate: function (date) {
                return '';
            }
        });

        dojo.declare("DateTextBox", dijit.form.DateTextBox, {
            popupClass: "Calendar"
        });

        dojo.ready(function () {
            window.focus();
            updateView();
        });

        function updateView(){
            var obj = dojo.byId("reportType");
            var willBeDisplayedId = null;
            if (obj.target == null) {
                willBeDisplayedId = obj.value;
            }
            else {
                willBeDisplayedId = obj.target.value;
            }

            if (willBeDisplayedId == businessTripReportType){
                showBusinessTrips();
                updateProject();
            }
            else if (willBeDisplayedId == illnessReportType){
                showIllnesses();
            }
        }

        function showBusinessTrips(){
            document.getElementById("illness").className = 'off';
            document.getElementById("businesstrip").className = 'creationform';
        }

        function showIllnesses(){
            document.getElementById("illness").className = 'creationform';
            document.getElementById("businesstrip").className = 'off';
        }

        function submitform(){
            if (validate()){
                if (${reportId == null}){
                    mainForm.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/tryAdd/" + "${employeeId}";
                } else {
                    mainForm.action = "<%=request.getContextPath()%>/businesstripsandillnessadd/trySave/" + "${reportId}";
                }
                mainForm.submit();
            }
        }

        function cancelform(){
            mainForm.action = "<%=request.getContextPath()%>/businesstripsandillness/";
            mainForm.submit();
        }

        function validate(){
            delete errors;
            errors = new Array();
            if (checkRequired() && checkDates()){
                return confirm("Вы действительно хотите сохранить данные?");
            } else {
                showErrors();
            }
        }

        function showErrors(){
            document.getElementById("errorboxdiv").className = 'fullwidth onblock errorbox';
            var errortext = "";
            for (var errorindex in errors) {
                errortext = errortext + errors[errorindex] + "\n";
            }
            document.getElementById("errorboxdiv").firstChild.nodeValue = errortext;
        }

        function checkRequired(){
            if (checkCommon()){
                if (document.getElementById("reportType").value == illnessReportType){
                    return checkIllness();
                } else if (document.getElementById("reportType").value == businessTripReportType){
                    return checkBusinessTrip();
                } else {
                    errors.push("Выбран неизвестный тип отчета!");
                    return false;
                }
            } else {
                errors.push("Необходимо правильно заполнить все поля!");
                return false;
            }
        }

        function checkBusinessTrip(){
            var businessTripType = document.getElementById("businessTripType").value;
            if (businessTripType != null){
                if (businessTripType == businesstrip_project){
                    var projectId = document.getElementById("projectId").value;
                    if (projectId == null || projectId == projectUndefined){
                        errors.push("Для проектной командировки необходимо выбрать проект!");
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
                errors.push("Не выбран тип командировки!");
                return false;
            }
        }

        function checkIllness(){
            return document.getElementById("reason").value != null;
        }

        function checkCommon(){
            return (document.getElementById("reportType").value != null && document.getElementById("beginDate").value != null &&
                    document.getElementById("endDate").value != null &&
                    checkComment());
        }

        function checkComment(){
            var comment = document.getElementById("comment").value;
            if (comment == null || comment.length <= 200){
                return true;
            } else {
                errors.push("Комментарий должен быть короче 200 символов!");
                return false;
            }
        }

        function checkDates(){
            var beginDate = parseDate(document.getElementById("beginDate").value);
            var endDate = parseDate(document.getElementById("endDate").value);

            if (endDate > beginDate){
                return true;
            } else {
                errors.push("Дата начала должна быть меньше даты конца!");
                return false;
            }

        }

        function parseDate(dateString){
            var dateParts = dateString.split(".");
            return new Date(dateParts[2], (dateParts[1] - 1), dateParts[0]);
        }

        function updateProject() {
            var businessTripType = dojo.byId("businessTripType").value;
            if (businessTripType == businesstrip_notproject){
                document.getElementById("businesstripproject").className = 'off';
            }
            else {
                document.getElementById("businesstripproject").className = 'onblock';
                var beginDate = dojo.byId("beginDate").value;
                var endDate = dojo.byId("endDate").value;
                var projectIdElement = dojo.byId("projectId");

                projectIdElement.innerHTML =
                        "<img src=\"<c:url value="/resources/img/loading_small.gif"/>\"/>";

                dojo.xhrGet({
                    url: "<%= request.getContextPath()%>/businesstripsandillnessadd/getprojects/${employeeId}/" + beginDate + "/" + endDate + "/",
                    handleAs: "json",

                    load: function(data) {
                        updateProjectList(data);
                    },

                    error: function(error) {
                        projectIdElement.setAttribute("class", "error");
                        projectIdElement.innerHTML = error;
                    }
                });
            }
        }

        function updateProjectList(obj){
            var projectIdElement = dojo.byId("projectId");

            for (var projectindex in obj) {
                var projectOption = dojo.doc.createElement("option");
                dojo.attr(projectOption, {
                    value:obj[projectindex].id
                });
                projectOption.title = obj[projectindex].value;

                projectOption.innerHTML = obj[projectindex].value;
                projectIdElement.appendChild(projectOption);
            }

        }

    </script>
</head>
<body>
<h1><fmt:message key="businesstripsandillness"/></h1>
    <br/>
    <form:form method="post" id="mainForm" commandName="businesstripsandillnessadd" name="mainForm" cssClass="chooseform">

        <div class="lowspace checkboxeslabel">
            Сотрудник:
        </div>

        <div class="lowspace checkboxesselect">
            ${employeeName}
        </div>

        <div class="checkboxeslabel lowspace">Создать:</div>
        <div class="checkboxesselect lowspace">
            <form:select path="reportType" id="reportType" onchange="updateView(this)"
                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();" required="true">
                <form:options items="${businesstripsandillnessadd.reportTypes}" itemLabel="name" itemValue="id" required="true"/>
            </form:select>
        </div>

        <div class="checkboxeslabel lowspace">Дата с:</div>
        <div class="checkboxesselect lowspace">
            <form:input path="beginDate" id="beginDate" class="date_picker" cssClass="fullwidth" data-dojo-type="DateTextBox" required="true"
                        onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();" onchange="updateProject()"/>
        </div>

        <div class="checkboxeslabel lowspace">Дата по:</div>
        <div class="checkboxesselect lowspace">
            <form:input path="endDate" id="endDate" class="date_picker" cssClass="fullwidth" data-dojo-type="DateTextBox" required="true"
                        onMouseOver="tooltip.show(getTitle(this));" onMouseOut="tooltip.hide();" onchange="updateProject()"/>
        </div>

        <div id="illness" class="creationform">

            <div class="checkboxeslabel lowspace">Основание:</div>
            <div class="checkboxesselect lowspace">
                <form:select path="reason" id="reason" onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false">
                    <form:options items="${businesstripsandillnessadd.illnessTypes}" itemLabel="name" itemValue="id" required="true"/>
                </form:select>
            </div>

        </div>

        <div id="businesstrip" class="creationform">

            <div class="checkboxeslabel lowspace">Тип:</div>
            <div class="checkboxesselect lowspace">
                <form:select path="businessTripType" id="businessTripType" onMouseOver="tooltip.show(getTitle(this));"
                             onMouseOut="tooltip.hide();" multiple="false" required="true" onchange="updateProject()">
                    <form:options items="${businesstripsandillnessadd.businessTripTypes}" itemLabel="name" itemValue="id" required="true"/>
                </form:select>
            </div>

            <div id="businesstripproject">
                <div class="checkboxeslabel lowspace">Проект:</div>
                <div class="checkboxesselect lowspace">
                    <form:select path="projectId" id="projectId" onMouseOver="tooltip.show(getTitle(this));"
                                 onMouseOut="tooltip.hide();" multiple="false" />
                </div>
            </div>

        </div>

        <div class="checkboxeslabel lowspace">Комментарий:</div>
        <div class="comment lowspace">
            <form:textarea path="comment" id="comment" rows="7" cssClass="fullwidth"/>
        </div>


        <div style="clear:both"/>

        <div class="bigspace onblock">
            <button id="create" type="button" class="button bigspace" onclick="submitform()">Сохранить</button>
            <button id="cancel" type="button" class="button bigspace" onclick="cancelform()">Отмена</button>
        </div>

        <div id="errorboxdiv" name="errorboxdiv" class="off errorbox">
        </div>
        <div id="servervalidationerrorboxdiv" name="servervalidationerrorboxdiv" class="errorbox">
            <form:errors path="*" delimiter="<br/><br/>" />
        </div>

    </form:form>

</body>
</html>