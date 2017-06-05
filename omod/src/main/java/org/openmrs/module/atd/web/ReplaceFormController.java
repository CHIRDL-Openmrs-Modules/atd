package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/replaceForm.form")
public class ReplaceFormController{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view name */
	private static final String FORM_VIEW = "/module/atd/replaceForm";
	
	/** Success form view */
	private static final String SUCCESS_FORM_VIEW = "replaceFormFields.form";

	@RequestMapping(method = RequestMethod.GET) 
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put("forms", forms);
		
		return FORM_VIEW;
	}

	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		FormService formService = Context.getFormService();
		
		Form newForm = null;
		String replaceFormIdStr = request.getParameter("formToReplace");
		Integer replaceFormId = Integer.parseInt(replaceFormIdStr);
		Form replaceForm = formService.getForm(replaceFormId);
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
						map.put("selectedForm", replaceFormIdStr);
						return new ModelAndView(FORM_VIEW, map);
					}
					
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("xml") && !extension.equalsIgnoreCase("fxf") && 
							!extension.equalsIgnoreCase("zip") && !extension.equalsIgnoreCase("jar") &&
							!extension.equalsIgnoreCase("csv")) {
						map.put("incorrectExtension", true);
						map.put("forms", formService.getAllForms(false));
						map.put("selectedForm", replaceFormIdStr);
						return new ModelAndView(FORM_VIEW, map);
					}
					
					String formName = replaceForm.getName() + "_replace_" + System.currentTimeMillis();
					if (extension.equalsIgnoreCase("csv")) {
						newForm = ConfigManagerUtil.loadFormFromCSVFile(dataFile, formName);
					} else {
						newForm = ConfigManagerUtil.loadTeleformFile(dataFile, formName);
					}
					
					if (newForm == null) {
						map.put("failedFileUpload", true);
						map.put("forms", formService.getAllForms(false));
						map.put("selectedForm", replaceFormIdStr);
						return new ModelAndView(FORM_VIEW, map);
					}
					
					LoggingUtil.logEvent(null, newForm.getFormId(), null, LoggingConstants.EVENT_CREATE_FORM, 
						Context.getUserContext().getAuthenticatedUser().getUserId(), 
						"Form created.  Class: " + ReplaceFormController.class.getCanonicalName());
				} else {
					map.put("missingFile", true);
					map.put("forms", formService.getAllForms(false));
					map.put("selectedForm", replaceFormIdStr);
					return new ModelAndView(FORM_VIEW, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while processing uploaded file from request", e);
			map.put("failedFileUpload", true);
			map.put("forms", formService.getAllForms(false));
			map.put("selectedForm", replaceFormIdStr);
			return new ModelAndView(FORM_VIEW, map);
		}
		
		ATDService atdService = Context.getService(ATDService.class);
		try {
			atdService.copyFormAttributeValues(replaceFormId, newForm.getFormId());
		}
		catch (Exception e) {
			log.error("Error copying form attribute values", e);
			map.put("failedAttrValCopy", true);
			map.put("forms", formService.getAllForms(false));
			map.put("selectedForm", replaceFormIdStr);
			ConfigManagerUtil.deleteForm(newForm.getFormId(), false); // CHICA-993 Updated to delete based on formId, also pass false so that LocationTagAttribute record is NOT deleted
			return new ModelAndView(FORM_VIEW, map);
		}
		
		map.put("formId", newForm.getFormId());
		map.put("replaceFormId", replaceFormIdStr);
		map.put("selectedFormName", newForm.getName());
		return new ModelAndView(new RedirectView(SUCCESS_FORM_VIEW), map);
	}
}
