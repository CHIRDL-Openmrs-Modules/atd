/**
 * 
 */

function checkSubClass(checkbox_id){
	var me = document.getElementById(checkbox_id);
	if(me.checked==true){
		var locAndTag = checkbox_id.split("_");
		var locName = locAndTag[0];
		if(locName=="ALL"){
			var targetBoxes = document.getElementsByClassName("ALL");
			for(var i=0; i<targetBoxes.length; i++){
				targetBoxes[i].checked=true;
				checkSubClass(targetBoxes[i].id);
			}
		}else{
			var targetBoxes = document.getElementsByClassName(locName);
			for(var i=0; i<targetBoxes.length; i++){
				targetBoxes[i].checked = true;
			}
		}
	}
}

function uncheckSuperClass(checkbox_id){
	var me = document.getElementById(checkbox_id);
	if(me.checked==false){
		var locAndTag = checkbox_id.split("_");
		var locName = locAndTag[0];
		document.getElementById(locName+"_ALL").checked=false;
		document.getElementById("ALL_ALL").checked=false;
	}
}

function checkTags(locationId)
{
	if(locationId == "ALL_ALL")
	{
		if($j("#selectAllButton").hasClass("showingAll"))
		{
			$j(':checkbox').each(function(){ this.checked = false});
			$j("#selectAllButton").removeClass("showingAll");
		}
		else
		{
			$j(':checkbox').each(function(){ this.checked = true});
			$j("#selectAllButton").addClass("showingAll");
		}
		
	}
	else
	{
		// This will select/unselect all location tags
		// If the first tag checkbox is already checked, it will uncheck all, 
		// if the first tag is unchecked, it will check all
		var $cbs = $j("." + locationId + ":checkbox:enabled");
		var checked = $cbs.filter(":first").prop('checked');
		$j('.' + locationId).each(function(){ this.checked = !checked});	
	}	
}

function validateSelected()
{
	 if($j("input:checked").length > 0)
	 {
		return true;	 
	 }
	 else
	 {
		 $j('<div id="errorMsg" title="Select Location and Tags">You must select at least one location tag.</div>').dialog({
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
