<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Export form Definition</title>
</head>
<body>
	<div id= "form_div"> 
		<form method="post" action = "exportFormDefinitionCSV2.form">
			form name:  <input type="text" name="formName">
			<input id="purpose" type="hidden" name="purpose"/> 
			<input style="padding: 0px 10px 0px 0px" type="submit"   value="show this form definition" onclick="document.getElementById('purpose').value='showForm'"/>
			<c:if test="${not empty error && error=='formNameEmpty'}" >Form name cannot be empty</c:if>
			<c:if test="${not empty error && error=='formNameNonexist'}" >Form name does not exist</c:if>
			<br/>
			<input style="padding: 0px 10px 0px 0px" type="submit"   value="show all forms definition" onclick="document.getElementById('purpose').value='showAllForms'"/>
			<br/>
			<input style="padding: 0px 10px 0px 0px" type="submit"   value="export to csv" onclick="document.getElementById('purpose').value='export'"/> 
			<c:if test="${not empty error && error=='NoFormChosen' }">No form is chosen</c:if>
			<br/>
			<a href="${pageContext.request.contextPath}/module/atd/configurationManager.form"><input style="padding: 0px 10px 0px 0px, margin:0px 200px 0px 0px" type="button" value="back to manager page"/></a>
		</form>
	</div>
	
	<div id= "formValue_div">
		<table border="1px">
			<tr style="padding: 5px">
				<td style="padding: 0px 10px 0px 0px">formName</td>
				<td style="padding: 0px 10px 0px 0px">formDescription</td>
				<td style="padding: 0px 10px 0px 0px">fieldName</td>
				<td style="padding: 0px 10px 0px 0px">fieldType</td>
				<td style="padding: 0px 10px 0px 0px">conceptName</td>
				<td style="padding: 0px 10px 0px 0px">defaultValue</td>
				<td style="padding: 0px 10px 0px 0px">fieldNumber</td>
				<td style="padding: 0px 10px 0px 0px">parentFieldName</td>
			</tr>
			<c:forEach var="fdd" items="${fddList}" varStatus="status">
				<tr style="padding: 5px">
					<td style="padding: 0px 10px 0px 0px">${fdd.formName}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.formDescription}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.fieldName}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.fieldType}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.conceptName}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.defaultValue}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.fieldNumber}</td>
					<td style="padding: 0px 10px 0px 0px">${fdd.parentFieldName}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>