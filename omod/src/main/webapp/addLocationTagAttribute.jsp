<%@ include file="/WEB-INF/template/include.jsp"%>


<openmrs:require privilege="Manage ATD" otherwise="/login.htm" redirect="/module/atd/editClinicTagAttributeForm.form" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"/>
<h2></h2>

<a class="toggleAddClinicTagAttribute" href="#">Add Clinic Tag Attribute</a>
<div id="addClinicTagAttribute" style="border: 1px black solid; background-color: #e0e0e0; display: none">
	<table>
		<tr>
			<th>Clinic Tag Attribute Name</th>
			<td>
				<input type="text" name="name"/>
				<span class="required">*</span>
			</td>
		</tr>
		<tr>
			<th>Description</th>
			<td><textarea name="description" rows="3" cols="41"></textarea></td>
		</tr>
		<tr>
			<th></th>
			<td>
				<input type="hidden" name="hiddenSubmit" />
				<input type="Submit" onclick="{document.input.hiddenSubmit.value=this.value;addClinicTagAttr()}" name="Save"  value="Save" class="toggleAddClinicTagAttribute"/>
				<input type="button" onclick="cancel()" value="Cancel" class="toggleAddClinicTagAttribute" />
			</td>
		</tr>
	</table>
</div>
<p></p>

<c:choose>
    <c:when test="${duplicateName == 'true'}">
	<tr style="padding: 5px">
		<td colspan="2" style="padding: 0px 0px 10px 0px">
			<font color="red"><div id="errorMessage">Location tag attribute name already exists!</div></font>
		</td>
	</tr>
    </c:when>    
    <c:otherwise>
        <font color="red"><div id="errorMessage"></div></font>
    </c:otherwise>
</c:choose>

