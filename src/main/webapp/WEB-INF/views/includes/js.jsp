<%@ page import="java.io.File" %>
<%@ page import="static com.aplana.timesheet.util.TimeSheetConstants.DOJO_PATH" %>
<!-- load Dojo -->
<% long modified = new File(application.getRealPath("/resources/js/timesheet.js")).lastModified();%>

<script
        src="<%=request.getContextPath()%><%= DOJO_PATH %>/dojo/dojo.js"
        djConfig="parseOnLoad: true">
</script>
<script
        src="<%=request.getContextPath()%>/resources/js/timesheet.js?modified=<%= modified %>">
</script>
<script
        src="<%=request.getContextPath()%>/resources/js/dformat.js?modified=<%= modified %>">
</script>
    
    