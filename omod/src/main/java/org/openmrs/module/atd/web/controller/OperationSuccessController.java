package org.openmrs.module.atd.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.atd.util.AtdConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author wang417
 * Controller for operationSuccess.form
 */
@Controller
@RequestMapping(value = "module/atd/operationSuccess.form")
public class OperationSuccessController {
    
    /** Form view */
    private static final String FORM_VIEW = "/module/atd/operationSuccess";

    /**
     * Redirects back to the Configuration Manager page.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER));
	}

    /**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		String operationType = request.getParameter(AtdConstants.PARAMETER_OPERATION_TYPE);
		map.put(AtdConstants.PARAMETER_OPERATION_TYPE, operationType);
		return FORM_VIEW;
	}
	
}
