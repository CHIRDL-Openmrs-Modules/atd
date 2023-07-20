<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="Manage ATD" otherwise="/login.htm" redirect="/module/atd/exportFormDefinitionCSV.form" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Export form Definition</title>
</head>
<body>
	<c:if test = "${error}=='serverError'">
		<h2>Server error, operation failed.</h2>
	</c:if>
	
	<h3>Export Form Definitions</h3>
	
	<div id= "form_div">
		<form method="post" action = "exportFormDefinitionCSV.form">
			<table style="padding-left: 5px;">
				<tr>
					<td><label for="formNameSelect">Form name:</label></td>
					<td><select name="formNameSelect" id="formNameSelect" onchange="this.form.submit();">
						<c:if test = "${selectedFormId == chooseFormOptionConstant}">
							<option value="-" disabled selected style="display:none;">--Please select a form</option>
						</c:if>
				        <c:forEach items="${forms}" var="form">
				        	<c:choose>
				        		<c:when test="${form.formId == selectedFormId}">
	            					<option value="${form.formId == null ? allFormsOptionConstant : form.formId}" selected>${form.name == null ? "- All Forms -" : form.name}</option>
	        					</c:when>
	        					<c:otherwise>
	        						<option value="${form.formId == null ? allFormsOptionConstant : form.formId}">${form.name == null ? "- All Forms -" : form.name}</option>
	        					</c:otherwise>
				        	</c:choose>
				        </c:forEach>
						</select>
					</td>
				</tr>
			</table>
			
			<table style="padding-left: 5px; padding-top: 5px;" width="100%">
				<tr>
				<td>
					<div id= "formDefinition_div">
						<table id="formDefinitionTable" class="display" cellspacing="0" width="100%">
							<thead>
								<tr>
									<th id="formName">Form Name</th>
									<th id="formDesc">Form Description</th>
									<th id="fieldName">Field Name</th>
									<th id="fieldType">Field Type</th>
									<th id="conceptName">Concept Name</th>
									<th id="defaultValue">Default Value</th>
									<th id="fieldNumber">Field Number</th>
									<th id="parentFieldName">Parent Field Name</th>
								</tr>
							</thead>
							
							<tbody>
								<c:forEach var="fdd" items="${fddList}" varStatus="status">
									<tr>
										<td>${fdd.formName}</td>
										<td>${fdd.formDescription}</td>
										<td>${fdd.fieldName}</td>
										<td>${fdd.fieldType}</td>
										<td>${fdd.conceptName}</td>
										<td>${fdd.defaultValue}</td>
										<td>${fdd.fieldNumber}</td>
										<td>${fdd.parentFieldName}</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</td>
				</tr>
			</table>
			
			<hr size="3" color="black"/>
			
			<table align="right">
				<tr><td><input type="Submit" name="exportToCSV" id="exportToCSV" value="Export to CSV"/></td>
					<td><input type="button" value="Back to Configuration Manager" onclick="backToConfigManager();"/></td>
				</tr>
			</table>
			<br/>
			<br/>
		</form>
	</div>
</body>


<!-- DWE CHICA-330 4/23/15 Updated datatables version in the ATD module -->
<openmrs:htmlInclude file="/scripts/jquery/jquery.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.min.js" />
<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables-1.10.6.min.js"></script>

<openmrs:htmlInclude file="/scripts/jquery-ui/css/jquery-ui.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables-1.10.6.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables_themeroller-1.10.6.css">


<script type="text/javascript">
	var definitionsTable;
	
	$j(document).ready(function() {
		definitionsTable = $j('#formDefinitionTable').dataTable(
				{
					// Table options have been updated for DataTables version 1.10
					"columns": [  { "sName": "formName", "bSortable": true},
				                 { "sName": "formDesc", "bSortable": false},
				                 { "sName": "fieldName", "bSortable": true},
				                 { "sName": "fieldType", "bSortable": true},
				                 { "sName": "conceptName", "bSortable": false},
				                 { "sName": "defaultValue", "bSortable": false},
				                 { "sName": "fieldNumber", "bSortable": true},
				                 { "sName": "parentFieldName", "bSortable": false}
				              ],
					"jQueryUI": true,
					"pagingType": "full_numbers", 
					"filter": false});
	} );
	
	function backToConfigManager()
	{
		window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
	}
</script>

</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>