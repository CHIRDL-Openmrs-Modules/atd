<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <p><h3>Configure Form Properties:</h3></p>
        <form name="input" action="configForm.form" method="post" enctype="multipart/form-data">
        <table>
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
            <c:if test="${failedScoringFileUpload == 'true'}">
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
            <tr style="padding: 5px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td colspan="3" align="right">
                   <input type="reset" name="Clear" value="Clear">
                   <input type="Submit" name="Next" value="Next"">
               </td>
            </tr>
        </table>
        <input type="hidden" name="formId" value="${formId}"/>
        <input type="hidden" name="formName" value="${formName}"/>
        <c:choose>
            <c:when test="${createWizard == 'true'}">
                <input type="hidden" name="createWizard" value="true"/>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="createWizard" value="false"/>
            </c:otherwise>
        </c:choose>
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