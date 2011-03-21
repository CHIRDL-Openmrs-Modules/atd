package org.openmrs.module.atd.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.atd.service.ATDService;
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
			// Load the Teleform XML file.
			if (request instanceof MultipartHttpServletRequest) {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile xmlFile = multipartRequest.getFile("xmlFile");
				if (xmlFile != null && !xmlFile.isEmpty()) {
					String formName = replaceForm.getName() + "_replace_" + System.currentTimeMillis();
					newForm = loadXmlFile(xmlFile, formName);
				} else {
					map.put("missingFile", true);
					map.put("forms", formService.getAllForms(false));
					map.put("selectedForm", replaceFormIdStr);
					return new ModelAndView(view, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while getting xmlFile from request", e);
			map.put("failedFileUpload", true);
			map.put("forms", formService.getAllForms(false));
			map.put("selectedForm", replaceFormIdStr);
			return new ModelAndView(view, map);
		}
		
		ATDService atdService = Context.getService(ATDService.class);
		try {
			copyFormFields(formService, newForm, replaceForm);
		}
		catch (Exception e) {
			log.error("Error copying form fields", e);
			map.put("failedFieldCopy", true);
			map.put("forms", formService.getAllForms(false));
			map.put("selectedForm", replaceFormIdStr);
			atdService.purgeFormAttributeValues(newForm.getFormId());
			formService.purgeForm(newForm);
			return new ModelAndView(view, map);
		}
		
		try {
			atdService.copyFormAttributeValues(replaceFormId, newForm.getFormId());
		}
		catch (Exception e) {
			log.error("Error copying form attribute values", e);
			map.put("failedAttrValCopy", true);
			map.put("forms", formService.getAllForms(false));
			map.put("selectedForm", replaceFormIdStr);
			atdService.purgeFormAttributeValues(newForm.getFormId());
			formService.purgeForm(newForm);
			return new ModelAndView(view, map);
		}
		
		view = getSuccessView();
		map.put("formId", newForm.getFormId());
		map.put("createWizard", true);
		map.put("replaceFormId", replaceFormIdStr);
		return new ModelAndView(new RedirectView(view), map);
	}
	
	private Form loadXmlFile(MultipartFile xmlFile, String formName) throws Exception {
		Form form = null;
		AdministrationService adminService = Context.getAdministrationService();
		TeleformTranslator translator = new TeleformTranslator();
		String formLoadDir = adminService.getGlobalProperty("atd.formLoadDirectory");
		// Place the file in the forms to load directory
		InputStream in = xmlFile.getInputStream();
		File file = new File(formLoadDir, formName + ".xml");
		if (file.exists()) {
			file.delete();
		}
		
		OutputStream out = new FileOutputStream(file);
		int nextChar;
		try {
			while ((nextChar = in.read()) != -1) {
				out.write(nextChar);
			}
			
			// Load the XML file
			form = translator.templateXMLToDatabaseForm(formName, file.getAbsolutePath());
		}
		finally {
			if (in != null) {
				in.close();
			}
			
			if (out != null) {
				out.close();
			}
		}
		
		return form;
	}
	
	private void copyFormFields(FormService formService, Form form, Form copiedForm) throws Exception {
		Set<FormField> formFields = form.getFormFields();
		Iterator<FormField> i = formFields.iterator();
		Map<String,FormField> formFieldsMap = mapFormFields(form);
		Map<String,FormField> copyFormFieldsMap = mapFormFields(copiedForm);
		while (i.hasNext()) {
			FormField formField = i.next();
			Field field = formField.getField();
			FormField copyFormField = copyFormFieldsMap.get(field.getName());
			if (copyFormField != null) {
				Field copyField = copyFormField.getField();
				field.setConcept(copyField.getConcept());
				field.setDefaultValue(copyField.getDefaultValue());
				field.setFieldType(copyField.getFieldType());
				formField.setFieldNumber(copyFormField.getFieldNumber());
				FormField copyParentFormField = copyFormField.getParent();
				if (copyParentFormField != null) {
					Field copyParentField = copyParentFormField.getField();
					String name = copyParentField.getName();
					FormField parentField = formFieldsMap.get(name);
					formField.setParent(parentField);
				}
				
				formService.saveFormField(formField);
				formService.saveField(field);
			}
		}
	}
	
	private Map<String,FormField> mapFormFields(Form form) {
		Map<String,FormField> returnMap = new HashMap<String,FormField>();
		Set<FormField> formFields = form.getFormFields();
		Iterator<FormField> i = formFields.iterator();
		while (i.hasNext()) {
			FormField formField = i.next();
			Field field = formField.getField();
			String name = field.getName();
			returnMap.put(name, formField);
		}
		
		return returnMap;
	}
}