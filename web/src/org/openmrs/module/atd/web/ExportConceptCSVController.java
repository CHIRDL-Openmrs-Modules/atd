package org.openmrs.module.atd.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.Util;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
/**
 * 
 * @author wang417
 * For the page exportConceptCSV.form, exporting concept information as csv files.
 */
public class ExportConceptCSVController extends SimpleFormController {
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
		List<ConceptDescriptor> cdList=null;
		try{
		Util.exportAllConceptsAsCSV(response.getWriter(), cdList);
		}
		catch(IOException e){
			map.put("error", "serverError");
			return new ModelAndView(getFormView(), map);
		}
		map.put("operationType", "export concept as csv file");
		return new ModelAndView(new RedirectView(getSuccessView()), map);
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		try{
		ATDService atdService = Context.getService(ATDService.class);
		List<ConceptDescriptor> cdList = atdService.getAllConcepts();
		map.put("cdList", cdList);
		}catch(SQLException e){
			map.put("error", "serverError");
		}
		return map;
	}
	
}

