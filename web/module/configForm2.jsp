<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ page import="org.openmrs.Location"%>
<%@ page import="org.openmrs.LocationTag"%>
<%@ page import="java.util.*"%>
<%@ page
	import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<html>
<head>
<link
	href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
	type="text/css" rel="stylesheet" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/moduleResources/atd/configForm.css" />
<script src="${pageContext.request.contextPath}/moduleResources/atd/configForm.js"></script>
<title>Configure Form</title>
</head>
<body>
	<div id="div_popup_faxable" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="faxable_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_faxable')" />
				<p>
				<h3>Choose Faxable</h3>
				</p>
				<table>
					<%
					ArrayList<ArrayList<Object>> positions = (ArrayList<ArrayList<Object>>)(request.getAttribute("positions"));
				%>
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue forcePrintable = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"forcePrintable");
					       int forcePrintableStatus = 0;
					       if(forcePrintable==null || forcePrintable.getValue()==null){
					    	   forcePrintableStatus = 0;
					       }else if (forcePrintable.getValue().equalsIgnoreCase("true")){
					    	   forcePrintableStatus = 1;
					       }else{
					    	   forcePrintableStatus = 2;
					       }
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%= currLoc.getName() %><%= " at "%><%= lTag.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="<%= "inpt_faxable_" + currLoc.getId()+"#$#"+lTag.getId() %>"
							value="Yes" onclick="input.scanNo.checked = true"
							<%if(forcePrintableStatus==1) {%> checked <%} %>>
							Yes&nbsp <input type="radio"
							name="<%="inpt_faxable_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="No" onclick="input.scoringFile.disabled = true"
							<%if(forcePrintableStatus==2) {%> checked <%} %>> No <input
							type="radio"
							name="<%="inpt_faxable_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="notSet" <%if(forcePrintableStatus==0) {%> checked <%} %>>
							Not Set</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="faxable_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_scannable" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="scannable_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_scannable')" />
				<p>
				<h3>Choose Scannable</h3>
				</p>
				<table>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic A:</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="inpt_scannable_A" value="Yes"
							onclick="input.scanNo.checked = true"> Yes&nbsp <input
							type="radio" name="inpt_scannable_A" value="No"
							onclick="input.scoringFile.disabled = true"> No</td>
					</tr>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic B:</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="inpt_scannable_B" value="Yes"
							onclick="input.scanNo.checked = true"> Yes&nbsp <input
							type="radio" name="inpt_scannable_B" value="No"
							onclick="input.scoringFile.disabled = true"> No</td>
					</tr>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic C:</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="inpt_scannable_C" value="Yes"
							onclick="input.scanNo.checked = true"> Yes&nbsp <input
							type="radio" name="inpt_scannable_C" value="No"
							onclick="input.scoringFile.disabled = true"> No</td>
					</tr>
				</table>
				<input type="submit" id="scannable_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_miniAge" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="minimal_age_form" class="the_form">
				<img src="images/3.png" class="close"
					onclick="closeForm('div_popup_miniAge')" />
				<p>
				<h3>Choose Minimal Age</h3>
				</p>
				<table>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic A:</td>
						<td style="padding: 0px 0px 10px 0px">minimal applicable age:
							<input type="text" name="ageMin_A" value="">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMinUnitSelect_A" name="ageMinUnit_A">
								<option>year</option>
								<option>month</option>
								<option>week</option>
								<option>day</option>
						</select>>
						</td>
					</tr>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic B:</td>
						<td style="padding: 0px 0px 10px 0px">minimal applicable age:
							<input type="text" name="ageMin_B" value="">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMinUnitSelect_B" name="ageMinUnit_B">
								<option>year</option>
								<option>month</option>
								<option>week</option>
								<option>day</option>
						</select>>
						</td>
					</tr>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic C:</td>
						<td style="padding: 0px 0px 10px 0px">minimal applicable age:
							<input type="text" name="ageMin_C" value="">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMinUnitSelect_C" name="ageMinUnit_C">
								<option>year</option>
								<option>month</option>
								<option>week</option>
								<option>day</option>
						</select>>
						</td>
					</tr>
				</table>
				<input type="submit" id="miniAge_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_maxAge" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="maximal_age_form" class="the_form">
				<img src="images/3.png" class="close"
					onclick="closeForm('div_popup_maxAge')" />
				<p>
				<h3>Choose Maximal Age</h3>
				</p>
				<table>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic A:</td>
						<td style="padding: 0px 0px 10px 0px">maximal applicable age:
							<input type="text" name="ageMax_A" value="">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMaxUnitSelect_A" name="ageMaxUnit_A">
								<option>year</option>
								<option>month</option>
								<option>week</option>
								<option>day</option>
						</select>>
						</td>
					</tr>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic B:</td>
						<td style="padding: 0px 0px 10px 0px">minimal applicable age:
							<input type="text" name="ageMax_B" value="">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMaxUnitSelect_B" name="ageMaxUnit_B">
								<option>year</option>
								<option>month</option>
								<option>week</option>
								<option>day</option>
						</select>>
						</td>
					</tr>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px">Clinic C:</td>
						<td style="padding: 0px 0px 10px 0px">minimal applicable age:
							<input type="text" name="ageMax_C" value="">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMaxUnitSelect_C" name="ageMaxUnit_C">
								<option>year</option>
								<option>month</option>
								<option>week</option>
								<option>day</option>
						</select>>
						</td>
					</tr>
				</table>
				<input type="submit" id="maxAge_submit" value="submit" />
			</form>
		</div>
	</div>


	<p>
	<h3>Configure Form Properties:</h3>
	</p>
	<table>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">Faxable form:</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_faxable" onclick="div_show('div_popup_faxable')">view
					/ edit</button>
			</td>
		</tr>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">Scannable form:</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_scanable" onclick="div_show('div_popup_scannable')">view
					/ edit</button>
			</td>
		</tr>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">Mimimal Age</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_miniAge" onclick="div_show('div_popup_miniAge')">view
					/ edit</button>
			</td>
		</tr>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">Maximal Age</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_maxAge" onclick="div_show('div_popup_maxAge')">view
					/ edit</button>
			</td>
		</tr>
	</table>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>