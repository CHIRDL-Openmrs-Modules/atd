<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Manage Location Tags" otherwise="/login.htm" redirect="/module/atd/createClinicTagForm.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop clinic creation?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    
    function addTag()
    {
    	var createNewTag = document.getElementById('addAdditionalTag');
    	createNewTag.value = 'true';
        document.getElementById('input').submit();
    }
    // -->
</script>
<html>
    <body OnLoad="document.input.tagName.focus();">
        <p><h3>Create Tag:</h3></p>
        <form id="input" name="input" action="createClinicTagForm.form" method="post">
        <table>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Tag name:</td>
                <td align="left" style="padding: 0px 0px 10px 0px">
                  <input type="text" name="tagName" value="${tagName}"/>
                </td>
            </tr>
            <c:if test="${duplicateName == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Tag name already exists!</font>
                  </td>
                </tr>
            </c:if>
            <c:if test="${missingName == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Please specify a tag name!</font>
                  </td>
                </tr>
            </c:if>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Description (optional):</td>
                <td style="padding: 0px 0px 10px 0px">
                    <input type="text" name="tagDescription" value="${tagDescription}"/>
                </td>         
            </tr>
            <c:if test="${failedCreateClinic == 'true'}">
                <tr style="padding: 5px">
                    <td colspan="2" style="padding: 0px 0px 10px 0px">
                        <font color="red">Error creating the location and tags.  Check server log for details!</font>
                    </td>
                </tr>
            </c:if>
            <tr style="padding: 5px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td align="left">
                   <input type="reset" name="Clear" value="Clear" style="width:70px">
               </td>
               <td align="right">
                  <input type="button" name="AddAnotherTag" value="Add Another Tag" onclick="addTag()">&nbsp;
                  <input type="Submit" name="Next" value="Next" style="width:70px">&nbsp;
                  <input type="button" name="Cancel" value="Cancel" onclick="confirmCancel()" style="width:70px">
               </td>
            </tr>
        </table>
        <input type="hidden" name="addAdditionalTag" id="addAdditionalTag" value="false"/>
        <input type="hidden" name="clinicName" id="clinicName" value="${clinicName}"/>
        <input type="hidden" name="clinicDescription" id="clinicDescription" value="${clinicDescription}"/>
        <input type="hidden" name="tagInfo" id="tagInfo" value="${tagInfo}"/>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>