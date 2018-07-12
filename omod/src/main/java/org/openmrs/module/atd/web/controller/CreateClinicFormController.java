package org.openmrs.module.atd.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.ChirdlLocationAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.ChirdlLocationAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/createClinicForm.form")
public class CreateClinicFormController {
	
    /** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/createClinicForm";
    
    /** Parameters */
    private static final String PARAMETER_ZIP = "zip";
    private static final String PARAMETER_STATE = "state";
    private static final String PARAMETER_CITY = "city";
    private static final String PARAMETER_ADDRESS_TWO = "addressTwo";
    private static final String PARAMETER_ADDRESS = "address";
    private static final String PARAMETER_LOCATION_ATTRIBUTES = "locationAttributes";
    
    /** Application name */
    private static final String APPLICATION_CREATE_CLINIC = "Create Clinic";
    
	/**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		map.put(PARAMETER_LOCATION_ATTRIBUTES, getAllLocationAttributes());
		
		return FORM_VIEW;
	}
	
    /**
     * Handles the submission of the page..
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		String clinicName = request.getParameter("name");
		
		// Check to see if clinic name was specified.
		if (clinicName == null || clinicName.trim().length() == 0) {
			map.put(AtdConstants.PARAMETER_MISSING_NAME, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			reloadValues(request, map);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		clinicName = clinicName.trim();
		// Check to see if the clinic name is already specified.
		LocationService locationService = Context.getLocationService();
		Location location = locationService.getLocation(clinicName);
		if (location != null) {
			map.put(AtdConstants.PARAMETER_DUPLICATE_NAME, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			reloadValues(request, map);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		// Check to see if the clinic name has spaces.
		if (clinicName.contains(" ")) {
			map.put(AtdConstants.PARAMETER_SPACES_IN_NAME, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			reloadValues(request, map);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		try {
			createClinic(request, clinicName);
		} catch (Exception e) {
			log.error("Error creating new clinic location", e);
			map.put(AtdConstants.PARAMETER_FAILED_CREATION, "Failed creating a new clinic location: " + e.getMessage());
			reloadValues(request, map);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		map.put(AtdConstants.PARAMETER_APPLICATION, APPLICATION_CREATE_CLINIC);
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
	
	private void reloadValues(HttpServletRequest request, Map<String, Object> map) {
		map.put(ChirdlUtilConstants.PARAMETER_NAME, request.getParameter(ChirdlUtilConstants.PARAMETER_NAME));
		map.put(ChirdlUtilConstants.PARAMETER_DESCRIPTION, request.getParameter(ChirdlUtilConstants.PARAMETER_DESCRIPTION));
		map.put(PARAMETER_ADDRESS, request.getParameter(PARAMETER_ADDRESS));
		map.put(PARAMETER_ADDRESS_TWO, request.getParameter(PARAMETER_ADDRESS_TWO));
		map.put(PARAMETER_CITY, request.getParameter(PARAMETER_CITY));
		map.put(PARAMETER_STATE, request.getParameter(PARAMETER_STATE));
		map.put(PARAMETER_ZIP, request.getParameter(PARAMETER_ZIP));
		List<ChirdlLocationAttribute> locationAttributes = getAllLocationAttributes();
		map.put(PARAMETER_LOCATION_ATTRIBUTES, locationAttributes);
		for (ChirdlLocationAttribute locationAttribute : locationAttributes) {
			String value = request.getParameter(locationAttribute.getName());
			if (value != null && value.trim().length() > 0) {
				map.put(locationAttribute.getName(), value);
			}
		}
	}
	
	private void createClinic(HttpServletRequest request, String clinicName) {
		Location location = createLocation(request, clinicName);
		addLocationAttributes(request, location);
	}
	
	private Location createLocation(HttpServletRequest request, String clinicName) {
		User user = Context.getAuthenticatedUser();
		Location location = new Location();
		location.setName(clinicName);
		location.setCreator(user);
		location.setDateCreated(new Date());
		String address = request.getParameter(PARAMETER_ADDRESS);
		if (address != null && address.trim().length() != 0) {
			location.setAddress1(address.trim());
		}
		
		String address2 = request.getParameter(PARAMETER_ADDRESS_TWO);
		if (address2 != null && address2.trim().length() != 0) {
			location.setAddress2(address2.trim());
		}
		
		String city = request.getParameter(PARAMETER_CITY);
		if (city != null && city.trim().length() != 0) {
			location.setCityVillage(city.trim());
		}
		
		String state = request.getParameter(PARAMETER_STATE);
		if (state != null && state.trim().length() != 0) {
			location.setStateProvince(state.trim());
		}
		
		String zip = request.getParameter(PARAMETER_ZIP);
		if (zip != null && zip.trim().length() != 0) {
			location.setPostalCode(zip.trim());
		}
		
		String description = request.getParameter(ChirdlUtilConstants.PARAMETER_DESCRIPTION);
		if (description != null && description.trim().length() != 0) {
			location.setDescription(description.trim());
		}
		
		return Context.getLocationService().saveLocation(location);
	}
	
	private void addLocationAttributes(HttpServletRequest request, Location location) {
		ChirdlUtilBackportsService backportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer locationId = location.getLocationId();
		for (ChirdlLocationAttribute locationAttribute : getAllLocationAttributes()) {
			String value = request.getParameter(locationAttribute.getName());
			if (value != null && value.trim().length() > 0) {
				addLocationAttribute(backportsService, locationId, value.trim(), locationAttribute);
			}
		}
	}
	
	private void addLocationAttribute(ChirdlUtilBackportsService backportsService, Integer locationId, String value, 
	        ChirdlLocationAttribute locationAttr) {
		ChirdlLocationAttributeValue lav = new ChirdlLocationAttributeValue();
		lav.setLocationAttributeId(locationAttr.getLocationAttributeId());
		lav.setLocationId(locationId);
		lav.setValue(value.trim());
		backportsService.saveLocationAttributeValue(lav);
	}
	
	private List<ChirdlLocationAttribute> getAllLocationAttributes() {
		return Context.getService(ChirdlUtilBackportsService.class).getAllLocationAttributes();
	}
}

