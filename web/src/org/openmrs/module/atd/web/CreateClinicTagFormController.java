package org.openmrs.module.atd.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Program;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.ProgramTagMap;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class CreateClinicTagFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
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
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		map.put("programs", backportsService.getAllPrograms());
		map.put("currentTags", Context.getLocationService().getAllLocationTags(false));
		map.put("locations", Context.getLocationService().getAllLocations(false));
		map.put("locationTagAttributes", getNonFormLocationTagAttributes());
		
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String tagName = request.getParameter(ChirdlUtilConstants.PARAMETER_TAG_NAME);
		String view = getFormView();
		
		String form = request.getParameter("form");
		map.put("form", form);
		LocationService locationService = Context.getLocationService();

		if (form.equals(ChirdlUtilConstants.EDIT_LOC_TAG_ATTR_FORM) ) {
			String submitAddLocTagAtt = request.getParameter("hiddenSubmit");
			String submitLocTagAttr = request.getParameter("Finish");
			
			if (submitAddLocTagAtt != null && submitAddLocTagAtt.trim().length() > 0) {
				try {
					String tagAttrName = request.getParameter("name");
					String tagAttrDescription = request.getParameter(ChirdlUtilConstants.PARAMETER_DESCRIPTION);
					
					LocationTagAttribute locTagAttribute = 
							Context.getService(ChirdlUtilBackportsService.class).getLocationTagAttribute(tagAttrName);
					if (locTagAttribute != null) {
						map.put("duplicateName", true);
						reloadValues(request, map);
						return new ModelAndView(view, map);
					}
					saveLocationTagAttribute(tagAttrName, tagAttrDescription);
					reloadValues(request, map);
					return new ModelAndView(view, map);
				} catch (Exception e) {
					log.error("Error updating clinic location tag attribute values", e);
					map.put("failedUpdation", "Failed updating clinic location tag attribute values: " + e.getMessage());
					return new ModelAndView(view, map);
				}
			} 
			if (submitLocTagAttr == null || submitLocTagAttr.trim().length() == 0) { 
					reloadValues(request, map);
					return new ModelAndView(view, map);
			}
		}
		
		String clinicIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION);
		Integer locationId = Integer.parseInt(clinicIdStr);
		Location location = locationService.getLocation(locationId);
		
		User user = null;
		String password = null;
		String existingLocProp = null;
		String existingLocTagProp = null;
		
		if (form.equals(ChirdlUtilConstants.CREATE_LOC_TAG_FORM) ) {
			// Check to see if clinic tag name was specified.
			if (tagName == null || tagName.trim().length() == 0) {
				map.put("missingName", true);
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
			tagName = tagName.trim();
			// Check to see if the clinic name is already specified.
			LocationTag locationTag = locationService.getLocationTagByName(tagName);
			if (locationTag != null) {
				map.put("duplicateName", true);
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
			// Check to see if the clinic name has spaces.
			if (tagName.contains(" ")) {
				map.put("spacesInName", true);
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
			String username = request.getParameter("username");
			// Check to see if username was specified.
			if (username == null || username.trim().length() == 0) {
				map.put("missingUsername", true);
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
			// Check to see if username exists.
			user = Context.getUserService().getUserByUsername(username);
			if (user == null) {
				map.put("unknownUsername", true);
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
			// Check to make sure a password was specified.
			password = request.getParameter("password");
			if (password == null || password.length() == 0) {
				map.put("invalidPassword", true);
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
			String locationName = location.getName();
			existingLocProp = user.getUserProperty("location");
			existingLocTagProp = user.getUserProperty("locationTags");
			try {
				if (existingLocProp == null || existingLocProp.trim().length() == 0) {
					user.setUserProperty("location", locationName);
				} else {
					String[] locations = existingLocProp.split(",");
					boolean found = false;
					for (String name : locations) {
						if (locationName.equalsIgnoreCase(name)) {
							found = true;
						}
					}
					
					if (!found) {
						user.setUserProperty("location", existingLocProp + ", " + locationName);
					}
				}
				
				if (existingLocTagProp == null || existingLocTagProp.trim().length() == 0) {
					user.setUserProperty("locationTags", tagName);
				} else {
					user.setUserProperty("locationTags", existingLocTagProp + ", " + tagName);
				}
				
				Context.getUserService().saveUser(user, password);
			} catch (Exception e) {
				log.error("Error creating new clinic location", e);
				map.put("failedCreation", "Failed creating a new clinic location: " + e.getMessage());
				reloadValues(request, map);
				return new ModelAndView(view, map);
			}
			
		}
		try {
			createOrUpdateClinicTag(request, location, tagName);
		} catch (Exception e) {
			if (form.equals(ChirdlUtilConstants.CREATE_LOC_TAG_FORM) ) {
				log.error("Error creating new clinic location", e);
				map.put("failedCreation", "Failed creating a new clinic location: " + e.getMessage());
			} else if (form.equals(ChirdlUtilConstants.EDIT_LOC_TAG_ATTR_FORM) ) {
				log.error("Error updating clinic location tag attribute values", e);
				map.put("failedUpdation", "Failed updating clinic location tag attribute values: " + e.getMessage());
			}
			reloadValues(request, map);
			if (form.equals(ChirdlUtilConstants.CREATE_LOC_TAG_FORM)) {
				if (existingLocProp == null) {
					user.removeUserProperty("location");
				} else {
					user.setUserProperty("location", existingLocProp);
				}
				
				if (existingLocTagProp == null) {
					user.removeUserProperty("locationTags");
				} else {
					user.setUserProperty("locationTags", existingLocTagProp);
				}
				
				Context.getUserService().saveUser(user, password);
			} 
			return new ModelAndView(view, map);
		}
		if (form.equals(ChirdlUtilConstants.CREATE_LOC_TAG_FORM) ) { 
			map.put("application", "Create Clinic Tag");
		} else {
			map.put("application", "Edit Clinic Tag Attribute Values");
		}
		
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
	}
	
	private void reloadValues(HttpServletRequest request, Map<String, Object> map) {
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		String form = (String)map.get("form");
		map.put("tagName", request.getParameter(ChirdlUtilConstants.PARAMETER_TAG_NAME));
		map.put("description", request.getParameter(ChirdlUtilConstants.PARAMETER_DESCRIPTION));
		map.put("username", request.getParameter("username"));
		map.put("selectedProgram", request.getParameter("program"));
		map.put("selectedTag", request.getParameter("establishedTag"));
		map.put("selectedLocation", request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION));
		map.put("programs", backportsService.getAllPrograms());
		map.put("currentTags", Context.getLocationService().getAllLocationTags(false));
		map.put("locations", Context.getLocationService().getAllLocations(false));
		
		Integer locationId = null;
		if (form.equals(ChirdlUtilConstants.EDIT_LOC_TAG_ATTR_FORM)) {
			String locationIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION);
			if (locationIdStr != null && locationIdStr.trim().length() > 0) {
				locationId = Integer.parseInt(locationIdStr);
				List<LocationTag> locationTags = backportsService.getLocationTagById(locationId); 
				map.put("locationTags", locationTags);
			}
		}
		String tagName = request.getParameter(ChirdlUtilConstants.PARAMETER_TAG_NAME);
		List<LocationTagAttribute> locationTagAttributes = getNonFormLocationTagAttributes();
		map.put("locationTagAttributes", locationTagAttributes);
		List<LocationTagAttributeValue> locationTagAttributeValues = new ArrayList<LocationTagAttributeValue>();
		for (LocationTagAttribute locationTagAttribute : locationTagAttributes) {
			if (form.equals(ChirdlUtilConstants.CREATE_LOC_TAG_FORM)) {
				String value = request.getParameter(locationTagAttribute.getName());
				if (value != null && value.trim().length() > 0) {
					map.put(locationTagAttribute.getName(), value);
				}
			}
			else if (form.equals(ChirdlUtilConstants.EDIT_LOC_TAG_ATTR_FORM) && tagName != null && tagName.trim().length() > 0 ) {
				LocationService locationService = Context.getLocationService();
				LocationTag locationTag = locationService.getLocationTagByName(tagName);
				LocationTagAttributeValue locationTagAttributeValue = backportsService.getLocationTagAttributeValue(locationTag.getLocationTagId(), locationTagAttribute.getName(), locationId);
				if (locationTagAttributeValue!=null)
					locationTagAttributeValues.add(locationTagAttributeValue);
				map.put("locationTagAttributeValues", locationTagAttributeValues);
			}
		}
	}
	
	private boolean createOrUpdateClinicTag(HttpServletRequest request, Location location, String tagName) 
	throws Exception {
		String form = request.getParameter("form");
		LocationTag locationTag = null; 
		if (form.equals(ChirdlUtilConstants.CREATE_LOC_TAG_FORM) ) { 
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
		
		String programIdStr = request.getParameter("program");
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
		String form = request.getParameter("form");
		List<LocationTagAttribute> locationTagAttributes = getNonFormLocationTagAttributes();
		for (LocationTagAttribute locationTagAttribute : locationTagAttributes) {
			String locTagAttrName = locationTagAttribute.getName();
			String value = request.getParameter(locTagAttrName);
			if (value != null && value.trim().length() > 0) {
				addLocationTagAttribute(request, backportsService, locationId, locationTagId, locationTagAttribute, 
					value.trim());
			} else if (form.equals(ChirdlUtilConstants.EDIT_LOC_TAG_ATTR_FORM) ) {
				LocationTagAttributeValue existingLtav = backportsService.getLocationTagAttributeValue(locationTagId, 
					locationTagAttribute.getName(), locationId);
				if (existingLtav!=null && (value == null || value.trim().length() == 0 )) {
					deleteLocationTagAttributeValue(backportsService, locationId, locationTagId, locationTagAttribute);
				}
			}
		}
	}
	
	private void addLocationTagAttribute(HttpServletRequest request, ChirdlUtilBackportsService backportsService, 
	                                  Integer locationId, Integer locationTagId, LocationTagAttribute locationTagAttr, 
	                                  String value) {
		LocationTagAttributeValue ltav = new LocationTagAttributeValue();
		ltav.setLocationTagAttributeId(locationTagAttr.getLocationTagAttributeId());
		ltav.setLocationId(locationId);
		ltav.setLocationTagId(locationTagId);
		ltav.setValue(value);
		String form = request.getParameter("form");
		if (form.equals(ChirdlUtilConstants.EDIT_LOC_TAG_ATTR_FORM)) {
			LocationTagAttributeValue existingLtav = backportsService.getLocationTagAttributeValue(locationTagId, 
				locationTagAttr.getName(), locationId);
			if (existingLtav != null) {
				Integer locTagAttrValId = existingLtav.getLocationTagAttributeValueId();
				ltav.setLocationTagAttributeValueId(locTagAttrValId);
			}
			backportsService.updateLocationTagAttributeValue(ltav);
		} else {
			backportsService.saveLocationTagAttributeValue(ltav);
		}
	}
	
	private void addFormAttributeValues(HttpServletRequest request, Location location, LocationTag locationTag) {
		String copyTagIdStr = request.getParameter("establishedTag");
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
	
	private void deleteLocationTagAttributeValue(ChirdlUtilBackportsService backportsService, 
		      	                                  Integer locationId, Integer locationTagId, LocationTagAttribute locationTagAttr) {
		if (locationTagAttr != null) {
			LocationTagAttributeValue existingLtav = backportsService.getLocationTagAttributeValue(locationTagId, 
				locationTagAttr.getName(), locationId);
			if (existingLtav != null) {
				backportsService.deleteLocationTagAttributeValue(existingLtav);
			}
		}
	}
	
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
}

