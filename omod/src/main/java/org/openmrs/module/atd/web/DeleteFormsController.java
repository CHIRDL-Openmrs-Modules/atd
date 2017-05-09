package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
		map.put("forms", formService.getAllForms());

		return map;
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, 
	                                             BindException errors) throws Exception 
	{
		String[] formsToDelete = request.getParameterValues("FormsToDelete");

		if (formsToDelete != null)
		{
			for (String currFormIdString : formsToDelete)
			{
				Integer currFormId = null;
				
				try
				{
					currFormId = Integer.parseInt(currFormIdString);
					ConfigManagerUtil.deleteForm(currFormId);
					LoggingUtil.logEvent(null, currFormId, null, LoggingConstants.EVENT_DELETE_FORM, 
						Context.getAuthenticatedUser().getUserId(), 
						"Form Deleted.  Class: " + DeleteFormsController.class.getCanonicalName());
				} catch (Exception e)
				{
					this.log.error(e.getMessage());
					this.log.error(Util.getStackTrace(e));
				}
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("application", "Delete Forms");
		return new ModelAndView(new RedirectView(getSuccessView()), map);
	}
}
