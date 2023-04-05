package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = "module/atd/deleteForms.form")
public class DeleteFormsController
{

	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(DeleteFormsController.class);

	/** Form view name */
	private static final String FORM_VIEW = "/module/atd/deleteForms";

	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(ModelMap modelMap)
	{
		FormService formService = Context.getFormService();
		modelMap.put("forms", formService.getAllForms());
		
		return FORM_VIEW;
	}

	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object)
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
					ConfigManagerUtil.deleteForm(currFormId, true); // CHICA-993 Updated to delete based on formId, also pass true to delete LocationTagAttribute record
					LoggingUtil.logEvent(null, currFormId, null, LoggingConstants.EVENT_DELETE_FORM, 
						Context.getAuthenticatedUser().getUserId(), 
						"Form Deleted.  Class: " + DeleteFormsController.class.getCanonicalName());
				} catch (Exception e)
				{
					log.error(e.getMessage());
					log.error(Util.getStackTrace(e));
				}
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("application", "Delete Forms");
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
}
