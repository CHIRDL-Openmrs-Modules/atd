<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="" otherwise="/login.htm"
	redirect="/module/atd/data.form" />


<style> 
#ruleBuilderTOC {
	float: top;
	margin-left: 0px;
	padding-left: 0px;
	padding-top: 0px;
	width: 900px;
	}
	#ruleBuilderTOC li {
		list-style-type: none;
		margin: 0;
		padding: 0;
		display: inline;
	}
	#ruleBuilderTOC li.selected {
		border-color: cadetblue cadetblue whitesmoke cadetblue;
		border-width: 1px;
		border-style: solid solid dashed solid;
		margin-bottom: -1px;
	}
	#ruleBuilderTOC li a {
		display: inline-block;
		padding: 3px;
	}
</style>
<h2><spring:message code="atd.rules.title" /></h2>

<spring:hasBindErrors name="rules">
	<spring:message code="atd.fix.error" />
	<div class="error"><c:forEach items="${errors.allErrors}" var="error">
		<spring:message code="${error.code}" text="${error.code}" />
		<br />
		<!-- ${error} -->
	</c:forEach></div>
	<br />
</spring:hasBindErrors>

<form method="post">

<div id="dataForm">
<fieldset><legend><spring:message code="atd.data.add.read" /></legend>
<table>
	<tr>
		<td>
			<table>
			<tr>
				<td><spring:message code="atd.data.add.name" /></td>
				<td><input type="text" name="${status.expression}" value="${status.value}"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.typeRead" /></td>
				<td><select name="${status.expression}"/> 
				<option value="${status.value}"/>Exist</option>
				<option value="${status.value}"/>Last</option>
				<option value="${status.value}"/>First</option>
				<option value="${status.value}"/>Max</option>
				<option value="${status.value}"/>Min</option>
				<option value="${status.value}"/>Count</option>
				<option value="${status.value}"/>Average</option>
				<option value="${status.value}"/>Sum</option>
				</select></td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.time" /></td>
				<td><select name="${status.expression}"/> 
				<option value="${status.value}"/>Where it Occured within Past</option>
				<option value="${status.value}"/>None</option>
				</select></td>
			</tr>
			<tr>
				<td colspan=""><spring:message code="atd.data.add.timeUnit" /></td>	
				<td><select name="${status.expression}"/> 
				<option value="${status.value}"/>365</option>
				<option value="${status.value}"/>364</option>
				<option value="${status.value}"/>363</option>
				<option value="${status.value}"/>362</option>
				<option value="${status.value}"/>361</option>
				<option value="${status.value}"/>360</option>
				<option value="${status.value}"/>359</option>
				<option value="${status.value}"/>358</option>
				</select>
				<select name="${status.expression}"/> 
				<option value="${status.value}"/>Days</option>
				<option value="${status.value}"/>Years</option>
				<option value="${status.value}"/>Months</option>
				<option value="${status.value}"/>Weeks</option>
				</select></td>
			</tr>
			<tr>
				<td align="center"><button type="button" name="button" value="button"/><spring:message code="atd.data.add.add" /></td>
					<td align="center"><button type="reset" name="reset" value="reset"/><spring:message code="atd.data.add.clear" /></td>
			</tr>&nbsp;
			<tr>
			</tr>
			<tr>&nbsp;
			</tr>
			</table>
		</td>
		<td>
			<table>
			<tr>
				<td><spring:message code="atd.data.add.type" /></td>
				<td><input type="text" name="${status.expression}" value="${status.value}"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.export" /><input type="checkbox" name="${status.expression}" value="${status.value}"></td>
				<td><spring:message code="atd.data.add.STUDY" /><input type="checkbox" name="${status.expression}" value="${status.value}">
				</td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.description" /></td>
				<td><input type="text" name="${status.expression}" value="${status.value}"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.answers" /></td>
				<td><input type="text" name="${status.expression}" value="${status.value}"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.units" /></td>
				<td><input type="text" name="${status.expression}" value="${status.value}"/>
				</td>
			</tr>
			<tr>
				<td colspan=2><spring:message code="atd.data.add.search" /></td>
			</tr>
			<tr>
				<td><spring:message code="atd.data.add.keyword" /></td>
				<td><input type="text" name="${status.expression}" value="${status.value}"/>
				</td>
			</tr>
			<tr>
				<td align="center"><button type="button" name="button" value="button"/><spring:message code="atd.data.add.update" /></td>
				<td align ="center"><button type="button" name="button" value="button"/><spring:message code="atd.data.add.delete" /></td>
			</tr>
			<tr>
				<td colspan = 2 align="center"><button type="button" name="button" value="button"/><spring:message code="atd.data.add.searchButton" /></td>
			</tr>
			</table>
		</td>
	</tr>
</table>
</fieldset>


<%@ include file="/WEB-INF/template/footer.jsp"%>
