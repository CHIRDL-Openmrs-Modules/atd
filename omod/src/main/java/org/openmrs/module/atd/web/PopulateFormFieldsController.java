package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class PopulateFormFieldsController extends SimpleFormController {
	
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
		map.put("formName", request.getParameter("formName"));
		FormService formService = Context.getFormService();
		
		String formIdString = request.getParameter("formId");		
		if (formIdString != null) {
			try {
				int formId = Integer.parseInt(formIdString);
				Form formToEdit = formService.getForm(formId);
				List<FormField> formFields = formToEdit.getOrderedFormFields();
				
				map.put("formId", formId);
				map.put("form", formToEdit);
				map.put("formFields", formFields);
				map.put("fieldTypes", formService.getAllFieldTypes());
				
			}
			catch (Exception e) {
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}
		}
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		long timeInMilliseconds = 0;
		FormService formService = Context.getFormService();
		String formIdString = request.getParameter("formToEdit");
		int formId = Integer.parseInt(formIdString);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(formId);
			return new ModelAndView(new RedirectView("configurationManager.form"));
		}
		
		ConceptService conceptService = Context.getConceptService();
		int numPrioritizedFields = 0;
		try {
			Form formToEdit = formService.getForm(formId);
			List<FormField> formFields = formToEdit.getOrderedFormFields();
			
			for (FormField currFormField : formFields) {
				Field currField = currFormField.getField();
				Integer fieldId = currField.getFieldId();
				String name = request.getParameter("name_" + fieldId);
				String fieldTypeIdStr = request.getParameter("fieldType_" + fieldId);
				String conceptName = request.getParameter("concept_" + fieldId);
				String defaultValue = request.getParameter("defaultValue_" + fieldId);
				String fieldNumber = request.getParameter("fieldNumber_" + fieldId);
				String parentFieldId = request.getParameter("parent_" + fieldId);
				
				if (name != null && name.length() > 0) {
					currField.setName(name);
				}
				try {
					if (fieldTypeIdStr != null && fieldTypeIdStr.length() > 0) {
						Integer fieldTypeId = Integer.parseInt(fieldTypeIdStr);
						FieldType fieldType = formService.getFieldType(fieldTypeId);
						currField.setFieldType(fieldType);
						if (fieldTypeId == 8) {
							numPrioritizedFields++;
						}
					} else {
						currField.setFieldType(null);
					}
				}
				catch (Exception e1) {
					this.log.error(e1.getMessage());
					this.log.error(Util.getStackTrace(e1));
				}
				try {
					if (conceptName != null && conceptName.length() > 0) {
						Concept concept = conceptService.getConcept(conceptName);
						currField.setConcept(concept);
					} else {
						currField.setConcept(null);
					}
				}
				catch (Exception e) {
					this.log.error(e.getMessage());
					this.log.error(Util.getStackTrace(e));
				}
				if (defaultValue != null && defaultValue.length() > 0) {
					currField.setDefaultValue(defaultValue);
				} else {
					currField.setDefaultValue(null);
				}
				try {
					if (fieldNumber != null && fieldNumber.length() > 0) {
						currFormField.setFieldNumber(Integer.parseInt(fieldNumber));
					}
				}
				catch (Exception e) {
					this.log.error(e.getMessage());
					this.log.error(Util.getStackTrace(e));
				}
				
				try {
					if (parentFieldId != null && parentFieldId.length() > 0) {
						FormField parentField = formService.getFormField(Integer.parseInt(parentFieldId));
						currFormField.setParent(parentField);
					} else {
						currFormField.setParent(null);
					}
				}
				catch (Exception e) {
					this.log.error(e.getMessage());
					this.log.error(Util.getStackTrace(e));
				}
				long startTime = System.currentTimeMillis();
				formService.saveFormField(currFormField);
				formService.saveField(currField);
				timeInMilliseconds += (System.currentTimeMillis() - startTime);
			}
			
			LoggingUtil.logEvent(null, formId, null, LoggingConstants.EVENT_MODIFY_FORM_FIELDS, 
				Context.getUserContext().getAuthenticatedUser().getUserId(), 
				"Form fields modified.  Class: " + PopulateFormFieldsController.class.getCanonicalName());
		}
		catch (Exception e) {
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", request.getParameter("formId"));
		map.put("formName", request.getParameter("formName"));
		map.put("numPrioritizedFields", numPrioritizedFields);
		String view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
	}
}