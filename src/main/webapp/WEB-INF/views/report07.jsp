<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>

<html>
<head>
    <title><fmt:message key="title.report07"/></title>
</head>

<body>
<h1><fmt:message key="title.reportparams"/></h1>

<h2><fmt:message key="title.report07"/></h2>
<br/>

<script type="text/javascript">
    dojo.ready(function (){
        dojo.require("dijit.form.DateTextBox");
        var filter = dojo.byId("filterDivisionOwner1");
        target = "divisionOwner"
        dojo.connect(filter, "onchange", function () {
            if (filter.checked) {
                dojo.removeAttr(target, "disabled");
            } else {
                dojo.attr(target, {disabled:"disabled"});
            }
        })
    })
</script>

<form:form commandName="reportForm" method="post" action="">

    <c:if test="${fn:length(errors) > 0}">
        <div class="errors_box">
            <c:forEach items="${errors}" var="error">
                <fmt:message key="${error.code}">
                    <fmt:param value="${error.arguments[0]}"/>
                </fmt:message><br/>
            </c:forEach>
        </div>
    </c:if>

    <table class="report_params" cellspacing="3">
        <tr>
            <td style="width: 225px">Название центра владельца проекта <span style="color:red">*</span> </td>
            <td>
                <form:select path="divisionOwner">
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
            <td>
                <form:checkbox path="filterDivisionOwner" cssStyle="margin: 5px"/> Показывать только проекты/присейлы центра
            </td>
        </tr>
    </table>
    <table  class="report_params" cellspacing="3">
        <tr>
            <td style="width: 225px">Центр сотрудников <span style="color:red">*</span> </td>
            <td colspan="2">
                <form:select path="divisionEmployee">
                    <form:option value="0">Все центры</form:option>
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select>
            </td>
        </tr>
        <tr>
            <td>Отчетный период <span style="color:red">*</span> </td>
            <td colspan="2">
                <form:select path="periodType">
                    <form:option value="1">Месяц</form:option>
                    <form:option value="3">Квартал</form:option>
                    <form:option value="6">Пол года</form:option>
                    <form:option value="12">Год</form:option>
                </form:select>
            </td>
        </tr>
        </table>
    <table class="report_params" cellspacing="3">
        <tr>
            <td style="width: 225px">Начало периода <span style="color:red">*</span> </td><td><form:input path="beginDate" id="beginDate" name="beginDate" class="date_picker"
                            data-dojo-id="fromDate"
                            dojoType="dijit.form.DateTextBox"
                            required="false"/></td>
            <td style="width: 225px; padding-left: 10px">Окончание периода <span style="color:red">*</span> </td><td><form:input path="endDate" id="endDate" name="endDate" class="date_picker"
                            data-dojo-id="toDate"
                            dojoType="dijit.form.DateTextBox"
                            required="false"/></td>
        </tr>
    </table>
    <div class="radiogroup">
        <div class="label"><fmt:message key="report.formattitle"/></div>
        <ul class="radio">
            <li><input type=radio name="printtype" id="printtype1" value="1" checked/><label
                    for="printtype1">HTML</label></li>
            <li><input type=radio name="printtype" id="printtype2" value="2"/><label for="printtype2">MS
                Excel</label>
            </li>
            <!--li><input type=radio name="printtype" id="printtype3" value="3"/><label for="printtype3">PDF</label>
            </li> -->
        </ul>
    </div>
    <button id="make_report_button" style="width:210px" type="submit">Сформировать отчет</button>
</form:form>
</body>

</html>
