package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/createForm.form")
public class CreateFormController {
	
	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(CreateFormController.class);
	
	/** Form view */
	private static final String FORM_VIEW = "/module/atd/createForm";
	
	/** Success form view */
	private static final String SUCCESS_FORM_VIEW = "popFormFields.form";
	
	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(ModelMap map) throws Exception{
		return FORM_VIEW;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String formName = request.getParameter("formName");
		FormService formService = Context.getFormService();
		
		// Check to see if form name was specified.
		if (formName == null || formName.trim().length() == 0) {
			map.put(AtdConstants.PARAMETER_MISSING_NAME, true);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		map.put("formName", formName);
		// Make sure there are no spaces in the form name.
		if (formName.indexOf(" ") >= 0) {
			map.put(AtdConstants.PARAMETER_SPACES_IN_NAME, true);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		// Check to see if the form name is already specified.
		Form form = formService.getForm(formName);
		if (form != null) {
			map.put(AtdConstants.PARAMETER_DUPLICATE_NAME, true);
			return new ModelAndView(FORM_VIEW, map);
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
						return new ModelAndView(FORM_VIEW, map);
					}
					
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("xml") && !extension.equalsIgnoreCase("fxf") && 
							!extension.equalsIgnoreCase("zip") && !extension.equalsIgnoreCase("jar") &&
							!extension.equalsIgnoreCase("csv")) {
						map.put("incorrectExtension", true);
						return new ModelAndView(FORM_VIEW, map);
					}
					
					if (extension.equalsIgnoreCase("csv")) {
						newForm = ConfigManagerUtil.loadFormFromCSVFile(dataFile, formName);
					} else {
						newForm = ConfigManagerUtil.loadTeleformFile(dataFile, formName);
					}
					
					if (newForm == null) {
						map.put("failedCreateForm", true);
						return new ModelAndView(FORM_VIEW, map);
					}
					
					map.put("formId", newForm.getFormId());
					LoggingUtil.logEvent(null, newForm.getFormId(), null, LoggingConstants.EVENT_CREATE_FORM, 
						Context.getUserContext().getAuthenticatedUser().getUserId(), 
						"New Form Created.  Class: " + CreateFormController.class.getCanonicalName());
				} else {
					map.put("missingFile", true);
					return new ModelAndView(FORM_VIEW, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while processing uploaded file from request", e);
			map.put("failedFileUpload", true);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		return new ModelAndView(new RedirectView(SUCCESS_FORM_VIEW), map);
	}
}
