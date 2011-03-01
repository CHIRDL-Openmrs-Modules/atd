package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


public class MLMFormController extends SimpleFormController {
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception{
		return "testing";
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		AdministrationService adminService = Context.getAdministrationService();
		String mlmDir = adminService.getGlobalProperty("dss.mlmRuleDirectory");
		map.put("mlmDir", mlmDir);
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		String view = getSuccessView();
		return new ModelAndView(new RedirectView(view));
	}
}
