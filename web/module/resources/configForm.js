/**
 * 
 */

function check_empty() {
	if (document.getElementById('name').value == ""
			|| document.getElementById('email').value == ""
			|| document.getElementById('msg').value == "") {
		alert("Fill All Fields !");
	} else {
		document.getElementById('form').submit();
		alert("Form submitted successfully...");
	}
}

// function to display Popup
function div_show(div_id) {
	alert("runs here");
	document.getElementById(div_id).style.display = "block";
}

// function to check target element
function check(e) {

	var target = (e && e.target) || (event && event.srcElement);

	var obj = document.getElementById('div_popup');
	var obj2 = document.getElementById('bt_faxable');
	alert("runs here 3");
	checkParent(target) ? obj.style.display = 'none' : null;
	target == obj2 ? obj.style.display = 'block' : null;

}

function closeForm(div_id) {
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