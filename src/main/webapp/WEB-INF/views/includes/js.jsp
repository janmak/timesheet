<%@ page import="java.io.File" %>
<%@ page import="static com.aplana.timesheet.constants.TimeSheetConstants.DOJO_PATH" %>
<!-- load Dojo -->
<%!
    private static final String CALENDAR_EXT_RES_PATH = "/resources/js/Calendar.ext.js";
%>

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

<% final long calModified =
        new File(application.getRealPath(CALENDAR_EXT_RES_PATH)).lastModified(); %>

<script type="text/javascript" src="<c:url value="<%= CALENDAR_EXT_RES_PATH %>" />?"<%= calModified %>></script>
    
<script type="text/javascript">
    function getContextPath() {
        return "<%= request.getContextPath() %>";
    }
</script>