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
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
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
	protected Map referenceData(HttpServletRequest request) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		FormService formService = Context.getFormService();
		map.put("forms", formService.getAllForms(false));
		return map;
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
			map.put("forms", formService.getAllForms(false));
			return new ModelAndView(view, map);
		}
		
		map.put("formName", formName);
		// Make sure there are no spaces in the form name.
		if (formName.indexOf(" ") >= 0) {
			map.put("spacesInName", true);
			map.put("forms", formService.getAllForms(false));
			return new ModelAndView(view, map);
		}
		
		// Check to see if the form name is already specified.
		Form form = formService.getForm(formName);
		if (form != null) {
			map.put("duplicateName", true);
			map.put("forms", formService.getAllForms(false));
			return new ModelAndView(view, map);
		}
		
		Form newForm = null;
		try {
			// Load the Teleform file.
			if (request instanceof MultipartHttpServletRequest) {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile dataFile = multipartRequest.getFile("dataFile");
				if (dataFile != null && !dataFile.isEmpty()) {
					String filename = dataFile.getOriginalFilename();
					int index = filename.lastIndexOf(".");
					if (index < 0) {
						map.put("incorrectExtension", true);
						map.put("forms", formService.getAllForms(false));
						return new ModelAndView(view, map);
					}
					
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("xml") && !extension.equalsIgnoreCase("fxf") && 
							!extension.equalsIgnoreCase("zip") && !extension.equalsIgnoreCase("jar") &&
							!extension.equalsIgnoreCase("csv")) {
						map.put("incorrectExtension", true);
						map.put("forms", formService.getAllForms(false));
						return new ModelAndView(view, map);
					}
					
					if (extension.equalsIgnoreCase("csv")) {
						newForm = ConfigManagerUtil.loadFormFromCSVFile(dataFile, formName);
					} else {
						newForm = ConfigManagerUtil.loadTeleformFile(dataFile, formName);
					}
					
					if (newForm == null) {
						map.put("failedCreateForm", true);
						map.put("forms", formService.getAllForms(false));
						return new ModelAndView(view, map);
					}
					
					LoggingUtil.logEvent(null, newForm.getFormId(), null, LoggingConstants.EVENT_CREATE_FORM, 
						Context.getUserContext().getAuthenticatedUser().getUserId(), 
						"New Form Created.  Class: " + CreateFormController.class.getCanonicalName());
				} else {
					map.put("missingFile", true);
					map.put("forms", formService.getAllForms(false));
					return new ModelAndView(view, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while processing uploaded file from request", e);
			map.put("failedFileUpload", true);
			map.put("forms", formService.getAllForms(false));
			return new ModelAndView(view, map);
		}
		
		try {
			ATDService atdService = Context.getService(ATDService.class);
			atdService.prePopulateNewFormFields(newForm.getFormId());
		} catch (Exception e) {
			log.error("Error pre-populating form fields", e);
			formService.purgeForm(newForm);
			map.put("failedPopulate", true);
			map.put("forms", formService.getAllForms(false));
			return new ModelAndView(view, map);
		}
		
		view = getSuccessView();
		map.put("formId", newForm.getFormId());
		return new ModelAndView(new RedirectView(view), map);
	}
}
