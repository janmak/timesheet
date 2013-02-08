<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.vacationApproval"/></title>
</head>
<body>

<script type="text/javascript">

    dojo.ready(function () {
        setVisibility();
    });

    function setVisibility(){
        if ("${vacationApprovalForm.isAllButtonsVisible}" != "true"){
            accDiv = dojo.byId("acceptence_div");
            dojo.attr(accDiv, {
                style: "display: none"
            });
        }
    }

    function submitResult(result){
        mainForm.action =
                "<%=request.getContextPath()%>/vacation_approval/save/<%=request.getParameter("uid")%>/" + result;
        mainForm.submit();
    }

</script>


<br/><br/><br/>
<form:form method="post" modelAttribute="vacationApprovalForm" name="mainForm">

    <form:label path="message">
        <c:out value="${vacationApprovalForm.message}"/>
    </form:label>

    <br/><br/>

    <div id="acceptence_div" >
        <button id="submit_button" style="width:210px" onclick="submitResult(true)" type="button">
            Согласен
        </button>
        <button id="refuse_button" style="width:210px" onclick="submitResult(false)" type="button">
            Не согласен
        </button>
        <button id="cancel_button" style="width:210px" onclick="window.close();" type="button">
            Закрыть
        </button>
    </div>

</form:form>

</body>

</html>