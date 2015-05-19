<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<openmrs:require allPrivileges="View Encounters, View Patients, View Concept Classes" otherwise="/login.htm" redirect="/module/chirdutil/importConceptsFromFile.form" />
<link
    href="${pageContext.request.contextPath}/moduleResources/atd/atd.css"
    type="text/css" rel="stylesheet" />
<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/jquery-1.11.1.min.js"></script>
<script type="text/javascript" charset="utf8" src="${pageContext.request.contextPath}/moduleResources/atd/update_jquery-ui.js"></script>

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
    <body OnLoad="document.input.formName.focus();">
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
    
    <script>
    var progressbar = $( "#progressbar" );
    var progressLabel = $( ".progress-label" );
    var checkProgressTimer;
    var ajaxURL = '/openmrs/moduleServlet/atd/importConceptsFromFile';
    
    $j(function(){
    	// Check to see if the user already has an import running and initialize the progress bar if necessary
    	checkImportProgress();
    });
    
    function validateSelected()
    {
    	if($j("#dataFile").attr("files").length > 0) // Make sure a file has been selected
    	{
    		startImport();
    	}
    	else
    	{
    		$j('<div id="noFileSelected" title="No file selected">Please select a file.</div>').dialog({
    			modal: true,
    			height: "auto",
    			width: 300,
    			resizable: false,
    			buttons: {
    					  OK: function() {
    					  $j( this ).dialog( "close" );
    					  }
    					 }
    			});
    	}
    }
    
    function confirmCancel()
    {
		if ($("#progressbar").hasClass('ui-widget-content')) { // Make sure the progress bar has been initialized
			$j('<div id="cancelImport" title="Cancel Import?">Are you sure you want to cancel the import?</div>').dialog({
				modal: true,
				height: "auto",
				width: 300,
				resizable: false,
				buttons: {
						  OK: function() {
							  cancelImport();
							  $j(this).dialog("close");
						  }, 
						  Cancel: function(){
							  $j(this).dialog("close");
						  }
						 }
				});
		   } 	
    }
    
    function cancelImport()
    {
	    	$.ajax({type: 'GET', cache: false, dataType: 'json', url: ajaxURL+'?cancelImport=true', success: function(data){
				progressbar.progressbar("value", data.currentRow)
				checkProgressTimer = setTimeout( checkImportProgress, 5000 );	
	    }});
    }
   
    function checkImportProgress()
    {
    	$.ajax({type: 'GET', cache: false, dataType: 'json', url: ajaxURL+'?checkProgress=true', success: function(data){
			if(data.importStarted)
			{
				if(!($("#progressbar").hasClass('ui-widget-content')))
				{
					if(!(data.isComplete))
					{
						progressbar.progressbar({
  						  max: data.totalRowsFound,
  				          value: data.currentRow,
  				          change: function() {
  				            progressLabel.text("Import Progress: " +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
  				          },
  				          complete: function() {   				        	  
  				             progressLabel.text( "Import Complete!" );   				        	  				            
  				          }
  				        });
						progressLabel.text("Import Progress: " +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
	  					progressLabel.show();
	  					progressbar.show();
	  					checkProgressTimer = setTimeout( checkImportProgress, 5000 );
					}
				}
				else
				{
					if(data.isComplete)
					{
						// Set the progress bar to the max value in case not all concepts were imported so that the complete event will be triggered
						progressbar.progressbar("value", progressbar.progressbar("option", "max"));
						clearTimeout(checkProgressTimer);
						
						// The import completed, but one or more errors occurred during the import
						if(data.errorOccurred)
						{
							displayError("The import is complete, but one or more errors occurred. Check the server log for details.")
						}
					}
					else
					{
						if(data.totalRowsFound != progressbar.progressbar("option", "max")) // The total rows found has changed, which means we should have moved on to creating concept answers
						{
							// Destroy the old progress bar and initialize a new one
							progressbar.progressbar("destroy");
							
							progressbar.progressbar({
	  						  max: data.totalRowsFound,
	  				          value: data.currentRow,
	  				          change: function() {
	  				            progressLabel.text("Import Progress: " +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
	  				          },
	  				          complete: function() {   				        	  
	  				             progressLabel.text( "Import Complete!" );   				        	  				            
	  				          }
	  				        });
							
							checkProgressTimer = setTimeout( checkImportProgress, 5000 );
						}
						progressbar.progressbar("value", data.currentRow)
						checkProgressTimer = setTimeout( checkImportProgress, 5000 );
					}
				}
			}
			else
			{
				if ($("#progressbar").hasClass('ui-widget-content')) {
					progressbar.progressbar("value", 0);
				}
			}
	    }});
    }
   
    function startImport()
    {
    	$(".errorMsg").hide();
    	var dataFile = $("#dataFile");
    	var formdata = false;
    	
    	if(window.FormData)
    	{
    		formdata = new FormData();
    		formdata.append('file', dataFile[0].files[0]);
    		    		
    		$.ajax({
    			type: 'POST', 
    			dataType: 'json',
    			processData: false, 
    			cache: false,
    			contentType: false, // NOTE: this must be false and not 'multipart/form-data'
    			data: formdata, 
    			url: ajaxURL+'?beginImport=true', 
    			success: function(data){
    				if(data.importStarted)
    				{
    					progressbar.progressbar({
    						  max: data.totalRowsFound,
    				          value: data.currentRow,
    				          change: function() {
    				            progressLabel.text("Import Progress: " +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
    				          },
    				          complete: function() {   				        	  
    				             progressLabel.text( "Import Complete!" );   				        	  				            
    				          }
    				        });
    					progressLabel.show();
    					progressbar.show();
    					checkProgressTimer = setTimeout( checkImportProgress, 5000 );
    				}
    				else
    				{
    					if(data.incorrectExtension)
    					{
    						$("#incorrectExtension").show();
    					}
    					else if(data.invalidUploadRequest || data.serverError)
    					{
    						$("#uploadError").show();
    					}
    					else if(data.alreadyRunning)
    					{
    						displayError("An import is already running.");
    					}
    				}
    	    }});
    	}
    }
    
    function displayError(errorMsg)
    {
    	$j('<div id="error" title="Error">' + errorMsg + '</div>').dialog({
			modal: true,
			height: "auto",
			width: 300,
			resizable: false,
			buttons: {
					  OK: function() {
					  $j( this ).dialog( "close" );
					  }
					 }
			});
    }
    </script>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>