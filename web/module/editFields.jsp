<%@ include file="/WEB-INF/template/include.jsp" %>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />

<SCRIPT LANGUAGE="JavaScript">
<!-- Idea by:  Nic Wolfe -->
<!-- This script and many more are available free online at -->
<!-- The JavaScript Source!! http://javascript.internet.com -->

<!-- Begin
function popUp(URL) {
day = new Date();
id = day.getTime();
eval("page" + id + " = window.open(URL, '" + id + "', 'toolbar=1,scrollbars=1,location=0,statusbar=1,menubar=1,resizable=1,width=300,height=300');");
}
// End -->
</script>
<form method="post" action="editFields.form">
<p>Fields for form ${form.name}: <input type="submit" value="Update"></p>
<input type="hidden" name="processFields" value="true"/>
<input type="hidden" name="formToEdit" value="${form.formId}"/>
<table border="1px">
<tr>
<td style="align:center"><b>Field Name</b></td>
<td style="align:center"><b>Field Type</b></td>
<td style="align:center"><b>Concept Id</b></td>
<td style="align:center"><b>Default Value</b></td>
<td style="align:center"><b>Field Number</b></td>
<td style="align:center"><b>Parent Field</b></td>
</tr>
<c:forEach items="${formFields}" var="formField">
<tr>
<td>
<input type="text" name="name_${formField.field.fieldId}" 
	value="${formField.field.name}" size="20">
</td>
<td>
<select name="fieldType_${formField.field.fieldId}">
<option></option>
<c:forEach items="${fieldTypes}" var="fieldType">
<option 
<c:if test="${formField.field.fieldType.fieldTypeId==fieldType.fieldTypeId}">
selected
</c:if>
value="${fieldType.fieldTypeId}">${fieldType.name}</option>
</c:forEach>
</select>
</td>
<td>
<table>
<tr>
<td><input type="text" name="concept_${formField.field.fieldId}" value="${formField.field.concept.name.name}" size="20"></td>
<td><input type=button value="..." onClick="javascript:popUp('http://localhost:8080/openmrs/dictionary/index.htm')"></td>
</tr>
</table>
</td>
<td>
<input type="text" name="defaultValue_${formField.field.fieldId}" 
	value="<c:out value="${formField.field.defaultValue}"/>" size="20">
</td>
<td>
<input type="text" name="fieldNumber_${formField.field.fieldId}" 
	value="${formField.fieldNumber}" size="20">
</td>
<td>
<select name="parent_${formField.field.fieldId}">
<option></option>
<c:forEach items="${formFields}" var="formField2">
<option 
<c:if test="${formField.parent.formFieldId==formField2.formFieldId}">
selected
</c:if>
value="${formField2.formFieldId}">${formField2.field.name}</option>
</c:forEach>
</select>
</td>
</tr>
</c:forEach>
</table>
</form>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>