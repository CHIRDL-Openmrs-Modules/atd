package org.openmrs.module.atd.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/faxableForm.form") 
public class FaxableFormController {
	
	private static final String SCAN_DIRECTORY_EXTENSION = "_SCAN";
	private static final String IMAGES_DIRECTORY_NAME = "images";
	private static final String FAX_DIRECTORY_NAME = "Fax";
	private static final String SCAN_DIRECTORY_NAME = "scan";
	private static final String APPLICATION_NAME = "Make Faxable Form";
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
    @RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) 
			throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(AtdConstants.PARAMETER_APPLICATION, APPLICATION_NAME);
		
		String formIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_TO_EDIT);
		if (formIdStr != null && formIdStr.trim().length() > 0) {
			try {
				Integer formId = Integer.parseInt(formIdStr);
				makeFaxableForm(formId);
			} catch (Exception e) {
				log.error("Error making form faxable", e);
				map.put(AtdConstants.PARAMETER_ERROR, true);
				map.put(AtdConstants.PARAMETER_FORMS, getForms(false));
				return new ModelAndView(AtdConstants.FORM_VIEW_FAXABLE, map);
			}
		}
		
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
	
	@RequestMapping(method = RequestMethod.GET) 
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		map.put(AtdConstants.PARAMETER_FORMS, getForms(false));
		return AtdConstants.FORM_VIEW_FAXABLE;
	}
	
	private void makeFaxableForm(Integer formId) throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		LocationService locService = Context.getLocationService();
		FormService formService = Context.getFormService();
		ChirdlUtilBackportsService chirdlutilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		
		String installationDirectory = adminService.getGlobalProperty(AtdConstants.GLOBAL_PROP_INSTALLATION_DIRECTORY);
		if (installationDirectory == null) {
			throw new Exception(AtdConstants.GLOBAL_PROP_INSTALLATION_DIRECTORY + " not specified.");
		}
		
		Form currentForm = formService.getForm(formId);
		String currentFormName = currentForm.getName();
		// We need to update retired versions of the form as well.
		List<Form> forms = findAllVersionsOfForm(currentForm);
		
		FormAttribute exportAttr = chirdlutilBackportsService.getFormAttributeByName(
			ChirdlUtilConstants.FORM_ATTR_DEFAULT_EXPORT_DIRECTORY);
		FormAttribute imageAttr = chirdlutilBackportsService.getFormAttributeByName(
			ChirdlUtilConstants.FORM_ATTRIBUTE_IMAGE_DIRECTORY);
		
		String newExportValue = installationDirectory + File.separator + SCAN_DIRECTORY_NAME + 
			File.separator + FAX_DIRECTORY_NAME +  File.separator + currentFormName + SCAN_DIRECTORY_EXTENSION;
		String newImageValue = installationDirectory + File.separator + IMAGES_DIRECTORY_NAME + File.separator + 
			FAX_DIRECTORY_NAME + File.separator + currentFormName;
		
		// Get the merge values.  This will guide us through creating the other info we need.
		for (Location location : locService.getAllLocations()) {
			Integer locationId = location.getLocationId();
			for (LocationTag tag : location.getTags()) {
				Integer locationTagId = tag.getLocationTagId();
				for (Form form : forms) {
					FormAttributeValue value = chirdlutilBackportsService.getFormAttributeValue(
						form.getFormId(), ChirdlUtilConstants.FORM_ATTR_DEFAULT_MERGE_DIRECTORY, locationTagId, locationId);
					if (value != null && value.getValue() != null) {
						// Create and save the new scan value.
						setupFormAttributeValue(chirdlutilBackportsService, form, locationId, locationTagId, 
							exportAttr, newExportValue);
						
						// Create and save the new image value.
						setupFormAttributeValue(chirdlutilBackportsService, form, locationId, locationTagId, 
							imageAttr, newImageValue);
					}
				}
			}
		}

		// Create the fax directories.
		createFaxDirectories(formId, installationDirectory, currentFormName);
	}
	
	private void setupFormAttributeValue(ChirdlUtilBackportsService chirdlutilBackportsService, 
	                                  Form form, Integer locationId, Integer locationTagId, FormAttribute formAttr, 
	                                  String newValue) {
		chirdlutilBackportsService.saveFormAttributeValue(form.getFormId(), formAttr.getName(), locationTagId, locationId, newValue);
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
		File scanDir = new File(installationDirectory + File.separator + SCAN_DIRECTORY_NAME + 
			File.separator + FAX_DIRECTORY_NAME + File.separator + formName + SCAN_DIRECTORY_EXTENSION);
		scanDir.mkdirs();
		
		// Create the images directory
		File imageDir = new File(installationDirectory + File.separator + IMAGES_DIRECTORY_NAME + 
			File.separator + FAX_DIRECTORY_NAME + File.separator + formName);
		imageDir.mkdirs();
	}
	
	private List<Form> getForms(boolean includeRetired) {
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(includeRetired);
		return forms;
		
	}
}
