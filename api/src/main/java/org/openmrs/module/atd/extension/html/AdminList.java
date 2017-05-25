package org.openmrs.module.atd.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

/**
 * Adds links to the administration page of the openmrs webapp
 * 
 * @author Tammy Dugan
 *
 */
public class AdminList extends AdministrationSectionExt {

	@Override
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public String getTitle() {
		return "atd.title";
	}
	
	@Override
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("module/atd/uploadForms.form", "atd.uploadForms");
		//map.put("module/atd/editForms.form", "Edit Forms");
		//map.put("module/atd/deleteForms.form", "Delete Forms");
		//map.put("module/atd/createForm.form", "Create Form");
		map.put("module/atd/ruleBuilder.form", "Meaghan's Rule Builder");
		//map.put("module/atd/printerFormSelectionForm.form", "Printer Configuration");
		//map.put("module/atd/replaceForm.form", "Replace Form");
		//map.put("module/atd/updateForm.form", "Update Form");
		map.put("module/atd/configurationManager.form", "Configuration Manager");
		map.put("module/atd/importConceptsFromFile.form", "Import Concepts From File");
		map.put("module/atd/createFormFromFile.form", "Create Form From File");

		return map;
	}
	
}