package org.openmrs.module.atd.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Program;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.ProgramTagMap;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CreateClinicTagFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String CREATE_TAG_FORM_VIEW = "/module/atd/createClinicTagForm";
	private static final String EDIT_TAG_FORM_VIEW = "/module/atd/editClinicTagAttributeForm";
	
	@RequestMapping(value = "module/atd/createClinicTagForm.form", method = RequestMethod.GET) 
	protected String initCreateTagForm(HttpServletRequest request, ModelMap map) throws Exception{
		initForm(map);
		return CREATE_TAG_FORM_VIEW;
	}
	
	@RequestMapping(value = "module/atd/editClinicTagAttributeForm.form", method = RequestMethod.GET) 
	protected String initEditTagForm(HttpServletRequest request, ModelMap map) throws Exception{
		initForm(map);
		return EDIT_TAG_FORM_VIEW;
	}
	
	@RequestMapping(value = {"module/atd/createClinicTagForm.form", "module/atd/editClinicTagAttributeForm.form"}, 
			method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String tagName = request.getParameter(ChirdlUtilConstants.PARAMETER_TAG_NAME);
		
		String form = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM);
		map.put(ChirdlUtilConstants.PARAMETER_FORM, form);
		LocationService locationService = Context.getLocationService();

		if (ChirdlUtilConstants.LOC_TAG_ATTR_FORM_EDIT.equals(form) ) {
			String submitAddLocTagAtt = request.getParameter(ChirdlUtilConstants.PARAMETER_HIDDEN_SUBMIT);
			String submitLocTagAttr = request.getParameter(ChirdlUtilConstants.PARAMETER_FINISH);
			
			if (submitAddLocTagAtt != null && submitAddLocTagAtt.trim().length() > 0) {
				try {
					String tagAttrName = request.getParameter(ChirdlUtilConstants.PARAMETER_NAME);
					String tagAttrDescription = request.getParameter(ChirdlUtilConstants.PARAMETER_DESCRIPTION);
					
					LocationTagAttribute locTagAttribute = 
							Context.getService(ChirdlUtilBackportsService.class).getLocationTagAttribute(tagAttrName);
					if (locTagAttribute != null) {
						map.put("duplicateName", true);
						reloadValues(request, map);
						return new ModelAndView(EDIT_TAG_FORM_VIEW, map);
					}
					saveLocationTagAttribute(tagAttrName, tagAttrDescription);
					reloadValues(request, map);
					return new ModelAndView(EDIT_TAG_FORM_VIEW, map);
				} catch (Exception e) {
					log.error("Error updating clinic location tag attribute values", e);
					map.put("UpdateFailed", "Failed updating clinic location tag attribute values: " + e.getMessage());
					return new ModelAndView(EDIT_TAG_FORM_VIEW, map);
				}
			} 
			if (submitLocTagAttr == null || submitLocTagAttr.trim().length() == 0) { 
					reloadValues(request, map);
					return new ModelAndView(EDIT_TAG_FORM_VIEW, map);
			}
		}
		
		String clinicIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION);
		Integer locationId = Integer.parseInt(clinicIdStr);
		Location location = locationService.getLocation(locationId);
		
		User user = null;
		String existingLocProp = null;
		String existingLocTagProp = null;
		
		UserService userService = Context.getUserService();
		
		if (ChirdlUtilConstants.LOC_TAG_FORM_CREATE.equals(form) ) {
			// Check to see if clinic tag name was specified.
			if (tagName == null || tagName.trim().length() == 0) {
				map.put("missingName", true);
				reloadValues(request, map);
				return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
			}
			
			tagName = tagName.trim();
			// Check to see if the clinic name is already specified.
			LocationTag locationTag = locationService.getLocationTagByName(tagName);
			if (locationTag != null) {
				map.put("duplicateName", true);
				reloadValues(request, map);
				return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
			}
			
			// Check to see if the clinic name has spaces.
			if (tagName.contains(" ")) {
				map.put("spacesInName", true);
				reloadValues(request, map);
				return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
			}
			
			String username = request.getParameter(ChirdlUtilConstants.PARAMETER_USERNAME);
			// Check to see if username was specified.
			if (username == null || username.trim().length() == 0) {
				map.put("missingUsername", true);
				reloadValues(request, map);
				return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
			}
			
			// Check to see if username exists.
			user = Context.getUserService().getUserByUsername(username);
			if (user == null) {
				map.put("unknownUsername", true);
				reloadValues(request, map);
				return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
			}
			
			String locationName = location.getName();
			existingLocProp = user.getUserProperty("location");
			existingLocTagProp = user.getUserProperty("locationTags");
			try {
				if (existingLocProp == null || existingLocProp.trim().length() == 0) {
					userService.setUserProperty(user, "location", locationName);
				} else {
					String[] locations = existingLocProp.split(",");
					boolean found = false;
					for (String name : locations) {
						if (locationName.equalsIgnoreCase(name)) {
							found = true;
						}
					}
					
					if (!found) {
						userService.setUserProperty(user, "location", existingLocProp + ", " + locationName);
					}
				}
				
				if (existingLocTagProp == null || existingLocTagProp.trim().length() == 0) {
					userService.setUserProperty(user, "locationTags", tagName);
				} else {
					userService.setUserProperty(user, "locationTags", existingLocTagProp + ", " + tagName);
				}
			} catch (Exception e) {
				log.error("Error creating new clinic location", e);
				map.put("failedCreation", "Failed creating a new clinic location: " + e.getMessage());
				reloadValues(request, map);
				return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
			}
			
		}
		try {
			createOrUpdateClinicTag(request, location, tagName);
		} catch (Exception e) {
			if (ChirdlUtilConstants.LOC_TAG_FORM_CREATE.equals(form) ) {
				log.error("Error creating new clinic location", e);
				map.put("failedCreation", "Failed creating a new clinic location: " + e.getMessage());
			} else if (ChirdlUtilConstants.LOC_TAG_ATTR_FORM_EDIT.equals(form) ) {
				log.error("Error updating clinic location tag attribute values", e);
				map.put("UpdateFailed", "Failed updating clinic location tag attribute values: " + e.getMessage());
			}
			reloadValues(request, map);
			if (ChirdlUtilConstants.LOC_TAG_FORM_CREATE.equals(form)) {
				if (existingLocProp == null) {
					userService.removeUserProperty(user, "location");
				} else {
					userService.setUserProperty(user, "location", existingLocProp);
				}
				
				if (existingLocTagProp == null) {
					userService.removeUserProperty(user, "locationTags");
				} else {
					userService.setUserProperty(user, "locationTags", existingLocTagProp);
				}
			} 
			return new ModelAndView(CREATE_TAG_FORM_VIEW, map);
		}
		if (ChirdlUtilConstants.LOC_TAG_FORM_CREATE.equals(form) ) { 
			map.put("application", "Create Clinic Tag");
		} else {
			map.put("application", "Edit Clinic Tag Attribute Values");
		}
		
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
	
	private void reloadValues(HttpServletRequest request, Map<String, Object> map) {
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		String form = (String)map.get(ChirdlUtilConstants.PARAMETER_FORM);
		map.put("tagName", request.getParameter(ChirdlUtilConstants.PARAMETER_TAG_NAME));
		map.put("description", request.getParameter(ChirdlUtilConstants.PARAMETER_DESCRIPTION));
		map.put("username", request.getParameter(ChirdlUtilConstants.PARAMETER_USERNAME));
		map.put("selectedProgram", request.getParameter(ChirdlUtilConstants.PARAMETER_PROGRAM));
		map.put("selectedTag", request.getParameter(ChirdlUtilConstants.PARAMETER_ESTABLISHED_TAG));
		map.put("selectedLocation", request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION));
		map.put("programs", backportsService.getAllPrograms());
		map.put("currentTags", Context.getLocationService().getAllLocationTags(false));
		map.put("locations", Context.getLocationService().getAllLocations(false));
		
		Integer locationId = null;
		if (ChirdlUtilConstants.LOC_TAG_ATTR_FORM_EDIT.equals(form)) {
			String locationIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION);
			if (locationIdStr != null && locationIdStr.trim().length() > 0) {
				locationId = Integer.parseInt(locationIdStr);
				LocationService locationService = Context.getLocationService();
				Location location = locationService.getLocation(locationId);
				Set<LocationTag> locationTagTreeSet = new TreeSet<LocationTag>(new LocationTagIdComp());
				locationTagTreeSet.addAll(location.getTags());
				map.put("locationTags", locationTagTreeSet);
			}
		}
		String tagName = request.getParameter(ChirdlUtilConstants.PARAMETER_TAG_NAME);
		List<LocationTagAttribute> locationTagAttributes = getNonFormLocationTagAttributes();
		map.put("locationTagAttributes", locationTagAttributes);
		List<LocationTagAttributeValue> locationTagAttributeValues = new ArrayList<LocationTagAttributeValue>();
		for (LocationTagAttribute locationTagAttribute : locationTagAttributes) {
			if (ChirdlUtilConstants.LOC_TAG_FORM_CREATE.equals(form)) {
				String value = request.getParameter(locationTagAttribute.getName());
				if (value != null && value.trim().length() > 0) {
					map.put(locationTagAttribute.getName(), value);
				}
			}
			else if (ChirdlUtilConstants.LOC_TAG_ATTR_FORM_EDIT.equals(form) && tagName != null && tagName.trim().length() > 0 ) {
				LocationService locationService = Context.getLocationService();
				LocationTag locationTag = locationService.getLocationTagByName(tagName);
				LocationTagAttributeValue locationTagAttributeValue = backportsService.getLocationTagAttributeValue(locationTag.getLocationTagId(), locationTagAttribute.getName(), locationId);
				if (locationTagAttributeValue == null) {
					locationTagAttributeValue = new LocationTagAttributeValue();
					locationTagAttributeValue.setLocationId(locationId);
					locationTagAttributeValue.setLocationTagAttributeId(locationTagAttribute.getLocationTagAttributeId());
					locationTagAttributeValue.setLocationTagId(locationTag.getLocationTagId());
					locationTagAttributeValue.setValue(ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING);
				}
				
				locationTagAttributeValues.add(locationTagAttributeValue);
				map.put("locationTagAttributeValues", locationTagAttributeValues);
			}
		}
		HashMap<Integer,String> locTagAttValMap = new HashMap<Integer, String>();
		for (LocationTagAttributeValue locationTagAttributeValue : locationTagAttributeValues) {
			locTagAttValMap.put(locationTagAttributeValue.getLocationTagAttributeId(), locationTagAttributeValue.getValue());
			map.put("locTagAttValMap", locTagAttValMap);
		}
	}
	
	private boolean createOrUpdateClinicTag(HttpServletRequest request, Location location, String tagName) 
	throws Exception {
		String form = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM);
		LocationTag locationTag = null; 
		if (ChirdlUtilConstants.LOC_TAG_FORM_CREATE.equals(form) ) { 
			locationTag = createLocationTag(request, location, tagName);
			addNonFormLocationTagAttributes(request, location, locationTag);
			addFormAttributeValues(request, location, locationTag);
		} else {
			LocationService locationService = Context.getLocationService();
			locationTag = locationService.getLocationTagByName(tagName);
			addNonFormLocationTagAttributes(request, location, locationTag);
		}
		return true;
	}
	
	private LocationTag createLocationTag(HttpServletRequest request, Location location, String tagName) throws Exception {
		User user = Context.getAuthenticatedUser();
		LocationService locationService = Context.getLocationService();
		LocationTag locationTag = new LocationTag();
		locationTag.setName(tagName);
		locationTag.setCreator(user);
		locationTag.setDateCreated(new Date());
		locationTag = locationService.saveLocationTag(locationTag);
		location.addTag(locationTag);
		locationService.saveLocation(location);
		
		String programIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_PROGRAM);
		if (programIdStr != null && programIdStr.trim().length() > 0) {
			Integer programId = Integer.parseInt(programIdStr.trim());
			ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
			Program program = backportsService.getProgram(programId);
			ProgramTagMap tagMap = new ProgramTagMap();
			tagMap.setLocationId(location.getLocationId());
			tagMap.setLocationTagId(locationTag.getLocationTagId());
			tagMap.setProgram(program);
			tagMap.setProgramId(programId);
			backportsService.saveProgramTagMap(tagMap);
		}
		
		return locationTag;
	}
	
	private void addNonFormLocationTagAttributes(HttpServletRequest request, Location location, LocationTag locationTag) 
	throws IOException {
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer locationId = location.getLocationId();
		Integer locationTagId = locationTag.getLocationTagId();
		String form = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM);
		List<LocationTagAttribute> locationTagAttributes = getNonFormLocationTagAttributes();
		for (LocationTagAttribute locationTagAttribute : locationTagAttributes) {
			String locTagAttrName = locationTagAttribute.getName();
			String value = request.getParameter(locTagAttrName);
			if (value != null && value.trim().length() > 0) {
				addLocationTagAttribute(request, backportsService, locationId, locationTagId, locationTagAttribute, 
					value.trim());
			} else if (ChirdlUtilConstants.LOC_TAG_ATTR_FORM_EDIT.equals(form) ) {
				LocationTagAttributeValue existingLtav = backportsService.getLocationTagAttributeValue(locationTagId, 
					locationTagAttribute.getName(), locationId);
				if (existingLtav!=null && (value == null || value.trim().length() == 0 )) {
					backportsService.deleteLocationTagAttributeValue(existingLtav);
				}
			}
		}
	}
	
	private void addLocationTagAttribute(HttpServletRequest request, ChirdlUtilBackportsService backportsService, 
	                                  Integer locationId, Integer locationTagId, LocationTagAttribute locationTagAttr, 
	                                  String value) {
		LocationTagAttributeValue existingLtav = backportsService.getLocationTagAttributeValue(locationTagId, 
			locationTagAttr.getName(), locationId);
		LocationTagAttributeValue ltav;
		if (existingLtav != null) {
			ltav = existingLtav;			
		} else {
			ltav = new LocationTagAttributeValue();
		}
		ltav.setLocationTagAttributeId(locationTagAttr.getLocationTagAttributeId());
		ltav.setLocationId(locationId);
		ltav.setLocationTagId(locationTagId);
		ltav.setValue(value);
		backportsService.saveLocationTagAttributeValue(ltav);
}
	
	private void addFormAttributeValues(HttpServletRequest request, Location location, LocationTag locationTag) {
		String copyTagIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_ESTABLISHED_TAG);
		if (copyTagIdStr == null || copyTagIdStr.trim().length() == 0) {
			return;
		}
		
		LocationService locationService = Context.getLocationService();
		LocationTag copyTag = locationService.getLocationTag(Integer.parseInt(copyTagIdStr.trim()));
		List<Location> locations = Context.getLocationService().getLocationsByTag(copyTag);
		if (locations == null || locations.size() == 0) {
			return;
		}
		
		Location copyLocation = locations.get(0);
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer locationId = location.getLocationId();
		Integer copyLocationId = copyLocation.getLocationId();
		List<Form> forms = Context.getFormService().getAllForms(false);
		String copyLocationName = copyLocation.getName();
		String locationName = location.getName();
		for (FormAttribute formAttr : backportsService.getAllFormAttributes()) {
			addFormAttributeValue(backportsService, locationId, locationTag.getLocationTagId(), copyLocationId, 
				copyTag.getLocationTagId(), formAttr, forms, copyLocationName, locationName);
		}
		
		addFormLocationTagAttributes(backportsService, locationId, locationTag.getLocationTagId(), forms, copyLocationId, 
			copyTag.getLocationTagId());
	}
	
	private void addFormAttributeValue(ChirdlUtilBackportsService backportsService, Integer locationId, 
	                                   Integer locationTagId, Integer copyLocationId, Integer copyLocationTagId, 
	                                   FormAttribute formAttribute, List<Form> forms, String replace, String replaceWith) {
		for (Form form : forms) {
			String formAttrName = formAttribute.getName();
			FormAttributeValue fav = backportsService.getFormAttributeValue(form.getFormId(), formAttrName, 
				copyLocationTagId, copyLocationId);
			if (fav != null) {
				FormAttributeValue newFav = new FormAttributeValue();
				newFav.setFormAttributeId(fav.getFormAttributeId());
				newFav.setFormId(fav.getFormId());
				newFav.setLocationId(locationId);
				newFav.setLocationTagId(locationTagId);
				String value = fav.getValue();
				if (replace != null) {
					newFav.setValue(value.replaceAll(replace, replaceWith));
				} else if (replace == null && replaceWith != null) {
					newFav.setValue(replaceWith);
				} else {
					newFav.setValue(value);
				}
				
				backportsService.saveFormAttributeValue(newFav);
				// This is hard coded to create directories automatically.
				boolean isDirectory = formAttribute.getName().toLowerCase().contains("directory");
				if (isDirectory) {
					String dir = newFav.getValue();
					// Have to hard code this because of additional directory needed.
					if ("defaultMergeDirectory".equalsIgnoreCase(formAttrName)) {
						dir += File.separator + "Pending";
					}
					
					File file = new File(dir);
					file.mkdirs();
				}
			}
		}
		
	}
	
	private void addFormLocationTagAttributes(ChirdlUtilBackportsService backportsService, Integer locationId, 
	  	                                   Integer locationTagId, List<Form> forms, Integer copyLocationId, 
	  	                                   Integer copyLocationTagId) {
		for (Form form : forms) {
			LocationTagAttribute lta = backportsService.getLocationTagAttribute(form.getName());
			if (lta != null) {
				LocationTagAttributeValue existingLtav = backportsService.getLocationTagAttributeValue(copyLocationTagId, 
					form.getName(), copyLocationId);
				if (existingLtav != null) {
					LocationTagAttributeValue ltav = new LocationTagAttributeValue();
					ltav.setLocationId(locationId);
					ltav.setLocationTagAttributeId(lta.getLocationTagAttributeId());
					ltav.setLocationTagId(locationTagId);
					ltav.setValue(form.getFormId().toString());
					backportsService.saveLocationTagAttributeValue(ltav);
				}
			}
		}
	}
	
	private List<LocationTagAttribute> getNonFormLocationTagAttributes() {
		List<LocationTagAttribute> locTagAttrs = 
			Context.getService(ChirdlUtilBackportsService.class).getAllLocationTagAttributes();
		Set<String> uniqueFormNames = new HashSet<String>();
		for (Form form : Context.getFormService().getAllForms(true)) {
			uniqueFormNames.add(form.getName());
		}
		
		for (int i=locTagAttrs.size()-1; i >= 0; i--) {
			LocationTagAttribute locTagAttr = locTagAttrs.get(i);
			if (uniqueFormNames.contains(locTagAttr.getName())) {
				locTagAttrs.remove(i);
			}
		}
		
		return locTagAttrs;
	}
	
	/*
	 * Saves Location Tag Attribute to the database
	 * @param name
	 * @param description
	 */
	private boolean saveLocationTagAttribute(String name, String description) {
		
		if (name == null || name.trim().length() == 0) {
			return false;
		}
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		LocationTagAttribute locTagAttr = new LocationTagAttribute();
    	locTagAttr.setName(name);
    	locTagAttr.setDescription(description);
    	backportsService.saveLocationTagAttribute(locTagAttr);
		return true;
	}
	
	/**
	 * Initialized the form the HTTP GET method.
	 * 
	 * @param map ModelMap where data will be stored for use by the client.
	 * @throws Exception
	 */
	private void initForm(ModelMap map) throws Exception {
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		map.put("programs", backportsService.getAllPrograms());
		map.put("currentTags", Context.getLocationService().getAllLocationTags(false));
		map.put("locations", Context.getLocationService().getAllLocations(false));
		map.put("locationTagAttributes", getNonFormLocationTagAttributes());
	}
	
	/*
	 * Sorts the Set LotationTags by location tag id
	 * 
	 */
	private class LocationTagIdComp implements java.util.Comparator<LocationTag>{
		public int compare(LocationTag tag1, LocationTag tag2) {
			return tag1.getId().compareTo(tag2.getId());
		}
	}
	
}

