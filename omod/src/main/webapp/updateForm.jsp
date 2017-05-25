<%@ include file="/WEB-INF/template/include.jsp" %>
    
<%@ include file="/WEB-INF/template/header.jsp" %>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<p>Please choose the form you would like to update:</p>
<form name="input" action="updateForm.form" method="post">
    <table>
        <tr>
		    <select name="formToEdit">
			    <c:forEach items="${forms}" var="form">
			        <option value="${form.formId}">${form.name} (${form.formId})</option>
			    </c:forEach>
		    </select>
        </tr>
        <tr style="padding: 5px">
            <td align="center"><hr size="3" color="black"/></td>
        </tr>
        <tr style="padding: 5px">
           <td align="right">
               <input type="reset" name="Clear" value="Clear">
               <input type="Submit" name="Next" value="Next">
           </td>
        </tr>
    </table>
</form>
      
<%@ include file="/WEB-INF/template/footer.jsp" %>