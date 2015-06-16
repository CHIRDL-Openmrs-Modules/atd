package org.openmrs.module.atd.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class CreateClinicFormController extends SimpleFormController {
	
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
		map.put("locationAttributes", getAllLocationAttributes());
		
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String clinicName = request.getParameter("name");
		String view = getFormView();
		
		// Check to see if clinic name was specified.
		if (clinicName == null || clinicName.trim().length() == 0) {
			map.put("missingName", true);
			reloadValues(request, map);
			return new ModelAndView(view, map);
		}
		
		clinicName = clinicName.trim();
		// Check to see if the clinic name is already specified.
		LocationService locationService = Context.getLocationService();
		Location location = locationService.getLocation(clinicName);
		if (location != null) {
			map.put("duplicateName", true);
			reloadValues(request, map);
			return new ModelAndView(view, map);
		}
		
		// Check to see if the clinic name has spaces.
		if (clinicName.contains(" ")) {
			map.put("spacesInName", true);
			reloadValues(request, map);
			return new ModelAndView(view, map);
		}
		
		try {
			createClinic(request, clinicName);
		} catch (Exception e) {
			log.error("Error creating new clinic location", e);
			map.put("failedCreation", "Failed creating a new clinic location: " + e.getMessage());
			reloadValues(request, map);
			return new ModelAndView(view, map);
		}
		
		map.put("application", "Create Clinic");
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
	}
	
	private void reloadValues(HttpServletRequest request, Map<String, Object> map) {
		map.put("name", request.getParameter("name"));
		map.put("description", request.getParameter("description"));
		map.put("address", request.getParameter("address"));
		map.put("addressTwo", request.getParameter("addressTwo"));
		map.put("city", request.getParameter("city"));
		map.put("state", request.getParameter("state"));
		map.put("zip", request.getParameter("zip"));
		List<LocationAttribute> locationAttributes = getAllLocationAttributes();
		map.put("locationAttributes", locationAttributes);
		for (LocationAttribute locationAttribute : locationAttributes) {
			String value = request.getParameter(locationAttribute.getName());
			if (value != null && value.trim().length() > 0) {
				map.put(locationAttribute.getName(), value);
			}
		}
	}
	
	private void createClinic(HttpServletRequest request, String clinicName) 
	throws Exception {
		Location location = createLocation(request, clinicName);
		addLocationAttributes(request, location);
	}
	
	private Location createLocation(HttpServletRequest request, String clinicName) throws Exception {
		User user = Context.getAuthenticatedUser();
		Location location = new Location();
		location.setName(clinicName);
		location.setCreator(user);
		location.setDateCreated(new Date());
		String address = request.getParameter("address");
		if (address != null && address.trim().length() != 0) {
			location.setAddress1(address.trim());
		}
		
		String address2 = request.getParameter("addressTwo");
		if (address2 != null && address2.trim().length() != 0) {
			location.setAddress2(address2.trim());
		}
		
		String city = request.getParameter("city");
		if (city != null && city.trim().length() != 0) {
			location.setCityVillage(city.trim());
		}
		
		String state = request.getParameter("state");
		if (state != null && state.trim().length() != 0) {
			location.setStateProvince(state.trim());
		}
		
		String zip = request.getParameter("zip");
		if (zip != null && zip.trim().length() != 0) {
			location.setPostalCode(zip.trim());
		}
		
		String description = request.getParameter("description");
		if (description != null && description.trim().length() != 0) {
			location.setDescription(description.trim());
		}
		
		return Context.getLocationService().saveLocation(location);
	}
	
	private void addLocationAttributes(HttpServletRequest request, Location location) throws Exception {
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer locationId = location.getLocationId();
		for (LocationAttribute locationAttribute : getAllLocationAttributes()) {
			String value = request.getParameter(locationAttribute.getName());
			if (value != null && value.trim().length() > 0) {
				addLocationAttribute(request, backportsService, locationId, value.trim(), locationAttribute);
			}
		}
	}
	
	private void addLocationAttribute(HttpServletRequest request, ChirdlUtilBackportsService backportsService, 
	                                  Integer locationId, String value, LocationAttribute locationAttr) {
		LocationAttributeValue lav = new LocationAttributeValue();
		lav.setLocationAttributeId(locationAttr.getLocationAttributeId());
		lav.setLocationId(locationId);
		lav.setValue(value.trim());
		backportsService.saveLocationAttributeValue(lav);
	}
	
	private List<LocationAttribute> getAllLocationAttributes() {
		return Context.getService(ChirdlUtilBackportsService.class).getAllLocationAttributes();
	}
}

