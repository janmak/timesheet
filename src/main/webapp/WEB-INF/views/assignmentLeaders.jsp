<%@ page import="java.io.File" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.assigmentleaders"/></title>
</head>
<body>

<script type="text/javascript">


    dojo.ready(function () {
        setFilter();
    });

    function setFilter(){
        var division = ${currentUserDivisionId};
        var filter = dojo.byId("filter");
        filter.value = division;
    }

    function filterChange(obj){
        var divisionId = obj.value;
        mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/" + divisionId;
        mainForm.submit();
    }

    function saveResult(obj){
        var divisionId = obj.value;
        mainForm.action = "<%=request.getContextPath()%>/admin/update/assignmentleaders/save/" + divisionId;
        mainForm.submit();
        alert("Изменения успешно сохранены");
    }

</script>



<br/><br/>
<form:form method="post" modelAttribute="assignmentLeadersForm" name="mainForm">

    <div id="filter_element">
        <span class="label">Подразделение</span>
        <form:select path="" id="filter" class="without_dojo" onchange="filterChange(this)"
                     onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
            <form:option label="" value="0"/>
            <form:options items="${divisionList}" itemLabel="name" itemValue="id"/>
        </form:select>
    </div>

    <div id="form_table">
        <table id="assignement_table">
            <tr id="assignment_header">
                <th width="20px">№</th>
                <th width="300px">Подразделение</th>
                <th width="300px">Регион</th>
                <th width="300px">Руководитель</th>
            </tr>

            <c:if test="${fn:length(assignmentLeadersForm.tableRows) > 0}">
                <c:forEach items="${assignmentLeadersForm.tableRows}" varStatus="row">
                    <tr class="assignment_row" id="assignment_row_${row.index}">
                        <td class="text_center_align row_number"><c:out value="${row.index + 1}"/></td>
                        <!-- Подразделение -->
                        <td class="top_align">
                            <form:label path="tableRows[${row.index}].division">
                                <c:out value="${assignmentLeadersForm.tableRows[row.index].division}"/>
                            </form:label>
                            <form:hidden id="division_id_${row.index}" path="tableRows[${row.index}].divisionId"/>
                        </td>
                        <!-- Регион -->
                        <td class="top_align">
                            <form:label path="tableRows[${row.index}].region">
                                <c:out value="${assignmentLeadersForm.tableRows[row.index].region}"/>
                            </form:label>
                            <form:hidden id="region_id_${row.index}" path="tableRows[${row.index}].regionId"/>
                        </td>
                        <!-- Руководитель -->
                        <td class="top_align">
                            <form:select path="tableRows[${row.index}].leaderId" id="leader_id_${row.index}"
                                         onmouseover="tooltip.show(getTitle(this));" onmouseout="tooltip.hide();">
                                <form:option label="" value="0"/>
                                <form:options items="${assignmentLeadersForm.tableRows[row.index].regionDivisionEmployees}"
                                              itemLabel="name" itemValue="id"/>
                            </form:select>
                        </td>
                    </tr>
                </c:forEach>
            </c:if>
        </table>
    </div>
    <div>
        <button id="submit_button" style="width:210px" onclick="saveResult(filter)" type="button">
            Сохранить
        </button>
        <button id="cancel_button" style="width:210px" onclick="cancel()" type="button">
            Отмена
        </button>
    </div>
</form:form>


</body>
</html>