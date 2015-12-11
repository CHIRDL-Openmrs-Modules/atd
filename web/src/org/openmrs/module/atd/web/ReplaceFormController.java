package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
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

public class ReplaceFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put("forms", forms);
		
		return map;
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String view = getFormView();
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
						return new ModelAndView(view, map);
					}
					
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("xml") && !extension.equalsIgnoreCase("fxf") && 
							!extension.equalsIgnoreCase("zip") && !extension.equalsIgnoreCase("jar") &&
							!extension.equalsIgnoreCase("csv")) {
						map.put("incorrectExtension", true);
						map.put("forms", formService.getAllForms(false));
						map.put("selectedForm", replaceFormIdStr);
						return new ModelAndView(view, map);
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
						return new ModelAndView(view, map);
					}
					
					LoggingUtil.logEvent(null, newForm.getFormId(), null, LoggingConstants.EVENT_CREATE_FORM, 
						Context.getUserContext().getAuthenticatedUser().getUserId(), 
						"Form created.  Class: " + ReplaceFormController.class.getCanonicalName());
				} else {
					map.put("missingFile", true);
					map.put("forms", formService.getAllForms(false));
					map.put("selectedForm", replaceFormIdStr);
					return new ModelAndView(view, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while processing uploaded file from request", e);
			map.put("failedFileUpload", true);
			map.put("forms", formService.getAllForms(false));
			map.put("selectedForm", replaceFormIdStr);
			return new ModelAndView(view, map);
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
			ConfigManagerUtil.deleteForm(newForm.getFormId());
			return new ModelAndView(view, map);
		}
		
		view = getSuccessView();
		map.put("formId", newForm.getFormId());
		map.put("replaceFormId", replaceFormIdStr);
		map.put("selectedFormName", newForm.getName());
		return new ModelAndView(new RedirectView(view), map);
	}
}
