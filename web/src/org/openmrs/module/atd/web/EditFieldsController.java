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
import org.openmrs.module.chirdlutil.util.Util;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class EditFieldsController extends SimpleFormController
{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception
	{
		return "testing";
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();

		FormService formService = Context.getFormService();

		String formIdString = request.getParameter("formToEdit");

		if (formIdString != null)
		{
			try
			{
				int formId = Integer.parseInt(formIdString);
				Form formToEdit = formService.getForm(formId);
				List<FormField> formFields = formToEdit.getOrderedFormFields();

				map.put("form", formToEdit);
				map.put("formFields", formFields);
				map.put("fieldTypes", formService.getAllFieldTypes());

			} catch (Exception e)
			{
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}
		}
		return map;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception
	{
		long timeInMilliseconds = 0;
		FormService formService = Context.getFormService();
		ConceptService conceptService = Context.getConceptService();
		String formIdString = request.getParameter("formToEdit");
		if (formIdString != null)
		{
			try
			{
				int formId = Integer.parseInt(formIdString);
				Form formToEdit = formService.getForm(formId);
				List<FormField> formFields = formToEdit.getOrderedFormFields();

				for (FormField currFormField : formFields)
				{
					Field currField = currFormField.getField();
					Integer fieldId = currField.getFieldId();
					String name = request.getParameter("name_" + fieldId);
					String fieldTypeId = request.getParameter("fieldType_"
							+ fieldId);
					String conceptName= request.getParameter("concept_"
							+ fieldId);
					String defaultValue = request.getParameter("defaultValue_"
							+ fieldId);
					String fieldNumber = request.getParameter("fieldNumber_"
							+ fieldId);
					String parentFieldId = request.getParameter("parent_"
							+ fieldId);

					if(name != null && name.length() >0)
					{
						currField.setName(name);
					}
					try
					{
						if(fieldTypeId != null && fieldTypeId.length() >0)
						{
							FieldType fieldType = formService.getFieldType(Integer
								.parseInt(fieldTypeId));
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
					try
					{
						if(conceptName != null && conceptName.length() >0)
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
					if(defaultValue != null && defaultValue.length() >0)
					{
						currField.setDefaultValue(defaultValue);
					}else
					{
						currField.setDefaultValue(null);
					}
					try
					{
						if(fieldNumber != null && fieldNumber.length() > 0)
						{
							currFormField.setFieldNumber(Integer
								.parseInt(fieldNumber));
						}
					} catch (Exception e)
					{
						this.log.error(e.getMessage());
						this.log.error(Util.getStackTrace(e));
					}

					try
					{
						if(parentFieldId != null && parentFieldId.length()>0)
						{
							FormField parentField = formService.getFormField(Integer
								.parseInt(parentFieldId));
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
					long startTime = System.currentTimeMillis();
					formService.saveFormField(currFormField);
					formService.saveField(currField);
					timeInMilliseconds+=(System.currentTimeMillis()-startTime);
				}
			} catch (Exception e)
			{
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
			}
		}
	
		String view = getSuccessView();
		return new ModelAndView(
				new RedirectView(view+"?formToEdit=" +formIdString));	}

}
