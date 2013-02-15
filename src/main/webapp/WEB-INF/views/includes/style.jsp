<%@ page import="java.io.File" %><%
long modifiedcss = new File(application.getRealPath("/resources/css/style.css")).lastModified();
out.print("<link href=\"" + request.getContextPath() + "/resources/js/dojo-release-1.8.3/dijit/themes/tundra/tundra.css\" rel=\"stylesheet\" type=\"text/css\" />");
out.print("<link href=\"" + request.getContextPath() + "/resources/js/dojo-release-1.8.3/dojo/resources/dojo.css\" rel=\"stylesheet\" type=\"text/css\" />");

out.print("<link href=\"" + request.getContextPath() + "/resources/css/style.css?modified=" + modifiedcss + "\" rel=\"stylesheet\" type=\"text/css\" />");

%>