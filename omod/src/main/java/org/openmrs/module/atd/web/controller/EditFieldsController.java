package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
@RequestMapping(value = "module/atd/editFields.form")
public class EditFieldsController
{

    /** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(EditFieldsController.class);
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/editFields";
    
    /** Application name */
    private static final String APPLICATION_EDIT_FORM_FIELDS = "Edit Form Fields";

	/**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map)
	{
		FormService formService = Context.getFormService();

		String formIdString = request.getParameter(AtdConstants.PARAMETER_FORM_TO_EDIT);

		if (formIdString != null)
		{
			try
			{
				Integer formId = Integer.valueOf(formIdString);
				Form formToEdit = formService.getForm(formId);
				List<FormField> formFields = formToEdit.getOrderedFormFields();

				map.put(ChirdlUtilConstants.PARAMETER_FORM, formToEdit);
				map.put(AtdConstants.PARAMETER_FORM_FIELDS, formFields);
				map.put(AtdConstants.PARAMETER_FIELD_TYPES, formService.getAllFieldTypes());

			} catch (Exception e)
			{
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}
		}
		return FORM_VIEW;
	}

    /**
     * Handles submission of the page.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request)
	{
		FormService formService = Context.getFormService();
		ConceptService conceptService = Context.getConceptService();
		String formIdString = request.getParameter(AtdConstants.PARAMETER_FORM_TO_EDIT);
		if (formIdString != null)
		{
			try
			{
				Integer formId = Integer.valueOf(formIdString);
				Form formToEdit = formService.getForm(formId);
				List<FormField> formFields = formToEdit.getOrderedFormFields();

				for (FormField currFormField : formFields)
				{
					Field currField = currFormField.getField();
					Integer fieldId = currField.getFieldId();
					String name = request.getParameter(AtdConstants.PARAMETER_NAME_PREFIX + fieldId);
					String fieldTypeId = request.getParameter(AtdConstants.PARAMETER_FIELD_TYPE_PREFIX + fieldId);
					String conceptName= request.getParameter(AtdConstants.PARAMETER_CONCEPT_PREFIX + fieldId);
					String defaultValue = request.getParameter(AtdConstants.PARAMETER_DEFAULT_VALUE_PREFIX + fieldId);
					String fieldNumber = request.getParameter(AtdConstants.PARAMETER_FIELD_NUMBER_PREFIX + fieldId);
					String parentFieldId = request.getParameter(AtdConstants.PARAMETER_PARENT_PREFIX + fieldId);

					if(name != null && name.length() >0)
					{
						currField.setName(name);
					}
					setFieldType(formService, fieldTypeId, currField);
					setConcept(conceptService, conceptName, currField);
					if(defaultValue != null && defaultValue.length() >0)
					{
						currField.setDefaultValue(defaultValue);
					}else
					{
						currField.setDefaultValue(null);
					}
					setFieldNumber(fieldNumber, currFormField);

					setParentField(formService, parentFieldId, currFormField);
					formService.saveFormField(currFormField);
					formService.saveField(currField);
				}
				
				LoggingUtil.logEvent(null, formId, null, LoggingConstants.EVENT_MODIFY_FORM_FIELDS, 
					Context.getUserContext().getAuthenticatedUser().getUserId(), 
					"Form fields modified.  Class: " + EditFieldsController.class.getCanonicalName());
			} catch (Exception e)
			{
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}
		}
	
		Map<String, Object> map = new HashMap<>();
		map.put(AtdConstants.PARAMETER_APPLICATION, APPLICATION_EDIT_FORM_FIELDS);
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);	
	}

    /**
     * Set the field type of a field.
     * 
     * @param formService The FormService object used to retrieve field type information
     * @param fieldTypeId The ID of the field type.  This can be null if the field should not have a field type.
     * @param currField The field to set the field type
     */
    private void setFieldType(FormService formService, String fieldTypeId, Field currField) {
        try
        {
            if(StringUtils.isNotBlank(fieldTypeId))
            {
                FieldType fieldType = formService.getFieldType(Integer.valueOf(fieldTypeId));
                currField.setFieldType(fieldType);
            }else
            {
                currField.setFieldType(null);
            }
        } catch (Exception e1)
        {
            this.log.error(e1.getMessage());
            this.log.error(Util.getStackTrace(e1));
        }
    }
    
    /**
     * Sets the concept for a field.
     * 
     * @param conceptService ConceptService used to retrieve concept information
     * @param conceptName The name of the concept.  This can be null if no concept is to be associated to the field.
     * @param currField The field to set the concept
     */
    private void setConcept(ConceptService conceptService, String conceptName, Field currField) {
        try
        {
            if(StringUtils.isNotBlank(conceptName))
            {
                Concept concept = conceptService.getConcept(conceptName);
                currField.setConcept(concept);
            }else
            {
                currField.setConcept(null);
            }
        } catch (Exception e)
        {
            this.log.error(e.getMessage());
            this.log.error(Util.getStackTrace(e));
        }
    }
    
    /**
     * Sets the field number for a field.
     * 
     * @param fieldNumber The field number to set
     * @param currFormField The form field to set to the field number
     */
    private void setFieldNumber(String fieldNumber, FormField currFormField) {
        try
        {
            if(StringUtils.isNotBlank(fieldNumber))
            {
                currFormField.setFieldNumber(Integer.valueOf(fieldNumber));
            }
        } catch (Exception e)
        {
            this.log.error(e.getMessage());
            this.log.error(Util.getStackTrace(e));
        }
    }
    
    /**
     * Sets a field's parent field.
     * 
     * @param formService FormService used to retrieve the parent field
     * @param parentFieldId The ID of the parent field
     * @param currFormField The field that will have its parent field set
     */
    private void setParentField(FormService formService, String parentFieldId, FormField currFormField) {
        try
        {
            if(StringUtils.isNotBlank(parentFieldId))
            {
                FormField parentField = formService.getFormField(Integer.valueOf(parentFieldId));
                currFormField.setParent(parentField);
            }else
            {
                currFormField.setParent(null);
            }
        } catch (Exception e)
        {
            this.log.error(e.getMessage());
            this.log.error(Util.getStackTrace(e));
        }
    }
}
