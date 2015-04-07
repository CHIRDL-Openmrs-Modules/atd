<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
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

<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<script type="text/javascript">
	var attributesTable;
	
	$j(document).ready(function() {
		attributesTable = $j('#formAttributeValueTable').dataTable(
				{"aoColumns": [  { "sName": "formName", "bSortable": false},
				                 { "sName": "locName", "bSortable": true},
				                 { "sName": "locTagName", "bSortable": false},
				                 { "sName": "attribName", "bSortable": true},
				                 { "sName": "attribValue", "bSortable": false}
				              ],
					"bJQueryUI": true, 
					"sPaginationType": "full_numbers", 
					"bFilter": false});
	} );
	
	function backToConfigManager()
	{
		window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
	}
</script>

</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>