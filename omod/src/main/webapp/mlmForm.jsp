<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/mlmForm.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <p><h3>MLM Form:</h3></p>
        <form name="input" action="mlmForm.form" method="post" enctype="multipart/form-data">
        <table>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Please copy all necessary MLM files to the following directory on the server:</td>
            </tr>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px"><c:out value="${mlmDir}"/></td>         
            </tr>
            <tr style="padding: 10px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td colspan="3" align="right">
                   <input type="Submit" name="Next" value="Finish" style="width:70px">
               </td>
            </tr>
        </table>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>