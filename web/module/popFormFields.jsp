<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
<link
	href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
	type="text/css" rel="stylesheet" />

<SCRIPT LANGUAGE="JavaScript"> Idea by:  Nic Wolfe -->
<!-- This script and many more are available free online at -->
<!-- The JavaScript Source!! http://javascript.internet.com -->

<!-- Begin
function popUp(URL) {
day = new Date();
id = day.getTime();
eval("page" + id + " = window.open(URL, '" + id + "', 'toolbar=1,scrollbars=1,location=0,statusbar=1,menubar=1,resizable=1,width=300,height=300');");
}
// End </script>
<p>
<h3>Populate Form Fields:</h3>
</p>
<form method="post" action="popFormFields.form">
<p>Fields for form ${form.name}: <c:if
	test="${createWizard != 'true'}">
	<input type="submit" value="Update">
</c:if></p>
<input type="hidden" name="processFields" value="true" /> <input
	type="hidden" name="formToEdit" value="${form.formId}" />
<table>
	<tr>
		<td>
		<table border="1px">
			<tr>
				<td style="align: center"><b>Field Name</b></td>
				<td style="align: center"><b>Field Type</b></td>
				<td style="align: center"><b>Concept Id</b></td>
				<td style="align: center"><b>Default Value</b></td>
				<td style="align: center"><b>Field Number</b></td>
				<td style="align: center"><b>Parent Field</b></td>
			</tr>
			<c:forEach items="${formFields}" var="formField">
				<tr>
					<td><input type="text" name="name_${formField.field.fieldId}"
						value="${formField.field.name}" size="20"></td>
					<td><select name="fieldType_${formField.field.fieldId}">
						<option></option>
						<c:forEach items="${fieldTypes}" var="fieldType">
							<option
								<c:if test="${formField.field.fieldType.fieldTypeId==fieldType.fieldTypeId}">
	                            selected
	                        </c:if>
								value="${fieldType.fieldTypeId}">${fieldType.name}</option>
						</c:forEach>
					</select></td>
					<td>
					<table>
						<tr>
							<td><input type="text"
								name="concept_${formField.field.fieldId}"
								value="${formField.field.concept.name.name}" size="20"></td>
							<td><input type=button value="..."
								onClick="javascript:popUp('http://localhost:8080/openmrs/dictionary/index.htm')"></td>
						</tr>
					</table>
					</td>
					<td><input type="text"
						name="defaultValue_${formField.field.fieldId}"
						value="<c:out value="${formField.field.defaultValue}"/>" size="20">
					</td>
					<td><input type="text"
						name="fieldNumber_${formField.field.fieldId}"
						value="${formField.fieldNumber}" size="20"></td>
					<td><select name="parent_${formField.field.fieldId}">
						<option></option>
						<c:forEach items="${formFields}" var="formField2">
							<option
								<c:if test="${formField.parent.formFieldId==formField2.formFieldId}">
selected
</c:if>
								value="${formField2.formFieldId}">${formField2.field.name}</option>
						</c:forEach>
					</select></td>
				</tr>
			</c:forEach>
		</table>
		</td>
	</tr>
	<tr>
	    <td>
		    <c:choose>
		        <c:when test="${createWizard == 'true'}">
		            <input type="hidden" name="createWizard" value="true" />
		            <table width="100%">
		                <tr>
		                    <td style="padding: 10px 0px 0px 0px">
		                    <hr size="3" color="black" />
		                    </td>
		                </tr>
		                <tr>
		                    <td align="right"><input type="submit" value="Next"></td>
		                </tr>
		            </table>
		        </c:when>
		        <c:otherwise>
		            <input type="hidden" name="createWizard" value="false" />
		        </c:otherwise>
	        </c:choose>
	    </td>
	</tr>
</table>
<input type="hidden" name="formId" value="${formId}" /> 
<input type="hidden" name="formName" value="${formName}" /> 
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>