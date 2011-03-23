<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/printerLocationForm.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop form printer configuration?");
        if (agree) {
               window.location.href('${pageContext.request.contextPath}/module/atd/configurationManager.form')
        }
    }
    // -->
</script>
<html>
    <body>
        <h3>Form Printer Configuration</h3>
        Form: <c:out value="${formName}"/><br/>
        Location: <c:out value="${locationName}"/>
        <form name="input" action="printerLocationForm.form" method="post">
        <table>
            <c:forEach items="${printerConfig.locationTagPrinterConfigs}" var="printerTag" varStatus="status">
	            <tr>
	               <td colspan="2">
	                   <c:if test="${status.count != 1}">
	                       <br/><br/>
	                   </c:if>
		               <table>
		                   <tr style="padding: 5px">
				                <td colspan="2" style="padding: 0px 0px 10px 0px"><h4>Tag: <c:out value="${printerTag.locationTagName}"/></h4></td>
		                   <tr style="padding: 5px">
		                   <tr style="padding: 5px">
                                <td style="padding: 0px 0px 10px 0px">Default Printer:</td>
                                <td style="padding: 0px 0px 10px 0px"><input type="text" name="${printerTag.locationTagId}_defaultPrinter" size="40" value="${printerTag.defaultPrinter.value}"></td>
                           <tr style="padding: 5px">
                           <tr style="padding: 5px">
                                <td style="padding: 0px 0px 10px 0px">Alternate Printer:</td>
                                <td style="padding: 0px 0px 10px 0px"><input type="text" name="${printerTag.locationTagId}_alternatePrinter" size="40" value="${printerTag.alternatePrinter.value}"></td>
                           <tr style="padding: 5px">
                           <tr style="padding: 5px">
                                <td style="padding: 0px 0px 10px 0px">Use Alternate Printer:</td>
                                <td style="padding: 0px 0px 10px 0px">
                                    <c:choose>
			                            <c:when test="${printerTag.useAlternatePrinter.value == 'true'}">
			                                <input type="radio" name="${printerTag.locationTagId}_useAlternatePrinter" value="true" checked>true
			                                <input type="radio" name="${printerTag.locationTagId}_useAlternatePrinter" value="false">false
			                            </c:when>
			                            <c:otherwise>
			                                <input type="radio" name="${printerTag.locationTagId}_useAlternatePrinter" value="true">true
			                                <input type="radio" name="${printerTag.locationTagId}_useAlternatePrinter" value="false" checked>false
			                            </c:otherwise>
			                        </c:choose>
                                </td>
                           <tr style="padding: 5px">
		               </table>
	               </td>
	            </tr>
            </c:forEach>
            <tr style="padding: 5px">
                <td colspan="2" align="center"><hr size="3" color="black"/></td>
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
        </table>
        <input type="hidden" name="formId" value="${formId}"/>
        <input type="hidden" name="locationId" value="${locationId}"/>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>