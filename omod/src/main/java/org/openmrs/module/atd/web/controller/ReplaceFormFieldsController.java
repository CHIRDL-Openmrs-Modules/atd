package org.openmrs.module.atd.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.Util;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/replaceFormFields.form")
public class ReplaceFormFieldsController{
	
	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(ReplaceFormFieldsController.class);
	
	/** Form view name */
	private static final String FORM_VIEW = "/module/atd/replaceFormFields";
	
	@RequestMapping(method = RequestMethod.GET) 
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		FormService formService = Context.getFormService();
		
		String formIdString = request.getParameter("formId");
		String replaceFormIdString = request.getParameter("replaceFormId");
		map.put("replaceFormId", replaceFormIdString);
		map.put(AtdConstants.PARAMETER_SELECTED_FORM_NAME, request.getParameter(AtdConstants.PARAMETER_SELECTED_FORM_NAME));
		
		if (formIdString != null) {
			try {
				int formId = Integer.parseInt(formIdString);
				Form formToEdit = formService.getForm(formId);
				List<FormField> formFields = formToEdit.getOrderedFormFields();
				Form replaceForm = formService.getForm(Integer.parseInt(replaceFormIdString));
				List<Boolean> newFieldIndicators = getNewFieldIndicators(
					formFields, replaceForm);
				map.put("form", formToEdit);
				map.put(AtdConstants.PARAMETER_FORM_FIELDS, formFields);
				map.put(AtdConstants.PARAMETER_FIELD_TYPES, formService.getAllFieldTypes());
				map.put("newFieldIndicators", newFieldIndicators);
			}
			catch (Exception e) {
				log.error(e.getMessage());
				log.error(Util.getStackTrace(e));
			}
		}
		return FORM_VIEW;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object command) throws Exception {
		FormService formService = Context.getFormService();
		String formIdString = request.getParameter("formId");
		int formId = Integer.parseInt(formIdString);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			ConfigManagerUtil.deleteForm(formId, false); // CHICA-993 Updated to delete based on formId, also pass false so that LocationTagAttribute record is NOT deleted
			return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER));
		}
		
		ConceptService conceptService = Context.getConceptService();
		String replaceFormIdString = request.getParameter("replaceFormId");
		String formNameToEdit = "";
		try {
			Form formToEdit = formService.getForm(formId);
			formNameToEdit = formToEdit.getName();
			List<FormField> formFields = formToEdit.getOrderedFormFields();
			
			for (FormField currFormField : formFields) {
				Field currField = currFormField.getField();
				Integer fieldId = currField.getFieldId();
				String name = request.getParameter(AtdConstants.PARAMETER_NAME_PREFIX + fieldId);
				String fieldTypeId = request.getParameter(AtdConstants.PARAMETER_FIELD_TYPE_PREFIX + fieldId);
				String conceptName = request.getParameter(AtdConstants.PARAMETER_CONCEPT_PREFIX + fieldId);
				String defaultValue = request.getParameter(AtdConstants.PARAMETER_DEFAULT_VALUE_PREFIX + fieldId);
				String fieldNumber = request.getParameter(AtdConstants.PARAMETER_FIELD_NUMBER_PREFIX + fieldId);
				String parentFieldId = request.getParameter(AtdConstants.PARAMETER_PARENT_PREFIX + fieldId);
				
				if (name != null && name.length() > 0) {
					currField.setName(name);
				}
				try {
					if (fieldTypeId != null && fieldTypeId.length() > 0) {
						FieldType fieldType = formService.getFieldType(Integer.parseInt(fieldTypeId));
						currField.setFieldType(fieldType);
					} else {
						currField.setFieldType(null);
					}
				}
				catch (Exception e1) {
					log.error(e1.getMessage());
					log.error(Util.getStackTrace(e1));
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
					log.error(e.getMessage());
					log.error(Util.getStackTrace(e));
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
					log.error(e.getMessage());
					log.error(Util.getStackTrace(e));
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
					log.error(e.getMessage());
					log.error(Util.getStackTrace(e));
				}
				formService.saveFormField(currFormField);
				formService.saveField(currField);
			}
			LoggingUtil.logEvent(null, formId, null, LoggingConstants.EVENT_MODIFY_FORM_FIELDS, 
				Context.getUserContext().getAuthenticatedUser().getUserId(), 
				"Form fields modified.  Class: " + ReplaceFormFieldsController.class.getCanonicalName());
		}
		catch (Exception e) {
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		
		// DWE CHICA-332 4/16/15 
		// Determine the locations and location tags that the form was previously configured for
		// so that the configFormAttributeValue.form can be displayed
		ATDService atdService = Context.getService(ATDService.class);
		HashMap<Integer, List<Integer>> locAndTagIdsMap = atdService.getFormAttributeValueLocationsAndTagsMap(formId); // This is the new form Id
		
		// Now build the list of location ids and location tag ids separated by "_" 
		// as expected by the existing functionality in ConfigFormAttributeValueController
		ArrayList<String> locationIdsAndTagIdsList = new ArrayList<String>();
		Set<Integer> locationIds = locAndTagIdsMap.keySet();
		Iterator<Integer> it = locationIds.iterator();
		while(it.hasNext())
		{
			Integer locationId = it.next();
			List<Integer> tagIds = locAndTagIdsMap.get(locationId);
			if(tagIds != null)
			{
				for(Integer tagId : tagIds)
				{
					locationIdsAndTagIdsList.add(String.valueOf(locationId) + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + String.valueOf(tagId));
				}
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("formId", replaceFormIdString);
		map.put("newFormId", formIdString);
		map.put(AtdConstants.PARAMETER_POSITIONS, locationIdsAndTagIdsList.toArray(new String[0]));
		map.put("selectedFormName", formNameToEdit);
		map.put(AtdConstants.PARAMETER_SUCCESS_VIEW_NAME, "replaceRetireForm.form");
		
		
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_FORM_ATTRIBUTE_VALUE), map);
	}
	
	private List<Boolean> getNewFieldIndicators(List<FormField> formFields, Form origForm) {
		List<Boolean> newFieldIndicators = new ArrayList<Boolean>(formFields.size());
		Map<String,FormField> replaceFormFieldMap = mapFormFields(origForm);
		for (FormField formField : formFields) {
			Field field = formField.getField();
			String fieldName = field.getName();
			FormField foundField = replaceFormFieldMap.get(fieldName);
			if (foundField != null) {
				newFieldIndicators.add(new Boolean(false));
			} else {
				newFieldIndicators.add(new Boolean(true));
			}
		}
		
		return newFieldIndicators;
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
