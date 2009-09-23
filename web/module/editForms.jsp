<%@ include file="/WEB-INF/template/include.jsp" %>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<p>Please choose the form you would like to edit:</p>
<form name="input" action="editFields.form" method="get">
<select name="formToEdit">
<c:forEach items="${forms}" var="form">
<option value="${form.formId}">${form.name} (${form.formId})</option>
</c:forEach>
</select>
<input type="submit" value="OK">
</form>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>