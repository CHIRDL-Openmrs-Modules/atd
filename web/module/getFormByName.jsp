<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Chica get form name</title>
</head>
<body>
<form method="post" action="getFormByName.form">
	Please enter the form name:  <input type="text" name="formName"/>   <input type="submit" value="submit"/>
	<c:if test="${not empty noSuchName and noSuchName =='true' }">
		&nbsp;The form name does not exist
	</c:if>
</form>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>