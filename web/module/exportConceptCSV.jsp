<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require privilege="Manage Concepts" otherwise="/login.htm" redirect="/module/atd/exportConceptCSV.form" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Export Concept to CSV</title>
 
<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/jquery-1.11.1.min.js"></script>
<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables-1.10.6.min.js"></script>
<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/jquery.throttle-debounce.js"></script>
<script src="${pageContext.request.contextPath}/moduleResources/atd/exportConceptCSV.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables-1.10.6.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery.dataTables_themeroller-1.10.6.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/jquery-ui-1.11.4.min.css">

<style>
	.conceptCheckbox{
		float: left;
    	margin: 0 auto;
   		width: 100%;
	}
	
</style>
<script>var ctx = "${pageContext.request.contextPath}"</script>
</head>

<body>
	<h3>Export Concepts</h3>
	
	<div id= "error_div"> 
		<c:if test="${error}=='serverError'">
			<h2>Server error, operation failed.</h2>
			<br/>
		</c:if>
	</div>

	<div id="filter_div">
		<table style="padding-left: 5px; padding-top: 5px;">
			<tr>
			</tr>
			<tr><td><label for="conceptClassSelect">Concept class:</label></td><td><select name="conceptClassSelect" id="conceptClassSelect" onchange="submitFilter();">
				<c:forEach items="${conceptClassList}" var="conceptClass">        	
		        	<option value="${conceptClass.conceptClassId == null ? allConceptClassesOptionConstant : conceptClass.conceptClassId}">${conceptClass.name == null ? "- All Concept Classes -" : conceptClass.name}</option>				
				</c:forEach>
			</select></td>
			<td><input class="inclRetired" type="checkbox" id="inclRetired" name="inclRetired" onclick="submitFilter();" value="Retired"/><label for="inclRetired">Include retired concepts</label></td>
			</tr>
		</table>
	</div>
	
	<div id= "exportConcept_div">
		<table id="conceptTable" style="padding-left: 5px; padding-top: 5px;" width="100%">
				<tr>
				<td>
					<div id= "conceptDefinition_div">
						<table id="conceptDefinitionTable" class="display" cellspacing="0" width="100%">
							<thead>
								<tr>
									<th id="selectConcept"><a href="#" id="selectAllConcepts">Select</a></th>
									<th id="name">Name</th>
									<th id="conceptClass">Concept Class</th>
									<th id="datatype">Data Type</th>
									<th id="description">Description</th>
									<th id="conceptId">Concept Id</th>
									<th id="dateCreated">Date Created</th>
									<th id="units">Units</th>
									<th id="parentConcept">Parent Concept</th>
								</tr>
							</thead>	
						</table>
					</div>
				</td>
				</tr>
		</table>
	</div>
	
	<hr size="3" color="black"/>
		
	<div id= "form_div">
		<form method="post" action ="exportConceptCSV.form">
			<input type="hidden" name="selectedIdsField" value="">
			<table align="right">
				<tr>
					<td><input type="submit" name="exportAll" id="exportAll" value="Export All to CSV"></td>
					<td><input type="button" value="Export Selected to CSV" onclick="updateAndSubmit();"></td>
					<td><input type="button" value="Back to Configuration Manager" onclick="backToConfigManager();"/></td>
				</tr>
			</table>
		</form>
		<br />
		<br />
	</div>	
	
</body>

<script>
function backToConfigManager()
{
	window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
}
</script>

</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>