$j(document).ready(function() {
	$j('.toggleAddClinicTagAttribute').click(function(event) {
		$j('#addClinicTagAttribute').slideToggle('fast');
		event.preventDefault();
	});
});

function addClinicTagAttr() {
	var clinicTagAttrName = document.input.name.value;
	if (clinicTagAttrName == "") {
		document.getElementById('errorMessage').innerHTML = "";
		var errorMsg = "Please enter a location tag attribute name!"
		document.getElementById('errorMessage').innerHTML = errorMsg;
		document.getElementById('errorMsg').innerHTML = "";
	} else {
		document.getElementById('errorMessage').innerHTML = "";
		document.getElementById('errorMsg').innerHTML = "";
		document.input.submit();
	}
}

function cancel() {
	document.getElementById('errorMessage').innerHTML = "";
}

function confirmCancel(backtoConfigpage)
{
    var agree=confirm("Are you sure you want to stop editing clinic tag attribute values?");
    if (agree) {
		   window.location = backtoConfigpage;
    }
}

function checkform() {
	var errorMsg = null;
	var location = document.input.location.value;
	var locationTag = document.input.tagName.value;
	if(location == "" && locationTag == "") {
		errorMsg = "Please enter a location name and location tag name!"
		document.getElementById('errorMsg').innerHTML = errorMsg;
		document.getElementById('errorMessage').innerHTML = "";
		return false;
	} 
	else if(location == "") {
		errorMsg = "Please enter a location name!"
		document.getElementById('errorMsg').innerHTML = errorMsg;
		document.getElementById('errorMessage').innerHTML = "";
		return false;
	} 
	else if (locationTag == "") {
		errorMsg = "Please enter a location tag name!"
		document.getElementById('errorMsg').innerHTML = errorMsg;
		document.getElementById('errorMessage').innerHTML = "";
		return false;
	}
	else {
		document.input.submit();
	}
}

