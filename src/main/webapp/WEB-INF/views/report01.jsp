<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<html>
<head>
    <title><fmt:message key="title.report01"/></title>
</head>

<body>

<script type="text/javascript">
    dojo.ready(function () {
        dojo.require("dijit.form.DateTextBox");

        reportForm.divisionId.value = <sec:authentication property="principal.employee.division.id"/>;
    });
</script>

<h1><fmt:message key="title.reportparams"/></h1>
<h2><fmt:message key="title.report01"/></h2>
<br/>

<c:url value="/managertools/report/1" var="formUrl"/>
<form:form commandName="reportForm" method="post" action="${formUrl}">

    <c:if test="${fn:length(errors) > 0}">
        <div class="errors_box">
            <c:forEach items="${errors}" var="error">
                <fmt:message key="${error.code}">
                    <fmt:param value="${error.arguments[0]}"/>
                </fmt:message><br/>
            </c:forEach>
        </div>
    </c:if>

    <div id="form_header">
        <table class="report_params" cellspacing="3">
			<tr>
				<td colspan="4"/>
				<td>
					<span class="label">Регион</span>
					<span style="color:red">*</span>
				</td>
				<td><span class="all_regions">
						<span class="checkbox_without_dojo">
							<form:checkbox id="allRegions" name="allRegions"  path="allRegions"/>
						</span>
						<span class="checkbox_without_dojo">Все регионы</span>
					</span>
				</td>
			</tr>
            <tr>
                <td><span class="label">Центр</span><span style="color:red">*</span></td>
                <td><form:select id="divisionId" name="divisionId" cssClass="without_dojo"
                                 onmouseover="tooltip.show(getTitle(this));"
                                 onmouseout="tooltip.hide();" path="divisionId">
                    <form:option label="" value="0"/>
                    <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
                </form:select></td>
				<td><span class="label">Категория переработок</span><span style="color:red">*</span></td>
				<td>					
					<form:select path="category" id="category" name="category" cssClass="without_dojo"
                                 onmouseover="tooltip.show(getTitle(this));"
                                 onmouseout="tooltip.hide();">
						<%--<form:option id="0"/>--%>
						<form:options items="${categoryList}" itemLabel="title"/>
					</form:select>
				</td>
                <td rowspan="2" colspan="2">					
					<span class="without_dojo">
						<form:select id="regionIds" name="regionIds" 
									 onmouseover="tooltip.show(getTitle(this));"
									 onmouseout="tooltip.hide();" path="regionIds" multiple="true"
									 cssClass ="region">
							<form:options items="${regionList}" itemLabel="name" itemValue="id"/>
						</form:select>
					</span>
				</td>				
            </tr>
            <tr>
                <td><span class="label">Начало периода</span><span style="color:red">*</span></td>
                <td><form:input path="beginDate" id="beginDate" name="beginDate" class="date_picker"
                                data-dojo-id="fromDate"
                                dojoType="dijit.form.DateTextBox"
                                required="false"
                                onmouseover="tooltip.show(getTitle(this));"
                                onmouseout="tooltip.hide();"/></td>
                <td><span class="label">Окончание периода</span><span style="color:red">*</span></td>
                <td><form:input path="endDate" id="endDate" name="endDate" class="date_picker"
                                data-dojo-id="toDate"
                                dojoType="dijit.form.DateTextBox"
                                required="false"
                                onmouseover="tooltip.show(getTitle(this));"
                                onmouseout="tooltip.hide();"/></td>
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
                <li><input type=radio name="printtype" id="printtype3" value="3"/><label for="printtype3">PDF</label>
                </li>
            </ul>
        </div>
    </div>

    <button id="make_report_button" style="width:210px" type="submit">Сформировать отчет</button>
</form:form>
</body>

</html>
