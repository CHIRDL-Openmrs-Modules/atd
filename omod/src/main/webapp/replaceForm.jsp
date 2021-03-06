<%@ include file="/WEB-INF/template/include.jsp" %>
    
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/replaceForm.form" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop form replace?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    // -->
</script>
<p><h3>Replace Form:</h3></p>
<form name="input" action="replaceForm.form" method="post" enctype="multipart/form-data">
    <table>
        <tr style="padding: 5px">
            <td style="padding: 0px 0px 10px 0px">Form to replace:</td>
            <td style="padding: 0px 0px 10px 0px">
                <select name="formToReplace">
                    <c:forEach items="${forms}" var="form">
                        <c:choose>
                            <c:when test="${form.formId == selectedForm}">
                                <option value="${form.formId}" selected>${form.name} (${form.formId})</option>
                            </c:when>
                            <c:otherwise>
                                <option value="${form.formId}">${form.name} (${form.formId})</option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
            </td>
        </tr>
        <tr style="padding: 5px">
            <td style="padding: 0px 0px 10px 0px">New Teleform file:</td>
            <td style="padding: 0px 0px 10px 0px">
                <input type=file name="dataFile" value="${dataFile}">
            </td>         
        </tr>
        <c:if test="${failedFileUpload == 'true'}">
            <tr style="padding: 5px">
                <td colspan="2" style="padding: 0px 0px 10px 0px">
                    <font color="red">Error uploading data file.  Check server log for details!</font>
                </td>
            </tr>
        </c:if>
        <c:if test="${missingFile == 'true'}">
            <tr style="padding: 5px">
                <td colspan="2" style="padding: 0px 0px 10px 0px">
                    <font color="red">Please specify a file.</font>
                </td>
            </tr>
        </c:if>
        <c:if test="${incorrectExtension == 'true'}">
            <tr style="padding: 5px">
                <td colspan="3" style="padding: 0px 0px 10px 0px">
                    <font color="red">Incorrect file extension found.  Only .xml, .fxf, .zip, and .jar are allowed.</font>
                </td>
            </tr>
        </c:if>
        <c:if test="${failedFieldCopy == 'true'}">
            <tr style="padding: 5px">
                <td colspan="2" style="padding: 0px 0px 10px 0px">
                    <font color="red">Failed copying form fields.  Check server log for details!</font>
                </td>
            </tr>
        </c:if>
        <c:if test="${failedPopulate == 'true'}">
            <tr style="padding: 5px">
              <td colspan="2" style="padding: 0px 0px 10px 0px">
                   <font color="red">Error pre-populating form field data.  Please check the server logs for more details!</font>
              </td>
            </tr>
        </c:if>
        <c:if test="${failedAttrValCopy == 'true'}">
            <tr style="padding: 5px">
                <td colspan="2" style="padding: 0px 0px 10px 0px">
                    <font color="red">Failed copying form attribute values.  Check server log for details!</font>
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
               <input type="Submit" name="Next" value="Next" style="width:70px">&nbsp;
               <input type="button" name="Cancel" value="Cancel" onclick="confirmCancel()" style="width:70px">
           </td>
        </tr>
    </table>
</form>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>