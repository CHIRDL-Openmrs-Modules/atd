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

function assignValueAll(inpt_id) {
	var idContent = inpt_id.split("_");
	var attributeId = idContent[1];
	var locationId = idContent[2];
	var value = $("#" + inpt_id).val();
	if (value != null && value != "") {
		if (locationId != "ALL") {
			var inputBoxes = document.getElementsByClassName(attributeId + "_"
					+ locationId);
			for (var i = 0; i < inputBoxes.length; i++) {
				if (inputBoxes[i].id != null) {
					var subInput = $("#" + inputBoxes[i].id);
					if (subInput != null) {
						subInput.val(value);
					}
				}
			}
		} else {
			var inputBoxes = document.getElementsByClassName(attributeId + "_ALL");
			for (var i = 0; i < inputBoxes.length; i++) {
				inputBoxes[i].value = value;
			}
		}
	}
}

function removeValueSuper(inpt_id) {
	var idContent = inpt_id.split("_");
	var attributeId = idContent[1];
	var locationId = idContent[2];
	var superClass = $("#inpt_" + attributeId + "_" + locationId + "_ALL");
	var superSuperClass = $("#inpt_" + attributeId + "_" + "ALL_ALL");
	superClass.val("");
	superSuperClass.val("");
}

//This is the old code for use with the kendo combobox
function assignValueSubClass(inpt_id) {
	var idContent = inpt_id.split("_");
	var attributeId = idContent[1];
	var locationId = idContent[2];
	var value = $("#" + inpt_id).val();
	if (value != null && value != "") {
		var subBoxes = document.getElementsByClassName("tags_" + attributeId);
		for (var i = 0; i < subBoxes.length; i++) {
			if (subBoxes[i].id != null) {
				var subBoxAsComboBoxId = subBoxes[i].id
				$("#" + subBoxAsComboBoxId).val(value);
			}
		}
	}
}

// This is the old code for use with the kendo combobox
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
