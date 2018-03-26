<%@ include file="/WEB-INF/template/include.jsp" %>
    
<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/replaceRetireForm.form" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<SCRIPT LANGUAGE="JavaScript">

    function confirmCancel() {
        var agree=confirm("Are you sure you want to stop form replace?");
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
<p><h3>Retire Form:</h3></p>
<form id="input" name="input" action="replaceRetireForm.form" method="post">
    <table>
        <tr style="padding: 5px">
            <td style="padding: 0px 0px 0px 0px">The original form will be retired.</td>
        </tr>
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
    <input type="hidden" name="formId" value="${form.formId}" />
    <input type="hidden" name="newFormId" value="${newForm.formId}" />
    <input type="hidden" id="cancelProcess" name="cancelProcess" value="false" />
</form>
</body>
</html>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>