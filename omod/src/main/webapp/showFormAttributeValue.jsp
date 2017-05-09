<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>check loading data</title>
</head>
<body>
<c:choose>
	<c:when test="${empty favdList}">
		
	</c:when>
	<c:otherwise>
		<table border = "1px">
				<tr style="padding: 5px">
					<td style="padding: 0px 0px 10px 0px">Form Name:</td>
					<td style="padding: 0px 0px 10px 0px">Attribute Name:</td>
					<td style="padding: 0px 0px 10px 0px">Location Name:</td>
					<td style="padding: 0px 0px 10px 0px">Location Tag Name:</td>
					<td style="padding: 0px 0px 10px 0px">Form Attribute Value:</td>
				</tr>
			<c:forEach var="favd" items="${favdList}" varStatus="status">
				<tr style="padding: 5px">
					<td style="padding: 0px 0px 10px 0px">${favd.formName }</td>
					<td style="padding: 0px 0px 10px 0px">${favd.locationName }</td>
					<td style="padding: 0px 0px 10px 0px">${favd.locationTagName }</td>
					<td style="padding: 0px 0px 10px 0px">${favd.attributeName }</td>
					<td style="padding: 0px 0px 10px 0px">${favd.attributeValue }</td>
				</tr>
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>
<br/>
<br/>
	<form method="post" action="showFormAttributeValue.form">
		<input type="submit" >
	</form>
</body>
</html>