<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/configurationManagerSuccess.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <p><h3>Finished:</h3></p>
        <form name="input" action="configurationManagerSuccess.form" method="post" enctype="multipart/form-data">
        <table>
            <tr style="padding: 5px">
				<c:choose>
					<c:when test="${not empty errorMsg}">
						<td style="padding: 0px 0px 10px 0px"><c:out value="${errorMsg}"/></td>
					</c:when>
					<c:otherwise>
						<td style="padding: 0px 0px 10px 0px"><c:out value="${application}"/> completed successfully!</td>
					</c:otherwise>
				</c:choose>
            </tr>
            <tr style="padding: 10px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td colspan="3" align="right">
                   <input type="Submit" name="Home" value="Home" style="width:70px">
               </td>
            </tr>
        </table>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>