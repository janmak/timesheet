<%@ page import="com.aplana.timesheet.util.DateTimeUtil" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Properties" %>
<%@ page import="com.aplana.timesheet.properties.TSPropertyProvider" %>
<%@ page import="org.springframework.beans.factory.annotation.Autowired" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    String version = TSPropertyProvider.getFooterText();
    String help = TSPropertyProvider.getTimesheetHelpUrl();
%>
<br/>
<hr/>
<br/>

<p style="text-align: center">
    <script type="text/javascript">
        var ua = navigator.userAgent.toLowerCase();
        // Определим Internet Explorer
        if (ua.indexOf("gecko") == -1 && ua.indexOf("chrome")) {
            document.write("<fmt:message key="recomendation.browser.using.text"/>" + "</br>");
        }
    </script>
    <%= version %> </br>
    <a href=<%= help %>><fmt:message key="help.text"/></a>
</p>