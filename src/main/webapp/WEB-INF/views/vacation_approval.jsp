<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title><fmt:message key="title.vacationApproval"/></title>
</head>

<body>

<form:form method="post" modelAttribute="vacationApprovalForm" name="mainForm">

    <form:label path="message">
        <c:out value="${vacationApprovalForm.message}"/>
    </form:label>

    <br/><br/>
    <div id="acceptence_div" ${vacationApprovalForm.buttonsVisible}>
        <a href="<%=request.getContextPath()%>/vacation_approval/save/<%=request.getParameter("uid")%>/true">
            <button id="submit_button" style="width:210px" type="button">
                Согласен
            </button></a>
        <a href="<%=request.getContextPath()%>/vacation_approval/save/<%=request.getParameter("uid")%>/false">
            <button id="refuse_button" style="width:210px" type="button">
                Не согласен
            </button></a>
    </div>

</form:form>

</body>

</html>