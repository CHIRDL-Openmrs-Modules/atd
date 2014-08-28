<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ page import="org.openmrs.Location"%>
<%@ page import="org.openmrs.LocationTag"%>
<%@ page import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute"%>
<%@ page import="java.util.*"%>
<%@ page import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<html>
<head>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/moduleResources/atd/configForm.css" />
<script src="${pageContext.request.contextPath}/moduleResources/atd/configForm.js"></script>
<link href="${pageContext.request.contextPath}/moduleResources/atd/kendo.common.min.css" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/kendo.default.min.css" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/kendo.dataviz.min.css" rel="stylesheet" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/kendo.dataviz.default.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/moduleResources/atd/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/moduleResources/atd/angular.min.js"></script>
<script src="${pageContext.request.contextPath}/moduleResources/atd/kendo.all.min.js"></script>



<title>Configure Form</title>

<%  
	List<ArrayList<Object>> positions = (ArrayList<ArrayList<Object>>)(request.getAttribute("positions"));
	Map<String, Object> formAttributesValueMap = (Map<String, Object>)(request.getAttribute("formAttributesValueMap"));
	Map<String, List<String>> formAttributesValueEnumMap = (Map<String, List<String>>)(request.getAttribute("formAttributesValueEnumMap"));
	List<FormAttribute> editableFormAttributes = (List<FormAttribute>)(request.getAttribute("editableFormAttributes"));
%>


</head>
<body>
	<form action="configForm3.form" method="post" id="attribute_form" class="the_form">
	
<%-- 	<c:forEach items = "${editableFormAttributes}" var="attribute" varStatus="status"> --%>
	<%for(FormAttribute fa: editableFormAttributes){ %>
	<div id="<%= "div_popup_"+fa.getFormAttributeId() %>" class="div_popup">
		<div class="div_popup_form">
			<div class="div_subForm">
				<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close"
					onclick="closeForm('<%= "div_popup_"+fa.getFormAttributeId()%>')" />
				<p>
				<h3>Choose <%= fa.getName() %></h3>
				</p>
				<table class="location_table">
					<% for(ArrayList<Object> position: positions){ 
					       Location currLoc = (Location)(position.get(0));
					       LocationTag lTag = (LocationTag)(position.get(1));
					       FormAttributeValue currentValue = (FormAttributeValue)formAttributesValueMap.get(fa.getFormAttributeId()+"#$#"+currLoc.getId()+"#$#"+lTag.getId());
					       String currentValueStr="";
					       if(currentValue!=null && currentValue.getValue()!=null){
					    	   currentValueStr = currentValue.getValue();
					       }
					       
					%>
					<tr style="padding: 5px">
						<td style="padding: 0px 0px 10px 0px"><%=  lTag.getName() %><%= " at "%><%= currLoc.getName() %>
						</td>
						<td style="padding: 0px 0px 10px 0px">
							<input type="text" name="<%= "inpt_"+fa.getFormAttributeId()+"#$#" + currLoc.getId()+"#$#"+lTag.getId()%>" value="<%= currentValueStr%>"  id="<%= "inpt"+fa.getName()+ currLoc.getName()+lTag.getName()%>" />

						</td>
					</tr>
					<script>
					<% String id = "inpt"+fa.getName()+ currLoc.getName()+lTag.getName();  %>
                    $("<%= "#"+id %>").kendoComboBox({
                        dataTextField: "text",
                        dataValueField: "value",
                        dataSource: [
                            <%	
                            	List<String> valueEnumList = formAttributesValueEnumMap.get(fa.getFormAttributeId().toString());
                            	if(valueEnumList!=null){
                            		for(String enumStr: valueEnumList){
                            %>
                            			<%= "{ text: "+ "\""+enumStr+"\"" + ", value: "+ "\""+enumStr +"\" }, "%>
                            <%
                            		}
                            	}
                           	 %>
                        ],
                        filter: "contains",
                        suggest: true,
                        index: 3
                    });
					</script>
					<%} %>
				</table>
				<button type="button" id="<%= fa.getFormAttributeId()+"_change" %>" class="location_attri_change" onclick="changeLocationAttributes('<%= "div_popup_"+fa.getFormAttributeId() %>')">change</button>
			</div>
		</div>
	</div>

<%} %>

</form>

	<p>
	<h2>Configure Form Properties:</h2>
	</p>
	<table>
		<% for(FormAttribute fa: editableFormAttributes){ %>
		<tr style="padding: 5px">
			<td style="padding: 0px 0px 10px 0px">attribute name:  <%= fa.getName() %></td>
			<td style="padding: 0px 0px 10px 0px">
				<button name="bt_scanable" onclick="div_show('div_popup_<%= fa.getFormAttributeId() %>')">view / edit</button>
			</td>
		</tr>
		<%} %>
	</table>
	<br/>
	<br/>
	<button onclick="document.forms['attribute_form'].submit()">submit</button>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>