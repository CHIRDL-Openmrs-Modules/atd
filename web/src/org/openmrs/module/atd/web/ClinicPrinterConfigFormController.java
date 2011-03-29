package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


public class ClinicPrinterConfigFormController extends SimpleFormController {

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
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String view = getFormView();
		// Check to see if the user checked any locations
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		boolean found = false;
		List<String> locNames = new ArrayList<String>();
		List<Integer> selectedLocations = new ArrayList<Integer>();
		for (Location location : locations) {
			String name = location.getName();
			locNames.add(name);
			Object foundLoc = request.getParameterValues("location_" + name);
			if (foundLoc != null) {
				found = true;
				selectedLocations.add(location.getLocationId());
				map.put("checked_" + name, "true");
			} else {
				map.put("checked_" + name, "false");
			}
		}
		
		String useAltPrintersStr = request.getParameter("useAltPrinter");
		if (useAltPrintersStr == null) {
			useAltPrintersStr = "false";
		} else {
			map.put("useAltPrinter", useAltPrintersStr);
		}
		
		if (!found) {
			map.put("noLocationsChecked", true);
			map.put("locations", locNames);
			return new ModelAndView(view, map);
		}
		
		Boolean useAltPrinters = Boolean.FALSE;
		if ("true".equalsIgnoreCase(useAltPrintersStr)) {
			useAltPrinters = Boolean.TRUE;
		}
		
		try {
			ATDService atdService = Context.getService(ATDService.class);
			atdService.setClinicUseAlternatePrinters(selectedLocations, useAltPrinters);
		} 
		catch (Exception e) {
			map.put("failedUpdate", true);
			map.put("locations", locNames);
			return new ModelAndView(view, map);
		}
		
		for (Integer locationId : selectedLocations) {
			LoggingUtil.logEvent(locationId, null, null, LoggingConstants.EVENT_MODIFY_CLINIC_PRINTER_CONFIG, 
				Context.getUserContext().getAuthenticatedUser().getUserId(), 
				"Clinic printer configuration modified.  Class: " + ClinicPrinterConfigFormController.class.getCanonicalName());
		}
		
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view));
	}
	
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		List<String> locNames = new ArrayList<String>();
		for (Location location : locations) {
			boolean checkLocation = false;
			String name = location.getName();
			String checked = request.getParameter("checked_" + name);
			if (checked != null && "true".equalsIgnoreCase(checked)) {
				checkLocation = true;
			}
			
			locNames.add(name);
			map.put("checked_" + name, checkLocation);
		}
		
		map.put("locations", locNames);
		
		return map;
	}
}
