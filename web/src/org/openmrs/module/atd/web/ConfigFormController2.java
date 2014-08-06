package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConfigFormController2 extends SimpleFormController
{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception
	{
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, 
	                                             BindException errors) throws Exception {
		Logger log = Logger.getLogger(ConfigFormController.class);
		Map<String, Object> map = new HashMap<String, Object>();
		FormService formService = Context.getFormService();
		String formIdStr = request.getParameter("formId");
		Integer formId = Integer.parseInt(formIdStr);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(formId);
			return new ModelAndView(new RedirectView("configurationManager.form"));
		}
		String formName = request.getParameter("formName");
		String printerCopy = request.getParameter("printerCopy");
		map.put("printerCopy", printerCopy);
		String view = getFormView();
		
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
				map.put("checked_" + name, "true");
			} else {
				map.put("checked_" + name, "false");
			}
		}
		
		map.put("locations", locNames);
		map.put("formId", formIdStr);
		
		boolean faxableForm = false;
		String faxChoice = request.getParameter("faxableForm");
		if (faxChoice != null) {
			if ("Yes".equalsIgnoreCase(faxChoice)) {
				faxableForm = true;
			}
		}
		
		map.put("faxableForm", faxableForm);
		
		boolean scannableForm = false;
		String scanChoice = request.getParameter("scannableForm");
		if (scanChoice != null) {
			if ("Yes".equalsIgnoreCase(scanChoice)) {
				scannableForm = true;
			}
		}
		
		map.put("scannableForm", scannableForm);
		
		boolean scorableForm = false;
		String scoreChoice = request.getParameter("scorableForm");
		if (scoreChoice != null) {
			if ("Yes".equalsIgnoreCase(scoreChoice)) {
				scorableForm = true;
			}
		}
		
		map.put("scorableForm", scorableForm);		
		map.put("formName", formName);
		if (!found) {
			map.put("noLocationsChecked", true);
			return new ModelAndView(view, map);
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
			return new ModelAndView(view, map);
		}
        
        // Copy form configuration
        ATDService atdService = Context.getService(ATDService.class);
    	try {
        	String serverName = adminService.getGlobalProperty("atd.serverName");
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
        			return new ModelAndView(view, map);
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
        		serverName, faxableForm, scannableForm, scorableForm, scoreConfigFile, numPrioritizedFields,
        		printerCopyFormId);
    	}
    	catch (Exception e) {
    		log.error("Error saving form changes", e);
            map.put("failedScoringFileUpload", true);
			// delete the directories
            ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
			return new ModelAndView(view, map);
        }
        
        try {
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
        		}
        		
        		LoggingUtil.logEvent(loc.getLocationId(), formId, null, LoggingConstants.EVENT_MODIFY_FORM_PROPERTIES, 
        			Context.getUserContext().getAuthenticatedUser().getUserId(), 
        			"Form configuration modified.  Class: " + ConfigFormController.class.getCanonicalName());
        	}
        } catch (Exception e) {
        	log.error("Error while creating data for the Chirdl location tag tables", e);
        	map.put("failedChirdlUpdate", true);
        	// delete the directories
        	ConfigManagerUtil.deleteFormDirectories(formName, locNames, faxableForm, scannableForm);
			// remove attribute values
			atdService.purgeFormAttributeValues(formId);
			return new ModelAndView(view, map);
        }
		
		view = getSuccessView();
		return new ModelAndView(
			new RedirectView(view), map);
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		//in inner list, the structure is {location, locationtag}
		String formId = request.getParameter("formId");
		Integer iFormId = Integer.parseInt(formId);
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		ArrayList<ArrayList<Object>> positions = new ArrayList<ArrayList<Object>>();
		LocationService locationService = Context.getLocationService();
		List<Location> locations = locationService.getAllLocations();
		for (Location currLoc : locations){
            Set<LocationTag> tags = currLoc.getTags();
            for (LocationTag tag : tags){
            	/*store positions to maps*/
            	ArrayList<Object> position = new ArrayList<Object>();
            	position.add(currLoc);
            	position.add(tag);
            	positions.add(position);
            	//will update to iterate the attributes table to export all attribute values later
            	FormAttributeValue forcePrintable =  cubService.getFormAttributeValue(iFormId, "forcePrintable", tag.getId(), currLoc.getId());
            	FormAttributeValue autoFax =  cubService.getFormAttributeValue(iFormId, "auto-fax", tag.getId(), currLoc.getId());
            	FormAttributeValue displayname =  cubService.getFormAttributeValue(iFormId, "displayName", tag.getId(), currLoc.getId());
            	FormAttributeValue mobileOnly =  cubService.getFormAttributeValue(iFormId, "mobileOnly", tag.getId(), currLoc.getId());
            	FormAttributeValue ageMin =  cubService.getFormAttributeValue(iFormId, "ageMin", tag.getId(), currLoc.getId());
            	FormAttributeValue ageMinUnits =  cubService.getFormAttributeValue(iFormId, "ageMinUnits", tag.getId(), currLoc.getId());
            	FormAttributeValue ageMax =  cubService.getFormAttributeValue(iFormId, "ageMax", tag.getId(), currLoc.getId());
            	FormAttributeValue ageMaxUnits =  cubService.getFormAttributeValue(iFormId, "ageMaxUnits", tag.getId(), currLoc.getId());
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"forcePrintable", forcePrintable);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"auto-fax", autoFax);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"displayname", displayname);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"mobileOnly", mobileOnly);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"miniAge", ageMin);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"miniAgeUnit", ageMinUnits);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"maxAge", ageMax);
            	map.put(currLoc.getId()+"#$#"+tag.getId()+"#$#"+"maxAgeUnit", ageMaxUnits);         
            }
        }
		map.put("positions", positions);
		map.put("formId", formId);
		map.put("formName", request.getParameter("formName"));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		return map;
	}
	
	private boolean checkParameter(HttpServletRequest request, String parameterName) {
		boolean positive = false;
		String parameter = request.getParameter(parameterName);
		if ("true".equalsIgnoreCase(parameter)) {
			positive = true;
		}
		
		return positive;
	}
}
