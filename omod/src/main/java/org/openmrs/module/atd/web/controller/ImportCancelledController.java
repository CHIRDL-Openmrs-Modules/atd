package org.openmrs.module.atd.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.module.atd.util.AtdConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/cancelledImport.form")
public class ImportCancelledController{
	
	/** Form view name */
	private static final String FORM_VIEW = "/module/atd/cancelledImport";
	
	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		String application = request.getParameter("application");
		map.put("application", application);
		return FORM_VIEW;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER));
	}
}
