<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <h3>Form Printer Configuration</h3>
        <h4>Form: <c:out value="${formName}"/></h4>
        <h4>Location: <c:out value="${locationName}"/></h4>
        <form name="input" action="printerLocationForm.form" method="post">
        <table>
            <c:forEach items="${printerConfig.locationTagPrinterConfigs}" var="printerTag" varStatus="status">
	            <tr>
	               <td>
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
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td colspan="3" align="right">
                   <input type="reset" name="Clear" value="Clear">
                   <input type="Submit" name="Finish" value="Finish">
               </td>
            </tr>
        </table>
        <input type="hidden" name="formId" value="${formId}"/>
        <input type="hidden" name="locationId" value="${locationId}"/>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>