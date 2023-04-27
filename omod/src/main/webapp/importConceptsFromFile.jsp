<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="Manage ATD" otherwise="/login.htm" redirect="/module/atd/importConceptsFromFile.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />

<openmrs:htmlInclude file="/scripts/jquery/jquery.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/jquery-ui.min.js" />
<script>var ctx = "${pageContext.request.contextPath}";</script>

<style>
  .ui-progressbar {
    position: relative;
    display: none;
    height: 25px;
  }
  .progress-label {
    text-shadow: 1px 1px 0 #fff;
    display: none;
    height: 25px;
  }
  
  .errorMsg{
  	display: none;
  	padding: 0px, 0px, 10px, 0px;
  }
  </style>
<html>
    <body>
		<p><h3>Import Concepts:</h3></p>
		<form name="input" action="importConceptsFromFile.form" method="post" enctype="multipart/form-data">
		<table>
		   
		    <tr style="padding: 5px">
		        <td style="padding: 0px 0px 10px 0px">Concept csv file:</td>
		        <td style="padding: 0px 0px 10px 0px">
		            <input type="file" name="dataFile" id="dataFile" accept=".csv" value="${dataFile}">
		        </td>         
		    </tr>
		    
			    <tr style="padding: 5px">
				    <td colspan="3" id="uploadError" class="errorMsg">
				        <font color="red">Error uploading data file.  Check server log for details!</font>
				    </td>
			    </tr>
		    
		    
      
                <tr style="padding: 5px">
                    <td colspan="3" id="incorrectExtension" class="errorMsg">
                        <font color="red">Incorrect file extension found.  Only .csv is allowed.</font>
                    </td>
                </tr>
            
            <tr>
            	<td colspan="3">
	            	<div class="progress-label">Starting import...</div>
					<div id="progressbar"></div>
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
		          <input type="button" name="import" id="import" value="Import" onclick="validateSelected();" style="width:70px">&nbsp;
		           <input type="button" name="Cancel" value="Cancel" onclick="confirmCancel();" style="width:70px">
		       </td>
		    </tr>
		</table>
		</form>
    </body>
    
    <script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/importConceptsFromFile.js"></script>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>