<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Manage Locations" otherwise="/login.htm" redirect="/module/atd/createClinicForm.form" />
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
    // -->
</script>
<html>
    <body OnLoad="document.input.clinicName.focus();">
        <p><h3>Create Form:</h3></p>
        <form name="input" action="createClinicForm.form" method="post">
        <table>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Clinic name:</td>
                <td align="left" style="padding: 0px 0px 10px 0px">
                  <input type="text" name="clinicName" value="${clinicName}"/>
                </td>
            </tr>
            <c:if test="${duplicateName == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Clinic name already exists!</font>
                  </td>
                </tr>
            </c:if>
            <c:if test="${missingName == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Please specify a clinic name!</font>
                  </td>
                </tr>
            </c:if>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Description (optional):</td>
                <td style="padding: 0px 0px 10px 0px">
                    <input type="text" name="clinicDescription" value="${clinicDescription}"/>
                </td>         
            </tr>
            <tr style="padding: 5px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
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
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>