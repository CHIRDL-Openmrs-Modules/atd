package org.openmrs.module.atd.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/enableLocationsForm.form")
public class EnableLocationsFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {

		LocationService locService = Context.getLocationService();
		ATDService atdService = Context.getService(ATDService.class);
		String formIdString = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_TO_EDIT);
		Integer formId = Integer.parseInt(formIdString);
		List<Location> locations = locService.getAllLocations(false);
		List<String> locNames = new ArrayList<String>();
		List<String> checkedLocations = new ArrayList<String>();
		for (Location location : locations) {
			String name = location.getName();
			Boolean checked = atdService.isFormEnabledAtClinic(formId, location.getLocationId());			
			locNames.add(name);
			if (checked == Boolean.TRUE) {
				checkedLocations.add(ChirdlUtilConstants.GENERAL_INFO_TRUE);
			} else {
				checkedLocations.add(ChirdlUtilConstants.GENERAL_INFO_FALSE);
			}
		}
		
		map.put("locations", locNames);
		map.put("checkedLocations", checkedLocations);

		FormService formService = Context.getFormService();
		try {
			Form formToEdit = formService.getForm(formId);
			map.put(ChirdlUtilConstants.PARAMETER_FORM_NAME, formToEdit.getName());
			map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, formId);
		} 
		catch (Exception e) {
			log.error("Error retrieving form", e);
			map.put("failedLoad", true);
		}
		List<String> primaryForms = Util.getPrimaryForms();
		map.put("primaryForms", primaryForms);
		
		return AtdConstants.FORM_ENABLE_LOCATIONS_FORM_VIEW;
	}

	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String formName = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_NAME);
		String printerCopy = request.getParameter(ChirdlUtilConstants.PARAMTER_PRINTER_COPY);
		map.put(ChirdlUtilConstants.PARAMTER_PRINTER_COPY, printerCopy);
		
		String formIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, formIdStr);
        Integer formId = Integer.parseInt(formIdStr);
		
		// Check to see if the user checked any locations
		LocationService locService = Context.getLocationService();
		ATDService atdService = Context.getService(ATDService.class);
		List<Location> locations = locService.getAllLocations(false);
		boolean found = false;
		List<String> locNames = new ArrayList<String>();
		List<String> selectedLocations = new ArrayList<String>();
		List<String> checkedLocations = new ArrayList<String>();
		for (Location location : locations) {
			String name = location.getName();
			locNames.add(name);
			Object foundLoc = request.getParameterValues("location_" + name);
			Boolean checked = atdService.isFormEnabledAtClinic(formId, location.getLocationId());			
			if (checked == Boolean.TRUE) {
				checkedLocations.add(ChirdlUtilConstants.GENERAL_INFO_TRUE);
			} else {
				checkedLocations.add(ChirdlUtilConstants.GENERAL_INFO_FALSE);
			}
			if (foundLoc != null) {
				found = true;
				selectedLocations.add(name);
			}
		}
		
		map.put("locations", locNames);
		map.put("checkedLocations", checkedLocations);

		List<String> primaryForms = Util.getPrimaryForms();
		map.put("primaryForms", primaryForms);
		
		boolean faxableForm = false;
		String faxChoice = request.getParameter(ChirdlUtilConstants.PARAMTER_FAXABLE_FORM);
		if (faxChoice != null) {
			if (ChirdlUtilConstants.GENERAL_INFO_YES.equalsIgnoreCase(faxChoice)) {
				faxableForm = true;
			}
		}
		
		map.put(ChirdlUtilConstants.PARAMTER_FAXABLE_FORM, faxableForm);
		
		boolean scannableForm = false;
		String scanChoice = request.getParameter(ChirdlUtilConstants.PARAMTER_SCANNABLE_FORM);
		if (scanChoice != null) {
			if (ChirdlUtilConstants.GENERAL_INFO_YES.equalsIgnoreCase(scanChoice)) {
				scannableForm = true;
			}
		}
		
		map.put(ChirdlUtilConstants.PARAMTER_SCANNABLE_FORM, scannableForm);
		
		boolean scorableForm = false;
		String scoreChoice = request.getParameter(ChirdlUtilConstants.PARAMTER_SCORABLE_FORM);
		if (scoreChoice != null) {
			if (ChirdlUtilConstants.GENERAL_INFO_YES.equalsIgnoreCase(scoreChoice)) {
				scorableForm = true;
			}
		}
		
		map.put(ChirdlUtilConstants.PARAMTER_SCORABLE_FORM, scorableForm);
		
		map.put(ChirdlUtilConstants.PARAMETER_FORM_NAME, formName);
		if (!found) {
			map.put(AtdConstants.PARAMETER_NO_LOCATIONS_CHECKED, true);
			return new ModelAndView(AtdConstants.FORM_ENABLE_LOCATIONS_FORM_VIEW, map);
		}
		
		AdministrationService adminService = Context.getAdministrationService();
		String installationDirectory = adminService.getGlobalProperty("atd.installationDirectory");
		try {	
			// Create the directories.
			ConfigManagerUtil.createFormDirectories(formName, selectedLocations, faxableForm, scannableForm, 
				installationDirectory);
		} catch (Exception e) {			
			log.error("Error creating directories", e);
			map.put("failedCreateDirectories", true);
			return new ModelAndView(AtdConstants.FORM_ENABLE_LOCATIONS_FORM_VIEW, map);
		}
		
    	try {
        	String scoreConfigFile = null;
        	if (request instanceof MultipartHttpServletRequest && scorableForm) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
                MultipartFile xmlFile = multipartRequest.getFile("scoringFile");
                if (xmlFile != null && !xmlFile.isEmpty()) {
                	scoreConfigFile = ConfigManagerUtil.loadFormScoringConfigFile(xmlFile, formName);
                } else {
                	map.put("missingScoringFile", true);
                	// delete the directories
                	ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
        			return new ModelAndView(AtdConstants.FORM_ENABLE_LOCATIONS_FORM_VIEW, map);
                }
            }
        	
        	FormService formService = Context.getFormService();
        	Integer numPrioritizedFields = getPrioritizedFieldCount(formService, formId);
        	Form printerCopyForm = formService.getForm(printerCopy);
        	Integer printerCopyFormId = null;
        	if (printerCopyForm != null) {
        		printerCopyFormId = printerCopyForm.getFormId();
        	}
        	atdService.setupInitialFormValues(formId, formName, selectedLocations, installationDirectory, 
        		faxableForm, scannableForm, scorableForm, scoreConfigFile, numPrioritizedFields, printerCopyFormId);
    	}
    	catch (Exception e) {
    		log.error("Error saving form changes", e);
            map.put("failedSaveChanges", true);
			// delete the directories
            ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
			return new ModelAndView(AtdConstants.FORM_ENABLE_LOCATIONS_FORM_VIEW, map);
        }
        
    	List<LocationTagAttributeValue> addedTags = new ArrayList<LocationTagAttributeValue>();
    	ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
        try {
        	LocationTagAttribute locTagAttr = chirdlutilbackportsService.getLocationTagAttribute(formName);
        	if (locTagAttr == null) {
        		locTagAttr = new LocationTagAttribute();
	        	locTagAttr.setName(formName);
	        	locTagAttr = chirdlutilbackportsService.saveLocationTagAttribute(locTagAttr);
        	}
        	
        	for (String locName : selectedLocations) {
        		Location loc = locService.getLocation(locName);
        		Set<LocationTag> tags = loc.getTags();
        		Iterator<LocationTag> iter = tags.iterator();
        		while (iter.hasNext()) {
        			LocationTag tag = iter.next();
        			LocationTagAttributeValue attrVal = new LocationTagAttributeValue();
        			attrVal.setLocationId(loc.getLocationId());
        			attrVal.setLocationTagAttributeId(locTagAttr.getLocationTagAttributeId());
        			attrVal.setLocationTagId(tag.getLocationTagId());
        			attrVal.setValue(String.valueOf(formId));
        			chirdlutilbackportsService.saveLocationTagAttributeValue(attrVal);
        			addedTags.add(attrVal);
        		}
        		
        		LoggingUtil.logEvent(loc.getLocationId(), formId, null, LoggingConstants.EVENT_MODIFY_FORM_PROPERTIES, 
        			Context.getUserContext().getAuthenticatedUser().getUserId(), 
        			"Form configuration modified.  Class: " + EnableLocationsFormController.class.getCanonicalName());
        	}
        } catch (Exception e) {
        	log.error("Error while creating data for the Chirdl location tag tables", e);
        	map.put("failedChirdlUpdate", true);
        	// delete the directories
        	ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
			// remove attribute values
			atdService.purgeFormAttributeValues(formId);
			// remove chirdl util attr vals
			for (LocationTagAttributeValue val : addedTags) {
				chirdlutilbackportsService.deleteLocationTagAttributeValue(val);
			}
			
			return new ModelAndView(AtdConstants.FORM_ENABLE_LOCATIONS_FORM_VIEW, map);
        }
		
		map.put("application", "Enable Form at Clinics");
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
	
	private Integer getPrioritizedFieldCount(FormService formService, Integer formId) {
		Form form = formService.getForm(formId);
		List<FormField> formFields = form.getOrderedFormFields();
		Integer numPrioritizedFields = 0;
		for (FormField currFormField : formFields) {
			Field field = currFormField.getField();
			if (field != null) {
				FieldType fieldType = field.getFieldType();
				if (fieldType != null && fieldType.getFieldTypeId() == 8) {
					numPrioritizedFields++;
				}
			}
		}
    	
    	return numPrioritizedFields;
	}
}
