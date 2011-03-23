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
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutil.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class EnableLocationsFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		LocationService locService = Context.getLocationService();
		ATDService atdService = Context.getService(ATDService.class);
		String formIdString = request.getParameter("formToEdit");
		Integer formId = Integer.parseInt(formIdString);
		List<Location> locations = locService.getAllLocations(false);
		List<String> locNames = new ArrayList<String>();
		List<String> checkedLocations = new ArrayList<String>();
		for (Location location : locations) {
			String name = location.getName();
			Boolean checked = atdService.isFormEnabledAtClinic(formId, location.getLocationId());			
			locNames.add(name);
			if (checked == Boolean.TRUE) {
				checkedLocations.add("true");
			} else {
				checkedLocations.add("false");
			}
		}
		
		map.put("locations", locNames);
		map.put("checkedLocations", checkedLocations);

		FormService formService = Context.getFormService();
		try {
			Form formToEdit = formService.getForm(formId);
			map.put("formName", formToEdit.getName());
			map.put("formId", formId);
		} 
		catch (Exception e) {
			log.error("Error retrieving form", e);
			map.put("failedLoad", true);
		}
		
		return map;
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, 
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String formName = request.getParameter("formName");
		String printerCopy = request.getParameter("printerCopy");
		map.put("printerCopy", printerCopy);
		String view = getFormView();
		
		String formIdStr = request.getParameter("formId");
		map.put("formId", formIdStr);
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
				checkedLocations.add("true");
			} else {
				checkedLocations.add("false");
			}
			if (foundLoc != null) {
				found = true;
				selectedLocations.add(name);
			}
		}
		
		map.put("locations", locNames);
		map.put("checkedLocations", checkedLocations);
		
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
		String driveLetter = adminService.getGlobalProperty("atd.driveLetter");
		try {	
			// Create the directories.
			ConfigManagerUtil.createFormDirectories(formName, selectedLocations, scannableForm, driveLetter);
		} catch (Exception e) {			
			log.error("Error creating directories", e);
			map.put("failedCreateDirectories", true);
			return new ModelAndView(view, map);
		}
        
        // Copy form configuration
        String formIdToCopy = request.getParameter("formToCopy");
    	try {
            if (formIdToCopy != null && !"No Selection".equalsIgnoreCase(formIdToCopy)) {
            	atdService.copyFormMetadata(Integer.parseInt(formIdToCopy), formId);
            } else {
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
	                	ConfigManagerUtil.deleteFormDirectories(formName, locNames, scannableForm);
	        			return new ModelAndView(view, map);
	                }
	            }
	        	
	        	FormService formService = Context.getFormService();
	        	Integer numPrioritizedFields = getPrioritizedFieldCount(formService, formId);
	        	Form printerCopyForm = formService.getForm(printerCopy);
	        	atdService.setupInitialFormValues(formId, formName, selectedLocations, driveLetter, 
	        		serverName, scannableForm, scorableForm, scoreConfigFile, numPrioritizedFields,
	        		printerCopyForm.getFormId());
            }
    	}
    	catch (Exception e) {
    		log.error("Error saving form changes", e);
            map.put("failedSaveChanges", true);
			// delete the directories
            ConfigManagerUtil.deleteFormDirectories(formName, locNames, scannableForm);
			return new ModelAndView(view, map);
        }
        
    	List<LocationTagAttributeValue> addedTags = new ArrayList<LocationTagAttributeValue>();
    	ChirdlUtilService chirdlService = Context.getService(ChirdlUtilService.class);
        try {
        	LocationTagAttribute locTagAttr = chirdlService.getLocationTagAttribute(formName);
        	if (locTagAttr == null) {
        		locTagAttr = new LocationTagAttribute();
	        	locTagAttr.setName(formName);
	        	locTagAttr = chirdlService.saveLocationTagAttribute(locTagAttr);
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
        			chirdlService.saveLocationTagAttributeValue(attrVal);
        			addedTags.add(attrVal);
        		}
        	}
        } catch (Exception e) {
        	log.error("Error while creating data for the Chirdl location tag tables", e);
        	map.put("failedChirdlUpdate", true);
        	// delete the directories
        	ConfigManagerUtil.deleteFormDirectories(formName, locNames, scannableForm);
			// remove attribute values
			atdService.purgeFormAttributeValues(formId);
			// remove chirdl util attr vals
			for (LocationTagAttributeValue val : addedTags) {
				chirdlService.deleteLocationTagAttributeValue(val);
			}
			
			return new ModelAndView(view, map);
        }
		
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
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
