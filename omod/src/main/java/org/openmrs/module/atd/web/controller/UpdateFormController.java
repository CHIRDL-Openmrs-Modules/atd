package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/updateForm.form")
public class UpdateFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/updateForm";
    
    /** Success view */
    private static final String SUCCESS_VIEW = "updateFieldsForm.form";

    /**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put(AtdConstants.PARAMETER_FORMS, forms);
		
		return FORM_VIEW;
	}

    /**
     * Handles submission of the page.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<>();
		String formId = request.getParameter(AtdConstants.PARAMETER_FORM_TO_EDIT);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, formId);
	
		return new ModelAndView(new RedirectView(SUCCESS_VIEW), map);
	}
}
