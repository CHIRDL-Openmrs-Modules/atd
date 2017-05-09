<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>
<link href="${pageContext.request.contextPath}/moduleResources/atd/atd.css" type="text/css" rel="stylesheet" />
<c:choose>
     <c:when test="${createForms}">
     <p>Creating forms...</p>
<c:forEach items="${newForms}" var="newForm">	 
Form ${newForm.name} successfully added <a href="editFields.form?formToEdit=${newForm.formId}">(Edit new form)</a><br />
</c:forEach>
</c:when>
       
<c:otherwise> 
<p>Files to process: </p>
<form name="input" action="uploadForms.form" method="get">
<c:forEach items="${files}" var="file" varStatus="i">
Please enter the form name for file:&nbsp;${file}<br/><br />
<input type="text" name="filename${i.count}" size="20"><br /><br />
<input type="hidden" name="fileLocation${i.count}" value="${file}" />	
<br /><br />
</c:forEach>
<input type="hidden" name="createForms" value="true" />
<input type="hidden" name="numFiles" value="${length}" />
<input type="submit" value="OK">
</form>
 </c:otherwise>
    </c:choose>
<%@ include file="/WEB-INF/template/footer.jsp" %>