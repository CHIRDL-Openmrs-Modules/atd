<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/configurationManager.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <center><p><h1><i>Configuration Manager</i></h1></p></center>
        <center><table>
            <tr style="padding: 5px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
                <td style="padding: 10px 10px 10px 10px">
                    <a href="${pageContext.request.contextPath}/module/atd/createForm.form"><img width="227" height="72" border="0" src="${pageContext.request.contextPath}/moduleResources/atd/createFormButton.gif"/></a>
                </td>
                <td style="padding: 10px 10px 10px 10px">
                    <a href="${pageContext.request.contextPath}/module/atd/replaceForm.form"><img width="227" height="72" border="0" src="${pageContext.request.contextPath}/moduleResources/atd/replaceFormButton.gif"/></a>
                </td>
                <td style="padding: 10px 10px 10px 10px">
                    <a href="${pageContext.request.contextPath}/module/atd/deleteForms.form"><img width="227" height="72" border="0" src="${pageContext.request.contextPath}/moduleResources/atd/deleteFormsButton.gif"/></a>
                </td>
            </tr>
        </table></center>
        <center><table>
            <tr style="padding: 5px">
                <td style="padding: 10px 10px 10px 10px">
                    <a href="${pageContext.request.contextPath}/module/atd/editForms.form"><img width="227" height="72" border="0" src="${pageContext.request.contextPath}/moduleResources/atd/editFormFieldsButton.gif"/></a>
                </td>
                <td style="padding: 10px 10px 10px 10px">
                    <a href="${pageContext.request.contextPath}/module/atd/printerFormSelectionForm.form"><img width="227" height="72" border="0" src="${pageContext.request.contextPath}/moduleResources/atd/printerConfigButton.gif"/></a>
                </td>
                <td style="padding: 10px 10px 10px 10px">
                    <a href="${pageContext.request.contextPath}/module/atd/clinicPrinterConfigForm.form"><img width="227" height="72" border="0" src="${pageContext.request.contextPath}/moduleResources/atd/clinicPrinterConfigButton.gif"/></a>
                </td>
            </tr>
        </table></center>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>