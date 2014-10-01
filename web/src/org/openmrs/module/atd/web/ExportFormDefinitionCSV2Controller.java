package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ExportFormDefinitionCSV2Controller extends SimpleFormController {
	String formName =null;
	List<FormDefinitionDescriptor> fddList;
	
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
			formName = request.getParameter("formName");
			if(formName==null || formName.equals("")){
				map.put("error", "formNameEmpty");
				return new ModelAndView(getFormView(), map);
			}
			Form form = fs.getForm(formName);
			if(form==null){
				map.put("error", "formNameNonexist");
				return new ModelAndView(getFormView(), map);
			}
			ATDService atdService = Context.getService(ATDService.class);
			fddList = atdService.getFormDefinitionAsDescriptor(form.getFormId());
			request.setAttribute("fddList", fddList);
			return new ModelAndView(getFormView());
		}else if(purpose.equals("showAllForms")){
			ATDService atdService = Context.getService(ATDService.class);
			fddList = atdService.getAllFormDefinitionAsDescriptor();
			request.setAttribute("fddList", fddList);
			return new ModelAndView(getFormView());
		}else{
			if(fddList==null){
				map.put("error", "NoFormChosen");
				return new ModelAndView(getFormView(), map);
			}
			String csvFileName = "form definitions.csv";
			response.setContentType("text/csv");
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
			response.setHeader(headerKey, headerValue);
			Util.exportAllFormDefinitionCSV(response.getWriter(), fddList);
			return new ModelAndView(new RedirectView(getSuccessView()), map);
		}
	}
	
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		fddList = null;
		return super.referenceData(request);
	}
	
}
