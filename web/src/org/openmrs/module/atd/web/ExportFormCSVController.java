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
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * 
 * @author wang417
 * Controller for exportFormCSV.form
 */
public class ExportFormCSVController extends SimpleFormController {
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		
		// DWE CHICA-334 3/31/15 Reworked most of this method, but also reused a lot of the existing logic
		Map<String, Object> map = new HashMap<String, Object>();
		String view = getFormView();
		
		// Allow multiple selection
		String[] selectedFormIdsString = request.getParameterValues("formNameSelect"); 
		Map<Integer, Boolean> selectedIdMap = new HashMap<Integer, Boolean>();
		
		if(selectedFormIdsString != null) 
		{
			FormService fs = Context.getFormService();
			ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
			List<FormAttributeValueDescriptor> favdList = new ArrayList<FormAttributeValueDescriptor>();
			
			for(String selectedFormId : selectedFormIdsString)
			{
				try
				{
					// Build a list of FormAttributeValueDescriptor to display in the table
					Form form = fs.getForm(Integer.parseInt(selectedFormId));
					
					selectedIdMap.put(form.getFormId(), true); // Add to the selected Ids map
					
					List<FormAttributeValue> favList = cubService.getAllFormAttributeValuesByFormId(form.getFormId());
					for(FormAttributeValue fav: favList)
					{
						FormAttributeValueDescriptor favd = Util.getFormAttributeValue(fav);
						favdList.add(favd);	
					}
				}
				catch(NumberFormatException nfe)
				{
					reloadValues(request, map);
					return new ModelAndView(view, map);
				}
			}
			
			request.setAttribute("selectedIdMap", selectedIdMap);
			request.setAttribute("favdList", favdList); 

			if(request.getParameter("exportToCSV") != null) // "Export to CSV" button
			{
				String csvFileName = "formAttributes.csv";
				response.setContentType("text/csv");
				String headerKey = "Content-Disposition";
				String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
				response.setHeader(headerKey, headerValue);

				try
				{
					Util.exportFormAttributeValueAsCSV(response.getWriter(), favdList);
				}
				catch(IOException e)
				{
					map.put("error", "serverError");
					reloadValues(request, map);
					return new ModelAndView(view, map);
				}
			}
			
			reloadValues(request, map);
			return new ModelAndView(view, map);
		}
		else
		{
			reloadValues(request, map);
			return new ModelAndView(view, map);
		}
	}
	
	/**
	 * DWE CHICA-334 3/31/15
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
	
		Map<String, String> selectedIdMap = new HashMap<String, String>();
		request.setAttribute("selectedIdMap", selectedIdMap);
		
		reloadValues(request, map);
		
		return map;
	}
	
	/**
	 * DWE CHICA-334 3/31/15
	 * 
	 * Currently only used to reload the values for the "Form name" drop-down
	 */
	private void reloadValues(HttpServletRequest request, Map<String, Object> map)
	{
		// Reload the values for the "Form name" drop-down
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put("forms", forms);
	}
}
