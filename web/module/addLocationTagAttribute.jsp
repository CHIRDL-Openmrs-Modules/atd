<%@ include file="/WEB-INF/template/include.jsp"%>


<openmrs:require privilege="Edit Users, Manage Location Tags, View Locations" otherwise="/login.htm" redirect="/module/atd/editClinicTagAttributeForm.form" />
<link rel="stylesheet" type="text/css" href="$%7BpageContext.request.contextPath%7D/moduleResources/atd/atd.css"/>

<script type="text/javascript">
	$j(document).ready(function() {
		$j('.toggleAddClinicTagAttribute').click(function(event) {
			$j('#addClinicTagAttribute').slideToggle('fast');
			event.preventDefault();
		});
	});
	
	function addClinicTagAttr() {
		var clinicTagAttrName = document.input.name.value;
		if (clinicTagAttrName == "") {
			var errorMsg = "Please enter a location tag attribute name!"
			document.getElementById('errorMessage').innerHTML = errorMsg;
			document.getElementById('errorMsg').innerHTML = "";
		} else {
			document.getElementById('errorMessage').innerHTML = "";
			document.getElementById('errorMsg').innerHTML = "";
			document.input.submit();
		}
	}
	
	function cancel() {
		document.getElementById('errorMessage').innerHTML = "";
	}

</script>

<h2></h2>

<a class="toggleAddClinicTagAttribute" href="#">Add Clinic Tag Attribute</a>
<div id="addClinicTagAttribute" style="border: 1px black solid; background-color: #e0e0e0; display: none">
	<form id="addLocTagAttribute" name="addLocTagAttribute" method="post"  action="">
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
	</form>
</div>
<p></p>
<c:if test="${duplicateName == 'true'}">
	<tr style="padding: 5px">
      <td colspan="2" style="padding: 0px 0px 10px 0px">
		<font color="red"><div id="errorMessage">Location tag attribute name already exists!</div></font>
	  </td>
	</tr>
</c:if>
<font color="red"><div id="errorMessage"></div></font>
