<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/deleteForms.form" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
<!--
// Nannette Thacker http://www.shiningstar.net
function confirmSubmit()
{
var agree=confirm("Are you sure you want to delete the selected forms?");
if (agree)
	return true ;
else
	return false ;
}
// -->
</script>

<c:forEach items="${formsToDelete}" var="currFormToDelete">
Deleting form: ${currFormToDelete}<br><br>
</c:forEach>

<p><b>Delete the following forms:</b></p>
<form name="input" action="deleteForms.form" method="get">
<table>
<tr style="padding: 5px">
<td>Please highlight the forms to delete:</td>
<td><input type="Submit" name="Delete" value="Delete"
onClick="return confirmSubmit()"></td>
</tr>
<tr style="padding: 5px">
<td colspan="2" style="text-align:right">
<select name="FormsToDelete" multiple>
<c:forEach items="${forms}" var="form">
<option value="${form.formId}">${form.name} (id: ${form.formId})</option>
</c:forEach>
</select>
</td>
</tr>
</table>
</form>
<%@ include file="/WEB-INF/template/footer.jsp" %>