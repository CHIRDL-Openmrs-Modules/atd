package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/printerFormSelectionForm.form")
public class PrinterFormSelectionFormController {
	
	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(PrinterFormSelectionFormController.class);
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/printerFormSelectionForm";
    
    /** Success view */
    private static final String SUCCESS_VIEW = "printerLocationForm.form";
	
	/**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		FormService formService = Context.getFormService();
		LocationService locService = Context.getLocationService();
		map.put(AtdConstants.PARAMETER_FORMS, formService.getAllForms(false));
		map.put(AtdConstants.PARAMETER_LOCATIONS, locService.getAllLocations(false));
	
		return FORM_VIEW;
	}
	
    /**
     * Parses the form and location IDs and passes them to the next controller.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		String formId = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID);
		String locationId = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION_ID);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, formId);
		map.put(ChirdlUtilConstants.PARAMETER_LOCATION_ID, locationId);
	
		return new ModelAndView(new RedirectView(SUCCESS_VIEW), map);
	}
}
