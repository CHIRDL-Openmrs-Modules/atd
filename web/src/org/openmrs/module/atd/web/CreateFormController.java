package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class CreateFormController extends SimpleFormController {
	
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
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String formName = request.getParameter("formName");
		String view = getFormView();
		FormService formService = Context.getFormService();
		
		
		// Check to see if form name was specified.
		if (formName == null || formName.trim().length() == 0) {
			map.put("missingName", true);
			return new ModelAndView(view, map);
		}
		
		map.put("formName", formName);
		// Make sure there are no spaces in the form name.
		if (formName.indexOf(" ") >= 0) {
			map.put("spacesInName", true);
			return new ModelAndView(view, map);
		}
		
		// Check to see if the form name is already specified.
		Form form = formService.getForm(formName);
		if (form != null) {
			map.put("duplicateName", true);
			return new ModelAndView(view, map);
		}
		
		Form newForm = null;
		try {
			// Load the Teleform XML file.
			if (request instanceof MultipartHttpServletRequest) {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile xmlFile = multipartRequest.getFile("xmlFile");
				if (xmlFile != null && !xmlFile.isEmpty()) {
					newForm = ConfigManagerUtil.loadTeleformXmlFile(xmlFile, formName);
				} else {
					map.put("missingFile", true);
					return new ModelAndView(view, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while getting xmlFile from request", e);
			map.put("failedFileUpload", true);
			return new ModelAndView(view, map);
		}
		
		view = getSuccessView();
		map.put("formId", newForm.getFormId());
		return new ModelAndView(new RedirectView(view), map);
	}
}
