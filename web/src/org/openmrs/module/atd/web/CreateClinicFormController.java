package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
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
		return new HashMap<String, Object>();
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String clinicName = request.getParameter("clinicName");
		String clinicDescription = request.getParameter("clinicDescription");
		String view = getFormView();
		
		// Check to see if clinic name was specified.
		if (clinicName == null || clinicName.trim().length() == 0) {
			map.put("missingName", true);
			map.put("clinicDescription", clinicDescription);
			return new ModelAndView(view, map);
		}
		
		clinicName = clinicName.trim();
		map.put("clinicName", clinicName);		
		// Check to see if the form name is already specified.
		LocationService locationService = Context.getLocationService();
		Location location = locationService.getLocation(clinicName);
		if (location != null) {
			map.put("duplicateName", true);
			map.put("clinicDescription", clinicDescription);
			return new ModelAndView(view, map);
		}
		
		view = getSuccessView();
		if (clinicDescription != null && clinicDescription.trim().length() > 0) {
			map.put("clinicDescription", clinicDescription.trim());
		}
		
		return new ModelAndView(new RedirectView(view), map);
	}
}

