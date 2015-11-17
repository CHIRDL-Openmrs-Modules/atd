package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author wang417
 * Controller for operationSuccess.form
 */
public class OperationSuccessController extends SimpleFormController {
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		return new ModelAndView(new RedirectView(getSuccessView()));
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String operationType = request.getParameter("operationType");
		map.put("operationType", operationType);
		return map;
	}
	
}
