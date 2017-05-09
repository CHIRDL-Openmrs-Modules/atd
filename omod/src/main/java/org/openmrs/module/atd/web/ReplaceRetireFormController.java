package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ReplaceRetireFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}
	
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		FormService formService = Context.getFormService();
		String formIdStr = request.getParameter("formId");
		String newFormIdStr = request.getParameter("newFormId");
		Integer formId = Integer.parseInt(formIdStr);
		Integer newFormId = Integer.parseInt(newFormIdStr);
		Form form = formService.getForm(formId);
		Form newForm = formService.getForm(newFormId);
		
		map.put("form", form);
		map.put("newForm", newForm);
		
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, 
	                                             BindException errors) throws Exception {
		String view = getSuccessView();
		String newFormIdStr = request.getParameter("newFormId");
		Integer newFormId = Integer.parseInt(newFormIdStr);
		String formIdStr = request.getParameter("formId");
		Integer formId = Integer.parseInt(formIdStr);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(newFormId);
			return new ModelAndView(new RedirectView("configurationManager.form"));
		}
		
		String retireForm = request.getParameter("retireForm");
		if ("Yes".equalsIgnoreCase(retireForm)) {
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
		}
		
		LoggingUtil.logEvent(null, newFormId, null, LoggingConstants.EVENT_REPLACE_FORM, 
			Context.getUserContext().getAuthenticatedUser().getUserId(), 
			"Form replaced.  Old form: " + formId + " New form: " + newFormId + "  Class: " + 
			ReplaceRetireFormController.class.getCanonicalName());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("application", "Replace Form");
		return new ModelAndView(new RedirectView(view), map);
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
