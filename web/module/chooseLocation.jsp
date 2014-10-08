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


</head>
<body>
	<p>
	<h2>choose the location and location tag you want to apply:</h2>
	</p>
	<c:if test="${NoPositionSelected == 'true'}">
  		<p>You have to choose at least one location</p>
	</c:if>
	
	
	<p></p>
	<form action="chooseLocation.form" method="post" id="location_form">
		<table class="location_table">
					<tr style="padding: 5px">
						<td>
							<input type="checkbox" name="positions_applicable" id= "ALL#$#ALL" onchange="checkSubClass('ALL#$#ALL')"/>
						</td>
						<td style="padding: 0px 0px 10px 0px">
							For All locations
						</td>				
					</tr>
					<c:forEach items = "${locationsList}" var = "location" varStatus = "locsStatus">
						<tr style="padding: 5px">
							<td style="padding: 0px 0px 10px 0px"> 
								<input type="checkbox" id ="${location.id}#$#ALL" class="ALL" onchange="checkSubClass('${location.id}#$#ALL'); uncheckSuperClass('${location.id}#$#ALL')"/>
							</td>
							<td style="padding: 0px 0px 10px 0px">for all tags of ${location.name}</td>
						</tr>
						<c:forEach items = "${locationTagsMap[location.id]}" var = "tag" varStatus = "tagStatus">
							<tr style="padding: 5px">
								<td style="padding: 0px 0px 10px 0px"> 
									<input type="checkbox" name = "positions_applicable"   value="${location.id}#$#${tag.id}" id="${location.id}#$#${tag.id}" class="${location.id}"  onchange="uncheckSuperClass('${location.id}#$#${tag.id}')"/>
								</td>
								<td style="padding: 0px 0px 10px 0px">location Tag: ${tag.name}</td>
							</tr>
						</c:forEach>
					
					</c:forEach>
		</table>
		<input type="submit" value="submit"/>
	</form>
</body>
</html>

<%@ include file="/WEB-INF/template/footer.jsp"%>