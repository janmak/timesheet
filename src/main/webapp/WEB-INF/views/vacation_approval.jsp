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

</form:form>
    <br/><br/>
    <div id="acceptence_div" ${vacationApprovalForm.buttonsVisible}>
        <c:set value="<%= request.getParameter("uid") %>" var="uid" />
        <c:url value="/vacation_approval/save/${uid}/true" var="confirm"/>
        <c:url value="/vacation_approval/save/${uid}/false" var="reject"/>

        <form:form id="confirm" name="confirm" action="${confirm}" >
            <button id="submit_button" style="width:210px" type="submit">
                Согласен
            </button>
        </form:form>

        <form:form id="reject" name="reject" action="${reject}" >
            <button id="refuse_button" style="width:210px" type="submit">
                Не согласен
            </button>
        </form:form>
    </div>

</body>

</html>