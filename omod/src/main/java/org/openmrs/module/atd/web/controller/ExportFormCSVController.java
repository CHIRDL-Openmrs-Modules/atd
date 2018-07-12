package org.openmrs.module.atd.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.util.FormAttributeValueDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author wang417
 * Controller for exportFormCSV.form
 */
@Controller
@RequestMapping(value = "module/atd/exportFormCSV.form")
public class ExportFormCSVController {
    
    /** Form view */
    private static final String FORM_VIEW = "/module/atd/exportFormCSV";
    
    /** Attributes */
    private static final String ATTRIBUTE_FAVD_LIST = "favdList";
    private static final String ATTRIBUTE_SELECTED_ID_MAP = "selectedIdMap";
    
    /** CSV Filename */
    private static final String FORM_ATTRIBUTES_CSV_FILENAME = "formAttributes.csv";
	
    /**
     * Handles submission of the page.
     * 
     * @param request The HTTP request information
     * @param response The HTTP response object
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response) {
		
		// DWE CHICA-334 3/31/15 Reworked most of this method, but also reused a lot of the existing logic
		Map<String, Object> map = new HashMap<>();
		
		// Allow multiple selection
		String[] selectedFormIdsString = request.getParameterValues(AtdConstants.PARAMETER_FORM_NAME_SELECT); 
		Map<Integer, Boolean> selectedIdMap = new HashMap<>();
		
		if(selectedFormIdsString != null) 
		{
			FormService fs = Context.getFormService();
			ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
			List<FormAttributeValueDescriptor> favdList = new ArrayList<>();
			
			for(String selectedFormId : selectedFormIdsString)
			{
				try
				{
					// Build a list of FormAttributeValueDescriptor to display in the table
					Form form = fs.getForm(Integer.valueOf(selectedFormId));
					
					// Add to the selected Ids map
					selectedIdMap.put(form.getFormId(), Boolean.TRUE); 
					
					List<FormAttributeValue> favList = cubService.getAllFormAttributeValuesByFormId(form.getFormId());
					for(FormAttributeValue fav: favList)
					{
						FormAttributeValueDescriptor favd = Util.getFormAttributeValue(fav);
						favdList.add(favd);	
					}
				}
				catch(NumberFormatException nfe)
				{
				    Log.error("Error formatting selected form ID: " + selectedFormId, nfe);
					reloadValues(map);
					return new ModelAndView(FORM_VIEW, map);
				}
			}
			
			request.setAttribute(ATTRIBUTE_SELECTED_ID_MAP, selectedIdMap);
			request.setAttribute(ATTRIBUTE_FAVD_LIST, favdList); 

			if(request.getParameter(AtdConstants.PARAMETER_EXPORT_TO_CSV) != null) // "Export to CSV" button
			{
				String csvFileName = FORM_ATTRIBUTES_CSV_FILENAME;
				response.setContentType(ChirdlUtilConstants.HTTP_CONTENT_TYPE_CSV);
				String headerKey = ChirdlUtilConstants.HTTP_HEADER_CONTENT_DISPOSITION;
				String headerValue = String.format(ChirdlUtilConstants.HTTP_HEADER_ATTACHMENT, csvFileName);
				response.setHeader(headerKey, headerValue);

				try
				{
					Util.exportFormAttributeValueAsCSV(response.getWriter(), favdList);
				}
				catch(IOException e)
				{
				    Log.error("Error exporting form attribute values", e);
					map.put(AtdConstants.PARAMETER_ERROR, AtdConstants.ERROR_TYPE_SERVER);
					reloadValues(map);
					return new ModelAndView(FORM_VIEW, map);
				}
			}
			
			reloadValues(map);
			return new ModelAndView(FORM_VIEW, map);
		}
			
		reloadValues(map);
		return new ModelAndView(FORM_VIEW, map);
	}
	
    /**
     * DWE CHICA-334 3/31/15
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map)
	{
		Map<String, String> selectedIdMap = new HashMap<>();
		request.setAttribute(ATTRIBUTE_SELECTED_ID_MAP, selectedIdMap);
		
		reloadValues(map);
		
		return FORM_VIEW;
	}
	
	/**
	 * DWE CHICA-334 3/31/15
	 * 
	 * Currently only used to reload the values for the "Form name" drop-down
	 */
	private void reloadValues(Map<String, Object> map)
	{
		// Reload the values for the "Form name" drop-down
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put(AtdConstants.PARAMETER_FORMS, forms);
	}
}
