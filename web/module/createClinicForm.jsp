<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Add Users, Delete Users, Edit Users, Manage Locations, View Locations" otherwise="/login.htm" redirect="/module/atd/createClinicForm.form" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"/>
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop clinic creation?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    // -->
</script>
<html>
<title>Create New Clinic</title>

<body onload="document.input.name.focus();">
<p></p>
<h3>New Clinic:</h3>
<form name="input" action="createClinicForm.form" method="post" enctype="multipart/form-data">
<table>
<tbody>
<c:if test="${failedCreation != null}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">${failedCreation}</font>
      </td>
    </tr>
</c:if>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Name: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="name" maxlength="255" size="25" name="name" value="${name}">&nbsp;<font color="red">*</font></td>
</tr>
<c:if test="${duplicateName == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Location name already exists!</font>
      </td>
    </tr>
</c:if>
<c:if test="${missingName == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Please specify a location name!</font>
      </td>
    </tr>
</c:if>
<c:if test="${spacesInName == 'true'}">
    <tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
           <font color="red">Spaces are not allowed in the location name!</font>
      </td>
    </tr>
</c:if>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Description: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="description" maxlength="255" size="25" name="description" value="${description}"></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Address: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="address" maxlength="50" size="25" name="address" value="${address}"></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Address 2: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="addressTwo" maxlength="50" size="25" name="addressTwo" value="${addressTwo}"></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>City: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="city" maxlength="50" size="25" name="city" value="${city}"></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>State: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="state" maxlength="2" size="2" name="state" value="${state}"></td>
</tr>
<tr style="padding: 5px">
<td style="padding: 0px 0px 10px 0px"><label>Zip: </label></td>
<td align="left" style="padding: 0px 0px 10px 0px"><input id="zip" maxlength="5" size="5" name="zip" type="number" value="${zip}"></td>
</tr>
<tr style="padding: 5px">
    <td colspan="2"><br/></td>
</tr>
<tr style="padding: 5px">
    <td colspan="2"><h3>Clinic Attributes:</h3></td>
</tr>
<c:forEach items="${locationAttributes}" var="locationAttribute">
    <tr style="padding: 5px">
		<td style="padding: 0px 0px 10px 0px"><label>${locationAttribute.name}: </label><c:if test="${locationAttribute.description != null}"><span TITLE="${locationAttribute.description}"><img src="${pageContext.request.contextPath}/moduleResources/atd/info.gif"/></span></c:if> </td>
		<td align="left" style="padding: 0px 0px 10px 0px"><input id="${locationAttribute.name}" maxlength="255" size="25" name="${locationAttribute.name}" type="text" value="${param[locationAttribute.name]}"></td>
    </tr>
</c:forEach>
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
</body></html>