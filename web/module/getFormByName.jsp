<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Chica get form name</title>

<style>
	span.uploadError{
		color: red;
		font-style: italic;
	}
</style>

<script src="${pageContext.request.contextPath}/moduleResources/atd/getFormByName.js"></script>

</head>
<body>
<c:if test = "${ioError=='true'}">
	<script>displayError('Server error occurred. Please see the error log for more details.');</script>
</c:if>
<c:if test = "${not empty csvFileError and csvFileError=='typeError'}">
	<script>displayError('Not a csv format file.');</script>
</c:if>
<c:if test = "${not empty csvFileError and csvFileError=='csvFileEmpty'}">	
	<script>displayError('csv file empty');</script>
</c:if>
<c:if test = "${not empty csvFileError and csvFileError=='notMultipart'}">	
	<script>displayError('Form error.');</script>
</c:if>
<c:if test = "${not empty csvFileError and csvFileError=='notFAV'}">
	<script>displayError('Unable to locate values for one or more columns. Verify that the file is in the proper format.');</script>
</c:if>

<h3>Edit Form Attribute Values</h3>
<form id="manualOrCSV" method="post" action="getFormByName.form" enctype="multipart/form-data">

	<table>
		<tr>
			<td style="vertical-align: top"><input style="vertical-align: top" type="radio" name="editType" id="manual" value="manual" ${selectedOption == "manual" ? "checked" : ""} onclick="$j('#csvFile').prop('disabled', true); $j('#formNameSelect').attr('disabled', false);"/></td>
			<td><label for="manual">Edit manually online</label></td>
		</tr>
		<tr>
			<td style="padding-top: 5px"></td>
			<td style="padding-top: 5px"><label for="formNameSelect">Form name:&nbsp;</label><select name="formNameSelect" id="formNameSelect">
						<c:if test = "${selectedFormId == chooseFormOptionConstant}">
							<option value="${chooseFormOptionConstant}" disabled selected style="display:none;">--Please select a form</option>
						</c:if>
				        <c:forEach items="${forms}" var="form">
				        	<c:choose>
				        		<c:when test="${form.formId == selectedFormId}">
	            					<option value="${form.formId}" selected>${form.name}</option>
	        					</c:when>
	        					<c:otherwise>
	        						<option value="${form.formId}">${form.name}</option>
	        					</c:otherwise>
				        	</c:choose>
				        </c:forEach>
						</select></td>
		</tr>
		<tr>
			<td style="vertical-align: top"><input style="vertical-align: top" type="radio" name="editType" id="csv" value="csv" ${selectedOption == "csv" ? "checked" : ""} onclick="$j('#csvFile').prop('disabled', false); $j('#formNameSelect').attr('disabled', true);"/></td>
			<td><label for="csv">Edit by uploading a csv file</label></td>
		</tr>
		<tr>
			<td style="padding-top: 5px"></td>
			<td style="padding-top: 5px">Upload csv file:&nbsp;<input type=file id="csvFile" name="csvFile" accept=".csv" ${selectedOption == "csv" ? "" : "disabled"}/>
			</td>
			</tr>
	</table>
	
	<hr size="3" color="black"/>
			
	<table align="right">
		<tr><td><input type="button" id="next" value="Next" onclick="validateSelections();"/></td>
			<td><input type="button" value="Back to Configuration Manager" onclick="backToConfigManager('${pageContext.request.contextPath}/module/atd/configurationManager.form');"/></td>
		</tr>
	</table>
	<br/>
	<br/>
		
</form>

</body>

</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>