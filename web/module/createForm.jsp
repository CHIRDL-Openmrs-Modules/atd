<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<html>
    <body OnLoad="document.input.formName.focus();">
		<p><h3>Create Form:</h3></p>
		<form name="input" action="createForm.form" method="post" enctype="multipart/form-data">
		<table>
		    <tr style="padding: 5px">
		        <td style="padding: 0px 0px 10px 0px">Form name:</td>
		        <td align="left" style="padding: 0px 0px 10px 0px">
		          <input type="text" name="formName" value="${formName}"/>
		        </td>
		    </tr>
		    <c:if test="${duplicateName == 'true'}">
			    <tr style="padding: 5px">
			      <td colspan="2" style="padding: 0px 0px 10px 0px">
	                   <font color="red">Form name already exists!</font>
			      </td>
			    </tr>
		    </c:if>
		    <c:if test="${missingName == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Please specify a form name!</font>
                  </td>
                </tr>
            </c:if>
            <c:if test="${spacesInName == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Spaces are not allowed in the form name!</font>
                  </td>
                </tr>
            </c:if>
		    <c:if test="${failedCreateForm == 'true'}">
		        <tr style="padding: 5px">
		          <td colspan="3" style="padding: 0px 0px 10px 0px">
		              <font color="red">Error creating form.  Check server log for details!</font>
		          </td>
		        </tr>
		    </c:if>
		    <tr style="padding: 5px">
		        <td style="padding: 0px 0px 10px 0px">Teleform XML file:</td>
		        <td style="padding: 0px 0px 10px 0px">
		            <input type=file name="xmlFile" accept="text/xml" value="${xmlFile}">
		        </td>         
		    </tr>
		    <c:if test="${failedFileUpload == 'true'}">
			    <tr style="padding: 5px">
				    <td colspan="3" style="padding: 0px 0px 10px 0px">
				        <font color="red">Error uploading XML file.  Check server log for details!</font>
				    </td>
			    </tr>
		    </c:if>
		    <c:if test="${missingFile == 'true'}">
                <tr style="padding: 5px">
                    <td colspan="3" style="padding: 0px 0px 10px 0px">
                        <font color="red">Please specify an XML file.</font>
                    </td>
                </tr>
            </c:if>
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