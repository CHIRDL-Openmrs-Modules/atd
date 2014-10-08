<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Chica get form name</title>
</head>
<body>
<c:if test = "${ioerror}=='true'">
	<h2>server mistake occurs</h2>
</c:if>
<form method="post" action="getFormByName.form" enctype="multipart/form-data">
	Form name:  <input type="text" name="formName"/>
	<c:if test="${not empty noSuchName and noSuchName =='true' }">
		&nbsp;The form name does not exist
	</c:if>
	<br/><br/>
	Edit type:  
	<c:if test = "${not empty typeNotChosen and typeNotChosen=='true'}"> 
		&nbsp;choose a editing type
	</c:if>
	<br/>
	<input type="radio" name="editType" value="manual" onclick="document.getElementById('csvFile').disabled = true"/> Edit manually online <br/>
	<input type="radio" name="editType" value="csv" onclick="document.getElementById('csvFile').disabled = false"/> Edit by uploading csv file <br/>
	<br/>
	upload csv file:   <input type=file id="csvFile" name="csvFile" accept="text/xml" disabled/>
	<c:if test = "${not empty csvFileError and csvFileError=='typeError'}">
		&nbsp;not a csv format file
	</c:if>
	<c:if test = "${not empty csvFileError and csvFileError=='csvFileEmpty'}">
		&nbsp;csv file empty
	</c:if>
	<c:if test = "${not empty csvFileError and csvFileError=='notMultipart'}">
		&nbsp;form error
	</c:if>
	<br/>
	<input type="submit" value="submit"/>

</form>
</body>
</html>
<%@ include file="/WEB-INF/template/footer.jsp"%>