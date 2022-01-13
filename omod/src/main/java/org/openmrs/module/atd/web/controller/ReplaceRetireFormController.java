package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/replaceRetireForm.form")
public class ReplaceRetireFormController {
	
	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(ReplaceRetireFormController.class);
	
	/** Form view name */
	private static final String FORM_VIEW = "/module/atd/replaceRetireForm";
	
	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		FormService formService = Context.getFormService();
		String formIdStr = request.getParameter("formId");
		String newFormIdStr = request.getParameter("newFormId");
		Integer formId = Integer.parseInt(formIdStr);
		Integer newFormId = Integer.parseInt(newFormIdStr);
		Form form = formService.getForm(formId);
		Form newForm = formService.getForm(newFormId);
		
		map.put("form", form);
		map.put("newForm", newForm);
		
		return FORM_VIEW;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		String newFormIdStr = request.getParameter("newFormId");
		Integer newFormId = Integer.parseInt(newFormIdStr);
		String formIdStr = request.getParameter("formId");
		Integer formId = Integer.parseInt(formIdStr);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(newFormId, false); // CHICA-993 Updated to delete based on formId, also pass false so that LocationTagAttribute record is NOT deleted
			return new ModelAndView(new RedirectView("configurationManager.form"));
		}
		
			// CHICA-1050 Remove yes/no (retireForm) parameter. The form will always be retired.
			FormService formService = Context.getFormService();
			Form form = formService.getForm(formId);
			String formName = form.getName();
			formService.retireForm(form, "Form replaced with a new version.");
			Form newForm = formService.getForm(newFormId);
			newForm.setName(formName);
			formService.saveForm(newForm);
			updateChirdlData(form, newForm);
			LoggingUtil.logEvent(null, formId, null, LoggingConstants.EVENT_RETIRE_FORM, 
				Context.getUserContext().getAuthenticatedUser().getUserId(), 
				"Form retired.  Class: " + ReplaceRetireFormController.class.getCanonicalName());
		
		LoggingUtil.logEvent(null, newFormId, null, LoggingConstants.EVENT_REPLACE_FORM, 
			Context.getUserContext().getAuthenticatedUser().getUserId(), 
			"Form replaced.  Old form: " + formId + " New form: " + newFormId + "  Class: " + 
			ReplaceRetireFormController.class.getCanonicalName());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("application", "Replace Form");
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
	
	private void updateChirdlData(Form form, Form newForm) throws Exception {
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);    	
		LocationTagAttribute replaceLocTagAttr = chirdlutilbackportsService.getLocationTagAttribute(form.getName());
    	if (replaceLocTagAttr != null) {
    		replaceLocTagAttr.setName(newForm.getName());
    		replaceLocTagAttr = chirdlutilbackportsService.saveLocationTagAttribute(replaceLocTagAttr);
        	LocationService locService = Context.getLocationService();
        	String newFormIdStr = String.valueOf(newForm.getFormId());
        	for (Location location : locService.getAllLocations(false)) {
        		Set<LocationTag> tags = location.getTags();
        		if (tags != null) {
        			Iterator<LocationTag> i = tags.iterator();
        			while (i.hasNext()) {
        				LocationTag tag = i.next();
        				LocationTagAttributeValue replaceVal = chirdlutilbackportsService.getLocationTagAttributeValue(
        					tag.getLocationTagId(), form.getName(), location.getLocationId());
        				if (replaceVal != null) {
        					replaceVal.setValue(newFormIdStr);
        					chirdlutilbackportsService.saveLocationTagAttributeValue(replaceVal);
        				}
        			}
        		}
        	}
    	}
	}
}
