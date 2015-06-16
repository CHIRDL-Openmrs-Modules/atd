<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ page import="org.openmrs.Location"%>
<%@ page import="org.openmrs.LocationTag"%>
<%@ page import="java.util.*"%>
<%@ page import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<html>
<head>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />

<title>Choose Location</title>
<script src="${pageContext.request.contextPath}/moduleResources/atd/chooseLocation.js"></script>
<%  
	ArrayList<ArrayList<Object>> positions = (ArrayList<ArrayList<Object>>)(request.getAttribute("positions"));
%>

<style>

	table.location_table{
		padding: 0px 0px 0px 5px;
		border-spacing: 0px;
		width: 40%;
	}
	
	td.locationSelect{
		padding: 5px 5px 5px 5px;
		color: white;
		align: left;
	}
	
	
</style>

</head>
<body>
	<p>
	<h3>Select Location and Location Tags (${selectedFormName})</h3>
	</p>
	<c:if test="${NoPositionSelected == 'true'}">
  		<p>You have to choose at least one location</p>
	</c:if>
	
	
	<button id="selectAllButton" onclick="checkTags('ALL#$#ALL');">Select All</button>
	<br/>
	<br/>
	<form action="chooseLocation.form" method="post" id="location_form">
		<input type="hidden" name = "formIdStr" value="${formIdStr}"/>
		<input type="hidden" name="selectedFormName" value="${selectedFormName}" />
		
		
		<c:forEach items = "${locationsList}" var = "location" varStatus = "locsStatus">
			<table class="location_table">
				<tr class="boxHeader" >
					<td colspan="2" class="locationSelect">
						<a style="color: white;" onclick="checkTags('${location.id}')" href="#">Select</a> all tags of ${location.name} 
					</td>
				</tr>
			</table>
			
				<table style="padding: 10px" cellspacing="0">
					<c:forEach items = "${locationTagsMap[location.id]}" var = "tag" varStatus = "tagStatus">
						<tr>
							<td style="padding: 0px 0px 5px 0px"> 
								<input type="checkbox" name = "positions_applicable"   value="${location.id}#$#${tag.id}" id="${location.id}#$#${tag.id}" class="${location.id}"/>
							</td>
							<td style="padding: 0px 0px 5px 5px">${tag.name}</td>
						</tr>
					</c:forEach>
					<c:if test="${empty locationTagsMap[location.id]}">
						<tr>
							<td colspan="2">No tags for ${location.name}</td>
						</tr>
					</c:if>
				</table>
			
		</c:forEach>
		
		<hr size="3" color="black"/>
			
		<table align="right">
			<tr><td><input type="button" value="Next" onclick="if(validateSelected()){this.form.submit();}"/></td>
				<td><input type="button" value="Back" onclick="chooseLocationBack();"/></td>
			</tr>
		</table>
		<br/>
		<br/>
	</form>
</body>


<script>

function chooseLocationBack()
{
	window.location = '${pageContext.request.contextPath}/module/atd/getFormByName.form';
}

</script>

</html>

<%@ include file="/WEB-INF/template/footer.jsp"%>