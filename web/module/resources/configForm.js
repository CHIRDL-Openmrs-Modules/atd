/**
 * 
 */

// function to display Popup
function div_show(div_id) {
	document.getElementById(div_id).style.display = "block";
}

function closeForm(div_id) {
	document.getElementById(div_id).style.display = "none";
}

function changeLocationAttributes(div_id) {
	document.getElementById(div_id).style.display = "none";
}

// function to check parent node and return result accordingly
function checkParent(t) {
	while (t.parentNode) {
		if (t == document.getElementById('div_popup')) {
			alert("return false");
			return false
		} else if (t == document.getElementById('close')) {
			alert("return true");
			return true
		}
		t = t.parentNode
	}
	alert("outside, return true");
	return true
}

// Escapes special characters and returns a valid jQuery selector
function jqSelector(str) {
	return str.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
}

function assignValueSubClass(inpt_id) {
	var idContent = inpt_id.split("_");
	var attributeId = idContent[1];
	var locationId = idContent[2];
	var value = document.getElementById(inpt_id).value;
	if (value != null && value != "") {
		if (locationId != "ALL") {
			var subBoxes = document.getElementsByClassName(attributeId + " "
					+ locationId);
			for (var i = 0; i < subBoxes.length; i++) {
				if (subBoxes[i].id != null) {
					var subBoxAsComboBox = $("#" + subBoxes[i].id).data(
							"kendoComboBox");
					if (subBoxAsComboBox != null) {
						subBoxAsComboBox.text(value);
					}
				}
			}
		} else {
			var subBoxes = document.getElementsByClassName(attributeId);
			for (var i = 0; i < subBoxes.length; i++) {
				subBoxes[i].value = value;
			}
		}
	}
	var thisComboBox = $(inpt_id).data("kendoComboBox");
	thisComboBox.text(value);
}

function removeValueSuperClass(inpt_id) {
	var idContent = inpt_id.split("_");
	var attributeId = idContent[1];
	var locationId = idContent[2];
	var value = document.getElementById(inpt_id).value;
	var superClass = $("#inpt_" + attributeId + "_" + locationId + "_ALL")
			.data("kendoComboBox");
	var superSuperClass = $("#inpt_" + attributeId + "_" + "ALL_ALL").data(
			"kendoComboBox");
	superClass.text("");
	superSuperClass.text("");
}

function restore(div_id, faId, originalValues){
	var allTexts = document.getElementsByClassName(faId);
	for (var i = 0; i < allTexts.length; i++) {
		allTexts[i].value = "";
	}
	var allPositionText = $("#"+"inpt_"+faId+"_ALL_ALL").data("kendoComboBox");
	allPositionText.text("");
	for(var id in originalValues[faId]){
		if(originalValues[faId].hasOwnProperty(id)){
			var textbox = $("#"+id).data("kendoComboBox");
			textbox.text(originalValues[faId][id]);
		}
	}
	closeForm(div_id);
	
}
