<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/atd/createForm.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<script LANGUAGE="JavaScript">
	<!--
	// Nannette Thacker http://www.shiningstar.net
	function confirmCancel()
	{
	    var agree=confirm("Are you sure you want to stop form creation?");
	    if (agree) {
	    	   window.location = '${pageContext.request.contextPath}/module/atd/configurationManager.form';
	    }
	}
    // -->
</script>
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
		        <td style="padding: 0px 0px 10px 0px">Teleform file:</td>
		        <td style="padding: 0px 0px 10px 0px">
		            <input type="file" name="dataFile" value="${dataFile}">
		        </td>         
		    </tr>
		    <c:if test="${failedFileUpload == 'true'}">
			    <tr style="padding: 5px">
				    <td colspan="3" style="padding: 0px 0px 10px 0px">
				        <font color="red">Error uploading data file.  Check server log for details!</font>
				    </td>
			    </tr>
		    </c:if>
		    <c:if test="${missingFile == 'true'}">
                <tr style="padding: 5px">
                    <td colspan="3" style="padding: 0px 0px 10px 0px">
                        <font color="red">Please specify a file.</font>
                    </td>
                </tr>
            </c:if>
            <c:if test="${incorrectExtension == 'true'}">
                <tr style="padding: 5px">
                    <td colspan="3" style="padding: 0px 0px 10px 0px">
                        <font color="red">Incorrect file extension found.  Only .xml, .fxf, .zip, and .jar are allowed.</font>
                    </td>
                </tr>
            </c:if>
            <c:if test="${failedPopulate == 'true'}">
                <tr style="padding: 5px">
                  <td colspan="2" style="padding: 0px 0px 10px 0px">
                       <font color="red">Error pre-populating form field data!  Please check the server logs for more details.</font>
                  </td>
                </tr>
            </c:if>
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