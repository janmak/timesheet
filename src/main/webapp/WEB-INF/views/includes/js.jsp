<%@ page import="java.io.File" %>
<!-- load Dojo -->
<% long modified = new File(application.getRealPath("/resources/js/timesheet.js")).lastModified();%>

<script
        src="<%=request.getContextPath()%>/resources/js/dojo-release-1.8.3/dojo/dojo.js"
        djConfig="parseOnLoad: true">
</script>
<script
        src="<%=request.getContextPath()%>/resources/js/timesheet.js?modified=<%= modified %>">
</script>
<script
        src="<%=request.getContextPath()%>/resources/js/dformat.js?modified=<%= modified %>">
</script>
    
    