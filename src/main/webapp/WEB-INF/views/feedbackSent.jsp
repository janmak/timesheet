<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <script type="text/javascript">
        //закрываем окно
        function cancel() {
            if (confirmCancelWindow()) {
                self.close();
            }
        };
    </script>
    <title><fmt:message key="feedback"/></title>
</head>

<body>
<br/>
<span class="label">Ваше сообщение успешно отправлено</span>
<br/>
<br/>
<form:form method="post" action="sendNewFeedbackMessage">
    <button id="submit_button" type="submit">Отправить новое сообщение</button>
    <%--<button id="cancel_button" name="cancel_button" style="width:150px" type="button" onclick="cancel()">Закрыть</button>--%>
</form:form>

</body>
</html>