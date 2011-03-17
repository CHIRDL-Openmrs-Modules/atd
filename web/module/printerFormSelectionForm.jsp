<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body>
        <p><h3>Printer Form/Location:</h3></p>
        <form name="input" action="printerFormSelectionForm.form" method="post">
        <table>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Form:</td>
                <td align="left" style="padding: 0px 0px 10px 0px">
                  <select name="formId">
                    <c:forEach items="${forms}" var="form">
                        <option value="${form.formId}">${form.name} (${form.formId})</option>
                    </c:forEach>
                  </select>
                </td>
            </tr>
            <tr style="padding: 5px">
                <td style="padding: 0px 0px 10px 0px">Location:</td>
                <td style="padding: 0px 0px 10px 0px">
                    <c:forEach items="${locations}" var="loc" varStatus="status">
                        <c:choose>
                            <c:when test="${status.count == 1}">
                                <input type="radio" name="locationId" value="${loc.locationId}" checked><c:out value="${loc.name}"/>
                            </c:when>
                            <c:otherwise>
                                <br/><input type="radio" name="locationId" value="${loc.locationId}"><c:out value="${loc.name}"/>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </td>         
            </tr>
            <tr style="padding: 5px">
                <td colspan="3" align="center"><hr size="3" color="black"/></td>
            </tr>
            <tr style="padding: 5px">
               <td colspan="3" align="right">
                   <input type="reset" name="Clear" value="Clear">
                   <input type="Submit" name="Next" value="Next">
               </td>
            </tr>
        </table>
        </form>
    </body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>