<%@ page import="com.aplana.timesheet.util.DateTimeUtil" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Properties" %>
<%
    String version = "";
    Properties property = new Properties();
    try {
        property.load(new FileInputStream("./webapps/timesheet.properties"));
    } catch (Exception e) { }
    if (property.getProperty("project.version")!=null)
        version = property.getProperty("project.version");
    else
        version = ".Unknown";
%>
<br/>
<hr/>
<br/>

<p style="text-align: center">APLANATS v<%=version%> Aplana Software Service <%=DateTimeUtil.currentDay()%>
</p>