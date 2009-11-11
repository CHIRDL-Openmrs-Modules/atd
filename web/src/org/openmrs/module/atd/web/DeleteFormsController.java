package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutil.util.Util;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class DeleteFormsController extends SimpleFormController
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
		String[] formsToDelete = request.getParameterValues("FormsToDelete");

		if (formsToDelete != null)
		{
			for (String currFormIdString : formsToDelete)
			{
				Integer currFormId = null;
				
				try
				{
					currFormId = Integer.parseInt(currFormIdString);
					Form currForm = formService.getForm(currFormId);
					
					//delete the form
					formService.purgeForm(currForm);
					
					//delete the orphaned fields
					for(FormField currFormField: currForm.getFormFields())
					{
						formService.purgeField(currFormField.getField());
					}
				} catch (Exception e)
				{
					this.log.error(e.getMessage());
					this.log.error(Util.getStackTrace(e));
				}
			}
			map.put("formsToDelete", formsToDelete);
		}
		
		map.put("forms", formService.getAllForms());

		return map;
	}

}
