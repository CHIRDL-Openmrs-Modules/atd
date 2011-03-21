<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/clinicPrinterConfigForm.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <p><h3>Configure Clinic Printers:</h3></p>
        <form name="input" action="clinicPrinterConfigForm.form" method="post" enctype="multipart/form-data">
	        <table>
	            <tr style="padding: 5px">
                   <td style="padding: 0px 0px 0px 0px">Use alternate printer:</td>
                   <td style="padding: 0px 0px 0px 0px">
                       <c:choose>
                           <c:when test="${useAltPrinter == 'true'}">
                               <input type="radio" name="useAltPrinter" value="true" checked> true&nbsp
                           </c:when>
                           <c:otherwise>
                               <input type="radio" name="useAltPrinter" value="true"> true&nbsp
                           </c:otherwise>
                       </c:choose>
                   </td>
                </tr>
                <tr>
                    <td></td>
                    <td style="padding: 0px 0px 10px 0px">
                       <c:choose>
                           <c:when test="${useAltPrinter == 'true'}">
                               <input type="radio" name="useAltPrinter" value="false"> false
                           </c:when>
                           <c:otherwise>
                               <input type="radio" name="useAltPrinter" value="false" checked> false
                           </c:otherwise>
                       </c:choose>
                   </td>
                </tr>
	            <tr style="padding: 5px">
	               <td style="padding: 0px 0px 10px 0px">Locations:</td>
	               <td colspan="3" style="padding: 0px 0px 10px 0px">
	                   <c:forEach items="${locations}" var="location" varStatus="status">
	                    <c:set var="locChecked" value="location_${location}"/>
	                    <c:if test="${status.count != 1}">
	                        <br/>
	                    </c:if>
	                    <c:choose>
	                        <c:when test="${param[locChecked] != null }">
	                            <input type="checkbox" name="<c:out value="location_${location}"/>" checked="true"/><c:out value="${location}"/>&nbsp
	                        </c:when>
	                        <c:otherwise>
	                            <input type="checkbox" name="<c:out value="location_${location}"/>"/><c:out value="${location}"/>&nbsp
	                        </c:otherwise>
	                    </c:choose>
	                   </c:forEach>
	               </td>
	            </tr>
	            <c:if test="${noLocationsChecked == 'true'}">
	                <tr style="padding: 5px">
	                     <td colspan="2" style="padding: 0px 0px 10px 0px">
	                         <font color="red">Please select at least one location!</font>
	                     </td>
	                </tr>
	            </c:if>
	            <c:if test="${failedUpdate == 'true'}">
                    <tr style="padding: 5px">
                         <td colspan="2" style="padding: 0px 0px 10px 0px">
                             <font color="red">Failed to save the changes.  Please check the server logs for detailed information.</font>
                         </td>
                    </tr>
                </c:if>
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
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>