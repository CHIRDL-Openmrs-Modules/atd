<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>export form as csv file</title>
</head>
<body>

	<div id= "form_div"> 
		<c:if test= "${error}=='serverError'">
			<h2>Server error: operation failed.</h2>
		</c:if>
		<form method="post" action = "exportFormCSV.form" >
			form name:  <input type="text" name="formName">
			<c:if test= "${error}=='formNameEmpty'">
				<h2>Form name cannot be empty.</h2>
			</c:if>
			<c:if test= "${error}=='formNameNonexist'">
				<h2>Form name does not exist.</h2>
			</c:if>
			<br/>
			<input id="purpose" type="hidden" name="purpose"/> 
			<input style="padding: 0px 10px 0px  0px" type="submit"   value="show form values" onclick="document.getElementById('purpose').value='showForm'"/>
			<input style="padding: 0px 10px 0px 0px" type="submit"   value="export to csv" onclick="document.getElementById('purpose').value='export'"/>
			<c:if test= "${error}=='exportNotReady'">
				<h2>No information is loaded to export.</h2>
			</c:if>			
			<br/>
			<a href="${pageContext.request.contextPath}/module/atd/configurationManager.form"><input style="padding: 0px 10px 0px 0px, margin:0px 200px 0px 0px" type="button" value="back to manager page"/></a>
		</form>
	
	</div>
	<br/>
	<br/>
	<hr/>
	<br/>
	<br/>
	<div id= "formValue_div">
		<table border="1px">
			<tr style="padding: 5px">
				<td style="padding: 0px 10px 0px 0px">form name</td>
				<td style="padding: 0px 10px 0px 0px">location name</td>
				<td style="padding: 0px 10px 0px 0px">location tag name</td>
				<td style="padding: 0px 10px 0px 0px">attribute name</td>
				<td style="padding: 0px 10px 0px 0px">attribute value name</td>
			</tr>
			<c:forEach var="favd" items="${favdList}" varStatus="status">
				<tr style="padding: 5px">
					<td style="padding: 0px 10px 0px 0px">${favd.formName}</td>
					<td style="padding: 0px 10px 0px 0px">${favd.locationName }</td>
					<td style="padding: 0px 10px 0px 0px">${favd.locationTagName}</td>
					<td style="padding: 0px 10px 0px 0px">${favd.attributeName}</td>
					<td style="padding: 0px 10px 0px 0px">${favd.attributeValue}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>