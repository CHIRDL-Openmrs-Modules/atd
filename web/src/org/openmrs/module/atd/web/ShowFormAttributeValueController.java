package org.openmrs.module.atd.web;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.FormAttributeValueDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;

public class ShowFormAttributeValueController extends SimpleFormController {
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing"; 
	}
	
	
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Object obj = request.getSession().getAttribute("favdList");
		List<FormAttributeValueDescriptor>  favdList = (List<FormAttributeValueDescriptor>)obj;
		List<FormAttributeValue>  favList = Util.getFormAttributeValues(favdList);
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		if(favList!=null){
			for(FormAttributeValue fav: favList){
				cubService.saveFormAttributeValue(fav);
			}
		}
		String view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
	}



	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		Object obj = request.getSession().getAttribute("favdList");
		List<FormAttributeValueDescriptor>  favdList = (List<FormAttributeValueDescriptor>)obj;
		map.put("favdList", favdList);
		return map;
	}
	
	
}
