<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<SCRIPT LANGUAGE="JavaScript">
    
    function confirmCancel() {
        var agree=confirm("Are you sure you want to stop form creation?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
               var cancel = document.getElementById('cancelProcess');
               cancel.value = 'true';
               document.getElementById('input').submit();
        }
    }
// End </script>
<html>
    <body>
        
        <form id="input" name="input" action="configForm.form" method="post" enctype="multipart/form-data">
        <p><h3>Configure Form Properties (${formName})</h3></p>
        <table>
            <tr style="padding: 5px">
               <td style="padding: 0px 0px 10px 0px">Locations:</td>
               <td style="padding: 0px 0px 10px 0px">
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
            <tr style="padding: 5px">
               <td style="padding: 0px 0px 10px 0px">Faxable form:</td>
               <td style="padding: 0px 0px 10px 0px">
                   <c:choose>
                       <c:when test="${faxableForm == 'true'}">
                           <input type="radio" id="faxYes" name="faxableForm" value="Yes" checked onclick="input.scanNo.checked = true"> Yes&nbsp
                           <input type="radio" id="faxNo" name="faxableForm" value="No"> No
                       </c:when>
                       <c:otherwise>
                           <input type="radio" id="faxYes" name="faxableForm" value="Yes" onclick="input.scanNo.checked = true"> Yes&nbsp
                           <input type="radio" id="faxNo" name="faxableForm" value="No" checked> No
                       </c:otherwise>
                   </c:choose>
               </td>
            </tr>
            <tr style="padding: 5px">
               <td style="padding: 0px 0px 10px 0px">Scannable form:</td>
               <td style="padding: 0px 0px 10px 0px">
                   <c:choose>
                       <c:when test="${scannableForm == 'true'}">
                           <input type="radio" id="scanYes" name="scannableForm" value="Yes" checked onclick="input.faxNo.checked = true"> Yes&nbsp
                           <input type="radio" id="scanNo" name="scannableForm" value="No"> No
                       </c:when>
                       <c:otherwise>
                           <input type="radio" id="scanYes" name="scannableForm" value="Yes" onclick="input.faxNo.checked = true"> Yes&nbsp
                           <input type="radio" id="scanNo" name="scannableForm" value="No" checked> No
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
            <c:if test="${failedScoringFileUpload == 'true'}">
                <tr style="padding: 5px">
                    <td colspan="2" style="padding: 0px 0px 10px 0px">
                        <font color="red">Error uploading Scoring XML file.  Check server log for details!</font>
                    </td>
                </tr>
            </c:if>
            <c:if test="${missingScoringFile == 'true'}">
                <tr style="padding: 5px">
                    <td colspan="2" style="padding: 0px 0px 10px 0px">
                        <font color="red">Please specify a Scoring XML file!</font>
                    </td>
                </tr>
            </c:if>
            <c:if test="${failedCreateDirectories == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                      <font color="red">Error creating directories.  Check server log for details!</font>
                  </td>
                </tr>
            </c:if>
            <c:if test="${failedChirdlUpdate == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                      <font color="red">Error populating the Chirdl Util tables.  Check server log for details!</font>
                  </td>
                </tr>
            </c:if>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Copy printer configuration:</td>
                <td style="padding: 0px 0px 10px 0px">
                    <select name="printerCopy">
						<option name="none" value="">None</option>
						<c:forEach items="${primaryForms}" var="primaryForm">
							<c:choose>
								<c:when test="${primaryForm == printerCopy}">
									<option value="${primaryForm}" selected>${primaryForm}</option>
								</c:when>
								<c:otherwise>
									<option value="${primaryForm}">${primaryForm}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
                    </select>
                </td>         
            </tr>
            <tr style="padding: 5px">
                <td colspan="2" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td align="left">
                   <input type="reset" name="Clear" value="Clear" style="width:70px">
               </td>
               <td align="right">
                   <input type="Submit" name="Next" value="Next" style="width:70px">&nbsp;
                   <input type="button" name="Cancel" value="Cancel" onclick="confirmCancel()" style="width:70px">
               </td>
            </tr>
        </table>
        <input type="hidden" name="formId" value="${formId}"/>
        <input type="hidden" name="formName" value="${formName}"/>
        <input type="hidden" id="cancelProcess" name="cancelProcess" value="false" />
        <c:choose>
            <c:when test="${numPrioritizedFields != null}">
                <input type="hidden" name="numPrioritizedFields" value="${numPrioritizedFields}"/>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="numPrioritizedFields" value="0"/>
            </c:otherwise>
        </c:choose>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>