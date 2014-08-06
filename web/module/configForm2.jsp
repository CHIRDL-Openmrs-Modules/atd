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

<%  
	ArrayList<ArrayList<Object>> positions = (ArrayList<ArrayList<Object>>)(request.getAttribute("positions"));
%>


</head>
<body>
	<div id="div_popup_displayname" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="displayname_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_displayname')" />
				<p>
				<h3>Choose Faxable</h3>
				</p>
				<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue displayname = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"displayname");
					       String shownDisplayname="";
					       if(displayname!=null && displayname.getValue()!=null){
					    	   shownDisplayname=displayname.getValue();
					       }
					       
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px">
							<input type="text" name="inpt_displayname" value="<%= shownDisplayname%>"/>
						</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="displayname_submit" class="location_attri_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_faxable" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="faxable_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_faxable')" />
				<p>
				<h3>Choose Faxable</h3>
				</p>
				<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue faxable = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"auto-fax");
					       int faxableStatus = 0;
					       if(faxable==null || faxable.getValue()==null){
					    	   faxableStatus = 0;
					       }else if (faxable.getValue().equalsIgnoreCase("true")){
					    	   faxableStatus = 1;
					       }else{
					    	   faxableStatus = 2;
					       }
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="<%= "inpt_faxable_" + currLoc.getId()+"#$#"+lTag.getId() %>"
							value="Yes" onclick="input.scanNo.checked = true"
							<%if(faxableStatus==1) {%> checked <%} %>>
							Yes&nbsp <input type="radio"
							name="<%="inpt_faxable_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="No" onclick="input.scoringFile.disabled = true"
							<%if(faxableStatus==2) {%> checked <%} %>> No <input
							type="radio"
							name="<%="inpt_faxable_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="notSet" <%if(faxableStatus==0) {%> checked <%} %>>
							Not Set</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="faxable_submit" class="location_attri_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_fprintable" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="fprintable_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_fprintable')" />
				<p>
				<h3>Choose forceprintable</h3>
				</p>
				<table class="location_table">
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
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="<%= "inpt_fprintable_" + currLoc.getId()+"#$#"+lTag.getId() %>"
							value="Yes" onclick="input.scanNo.checked = true"
							<%if(forcePrintableStatus==1) {%> checked <%} %>>
							Yes&nbsp <input type="radio"
							name="<%="inpt_fprintable_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="No" onclick="input.scoringFile.disabled = true"
							<%if(forcePrintableStatus==2) {%> checked <%} %>> No <input
							type="radio"
							name="<%="inpt_fprintable_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="notSet" <%if(forcePrintableStatus==0) {%> checked <%} %>>
							Not Set</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="fprintable_submit" class="location_attri_submit" value="submit" />
			</form>
		</div>
	</div>	
	
	<div id="div_popup_mobileOnly" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="mobileOnly_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_mobileOnly')" />
				<p>
				<h3>Choose Mobile Only</h3>
				</p>
				<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue mobileOnly = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"mobileOnly");
					       int mobileOnlyStatus = 0;
					       if(mobileOnly==null || mobileOnly.getValue()==null){
					    	   mobileOnlyStatus = 0;
					       }else if (mobileOnly.getValue().equalsIgnoreCase("true")){
					    	   mobileOnlyStatus = 1;
					       }else{
					    	   mobileOnlyStatus = 2;
					       }
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px"><input type="radio"
							name="<%= "inpt_mobileOnly_" + currLoc.getId()+"#$#"+lTag.getId() %>"
							value="Yes" onclick="input.scanNo.checked = true"
							<%if(mobileOnlyStatus==1) {%> checked <%} %>>
							Yes&nbsp <input type="radio"
							name="<%="inpt_mobileOnly_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="No" onclick="input.scoringFile.disabled = true"
							<%if(mobileOnlyStatus==2) {%> checked <%} %>> No <input
							type="radio"
							name="<%="inpt_mobileOnly_"+ currLoc.getId()+"#$#"+lTag.getId() %>"
							value="notSet" <%if(mobileOnlyStatus==0) {%> checked <%} %>>
							Not Set</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="mobileOnly_submit" class="location_attri_submit" value="submit" />
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
				<table class="location_table">
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
				<input type="submit" id="scannable_submit" class="location_attri_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_miniAge" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="minimal_age_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_miniAge')" />
				<p>
				<h3>Choose Minimal Age</h3>
				</p>
				
				<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue miniAge = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"miniAge");
					       FormAttributeValue miniAgeUnit = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"miniAgeUnit");
					       String shownMiniAge="";
					       if(miniAge!=null && miniAge.getValue()!=null){
					    	   shownMiniAge = miniAge.getValue();
					       }
					       String shownAgeUnit="";
					       if(miniAgeUnit!=null && miniAgeUnit.getValue()!=null){
					    	   shownAgeUnit = miniAgeUnit.getValue();
					       }
					       
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %></td>
						<td style="padding: 0px 0px 10px 0px">minimal applicable age:
							<input type="text" name="<%="inpt_miniAge_"+ currLoc.getId()+"#$#"+lTag.getId() %>" value="<%= shownMiniAge %>">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMinUnitSelect_A" name="<%="inpt_miniAgeUnit_"+ currLoc.getId()+"#$#"+lTag.getId() %>" >
								<option value="none" <%if(shownAgeUnit.equals("")){ %>selected="selected" <%} %>>none</option>
								<option value="yo" <%if(shownAgeUnit.equals("yo")){ %>selected="selected" <%} %>>year</option>
								<option value="mo" <%if(shownAgeUnit.equals("mo")){ %>selected="selected" <%} %>>month</option>
								<option value="wk" <%if(shownAgeUnit.equals("wk")){ %>selected="selected" <%} %>>week</option>
								<option value="do" <%if(shownAgeUnit.equals("do")){ %>selected="selected" <%} %>>day</option>
						</select>>
						</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="miniAge_submit" class="location_attri_submit" value="submit" />
			</form>
		</div>
	</div>
	<div id="div_popup_maxAge" class="div_popup">
		<div class="div_popup_form">
			<form action="#" method="post" id="maximal_age_form" class="the_form">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('div_popup_maxAge')" />
				<p>
				<h3>Choose Maximal Age</h3>
				</p>
				<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue maxAge = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"maxAge");
					       FormAttributeValue maxAgeUnit = (FormAttributeValue)request.getAttribute(currLoc.getId()+"#$#"+lTag.getId()+"#$#"+"maxAgeUnit");
					       String shownMaxAge="";
					       if(maxAge!=null && maxAge.getValue()!=null){
					    	   shownMaxAge = maxAge.getValue();
					       }
					       String shownAgeUnit="";
					       if(maxAgeUnit!=null && maxAgeUnit.getValue()!=null){
					    	   shownAgeUnit = maxAgeUnit.getValue();
					       }
					       
					%>				
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %></td>
						<td style="padding: 0px 0px 10px 0px">maximal applicable age:
							<input type="text" name="<%="inpt_maxAge_"+ currLoc.getId()+"#$#"+lTag.getId() %>" value="<%= shownMaxAge%>">
						</td>
						<td style="padding: 0px 0px 10px 0px">age unit: <select
							id="ageMaxUnitSelect_A" name="<%= "inpt_maxAge_"+ currLoc.getId()+"#$#"+lTag.getId() %>" >
								<option value="none" <%if(shownAgeUnit.equals("")){ %>selected="selected" <%} %>>none</option>
								<option value="yo" <%if(shownAgeUnit.equals("yo")){ %>selected="selected" <%} %>>year</option>
								<option value="mo" <%if(shownAgeUnit.equals("mo")){ %>selected="selected" <%} %>>month</option>
								<option value="wk" <%if(shownAgeUnit.equals("wk")){ %>selected="selected" <%} %>>week</option>
								<option value="do" <%if(shownAgeUnit.equals("do")){ %>selected="selected" <% } %>>day</option>
						</select>
						</td>
					</tr>
					<%} %>
				</table>
				<input type="submit" id="maxAge_submit" class="location_attri_submit" value="submit" />
			</form>
		</div>
	</div>


	<p>
	<h2>Configure Form Properties:</h2>
	</p>
	<table>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">form display name:</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_scanable" onclick="div_show('div_popup_displayname')">view
					/ edit</button>
			</td>
		</tr>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">Faxable form:</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_faxable" onclick="div_show('div_popup_faxable')">view
					/ edit</button>
			</td>
		</tr>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">printable form:</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_scanable" onclick="div_show('div_popup_fprintable')">view
					/ edit</button>
			</td>
		</tr>
		
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">For mobile only:</td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_scanable" onclick="div_show('div_popup_mobileOnly')">view
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