<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Edit Users, Manage Location Tags, View Locations" otherwise="/login.htm" redirect="/module/atd/createClinicForm.form" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"/>
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop clinic tag creation?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    // -->
</script>
<title>Create New Clinic Tag</title>

<p></p>
<h3>Create New Clinic Tag:</h3>
<form name="input" action="createClinicTagForm.form" method="post" enctype="multipart/form-data">
<table>
<tbody>
<input type="hidden" name="form" value="createClinicTagForm" />
<c:if test="${failedCreation != null}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">${failedCreation}</font>
      </td>
    </tr>
</c:if>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Clinic: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px">
<select id="location" name="location">
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
</td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Tag Name: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="tagName" maxlength="255" size="25" name="tagName" value="${tagName}">&nbsp;<font color="red">*</font></td>
</tr>
<c:if test="${duplicateName == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Location tag name already exists!</font>
      </td>
    </tr>
</c:if>
<c:if test="${missingName == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Please specify a location tag name!</font>
      </td>
    </tr>
</c:if>
<c:if test="${spacesInName == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Spaces are not allowed in the location tag name!</font>
      </td>
    </tr>
</c:if>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Description: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="description" maxlength="255" size="25" name="description" value="${description}"></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Username: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="username" maxlength="50" size="25" name="username" value="${username}">&nbsp;<font color="red">*</font></td>
</tr>
<c:if test="${missingUsername == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Please enter the username for this location tag!</font>
      </td>
    </tr>
</c:if>
<c:if test="${unknownUsername == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Username not found!</font>
      </td>
    </tr>
</c:if>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Program: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px">
<select id="program" name="program">
<c:forEach items="${programs}" var="prog">
    <c:choose>
        <c:when test="${prog.programId == selectedProgram}">
            <option value="${prog.programId}" selected>${prog.name}</option>
        </c:when>
        <c:otherwise>
            <option value="${prog.programId}">${prog.name}</option>
        </c:otherwise>
    </c:choose>
</c:forEach>
</select>
</td>
</tr>
<tr style="padding: 5px">
    <td colspan="2"><br/></td>
</tr>
<tr style="padding: 5px">
    <td colspan="2"><h3>Clinic Tag Attributes:</h3></td>
</tr>
<c:forEach items="${locationTagAttributes}" var="locationTagAttribute">
    <tr style="padding: 5px">
        <td style="padding: 0px 0px 10px 0px"><label>${locationTagAttribute.name}: </label><c:if test="${locationTagAttribute.description != null}"><span TITLE="${locationTagAttribute.description}"><img src="${pageContext.request.contextPath}/moduleResources/atd/info.gif"/></span></c:if> </td>
        <td align="left" style="padding: 0px 0px 10px 0px"><input id="${locationTagAttribute.name}" maxlength="255" size="25" name="${locationTagAttribute.name}" type="text" value="${param[locationTagAttribute.name]}"></td>
    </tr>
    <c:set var="tiphtml" value=""/>
</c:forEach>
<tr style="padding: 5px">
    <td colspan="2"><br/></td>
</tr>
<tr style="padding: 5px">
    <td colspan="2"><h3>Clinic Form Attributes Config:</h3></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Copy Form Config of: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px">
<select id="establishedTag" name="establishedTag">
<c:forEach items="${currentTags}" var="tag">
    <c:choose>
        <c:when test="${tag.locationTagId == selectedTag}">
            <option value="${tag.locationTagId}" selected>${tag.name}</option>
        </c:when>
        <c:otherwise>
            <option value="${tag.locationTagId}">${tag.name}</option>
        </c:otherwise>
    </c:choose>
</c:forEach>
</select>
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
      <input type="Submit" name="Finish" value="Finish" style="width:70px">&nbsp;
      <input type="button" name="Cancel" value="Cancel" onclick="confirmCancel()" style="width:70px">
   </td>
</tr>
</tbody>
</table>
</form>
