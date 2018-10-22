package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.LocationTagPrinterConfig;
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
@RequestMapping(value = "module/atd/printerLocationForm.form")
public class PrinterLocationFormController {
	
    /** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/printerLocationForm";
    
    /** Parameters */
    private static final String PARAMETER_PRINTER_CONFIG = "printerConfig";
    private static final String PARAMETER_LOCATION_NAME = "locationName";
    
    /** Application name */
    private static final String APPLICATION_FORM_PRINTER_CONFIGURATION = "Form Printer Configuration";
    
    /** Printer option suffixes */
    private static final String USE_ALTERNATE_PRINTER_SUFFIX = "_useAlternatePrinter";
    private static final String ALTERNATE_PRINTER_SUFFIX = "_alternatePrinter";
    private static final String DEFAULT_PRINTER_SUFFIX = "_defaultPrinter";
	
	/**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		String formIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID);
		Integer formId = Integer.valueOf(formIdStr);
		String locationIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION_ID);
		Integer locationId = Integer.valueOf(locationIdStr);
		
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formId);
		String formName = form.getName();
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, formId);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_NAME, formName);
		
		LocationService locService = Context.getLocationService();
		Location location = locService.getLocation(locationId);
		map.put(ChirdlUtilConstants.PARAMETER_LOCATION_ID, locationId);
		map.put(PARAMETER_LOCATION_NAME, location.getName());
		
		ATDService atdService = Context.getService(ATDService.class);
		FormPrinterConfig printerConfig = atdService.getPrinterConfigurations(formId, locationId);
		map.put(PARAMETER_PRINTER_CONFIG, printerConfig);
	
		return FORM_VIEW;
	}
	
    /**
     * Processes printer changes for clinic tags.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		String formIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID);
		Integer formId = Integer.valueOf(formIdStr);
		String locationIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_LOCATION_ID);
		Integer locationId = Integer.valueOf(locationIdStr);
		
		ATDService atdService = Context.getService(ATDService.class);
		FormPrinterConfig printerConfig = atdService.getPrinterConfigurations(formId, locationId);
		List<LocationTagPrinterConfig> configs = printerConfig.getLocationTagPrinterConfigs();
		for (LocationTagPrinterConfig config : configs) {
			String defaultPrinter = request.getParameter(config.getLocationTagId() + DEFAULT_PRINTER_SUFFIX);
			String alternatePrinter = request.getParameter(config.getLocationTagId() + ALTERNATE_PRINTER_SUFFIX);
			String useAlternatePrinter = request.getParameter(config.getLocationTagId() + USE_ALTERNATE_PRINTER_SUFFIX);
			config.getDefaultPrinter().setValue(defaultPrinter);
			config.getAlternatePrinter().setValue(alternatePrinter);
			config.getUseAlternatePrinter().setValue(useAlternatePrinter);
		}
	
		atdService.savePrinterConfigurations(printerConfig);
		LoggingUtil.logEvent(locationId, formId, null, LoggingConstants.EVENT_FORM_PRINTER_CHANGE, 
			Context.getUserContext().getAuthenticatedUser().getUserId(), 
			"Form printer configuration changed.  Class: " + PrinterLocationFormController.class.getCanonicalName());
		Map<String, Object> map = new HashMap<>();
		map.put(AtdConstants.PARAMETER_APPLICATION, APPLICATION_FORM_PRINTER_CONFIGURATION);
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
}
