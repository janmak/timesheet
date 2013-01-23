<%@ page import="org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.userdetails.UserDetails" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
    <title>Вход в корпоративную систему списания занятости Aplana</title>
    <script type="text/javascript">
        dojo.ready(function() {
            <%
                String loginJ="";
                HttpSession httpSession=request.getSession(false);
                if(httpSession.getAttribute("lastLogin")!=null) {
                    loginJ=httpSession.getAttribute("lastLogin").toString();
                }

            %>
            dojo.byId("inputLogin").setAttribute("value","<%=loginJ%>")
        })
    </script>
</head>
<body onload='document.f.j_username.focus();'>
<div class="login_div">
    <c:if test="${not empty error}">
        <div class="errorblock">
            <form action="adminMessage" method="GET">
                Попытка входа  была неудачна. Попробуйте еще раз.<br/>
                Причина: <label id="error">${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}</label>.<br>
                <button type="submit"> Отправить сообщение администратору</button>
            </form>
        </div>
    </c:if>

    <!--[if lt IE 7]>
    <div class="errorblock">
        Внимание! Версия вашего браузера не поддерживается, система может работать нестабильно. Пожалуйста обновите
        версию браузера.
    </div>
    <![endif]-->

    <form name='f' action="<c:url value='j_spring_security_check' />" method='POST'>

        <div class="authBlock">
            <div class="authTitle">Введите логин и пароль для домена Aplana</div>
            <div class="authForm">
                <form action="index.html" method="POST" name="authForm">
                    <label for="inputLogin">Логин: </label>
                    <br/>
                    <input id="inputLogin" type="text" name="j_username" value=""/>
                    <br/>
                    <label for="inputPass">Пароль: </label>
                    <br/>
                    <input id="inputPass" type="password" name="j_password" value=""/>
                    <br/>
                    <br/>
                    <input type='checkbox' name='remember' value="value" style="width: auto"/> Запомнить меня
                    <br/>
                    <input class="btnSend" type="submit" value="Войти"/>
                </form>  <br>


                <%--<c:forEach items="${sessionScope}" var="e">--%>
                    <%--<fmt:message key="${e.key}">--%>
                        <%--<fmt:param value="${e.value}"/>--%>
                    <%--</fmt:message><br>--%>
                <%--</c:forEach>--%>
            </div>
        </div>

    </form>
</div>
</body>
</html>