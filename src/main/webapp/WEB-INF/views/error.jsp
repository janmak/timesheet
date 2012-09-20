<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
	<%@ include file="./includes/style.jsp" %>
<head>
	<title>Ошибка</title>
</head>
	<body class="tundra">
		<div id="header">
			<%@ include file="./includes/header.jsp"%>
		</div> <!-- header -->
		<div class="errors_box">
			Ваше сообщение не отправлено. Превышен лимит на суммарный размер файлов (8 Мб).
			Вернитесь назад и попробуйте приложить файлы меньшего размера.
		</div>
	</body>
</html>