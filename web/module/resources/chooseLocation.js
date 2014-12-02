/**
 * 
 */

function checkSubClass(checkbox_id){
	var me = document.getElementById(checkbox_id);
	if(me.checked==true){
		var locAndTag = checkbox_id.split("#$#");
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
		var locAndTag = checkbox_id.split("#$#");
		var locName = locAndTag[0];
		document.getElementById(locName+"#$#ALL").checked=false;
		document.getElementById("ALL#$#ALL").checked=false;
	}
}