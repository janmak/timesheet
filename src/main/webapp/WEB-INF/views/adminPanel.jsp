<%@page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
    <head>
        <title><fmt:message key="title.adminpanel"/></title>
    </head>
    <body>

        <h1><fmt:message key="title.adminpanel"/></h1>

        <ul>
            <li><a href="admin/update/ldap"><fmt:message key="link.updateldap"/></a></li>
            <li><a href="admin/update/checkreport"><fmt:message key="link.checkemails"/></a></li>
            <li><a href="admin/update/oqsync"><fmt:message key="link.oqsync"/></a></li>
            <li><a href="admin/update/properties"><fmt:message key="link.update.properties"/></a></li>
            <c:choose>
                <c:when test="${showalluser == true}">
                    <li><a href="admin/update/hidealluser"><fmt:message key="link.hidealluser"/></a></li>
                </c:when>
                <c:otherwise>
                    <li><a href="admin/update/showalluser"><fmt:message key="link.showalluser"/></a></li>
                </c:otherwise>
            </c:choose>
        </ul>

    </body>
</html>