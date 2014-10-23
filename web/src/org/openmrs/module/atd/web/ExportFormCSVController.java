package org.openmrs.module.atd.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.FormAttributeValueDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

@Scope("session")
public class ExportFormCSVController extends SimpleFormController {
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}
	
	
	
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		FormService fs = Context.getFormService();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		Map<String, Object> map = new HashMap<String, Object>();
		String purpose = request.getParameter("purpose");
		if(purpose.equals("showForm")){
			String formName = request.getParameter("formName");
			//map.put("checkedFormName", formName);
			request.setAttribute("checkedFormName", formName);
			List<FormAttributeValueDescriptor> favdList = new ArrayList<FormAttributeValueDescriptor>();
			if(formName==null || formName.equals("")){
				map.put("error", "formNameEmpty");
				return new ModelAndView(getFormView(), map);
			}
			Form form = fs.getForm(formName);
			if(form==null){
				map.put("error", "formNameNonexist");
				return new ModelAndView(getFormView(), map);
			}
			List<FormAttributeValue> favList = cubService.getAllFormAttributeValuesByFormId(form.getFormId());
			for(FormAttributeValue fav: favList){
				FormAttributeValueDescriptor favd = Util.getFormAttributeValue(fav);
				favdList.add(favd);
				
			}
			//map.put("favdList", favdList);
			request.setAttribute("favdList", favdList);
			return new ModelAndView(getFormView());
		}else{
			String checkedFormName = request.getParameter("checkedFormName");
			if(checkedFormName==null){
				map.put("error", "exportNotReady");
				return new ModelAndView(getFormView(), map);
			}
			String csvFileName = checkedFormName+".csv";
			response.setContentType("text/csv");
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
			response.setHeader(headerKey, headerValue);
			Form form = fs.getForm(checkedFormName);
			List<FormAttributeValue> favList = cubService.getAllFormAttributeValuesByFormId(form.getFormId());
			List<FormAttributeValueDescriptor> favdList = new ArrayList<FormAttributeValueDescriptor>();
			for(FormAttributeValue fav: favList){
				FormAttributeValueDescriptor favd = Util.getFormAttributeValue(fav);
				favdList.add(favd);
				
			}
			try{
				Util.exportFormAttributeValueAsCSV(response.getWriter(), favdList);
			}catch(IOException e){
				map.put("error", "serverError");
				return new ModelAndView(getFormView(), map);
			}
			return new ModelAndView(new RedirectView(getSuccessView()), map);
		}
		
	}
	
}
