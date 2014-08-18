<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ page import="org.openmrs.Location"%>
<%@ page import="org.openmrs.LocationTag"%>
<%@ page import="java.util.*"%>
<%@ page import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<html>
<head>
<link
	href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
	type="text/css" rel="stylesheet" />
<title>Choose Location</title>
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
	<form action="" method="post" id="location_form">
		<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px"> 
						<input type="checkbox" name="positions_applicable" value="<%= currLoc.getId() %>#$#<%= lTag.getId() %>"/>
						</td>
					</tr>
					<%} %>
		</table>
		<input type="submit" value="submit"/>
	</form>
</body>
</html>

<%@ include file="/WEB-INF/template/footer.jsp"%>