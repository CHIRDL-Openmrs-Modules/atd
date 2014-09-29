<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Export Concept to CSV</title>
</head>
<body>

	<div id= "form_div"> 
		<form method="post" action = "exportConceptCSV.form" >
			<input style="padding: 0px 10px 0px 0px" type="submit"   value="export to csv">
			<a href="${pageContext.request.contextPath}/module/atd/configurationManager.form"><input style="padding: 0px 10px 0px 0px, margin:0px 200px 0px 0px" type="button" value="back to manager page"/></a>
		</form>
	</div>


	<div id= "formValue_div">
		<table border="1px">
			<tr style="padding: 5px">
				<td style="padding: 0px 10px 0px 0px">name</td>
				<td style="padding: 0px 10px 0px 0px">concept class</td>
				<td style="padding: 0px 10px 0px 0px">datatype</td>
				<td style="padding: 0px 10px 0px 0px">description</td>
				<td style="padding: 0px 10px 0px 0px">concept_id</td>
				<td style="padding: 0px 10px 0px 0px">units</td>
				<td style="padding: 0px 10px 0px 0px">parent concept</td>
			</tr>
			<c:forEach var="cd" items="${cdList}" varStatus="status">
				<tr style="padding: 5px">
					<td style="padding: 0px 10px 0px 0px">${cd.name}</td>
					<td style="padding: 0px 10px 0px 0px">${cd.conceptClass }</td>
					<td style="padding: 0px 10px 0px 0px">${cd.datatype}</td>
					<td style="padding: 0px 10px 0px 0px">${cd.description}</td>
					<td style="padding: 0px 10px 0px 0px">${cd.conceptId}</td>
					<td style="padding: 0px 10px 0px 0px">${cd.units}</td>
					<td style="padding: 0px 10px 0px 0px">${cd.parentConcept}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>