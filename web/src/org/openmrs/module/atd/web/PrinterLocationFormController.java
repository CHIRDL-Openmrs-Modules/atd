package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class PrinterLocationFormController extends SimpleFormController {
	
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
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String formIdStr = request.getParameter("formId");
		Integer formId = Integer.parseInt(formIdStr);
		String locationIdStr = request.getParameter("locationId");
		Integer locationId = Integer.parseInt(locationIdStr);
		
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formId);
		String formName = form.getName();
		map.put("formId", formId);
		map.put("formName", formName);
		
		LocationService locService = Context.getLocationService();
		Location location = locService.getLocation(locationId);
		map.put("locationId", locationId);
		map.put("locationName", location.getName());
		
		ATDService atdService = Context.getService(ATDService.class);
		FormPrinterConfig printerConfig = atdService.getPrinterConfigurations(formId, locationId);
		map.put("printerConfig", printerConfig);
	
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		String formIdStr = request.getParameter("formId");
		Integer formId = Integer.parseInt(formIdStr);
		String locationIdStr = request.getParameter("locationId");
		Integer locationId = Integer.parseInt(locationIdStr);
		
		ATDService atdService = Context.getService(ATDService.class);
		FormPrinterConfig printerConfig = atdService.getPrinterConfigurations(formId, locationId);
		List<LocationTagPrinterConfig> configs = printerConfig.getLocationTagPrinterConfigs();
		for (LocationTagPrinterConfig config : configs) {
			String defaultPrinter = request.getParameter(config.getLocationTagId() + "_defaultPrinter");
			String alternatePrinter = request.getParameter(config.getLocationTagId() + "_alternatePrinter");
			String useAlternatePrinter = request.getParameter(config.getLocationTagId() + "_useAlternatePrinter");
			config.getDefaultPrinter().setValue(defaultPrinter);
			config.getAlternatePrinter().setValue(alternatePrinter);
			config.getUseAlternatePrinter().setValue(useAlternatePrinter);
		}
	
		atdService.savePrinterConfigurations(printerConfig);
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
}
