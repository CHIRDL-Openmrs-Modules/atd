package org.openmrs.module.atd.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.hibernateBeans.FormAttribute;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.service.ATDService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


public class FaxableFormController extends SimpleFormController {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("application", "Make Faxable Form");
		String view = getFormView();
		
		String formIdStr = request.getParameter("formToEdit");
		if (formIdStr != null && formIdStr.trim().length() > 0) {
			try {
				Integer formId = Integer.parseInt(formIdStr);
				makeFaxableForm(formId);
			} catch (Exception e) {
				Log.error("Error making form faxable", e);
				map.put("error", true);
				map.put("forms", getForms(false));
				return new ModelAndView(view, map);
			}
		}
		
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
	}
	
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("forms", getForms(false));
		
		return map;
	}
	
	private void makeFaxableForm(Integer formId) throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		ATDService atdService = Context.getService(ATDService.class);
		LocationService locService = Context.getLocationService();
		FormService formService = Context.getFormService();
		
		String installationDirectory = adminService.getGlobalProperty("atd.installationDirectory");
		if (installationDirectory == null) {
			throw new Exception("atd.installationDirectory not specified.");
		}
		
		String serverName = adminService.getGlobalProperty("atd.serverName");
		if (serverName == null) {
			throw new Exception("atd.serverName not specified");
		}
		
		Form currentForm = formService.getForm(formId);
		String currentFormName = currentForm.getName();
		// We need to update retired versions of the form as well.
		List<Form> forms = findAllVersionsOfForm(currentForm);
		
		FormAttribute exportAttr = atdService.getFormAttributeByName("defaultExportDirectory");
		FormAttribute imageAttr = atdService.getFormAttributeByName("imageDirectory");
		
		String newExportValue = installationDirectory + File.separator + "scan" + File.separator + "Fax" + 
			File.separator + currentFormName + "_SCAN";
		String newImageValue = File.separator + File.separator + serverName + File.separator + "images" + 
			File.separator + "Fax" + File.separator + currentFormName;
		
		// Get the merge values.  This will guide us through creating the other info we need.
		for (Location location : locService.getAllLocations()) {
			Integer locationId = location.getLocationId();
			for (LocationTag tag : location.getTags()) {
				Integer locationTagId = tag.getLocationTagId();
				for (Form form : forms) {
					FormAttributeValue value = atdService.getFormAttributeValue(form.getFormId(), "defaultMergeDirectory", 
						locationTagId, locationId);
					if (value != null && value.getValue() != null) {
						// Create and save the new scan value.
						setupFormAttributeValue(atdService, value, installationDirectory, form, locationId, locationTagId, 
							exportAttr, newExportValue);
						
						// Create and save the new image value.
						setupFormAttributeValue(atdService, value, serverName, form, locationId, locationTagId, 
							imageAttr, newImageValue);
					}
				}
			}
		}

		// Create the fax directories.
		createFaxDirectories(formId, installationDirectory, currentFormName);
	}
	
	private void setupFormAttributeValue(ATDService atdService, FormAttributeValue mergeValue,String installationDirectory, 
	                                  Form form, Integer locationId, Integer locationTagId, FormAttribute formAttr, 
	                                  String newValue) {
		FormAttributeValue exportValue = atdService.getFormAttributeValue(mergeValue.getFormId(), formAttr.getName(), 
			mergeValue.getLocationTagId(), mergeValue.getLocationId());
		if (exportValue != null && exportValue.getValue() != null) {
			// A value already exists...we need to replace it.
			exportValue.setValue(newValue);
		} else {
			// A value does not exist...we need to create it.
			exportValue = new FormAttributeValue();
			exportValue.setFormAttributeId(formAttr.getFormAttributeId());
			exportValue.setFormId(form.getFormId());
			exportValue.setLocationId(locationId);
			exportValue.setLocationTagId(locationTagId);
			exportValue.setValue(newValue);
		}
		
		// Save the value
		atdService.saveFormAttributeValue(exportValue);
	}
	
	private List<Form> findAllVersionsOfForm(Form matchForm) {
		// Match based on form form name
		String formName = matchForm.getName();
		List<Form> forms = new ArrayList<Form>();
		for (Form form : getForms(true)) {
			if (formName.equals(form.getName())) {
				forms.add(form);
			}
		}
		
		return forms;
	}
	
	private void createFaxDirectories(Integer formId, String installationDirectory, String formName) throws Exception {			
		// Create the scan directory
		File scanDir = new File(installationDirectory + File.separator + "scan" + 
			File.separator + "Fax" + File.separator + formName + "_SCAN");
		scanDir.mkdirs();
		
		// Create the images directory
		File imageDir = new File(installationDirectory + File.separator + "images" + 
			File.separator + "Fax" + File.separator + formName);
		imageDir.mkdirs();
	}
	
	private List<Form> getForms(boolean includeRetired) {
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(includeRetired);
		return forms;
		
	}
}
