<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/operationSuccess.form" />
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
        <form name="input" action="operationSuccess.form" method="post" enctype="multipart/form-data">
        <table>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">${operationType} successful</td>
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