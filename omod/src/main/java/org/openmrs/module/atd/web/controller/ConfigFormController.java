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
import org.openmrs.Form;
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
@RequestMapping(value = "module/atd/configForm.form")
public class ConfigFormController
{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		FormService formService = Context.getFormService();
		String formIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID);
		Integer formId = Integer.parseInt(formIdStr);
		String cancel = request.getParameter(ChirdlUtilConstants.PARAMETER_CANCEL_PROCESS);
		if (ChirdlUtilConstants.GENERAL_INFO_TRUE.equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(formId, true); // CHICA-993 Updated to delete based on formId, also pass true to delete LocationTagAttribute record
			return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER));
		}
		String formName = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_NAME);
		String printerCopy = request.getParameter(ChirdlUtilConstants.PARAMTER_PRINTER_COPY);
		map.put(ChirdlUtilConstants.PARAMTER_PRINTER_COPY, printerCopy);
		
		// Check to see if the user checked any locations
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		boolean found = false;
		List<String> locNames = new ArrayList<String>();
		List<String> selectedLocations = new ArrayList<String>();
		for (Location location : locations) {
			String name = location.getName();
			locNames.add(name);
			Object foundLoc = request.getParameterValues("location_" + name);
			if (foundLoc != null) {
				found = true;
				selectedLocations.add(name);
				map.put("checked_" + name, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			} else {
				map.put("checked_" + name, ChirdlUtilConstants.GENERAL_INFO_FALSE);
			}
		}
		List<String> primaryForms = Util.getPrimaryForms();
		
		map.put("locations", locNames);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, formIdStr);
		map.put(AtdConstants.PARAMETER_SELECTED_FORM_NAME, request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_NAME));
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
			map.put("noLocationsChecked", true);
			return new ModelAndView(AtdConstants.FORM_CONFIG_FORM_VIEW, map);
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
			return new ModelAndView(AtdConstants.FORM_CONFIG_FORM_VIEW, map);
		}
        
        // Copy form configuration
        ATDService atdService = Context.getService(ATDService.class);
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
        			return new ModelAndView(AtdConstants.FORM_CONFIG_FORM_VIEW, map);
                }
            }
        	
        	Integer numPrioritizedFields = 0;
        	String pFieldString = request.getParameter("numPrioritizedFields");
        	if (pFieldString != null && pFieldString.trim().length() > 0) {
        		numPrioritizedFields = Integer.parseInt(pFieldString);
        	}
        	
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
            map.put("failedScoringFileUpload", true);
			// delete the directories
            ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
			return new ModelAndView(AtdConstants.FORM_CONFIG_FORM_VIEW, map);
        }
        
        try {
        	// DWE CHICA-332 4/15/15 Create a list of locations and tags so that the
        	// configFormAttributeValue.jsp can be incorporated into the create form process
        	ArrayList<String> locationsAndTags = new ArrayList<String>();
        	
        	ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
        	LocationTagAttribute locTagAttr = new LocationTagAttribute();
        	locTagAttr.setName(formName);
        	locTagAttr = chirdlutilbackportsService.saveLocationTagAttribute(locTagAttr);
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
        			locationsAndTags.add(loc.getLocationId() + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + tag.getLocationTagId()); // Separating the location id and the tag id by "_" to match existing functionality
        		}
        		
        		LoggingUtil.logEvent(loc.getLocationId(), formId, null, LoggingConstants.EVENT_MODIFY_FORM_PROPERTIES, 
        			Context.getUserContext().getAuthenticatedUser().getUserId(), 
        			"Form configuration modified.  Class: " + ConfigFormController.class.getCanonicalName());
        	}
        	
        	map.put("positions", locationsAndTags.toArray(new String[0]));
        	
        } catch (Exception e) {
        	log.error("Error while creating data for the Chirdl location tag tables", e);
        	map.put("failedChirdlUpdate", true);
        	// delete the directories
        	ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
			// remove attribute values
			atdService.purgeFormAttributeValues(formId);
			return new ModelAndView(AtdConstants.FORM_CONFIG_FORM_VIEW, map);
        }
		
		map.put("successViewName", AtdConstants.FORM_VIEW_CREATE_FORM_MLM_SUCCESS); // Success view will depend on which page the user came from
		return new ModelAndView(
			new RedirectView(AtdConstants.FORM_VIEW_CONFIG_SUCCESS), map);
	}

	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		List<String> locNames = new ArrayList<String>();
		
		for (Location location : locations) {
			boolean checkLocation = false;
			String name = location.getName();
			String checked = request.getParameter("checked_" + name);
			if (checked != null && ChirdlUtilConstants.GENERAL_INFO_TRUE.equalsIgnoreCase(checked)) {
				checkLocation = true;
			}
			
			locNames.add(name);
			map.put("checked_" + name, checkLocation);
		}
		
		List<String> primaryForms = Util.getPrimaryForms();

		map.put("locations", locNames);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_NAME, request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_NAME));
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		map.put("primaryForms", primaryForms);
		return AtdConstants.FORM_CONFIG_FORM_VIEW;
	}
}
