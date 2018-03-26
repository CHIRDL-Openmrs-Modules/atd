<%@ include file="/WEB-INF/template/include.jsp" %>
    
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/faxableForm.form" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop creating the faxable form?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    // -->
</script>
<p>Please choose the form you would like to make faxable:</p>
<form name="input" action="faxableForm.form" method="post">
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
        <c:if test="${error == 'true'}">
            <tr style="padding: 5px">
                <td colspan="2" style="padding: 0px 0px 10px 0px">
                    <font color="red">Error making the form faxable.  Check the server log for details!</font>
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
</form>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>