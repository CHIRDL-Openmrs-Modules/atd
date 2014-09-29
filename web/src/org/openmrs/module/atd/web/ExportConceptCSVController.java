package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.Util;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ExportConceptCSVController extends SimpleFormController {
	private List<ConceptDescriptor> cdList;
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String csvFileName = "concepts.csv";
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
		response.setHeader(headerKey, headerValue);
		Util.exportAllConceptsAsCSV(response.getWriter(), cdList);
		return new ModelAndView(new RedirectView(getSuccessView()), map);
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		ATDService atdService = Context.getService(ATDService.class);
		cdList = atdService.getAllConceptsAsDescriptor();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cdList", cdList);
		return map;
	}
	
}

