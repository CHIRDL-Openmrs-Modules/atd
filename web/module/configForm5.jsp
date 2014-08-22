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
	<form action="configForm5.form" method="post" id="attribute_form" class="the_form">
		<c:forEach items="${editableFormAttributes}" var="fa" varStatus="status">
			<c:set var="formAttributeId">${fa.formAttributeId}</c:set>
			<c:set var="valueEnumList" value="${formAttributesValueEnumMap[formAttributeId]}" />
			<div id="div_popup_${fa.formAttributeId}" class="div_popup">
				<div class="div_popup_form">
					<div class="div_subForm">
						<img src="${pageContext.request.contextPath}/moduleResources/atd/3.png" class="close" onclick="closeForm('div_popup_${fa.formAttributeId}')" />
						<p>
						<h3>
							Choose <c:out value="${fa.name}" />
						</h3>
						</p>

						<table class="location_table">
							<tr style="padding: 5px">
								<td style="padding: 0px 0px 10px 0px">for all position</td>
								<td style="padding: 0px 0px 10px 0px">
									<input type="text" name="inpt_${fa.formAttributeId}#$#ALL#$#ALL" value="  " id="inpt_${fa.formAttributeId}_ALL_ALL" />
								</td>
							</tr>
						<script>
					<c:set var = "inptallId" value = "inpt_${fa.formAttributeId}_ALL_ALL"/>
                    $("#${inptallId}").kendoComboBox({
                        dataTextField: "text",
                        dataValueField: "value",
                        dataSource: [
                                <c:if test = "${not empty valueEnumList}">
                                	<c:forEach items = "${valueEnumList}" var = "enumStr" varStatus="comboxStatus">
                                		{text: "${enumStr}" , value: "${enumStr}"},
                                	</c:forEach>
                                </c:if>
                        ],
                        filter: "contains",
                        suggest: true,
                        index: 3
                    });
					</script>
					<c:forEach items="${locationsList}" var="currLoc" varStatus="locStatus">
						<c:forEach items="${locationTagsMap[currLoc.id]}" var="lTag" varStatus="tagStatus"> 
								<c:set var="theId" value="${fa.formAttributeId}#$#${currLoc.id}#$#${lTag.id}" />
								<c:set var="currentValue"  value="${formAttributesValueMap[theId]}" />
								<c:set var="currentValueStr" value="" />
								<c:if test="${not empty currentValue }">
									<c:set var="currentValueStr" value="${currentValue.value}" />
								</c:if>
								<tr style="padding: 5px">
									<td style="padding: 0px 0px 10px 0px">${lTag.name} at ${currLoc.name}</td>
									<td style="padding: 0px 0px 10px 0px">
										<input type="text" name="inpt_${fa.formAttributeId}#$#${currLoc.id}#$#${lTag.id}" value="${currentValueStr}" id="inpt_${fa.formAttributeId}_${currLoc.id}_${lTag.id}" />
									</td>
								</tr>
					<script>
					<c:set var = "inptId" value = "inpt_${fa.formAttributeId}_${currLoc.id}_${lTag.id}"/>
                    $("#${inptId}").kendoComboBox({
                        dataTextField: "text",
                        dataValueField: "value",
                        dataSource: [
                                <c:if test = "${not empty valueEnumList}">
                                	<c:forEach items = "${valueEnumList}" var = "enumStr" varStatus="comboxStatus">
                                		{text: "${enumStr}" , value: "${enumStr}"},
                                	</c:forEach>
                                </c:if>
                        ],
                        filter: "contains",
                        suggest: true,
                        index: 3
                    });
					</script>
								</c:forEach>
							</c:forEach>
						</table>
						<button type="button" id="${fa.formAttributeId}_change"
							class="location_attri_change"
							onclick="changeLocationAttributes('div_popup_${fa.formAttributeId}')">change</button>
					</div>
				</div>
			</div>
		</c:forEach>
		<%-- <%} %> --%>

	</form>

	<p>
	<h2>Configure Form Properties:</h2>
	</p>
	<table>
		<c:forEach var="fa" items="${editableFormAttributes }" varStatus="status">
			<tr style="padding: 5px">
				<td style="padding: 0px 0px 10px 0px">attribute name:
					${fa.name}</td>
				<td style="padding: 0px 0px 10px 0px">
					<button name="bt_scanable"
						onclick="div_show('div_popup_${fa.formAttributeId}')">view
						/ edit</button>
				</td>
			</tr>

		</c:forEach>
	</table>
	<br />
	<br />
	<button onclick="document.forms['attribute_form'].submit()">submit</button>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>