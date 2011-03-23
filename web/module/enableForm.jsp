<%@ include file="/WEB-INF/template/include.jsp" %>
    
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/editFields.form" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop enabling a form at other clinics?");
        if (agree) {
               window.location.href('${pageContext.request.contextPath}/module/atd/configurationManager.form')
        }
    }
    // -->
</script>
<p>Please choose the form you would like to enable at other clinics:</p>
<form name="input" action="enableLocationsForm.form" method="get">
    <table>
        <tr style="padding: 5px">
            <td colspan="2" style="padding: 0px 0px 10px 0px">
                <select name="formToEdit">
                    <c:forEach items="${forms}" var="form">
                        <option value="${form.formId}">${form.name} (${form.formId})</option>
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
</form>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>