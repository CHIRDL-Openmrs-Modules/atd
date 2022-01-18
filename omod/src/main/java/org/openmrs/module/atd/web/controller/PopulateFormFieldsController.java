package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/popFormFields.form")
public class PopulateFormFieldsController{
	
	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(PopulateFormFieldsController.class);
	
	/** Form view */
	private static final String FORM_VIEW = "/module/atd/popFormFields";
	
	/** Success form view */
	private static final String SUCCESS_FORM_VIEW = "configForm.form";
	
	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
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
				map.put(AtdConstants.PARAMETER_FORM_FIELDS, formFields);
				map.put(AtdConstants.PARAMETER_FIELD_TYPES, formService.getAllFieldTypes());
				
			}
			catch (Exception e) {
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}
		}
		return FORM_VIEW;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		FormService formService = Context.getFormService();
		String formIdString = request.getParameter(AtdConstants.PARAMETER_FORM_TO_EDIT);
		int formId = Integer.parseInt(formIdString);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(formId, true); // CHICA-993 Updated to delete based on formId, also pass true to delete LocationTagAttribute record
			return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER));
		}
		
		ConceptService conceptService = Context.getConceptService();
		int numPrioritizedFields = 0;
		try {
			Form formToEdit = formService.getForm(formId);
			List<FormField> formFields = formToEdit.getOrderedFormFields();
			
			for (FormField currFormField : formFields) {
				Field currField = currFormField.getField();
				Integer fieldId = currField.getFieldId();
				String name = request.getParameter(AtdConstants.PARAMETER_NAME_PREFIX + fieldId);
				String fieldTypeIdStr = request.getParameter(AtdConstants.PARAMETER_FIELD_TYPE_PREFIX + fieldId);
				String conceptName = request.getParameter(AtdConstants.PARAMETER_CONCEPT_PREFIX + fieldId);
				String defaultValue = request.getParameter(AtdConstants.PARAMETER_DEFAULT_VALUE_PREFIX + fieldId);
				String fieldNumber = request.getParameter(AtdConstants.PARAMETER_FIELD_NUMBER_PREFIX + fieldId);
				String parentFieldId = request.getParameter(AtdConstants.PARAMETER_PARENT_PREFIX + fieldId);
				
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
				formService.saveFormField(currFormField);
				formService.saveField(currField);			
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
		return new ModelAndView(new RedirectView(SUCCESS_FORM_VIEW), map);
	}
}
