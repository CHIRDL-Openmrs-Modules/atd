<%@ include file="/WEB-INF/template/include.jsp" %>
    
<%@ include file="/WEB-INF/template/header.jsp" %>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<html>
<body>
<p><h3>Retire Form:</h3></p>
<form name="input" action="replaceRetireForm.form" method="post">
    <table>
        <tr style="padding: 5px">
            <td style="padding: 0px 0px 0px 0px">Do you wish to retire the original form?</td>
        </tr>
        <tr style="padding: 0px">
            <td align="left" style="padding: 0px 0px 0px 0px">
              <input type="radio" name="retireForm" value="Yes" checked>Yes
            </td>
        </tr>
        <tr style="padding: 0px">
            <td align="left" style="padding: 0px 0px 10px 0px">
              <input type="radio" name="retireForm" value="No">No
            </td>
        </tr>
        <tr style="padding: 5px">
            <td colspan="2" align="center"><hr size="3" color="black"/></td>
        </tr>
        <tr style="padding: 5px">
           <td colspan="2" align="right">
               <input type="reset" name="Clear" value="Clear">
               <input type="Submit" name="Finish" value="Finish">
           </td>
        </tr>
    </table>
    <input type="hidden" name="formId" value="${form.formId}" />
    <input type="hidden" name="newFormId" value="${newForm.formId}" />
</form>
</body>
</html>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>