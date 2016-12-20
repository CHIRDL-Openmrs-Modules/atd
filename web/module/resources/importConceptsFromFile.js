/**
 * DWE CHICA-426
 */    
var progressbar = $( "#progressbar" );
var progressLabel = $( ".progress-label" );
var checkProgressTimer;
var ajaxURL = ctx + '/moduleServlet/atd/importConceptsFromFile';
var changeEventText = 'Import Progress: ';
var completeEventText = 'Import Complete!';
var canceledEventText = 'Import Canceled: ';


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
  				            progressLabel.text(changeEventText +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
  				          },
  				          complete: function() {   				        	  
  				             progressLabel.text( completeEventText );   				        	  				            
  				          }
  				        });
						// The import was already running when the page was loaded, set the label to the appropriate text instead of waiting for the "change" event
						progressLabel.text(changeEventText +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
	  					progressLabel.show();
	  					progressbar.show();
	  					checkProgressTimer = setTimeout( checkImportProgress, 5000 );
					}
				}
				else
				{
					if(data.isComplete && !data.isImportCancelled)
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
					else if(data.isComplete && data.isImportCancelled)
					{
						progressLabel.text(canceledEventText + data.currentRow + " of " + progressbar.progressbar("option", "max"));
						clearTimeout(checkProgressTimer);
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
	  				            progressLabel.text(changeEventText +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
	  				          },
	  				          complete: function() {   				        	  
	  				             progressLabel.text(completeEventText);   				        	  				            
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
  				            progressLabel.text(changeEventText +  progressbar.progressbar( "value" ) + " of " + progressbar.progressbar("option", "max") );
  				          },
  				          complete: function() {   				        	  
  				             progressLabel.text(completeEventText );   				        	  				            
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
    					else if(data.fileParseError)
    					{
    						displayError("Unable to start the import. An error occurred while parsing the import file.")	
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