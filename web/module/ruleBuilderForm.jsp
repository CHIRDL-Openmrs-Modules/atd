<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="" otherwise="/login.htm"
	redirect="/module/atd/ruleBuilder.form" />

<script type="text/javascript">
window.onload = init;

function my_win()
{
window.open('${pageContext.request.contextPath}/module/atd/data.form','mywindow','fullscreen=yes,toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,copyhistory=yes,resizable=yes');
}

function init() {
	var sections = new Array();
	var optform = document.getElementById("ruleBuilderForm");
	children = optform.childNodes;
	var seci = 0;
	for(i=0;i<children.length;i++) {
		if(children[i].nodeName.toLowerCase().indexOf('fieldset') != -1) {
			children[i].id = 'optsection-' + seci;
			children[i].className = 'optsection';
			legends = children[i].getElementsByTagName('legend');
			sections[seci] = new Object();
			if(legends[0] && legends[0].firstChild.nodeValue)
				sections[seci].text = legends[0].firstChild.nodeValue;
			else
				sections[seci].text = '# ' + seci;
			sections[seci].secid = children[i].id;
			sections[seci].error = containsError(children[i]);
			seci++;
			if(sections.length != 1)
				children[i].style.display = 'none';
			else
				var selectedid = children[i].id;
		}
	}
	
	var toc = document.createElement('ul');
	toc.id = 'ruleBuilderTOC';
	toc.selectedid = selectedid;
	for(i=0;i<sections.length;i++) {
		var li = document.createElement('li');
		if(i == 0) li.className = 'selected';
		var a =  document.createElement('a');
		a.href = '#' + sections[i].secid;
		a.onclick = uncoversection;
		a.appendChild(document.createTextNode(sections[i].text));
		a.secid = sections[i].secid;
		a.id = sections[i].secid + "_link";
		if (sections[i].error) {
			a.className = "error";
		}
		li.appendChild(a);
		toc.appendChild(li);
	}
	optform.insertBefore(toc, children[0]);

	var hash = document.location.hash;
	if (hash.length > 1) {
		var autoSelect = hash.substring(1, hash.length);
		for(i=0;i<sections.length;i++) {
			if (sections[i].text == autoSelect)
				uncoversection(sections[i].secid + "_link");
		}
	}
}

function uncoversection(secid) {
	var obj = this;
	if (typeof secid == 'string') {
		obj = document.getElementById(secid);
		if (obj == null)
			return false;
	}

	var ul = document.getElementById('ruleBuilderTOC');
	var oldsecid = ul.selectedid;
	var newsec = document.getElementById(obj.secid);
	if(oldsecid != obj.secid) {
		document.getElementById(oldsecid).style.display = 'none';
		newsec.style.display = 'block';
		ul.selectedid = obj.secid;
		lis = ul.getElementsByTagName('li');
		for(i=0;i< lis.length;i++) {
			lis[i].className = '';
		}
		obj.parentNode.className = 'selected';
	}
	newsec.blur();
	return false;
}

function containsError(element) {
	if (element) {
		var child = element.firstChild;
		while (child != null) {
			if (child.className == 'error') {
				return true;
			}
			else if (containsError(child) == true) {
				return true;
			}
			child = child.nextSibling;
		}
	}
	return false;
}

</script>
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
		background-color: whitesmoke;
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

<div id="ruleBuilderForm">
<fieldset><legend><spring:message code="atd.rules.maintenance.legend" /></legend>
<table>
	<tr>
		<td width="1"><spring:message code="atd.rules.maintenance.title" /></td>
		<td width="202"><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.filename" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.version" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.institution" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.author" /></td>
		<td><div align="center">
		  <select name="${status.expression}"/>
	       
		  <option value="${status.value}"/>                      
		  </option>
		  </select>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.specialist" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.date" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.maintenance.validation" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
</table>
</fieldset>
<fieldset><legend><spring:message code="atd.rules.library.legend" /></legend>
<table>
	<tr>
		<td width="1"><spring:message code="atd.rules.library.purpose" /></td>
		<td width="200"><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.library.explanation" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.library.keywords" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.library.citations" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.library.links" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
</table>
</fieldset>
<fieldset><legend><spring:message code="atd.rules.knowledge.legend" /></legend>
<table width="210">
	<tr>
		<td><spring:message code="atd.rules.knowledge.type" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.knowledge.priority" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.knowledge.evoke" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td><spring:message code="atd.rules.knowledge.urgency" /></td>
		<td><div align="center">
		  <input type="text" name="${status.expression}" value="${status.value}"/>
	    </div></td>
	</tr>
	<tr>
		<td colspan="2"><spring:message code="atd.rules.knowledge.message" /></td>
	</tr>
</table>
</fieldset>
<fieldset><legend><spring:message code="atd.rules.data.legend" /></legend>
<table>
	<tr>
		<td colspan="2"><input type="text" name="${status.expression}" value="${status.value}" size="100" />
		</td>
	</tr>
	<tr>
		<td><button type="button" name="${status.expression}" value="${status.value}" onClick="javascript:my_win()" value="New Window"/><spring:message code="atd.rules.data.add" /></td>
		<td><button type="reset" name="reset" value="reset"/><spring:message code="atd.rules.data.clear" /></td>
	</tr>
</table>
</fieldset>
<fieldset><legend><spring:message code="atd.rules.logic.legend" /></legend>
<table>
	<tr>
		<td colspan="2"><input type="text" name="${status.expression}" value="${status.value}" size="100" />
		</td>
	</tr>
	<tr>
		<td><button type="submit" name="submit" value="submit"/><spring:message code="atd.rules.logic.add" /></td>
		<td><button type="reset" name="reset" value="reset"/><spring:message code="atd.rules.logic.clear" /></td>
	</tr>
</table>
</fieldset>
<fieldset><legend><spring:message code="atd.rules.action.legend" /></legend>
<table>
	<tr>
		<td colspan="2"><spring:message code="atd.rules.action.instructions" />
		</td>
	</tr>
	<tr>
		<td><select name="${status.expression}"/> 
			<option value="${status.value}"/></option>
			</select>
			<button type="button" name="button" value="button"/><spring:message code="atd.rules.action.write" />
		</td>
		<td><input type="checkbox" name="${status.expression}" value="${status.value}">
			<button type="button" name="button" value="button"/><spring:message code="atd.rules.action.AT" />
		</td>
	</tr>
	<tr>
		<td colspan="2"><spring:message code="atd.rules.action.textInstructions" />
		</td>
	</tr>
	<tr>
		<td colspan="2"><input type="text" name="${status.expression}" value="${status.value}" size="100" />
		</td>
	</tr>
	<tr>
		<td colspan="2"><spring:message code="atd.rules.action.bottomInstructions" />
		</td>
	</tr>
</table>
</fieldset>
<fieldset><legend>Browse Rules</legend>
<p><b>Please enter rule search terms:</b></p>
<form name="input" action="ruleBuilder.form#optsection-6" method="get">
<table>
<tr><td width="140">Restrict on <b>title:</b></td><td width="194"> <div align="center">
  <input type="text" name="title" value="${title}" size="20">
</div></td>
  <td width="96">&nbsp;</td>
<td width="178">Restrict on <b>author:</b></td><td width="218"> <div align="center">
  <input type="text" name="author" value="${author}" size="20">
</div></td></tr>
<tr><td>Restrict on <b>keywords:</b></td><td> <div align="center">
  <input type="text" name="keywords" value="${keywords}" size="20">
</div></td>
  <td>&nbsp;</td>
<td>Restrict on <b>rule type:</b></td><td> <div align="center">
  <input type="text" name="ruleType" value="${ruleType}" size="20">
</div></td></tr>
<tr><td>Restrict on <b>action:</b></td><td> <div align="center">
  <input type="text" name="action" value="${action}" size="20">
</div></td>
  <td>&nbsp;</td>
<td>Restrict on <b>logic:</b></td><td> <div align="center">
  <input type="text" name="logic" value="${logic}" size="20">
</div></td></tr>
<tr><td>Restrict on <b>data:</b></td><td><div align="center">
  <input type="text" name="data" value="${data}" size="20">
</div></td>
  <td>&nbsp;</td>
<td>Restrict on <b>links:</b></td><td> <div align="center">
  <input type="text" name="links" value="${links}" size="20">
</div></td></tr>
<tr><td>Restrict on <b>citations:</b></td><td> <div align="center">
  <input type="text" name="citations" value="${citations}" size="20">
</div></td>
  <td>&nbsp;</td>
<td>Restrict on <b>explanation:</b></td><td> <div align="center">
  <input type="text" name="explanation" value="${explanation}" size="20">
</div></td></tr>
<tr><td>Restrict on <b>purpose:</b></td><td> <div align="center">
  <input type="text" name="purpose" value="${purpose}" size="20">
</div></td>
  <td>&nbsp;</td>
<td>Restrict on <b>specialist:</b></td><td><div align="center">
  <input type="text" name="specialist" value="${specialist}" size="20">
</div></td></tr>
<tr><td>Restrict on <b>institution:</b></td><td><div align="center">
  <input type="text" name="institution" value="${institution}" size="20">
</div></td>
  <td>&nbsp;</td>
<td>Restrict on <b>class file name:</b></td><td><div align="center">
  <input type="text" name="classFilename" value="${classFilename}" size="20">
</div></td></tr>
<tr>
  <td colspan="5" style="text-align:right">&nbsp;</td>
</tr>
<tr><td style="text-align:right"><div align="center"></div></td>
  <td style="text-align:right">&nbsp;</td>
  <td style="text-align:right">&nbsp;</td>
  <td style="text-align:right">&nbsp;</td>
  <td style="text-align:right"><div align="center">
    <input type="submit" value="Start Search" />
  </div></td>
</tr>
</table>
<input type="hidden" name="runSearch" value="true"/>
</form>
<c:if test="${runSearch}">
	Here are the matching rules:<br><br>
<c:forEach items="${rules}" var="databaseRule">
		<table style="border-width: 2px;border-style: solid;" width="100%">
		<tr>
		<td><b>Rule&nbsp;Id:</b></td>
		<td>${databaseRule.ruleId}</td>
		</tr>
		<tr>
		<td><b>Title:</b></td>
		<td>${databaseRule.title}</td>
		</tr>
		<tr>
		<td><b>Class&nbsp;File&nbsp;name:</b></td>
		<td>${databaseRule.classFilename}</td>
		</tr>
		<tr>
		<td><b>Creation&nbsp;time:</b></td>
		<td>${databaseRule.creationTime}</td>
		</tr>
		<tr>
		<td><b>Priority:</b></td>
		<td>${databaseRule.priority}</td>
		</tr>
		<tr>
		<td><b>Version:</b></td>
		<td>${databaseRule.version}</td>
		</tr>
		<tr>
		<td><b>Institution:</b></td>
		<td>${databaseRule.institution}</td>
		</tr>
		<tr>
		<td><b>Author:</b></td>
		<td>${databaseRule.author}</td>
		</tr>
		<tr>
		<td><b>Specialist:</b></td>
		<td>${databaseRule.specialist}</td>
		</tr>
		<tr>
		<td><b>Rule&nbsp;Creation&nbsp;Date:</b></td>
		<td>${databaseRule.ruleCreationDate}</td>
		</tr>
		<tr>
		<td><b>Purpose:</b></td>
		<td>${databaseRule.purpose}</td>
		</tr>
		<tr>
		<td><b>Explanation:</b></td>
		<td>${databaseRule.explanation}</td>
		</tr>
		<tr>
		<td><b>Keywords:</b></td>
		<td>${databaseRule.keywords}</td>
		</tr>
		<tr>
		<td><b>Citations:</b></td>
		<td>${databaseRule.citations}</td>
		</tr>
		<tr>
		<td><b>Links:</b></td>
		<td>${databaseRule.links}</td>
		</tr>
		<tr>
		<td><b>Data:</b></td>
		<td>${databaseRule.data}</td>
		</tr>
		<tr>
		<td><b>Logic:</b></td>
		<td>${databaseRule.logic}</td>
		</tr>
		<tr>
		<td><b>Action:</b></td>
		<td>${databaseRule.action}</td>
		</tr>
		<tr>
		<td><b>Rule&nbsp;Type:</b></td>
		<td>${databaseRule.ruleType}</td>
		</tr>
		<tr>
		<td><b>Last&nbsp;Modified:</b></td>
		<td>${databaseRule.lastModified}</td>
		</tr>
		</table>
	</c:forEach>
	</c:if>
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>
