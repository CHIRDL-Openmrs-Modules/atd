package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class GetFormByNameController extends SimpleFormController {
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, 
	                                             BindException errors) throws Exception {
		String formName = request.getParameter("formName");
		Map<String, Object> map = new HashMap<String, Object>();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		FormService fs =Context.getFormService();
		Form form = fs.getForm(formName);
		if(form==null){
			map.put("noSuchName", "true");
			String view = this.getFormView();
			return new ModelAndView(view, map);
		}
		map.put("formId", form.getId());
		String view = getSuccessView();
		return new ModelAndView(
				new RedirectView(view), map);
	}

	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		return map;
	}
	
	
	
}
