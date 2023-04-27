<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="Manage ATD" otherwise="/login.htm" redirect="/module/atd/exportFormCSV.form" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Export Form Attributes as CSV File</title>
</head>

<h3>Export Form Attributes</h3>

<body>

	<div id= "form_div"> 
		<c:if test= "${error}=='serverError'">
			<h2>Server error: operation failed.</h2>
		</c:if>
		<form method="post" action = "exportFormCSV.form" >   	
			<table style="padding-left: 5px;">
				<tr>
					<td valign="top"><label for="formNameSelect">Form name:</label></td>
					<td><select multiple name="formNameSelect" id="formNameSelect" onchange="this.form.submit();">
			        		<c:forEach items="${forms}" var="form">
			        			<c:choose>
							        <c:when test="${not empty selectedIdMap[form.formId]}">
				            				<option value="${form.formId}" selected>${form.name}</option>
				        			</c:when>
				        			<c:otherwise>
				        				<option value="${form.formId}">${form.name}</option>
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
					<div id= "formValue_div">
						<table id="formAttributeValueTable" class="display" cellspacing="0" width="100%">
							<thead>
								<tr>
									<th id="formName">Form Name</th>
									<th id="locName">Location Name</th>
									<th id="locTagName">Location Tag Name</th>
									<th id="attribName">Attribute Name</th>
									<th id="attribValue">Attribute Value</th>
								</tr>
							</thead>
							
							<tbody>
								<c:forEach var="favd" items="${favdList}" varStatus="status">
									<tr>
										<td>${favd.formName}</td>
										<td>${favd.locationName }</td>
										<td>${favd.locationTagName}</td>
										<td>${favd.attributeName}</td>
										<td>${favd.attributeValue}</td>
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
<openmrs:htmlInclude file="/scripts/jquery-ui/jquery-ui.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/jquery-ui.min.css" />


<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables-1.10.6.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables-1.10.6.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables_themeroller-1.10.6.css">

<script type="text/javascript">
	var attributesTable;
	
	$(document).ready(function() {
		attributesTable = $('#formAttributeValueTable').dataTable(
				{
					// Table options have been updated for DataTables version 1.10
					"columns": [  { "sName": "formName", "bSortable": false},
				                 { "sName": "locName", "bSortable": true},
				                 { "sName": "locTagName", "bSortable": false},
				                 { "sName": "attribName", "bSortable": true},
				                 { "sName": "attribValue", "bSortable": false}
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