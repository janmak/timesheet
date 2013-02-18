<%@ page import="com.aplana.timesheet.util.DateTimeUtil" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Properties" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    Properties property = new Properties();
    try {
        property.load(new FileInputStream("./webapps/timesheet.properties"));
    } catch (Exception e) { }
    String version = property.getProperty("footer.text");
    String help = property.getProperty("timesheet.help.url");
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