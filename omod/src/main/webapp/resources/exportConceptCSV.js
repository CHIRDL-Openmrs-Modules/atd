var selectedIds = [];
var dtable;

function updateAndSubmit()
{
	document.forms[0].selectedIdsField.value = selectedIds;
	document.forms[0].submit();
}

//Function to check/uncheck all checkboxes, only supported for 
//the page in the datatable that is currently displayed 
function toggleSelectedConcepts()
{
	var $cbs = $j(".conceptCheckbox:checkbox:enabled");
	var checked = $cbs.filter(":first").prop('checked');
	$j('.conceptCheckbox').each(function(){ 
		this.checked = !checked; 
		toggleSelectedObject(this.id);
	});
}

//Function to send request to hide/show retired concepts
//and concept class drop-down
function submitFilter()
{
	dtable.ajax.url(ctx + "/moduleServlet/atd/exportConceptServlet?loadTable=true&includeRetired=" + $j("#inclRetired").is(':checked') + "&conceptClassSelect=" + $j("#conceptClassSelect").val()).load();
}

// Create the checkbox column
function renderCheckbox(data, type, full, meta)
{
	// data is mData for this column, which is conceptId for the checkbox column
	// type defaults to display
	// full is the full data source for this row
	var parentConceptId = full.parentConceptId; // Use the parentConceptId in the data-* attribute
	var combinedId = data + "_" + parentConceptId;
	var checked = $j.inArray(combinedId, selectedIds) > -1 ? "checked" : "";
	
	return "<input class='conceptCheckbox' " + checked + " value='" + data + "' onclick='toggleSelectedObject(this.id);' data-parentconceptid='" + parentConceptId + "' type='checkbox' id='" + combinedId + "' name='" + combinedId + "'/>";
}

// Add/remove the selected Id
function toggleSelectedObject(id)
{
	if($j("#" + id).is(':checked'))
	{
		selectedIds.push(id);
	}
	else
	{
		selectedIds.splice($j.inArray(id, selectedIds), 1);
	}
}

$j(document).ready(function() {
	
	dtable = $j('#conceptDefinitionTable').dataTable(
				{
					// Table options have been updated for DataTables version 1.10
					"jQueryUI": true,
					"order": [[ 5, "asc" ]],
					"processing": true,
					"serverSide": true,
					"stateSave": false,
					"pagingType": "full_numbers",
					"ajax": ctx + "/moduleServlet/atd/exportConceptServlet?loadTable=true&includeRetired=" + $j("#inclRetired").is(":checked") + "&conceptClassSelect=" + $j("#conceptClassSelect").val(),
					"columns": [
									{"mData": "conceptId", "bSortable": false, "searchable": false, "mRender": renderCheckbox},
									{"mData": "name", "bSortable": true, "searchable": true},
									{"mData": "conceptClass", "bSortable": false},
									{"mData": "datatype", "bSortable": false },
									{"mData": "description", "bSortable": false},
									{"mData": "conceptId", "bSortable": false },
									{"mData": "formattedDateCreated", "bSortable": true},
									{"mData": "units", "bSortable": false },
									{"mData": "parentConcept", "bSortable": true }
								 ]
				}).api();	
	
	// Unbind the default behavior and bind debounce functionality so that the 
	// search is performed only after the user has stopped typing for 500ms
	$j('.dataTables_filter input').unbind().bind('keyup', $j.debounce(function(event){
		var searchValue = $j('.dataTables_filter input').val();
		
		// Only perform the search on the server-side if
		// the enter key was pressed, there has been >= 3 characters entered, or
		// the backspace key was pressed
		if(event.keyCode == 13 || searchValue.length >=3 || event.keyCode == 8)
		{
			dtable.search(searchValue).draw();
		}
	}, 500, false));
	
	$j("#selectAllConcepts").click(function(){toggleSelectedConcepts();return false});
	
} );
