<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ page import="org.openmrs.Location"%>
<%@ page import="org.openmrs.LocationTag"%>
<%@ page
	import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute"%>
<%@ page import="java.util.*"%>
<%@ page
	import="org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<html>
<head>
	<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"  type="text/css" rel="stylesheet"/>
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
	<script type="text/javascript">
		var originalValues = {};
	</script>
</head>

<body>

<div>
	<h3>Edit Form Attribute Values</h3>
</div>
<button id="showAll" onclick="showHideValues(this.id);">Show All</button>
<form action="configFormAttributeValue.form" method="post" id="attribute_form" class="the_form">
	<input type="hidden" name="formId" value="${formId }" />
	<c:forEach items="${positionStrs}" var="ps" varStatus="status">
		<input type="hidden" name="positions" value="${ps}" />
	</c:forEach>
	<input type="hidden" name="successViewName" value="${successViewName}" />
	<input type="hidden" name="replaceFormId" value="${replaceFormId}" />
	<input type="hidden" id="cancelProcess" name="cancelProcess" value="false" />
	
	<c:forEach items="${editableFormAttributes}" var="fa" varStatus="status">
		<script>
				originalValues["${fa.formAttributeId}"]={};
		</script>
		<c:set var="formAttributeId">${fa.formAttributeId}</c:set>
		<c:set var="valueEnumList" value="${formAttributesValueEnumMap[formAttributeId]}" />
		
		<div>
			<table>
			
			<!-- ATTRIBUTE NAME SECTION -->
				<tr style="padding-left: 10px;">
					<td>
						<table>
							<tr>
								<td><h5>${fa.name}</h5></td><td class="attributeNameSection"><a href="javascript:showHideValues('${fa.formAttributeId}');">hide/show</a></td>
							</tr>	
						</table>
					</td>
				</tr>
				
			<!-- APPLY TO ALL LOCATIONS FOR THIS ATTRIBUTE -->
					<tr>
						<td>
							<div class="divApplyToAllLocations" id="div_formAttribute_${fa.formAttributeId}">
							<table style="padding-left: 20px;" class="location_table">
							<tr>
							<td class="locationText">Apply to all locations and tags&nbsp;<input type="text"
								name="inpt_${fa.formAttributeId}#$#ALL#$#ALL"
								id="inpt_${fa.formAttributeId}_ALL_ALL"
								onchange="assignValueSubClass('inpt_${fa.formAttributeId}_ALL_ALL')" />
							</td>
							</tr>
							
			<!-- VALUE FOR LOCATIONS AND TAGS FOR THIS ATTRIBUTE  -->			
							<tr>
							<td>
							<table style="padding-left: 10px;">					
								<tr >
								<td>
								<div class="locationsAndTagsClass" id="div_locationsAndTags_${fa.formAttributeId}">
								<table>
									<c:forEach items="${locationsList}" var="currLoc" varStatus="locStatus">
										<c:set var="allId" value="${fa.formAttributeId}#$#${currLoc.id}#$#ALL" />
										<c:set var="allInptId" value="inpt_${fa.formAttributeId}_${currLoc.id}_ALL" />
										<c:choose>
											<c:when test = "${locStatus.index mod 2 eq 0 }">
												<c:set var="className" value="blockColor1"/>
											</c:when>
											<c:otherwise>
												<c:set var="className" value="blockColor2"/>
											</c:otherwise>
										</c:choose>
								
								<tr>
								<td>
									<div class="${className } div_table">
										<table class="location_table">
											<tr style="padding: 5px">
												<td class="locationText" align="right">Apply to all location tags
													at ${currLoc.name}</td>
												<td align="right"><input type="text"
													id="${allInptId}" class="${fa.formAttributeId} ALL"
													onchange="assignValueSubClass('${allInptId}'); removeValueSuperClass('${allInptId}')" />
												</td>
											</tr>
			
											<script>
			
						                    	$("#${allInptId}").kendoComboBox({
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
						                        	index: 3,
						                            autoBind:false,
						                            text:""
						                    	});
											</script>
			
											<c:forEach items="${locationTagsMap[currLoc.id]}" var="lTag"
												varStatus="tagStatus">
												<c:set var="theId"
													value="${fa.formAttributeId}#$#${currLoc.id}#$#${lTag.id}" />
												<c:set var="inptId"
													value="inpt_${fa.formAttributeId}_${currLoc.id}_${lTag.id}" />
												<c:set var="currentValue"
													value="${formAttributesValueMap[theId]}" />
												<c:set var="currentValueStr" value="" />
												<c:if test="${not empty currentValue }">
													<c:set var="currentValueStr" value="${currentValue.value}" />
												</c:if>
												<tr style="padding: 5px">
													<td class="locationText" align="right">${lTag.name} at 
														${currLoc.name}</td>
													<td align="right"><input type="text"
														name="inpt_${fa.formAttributeId}#$#${currLoc.id}#$#${lTag.id}"
														value="${currentValueStr}" id="${inptId}"
														class="${fa.formAttributeId} ${currLoc.id}"
														onchange="removeValueSuperClass('${inptId}')" /></td>
												</tr>
												<script>
													originalValues["${fa.formAttributeId}"]["${inptId}"]="${currentValueStr}";
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
								                        index: 3,
								                        autoBind:false,
								                        text:"${currentValueStr}"
								                    });
			                    
												</script>
											</c:forEach>
										</table>
									</div>
								</td>
								</tr>
								</c:forEach>
								</table>
							</div>
							</td>
							</tr>
							</table>
							</td>
							</tr>
							</table>
							</div>
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
				                        index: 3,
				                        autoBind:false,
				                        text:""
				                    });
								</script>
							</td>
							</tr>
			</table>
		</div>
	</c:forEach>
</form>

<hr size="3" color="black"/>
			
<table align="right">
	<tr>
		<td><button onclick="document.forms['attribute_form'].submit()">Next</button></td>
		<td><input type="button" value="Cancel" onclick="displayConfirmCancel();"/></td>
	</tr>
</table>

<br />
<br />
</body>

<script>

// Hides the values for the div with the given id
function showHideValues(divFormAttributeId)
{
	if(divFormAttributeId === 'showAll')
	{
		$j("[id^=div_formAttribute_]").show();
		$j("[id^=div_locationsAndTags_]").show();
	}
	else
	{
		$( "#div_formAttribute_"+divFormAttributeId).slideToggle("fast", function(){});
		 
		 $( "#div_locationsAndTags_"+divFormAttributeId ).slideToggle("fast", function(){});
	}	 
}

function displayConfirmCancel()
{
	$j('<div id="confirmCancel" title="Cancel Edit?">Are you sure you want to cancel?</div>').dialog({
		 height: "auto",
		 width: "auto",
		 modal: true,
		 resizable: false,
		 buttons: {
		    "OK": function() {
		    	$j('#cancelProcess').val('true');
	            $j('#attribute_form').submit();
		    	$j(this).dialog("close");
		     },
		     Cancel: function() {
		     $j(this).dialog("close");
		     }
		  }
	});	
}

</script>

</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>