<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/clinicPrinterConfigForm.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop enabling a form at other clinics?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    // -->
</script>
<html>
    <body>
        <p><h3>Enable Form at Clinics:</h3></p>
        Form: ${formName}
        <form name="input" action="enableLocationsForm.form" method="post" enctype="multipart/form-data">
            <table>
                <tr style="padding: 5px">
                   <td style="padding: 0px 0px 10px 0px">Locations:</td>
                   <td style="padding: 0px 0px 10px 0px">
                       <c:forEach items="${locations}" var="location" varStatus="status">
                        <c:set var="locSelected" value="location_${location}"/>
                        <c:set var="locChecked" value="checked_${location}"/>
                        <c:if test="${status.count != 1}">
                            <br/>
                        </c:if>
                        <c:choose>
                            <c:when test="${checkedLocations[status.count - 1] == 'true' }">
                                <input type="checkbox" name="<c:out value="location_${location}"/>" checked disabled/><c:out value="${location}"/>&nbsp
                            </c:when>
                            <c:when test="${param[locSelected] != null }">
	                            <input type="checkbox" name="<c:out value="location_${location}"/>" checked/><c:out value="${location}"/>&nbsp
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
                <tr style="padding: 5px">
	               <td style="padding: 0px 0px 10px 0px">Scannable form:</td>
	               <td style="padding: 0px 0px 10px 0px">
	                   <c:choose>
	                       <c:when test="${scannableForm == 'true'}">
	                           <input type="radio" name="scannableForm" value="Yes" checked> Yes&nbsp
	                           <input type="radio" name="scannableForm" value="No"> No
	                       </c:when>
	                       <c:otherwise>
	                           <input type="radio" name="scannableForm" value="Yes"> Yes&nbsp
	                           <input type="radio" name="scannableForm" value="No" checked> No
	                       </c:otherwise>
	                   </c:choose>
	               </td>
	            </tr>
	            <tr style="padding: 5px">
	               <td style="padding: 0px 0px 10px 0px">Scorable form:</td>
	               <td style="padding: 0px 0px 10px 0px">
	                   <c:choose>
	                       <c:when test="${scorableForm == 'true'}">
	                           <input type="radio" name="scorableForm" value="Yes" checked onclick="input.scoringFile.disabled = false"> Yes&nbsp
	                           <input type="radio" name="scorableForm" value="No" onclick="input.scoringFile.disabled = true"> No
	                       </c:when>
	                       <c:otherwise>
	                           <input type="radio" name="scorableForm" value="Yes" onclick="input.scoringFile.disabled = false"> Yes&nbsp
	                           <input type="radio" name="scorableForm" value="No" checked onclick="input.scoringFile.disabled = true"> No
	                       </c:otherwise>
	                   </c:choose>
	               </td>
	            </tr>
	            <tr style="padding: 5px">
	                <td style="padding: 0px 0px 10px 0px">Scoring XML File:</td>
	                <td style="padding: 0px 0px 10px 0px">
	                    <c:choose>
	                       <c:when test="${scorableForm == 'true'}">
	                            <input type=file name="scoringFile" accept="text/xml">
	                       </c:when>
	                       <c:otherwise>
	                            <input type=file name="scoringFile" accept="text/xml" disabled>
	                       </c:otherwise>
	                   </c:choose>
	                </td>         
	            </tr>
	            <c:if test="${failedSaveChanges == 'true'}">
	                <tr style="padding: 5px">
	                    <td colspan="3" style="padding: 0px 0px 10px 0px">
	                        <font color="red">Error uploading Scoring XML file.  Check server log for details!</font>
	                    </td>
	                </tr>
	            </c:if>
	            <c:if test="${missingScoringFile == 'true'}">
	                <tr style="padding: 5px">
	                    <td colspan="3" style="padding: 0px 0px 10px 0px">
	                        <font color="red">Please specify a Scoring XML file!</font>
	                    </td>
	                </tr>
	            </c:if>
	            <c:if test="${failedCreateDirectories == 'true'}">
	                <tr style="padding: 5px">
	                  <td colspan="3" style="padding: 0px 0px 10px 0px">
	                      <font color="red">Error creating directories.  Check server log for details!</font>
	                  </td>
	                </tr>
	            </c:if>
	            <c:if test="${failedChirdlUpdate == 'true'}">
	                <tr style="padding: 5px">
	                  <td colspan="3" style="padding: 0px 0px 10px 0px">
	                      <font color="red">Error populating the Chirdl Util tables.  Check server log for details!</font>
	                  </td>
	                </tr>
	            </c:if>
	            <tr style="padding: 5px">
	                <td style="padding: 0px 0px 10px 0px">Copy printer configuration:</td>
	                <td style="padding: 0px 0px 10px 0px">
	                    <select name="printerCopy">
	                        <c:choose>
	                            <c:when test="${printerCopy == 'PWS' }">
	                                <option>PSF</option>
	                                <option selected="selected">PWS</option>
	                            </c:when>
	                            <c:otherwise>
	                                <option selected="selected">PSF</option>
	                                <option>PWS</option>
	                            </c:otherwise>
	                        </c:choose>
	                    </select>
	                </td>         
	            </tr>
                <c:if test="${failedLoad == 'true'}">
                    <tr style="padding: 5px">
                         <td colspan="2" style="padding: 0px 0px 10px 0px">
                             <font color="red">Failed to load form data.  Please check the server logs for detailed information.</font>
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
            <input type="hidden" name="formName" value="${formName}"/>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>