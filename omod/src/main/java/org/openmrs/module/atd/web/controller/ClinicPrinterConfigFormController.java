package org.openmrs.module.atd.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jfree.util.Log;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/clinicPrinterConfigForm.form")
public class ClinicPrinterConfigFormController {
    
    /** Form view */
    private static final String FORM_VIEW = "/module/atd/clinicPrinterConfigForm";
    
    /** Parameters */
    private static final String PARAMETER_FAILED_UPDATE = "failedUpdate";
    private static final String PARAMETER_LOCATIONS = "locations";
    private static final String PARAMETER_NO_LOCATIONS_CHECKED = "noLocationsChecked";
    private static final String PARAMETER_USE_ALT_PRINTER = "useAltPrinter";
    private static final String PARAMETER_CHECKED_PREFIX = "checked_";
    private static final String PARAMETER_LOCATION_PREFIX = "location_";
    
    /** Application name */
    private static final String APPLICATION_CLINIC_PRINTER_CONFIGURATION = "Clinic Printer Configuration";
	
    /**
     * Sets the clinic tags to use alternate printers (if selected).
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		// Check to see if the user checked any locations
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		boolean found = false;
		List<String> locNames = new ArrayList<>();
		List<Integer> selectedLocations = new ArrayList<>();
		for (Location location : locations) {
			String name = location.getName();
			locNames.add(name);
			Object foundLoc = request.getParameterValues(PARAMETER_LOCATION_PREFIX + name);
			if (foundLoc != null) {
				found = true;
				selectedLocations.add(location.getLocationId());
				map.put(PARAMETER_CHECKED_PREFIX + name, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			} else {
				map.put(PARAMETER_CHECKED_PREFIX + name, ChirdlUtilConstants.GENERAL_INFO_FALSE);
			}
		}
		
		String useAltPrintersStr = request.getParameter(PARAMETER_USE_ALT_PRINTER);
		if (useAltPrintersStr == null) {
			useAltPrintersStr = ChirdlUtilConstants.GENERAL_INFO_FALSE;
		} else {
			map.put(PARAMETER_USE_ALT_PRINTER, useAltPrintersStr);
		}
		
		if (!found) {
			map.put(PARAMETER_NO_LOCATIONS_CHECKED, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			map.put(PARAMETER_LOCATIONS, locNames);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		Boolean useAltPrinters = Boolean.FALSE;
		if (ChirdlUtilConstants.GENERAL_INFO_TRUE.equalsIgnoreCase(useAltPrintersStr)) {
			useAltPrinters = Boolean.TRUE;
		}
		
		try {
			ATDService atdService = Context.getService(ATDService.class);
			atdService.setClinicUseAlternatePrinters(selectedLocations, useAltPrinters);
		} 
		catch (Exception e) {
		    Log.error("Error setting clinic to use alternate printer", e);
			map.put(PARAMETER_FAILED_UPDATE, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			map.put(PARAMETER_LOCATIONS, locNames);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		for (Integer locationId : selectedLocations) {
			LoggingUtil.logEvent(locationId, null, null, LoggingConstants.EVENT_MODIFY_CLINIC_PRINTER_CONFIG, 
				Context.getUserContext().getAuthenticatedUser().getUserId(), 
				"Clinic printer configuration modified.  Class: " + ClinicPrinterConfigFormController.class.getCanonicalName());
		}
		
		map.put(AtdConstants.PARAMETER_APPLICATION, APPLICATION_CLINIC_PRINTER_CONFIGURATION);
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
	
    /**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		List<String> locNames = new ArrayList<>();
		for (Location location : locations) {
			String checkLocation = ChirdlUtilConstants.GENERAL_INFO_FALSE;
			String name = location.getName();
			String checked = request.getParameter(PARAMETER_CHECKED_PREFIX + name);
			if (ChirdlUtilConstants.GENERAL_INFO_TRUE.equalsIgnoreCase(checked)) {
				checkLocation = ChirdlUtilConstants.GENERAL_INFO_TRUE;
			}
			
			locNames.add(name);
			map.put(PARAMETER_CHECKED_PREFIX + name, checkLocation);
		}
		
		map.put(PARAMETER_LOCATIONS, locNames);
		
		return FORM_VIEW;
	}
}
