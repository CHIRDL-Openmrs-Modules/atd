package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.atd.util.Util;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * 
 * @author wang417
 * For the page exportFormDefinitionCSV.form, exporting form definition information as csv files.
 */
public class ExportFormDefinitionCSVController extends SimpleFormController {

	protected final static String CHOOSE_FORMS_OPTION = "-1";
	protected final static String ALL_FORMS_OPTION = "-999";
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		// DWE CHICA-280 4/1/15 Reworked most of this method, but also reused a lot of the existing logic
		String view = getFormView();
		String selectedFormId = request.getParameter("formNameSelect");
		if(selectedFormId != null)
		{
			FormService fs = Context.getFormService();
			request.setAttribute("selectedFormId", selectedFormId);
			
			try
			{
				int formId = Integer.parseInt(selectedFormId); 
				
				List<FormDefinitionDescriptor> fddList = new ArrayList<FormDefinitionDescriptor>();
				ATDService atdService = Context.getService(ATDService.class);
				
				if(formId > -1) // Not showing all form definitions
				{
					Form form = fs.getForm(formId);
					
					fddList = atdService.getFormDefinition(form.getFormId());
				}
				else
				{
					// Show all form definitions
					// Using -999 as the id for the "- All Forms -" option
					fddList = atdService.getAllFormDefinitions();
				}
				
				request.setAttribute("fddList", fddList);
				
				if(request.getParameter("exportToCSV") != null) // "Export to CSV" button
				{
					response.setContentType("text/csv");
					response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", "formDefinitions.csv"));
					
					// Export the list that was populated above, 
					// which will contain either a selected form or all form definitions
					Util.exportAllFormDefinitionCSV(response.getWriter(), fddList);
					map.put("operationType", "export form definition as csv file");
					
					return new ModelAndView(view, map);
				}
			}
			catch(Exception e)
			{
				log.error("Error in processFormSubmission().", e);
				reloadValues(request, map);
				return new ModelAndView(view, map);
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
	 * DWE CHICA-280 4/1/15
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		request.setAttribute("selectedFormId", CHOOSE_FORMS_OPTION);
		
		reloadValues(request, map);
		
		return map;
	}
	
	/**
	 * DWE CHICA-280 4/1/15
	 * 
	 * Currently only used to reload the values for the "Form name" drop-down
	 */
	private void reloadValues(HttpServletRequest request, Map<String, Object> map)
	{
		// Reload the values for the "Form name" drop-down
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		forms.add(0, new Form()); // Add a place holder for the "- All Forms -" option
		
		map.put("forms", forms);
		
		request.setAttribute("chooseFormOptionConstant", CHOOSE_FORMS_OPTION);
		request.setAttribute("allFormsOptionConstant", ALL_FORMS_OPTION);
	}
}
