<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
    <!--
    // Nannette Thacker http://www.shiningstar.net
    function confirmCancel()
    {
        var agree=confirm("Are you sure you want to stop form printer configuration?");
        if (agree) {
               window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
        }
    }
    // -->
</script>
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