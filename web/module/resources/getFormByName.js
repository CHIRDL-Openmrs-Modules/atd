// DWE CHICA-331 4/10/15 JS for getFormByName.form

function displayError(errorMsg)
{
	$j('<div id="errorMsg" title="Error">'+ errorMsg +'</div>').dialog({
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


function validateSelections()
{
	if($j("#csv").is(':checked')) // Edit with csv file, show the confirm dialog
	{
		if($j("#csvFile").attr("files").length > 0) // Make sure a file has been selected
		{
			$j('<div id="confirmUpload" title="Edit by uploading csv?"><p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>Are you sure you want to upload the file? <br/>Form attribute values will be updated and changes cannot be undone.</p></div>').dialog({
				 height: "auto",
				 width: "auto",
				 modal: true,
				 resizable: false,
				 buttons: {
				    "OK": function() {
				   	 $j(this).dialog("close");
				   		$j("#manualOrCSV").submit();
				     },
				     Cancel: function() {
				     $j(this).dialog("close");
				     }
				  }
			});	
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
	else
	{
		// Edit manually, make sure a form has been selected in the drop-down
		var formId = Number($j("#formNameSelect").val());
		if(formId > -1)
		{
			$j("#manualOrCSV").submit();
		}
		else
		{
			$j('<div id="noFormSelected" title="No form selected">Please select a form.</div>').dialog({
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
}

function backToConfigManager(backPagePath)
{
	window.location = backPagePath;
}