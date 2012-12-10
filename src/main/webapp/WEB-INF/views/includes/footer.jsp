<%@ page import="com.aplana.timesheet.util.DateTimeUtil" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.util.Properties" %>
<%
    String version = "";
    Properties property = new Properties();
    try {
        property.load(new FileInputStream("./webapps/timesheet.properties"));
    } catch (Exception e) { }
    String footer = property.getProperty("footer.text");
%>
<br/>
<hr/>
<br/>

<p style="text-align: center"><%= footer %></p>