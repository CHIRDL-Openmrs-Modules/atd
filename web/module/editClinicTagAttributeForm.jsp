<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Edit Users, Manage Location Tags, View Locations" otherwise="/login.htm" redirect="/module/atd/editClinicTagAttributeForm.form" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"/>
<script src="${pageContext.request.contextPath}/moduleResources/atd/editLocationTagAttribute.js"></script>


<title>Edit Clinic Tag Attributes</title>
<p></p>
<h3>Edit Clinic Tag Attribute Values:</h3>
<form name="input" action="editClinicTagAttributeForm.form" method="post" onsubmit="return checkform()" enctype="multipart/form-data">
<table>
<tbody>
<input type="hidden" name="form" value="editClinicTagAttributeForm" />
<c:if test="${UpdateFailed != null}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">${UpdateFailed}</font>
      </td>
    </tr>
</c:if>
<tr style="padding: 5px">
	<td style="padding: 0px 0px 10px 0px"><label>Clinic: </label></td>
	<td align="left" style="padding: 0px 0px 10px 0px">
		<select id="location" name="location" onchange="submit();">
			<option value="">Select Clinic</option>
			<c:forEach items="${locations}" var="loc">
				<c:choose>
					<c:when test="${loc.locationId == selectedLocation}">
						<option value="${loc.locationId}" selected>${loc.name}</option>
					</c:when>
					<c:otherwise>
						<option value="${loc.locationId}">${loc.name}</option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</select>
	<font color="red">*</font>
	</td>
</tr>
<tr style="padding: 5px">
	<td style="padding: 0px 0px 10px 0px"><label>Tag Name: </label></td>
	<td align="left" style="padding: 0px 0px 10px 0px">

		<select id="tagName" name="tagName" onchange="submit()">
			<option value="">Select Tag Name</option>
			<c:forEach items="${locationTags}" var="locTagNames">
				<c:choose>
					<c:when test="${locTagNames.name == tagName}">
						<option value="${locTagNames}" selected>${locTagNames.name}</option>
					</c:when>
					<c:otherwise>
						<option value="${locTagNames}">${locTagNames.name}</option>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		</select>
		<font color="red">*</font>

	</td>
</tr>

 <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red"><div id="errorMsg"></div></font>
      </td>
</tr>

<tr style="padding: 5px">
    <td colspan="2">
		
	<br/>
	</td>
</tr>
<tr style="padding: 5px">
    <td colspan="2"><h3>Clinic Tag Attributes:</h3></td>
</tr>
<c:forEach items="${locationTagAttributes}" var="locationTagAttribute">
    <tr style="padding: 5px">
        <td style="padding: 0px 0px 10px 0px"><label>${locationTagAttribute.name}: </label>
			<c:if test="${locationTagAttribute.description != null}">
				<span TITLE="${locationTagAttribute.description}"><img src="${pageContext.request.contextPath}/moduleResources/atd/info.gif"/></span>
			</c:if> 
		</td>
        <td align="left" style="padding: 0px 0px 10px 0px">
			<c:choose>
				<c:when test="${not empty locationTagAttributes && not empty locationTagAttributeValues}">
					<c:choose>
						<c:when test="${not empty locTagAttValMap[locationTagAttribute.locationTagAttributeId]}">
							<c:forEach items="${locationTagAttributeValues}" var="locationTagAttributeValue">
								<c:choose>
									<c:when test="${locationTagAttribute.locationTagAttributeId == locationTagAttributeValue.locationTagAttributeId}">
										<input id="${locationTagAttribute.name}" maxlength="255" size="40" name="${locationTagAttribute.name}" type="text" value="${locationTagAttributeValue.value}">
									</c:when>
								</c:choose>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<input id="${locationTagAttribute.name}" maxlength="254" size="40" name="${locationTagAttribute.name}" type="text" value="">
						</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<input id="" maxlength="256" size="40" name="" type="text" value="">
				</c:otherwise>
			</c:choose>
		</td>
    </tr>
 </c:forEach>
</tbody>
</table>
<table width="580">
<tbody>
<tr style="padding: 5px">
	<td colspan="3" style="padding: 0px 0px 10px 0px">
		<%@ include file="addLocationTagAttribute.jsp" %>
	</td>
</tr>

<tr style="padding: 5px">
    <td colspan="3" align="center"><hr size="3" color="black"/></td>
</tr>
<tr style="padding: 5px">
   <td align="left">
       <input type="reset" name="Clear" value="Clear" style="width:70px">
   </td>
   <td align="right">
	  <input type="Submit" name="Finish" value="Finish" onclick="" style="width:70px">&nbsp;
	  <input type="button" name="Cancel" value="Cancel" onclick="confirmCancel('${pageContext.request.contextPath}/module/atd/configurationManager.form')" style="width:70px">
   </td>
</tr>
</tbody>
</table>
</form>
