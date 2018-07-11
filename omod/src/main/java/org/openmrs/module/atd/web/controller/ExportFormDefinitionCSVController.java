package org.openmrs.module.atd.web.controller;

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
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author wang417
 * For the page exportFormDefinitionCSV.form, exporting form definition information as csv files.
 */
@Controller
@RequestMapping(value = "module/atd/exportFormDefinitionCSV.form")
public class ExportFormDefinitionCSVController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/exportFormDefinitionCSV";
    
    /** Attributes */
    private static final String ATTRIBUTE_FDD_LIST = "fddList";
    private static final String ATTRIBUTES_ALL_FORMS_OPTION_CONSTANT = "allFormsOptionConstant";
    
    /** CSV filename */
    private static final String FORM_DEFINITIONS_CSV_FILENAME = "formDefinitions.csv";
    
    /** Operation type */
    private static final String OPERATION_TYPE_EXPORT_FORM_DEFINITION = "export form definition as csv file";
    
    /** Options */
    protected static final String CHOOSE_FORMS_OPTION = "-1";
    protected static final String ALL_FORMS_OPTION = "-999";

	/**
     * Handles submission of the page.
     * 
     * @param request The HTTP request information
     * @param response The HTTP response object
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		
		// DWE CHICA-280 4/1/15 Reworked most of this method, but also reused a lot of the existing logic
		String selectedFormId = request.getParameter(AtdConstants.PARAMETER_FORM_NAME_SELECT);
		if(selectedFormId != null)
		{
			FormService fs = Context.getFormService();
			request.setAttribute(AtdConstants.ATTRIBUTE_SELECTED_FORM_ID, selectedFormId);
			
			try
			{
				Integer formId = Integer.valueOf(selectedFormId); 
				
				List<FormDefinitionDescriptor> fddList = null;
				ATDService atdService = Context.getService(ATDService.class);
				
				if(formId.intValue() > -1) // Not showing all form definitions
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
				
				request.setAttribute(ATTRIBUTE_FDD_LIST, fddList);
				
				if(request.getParameter(AtdConstants.PARAMETER_EXPORT_TO_CSV) != null) // "Export to CSV" button
				{
					response.setContentType(ChirdlUtilConstants.HTTP_CONTENT_TYPE_CSV);
					response.setHeader(ChirdlUtilConstants.HTTP_HEADER_CONTENT_DISPOSITION, 
					    String.format(ChirdlUtilConstants.HTTP_HEADER_ATTACHMENT, FORM_DEFINITIONS_CSV_FILENAME));
					
					// Export the list that was populated above, 
					// which will contain either a selected form or all form definitions
					Util.exportAllFormDefinitionCSV(response.getWriter(), fddList);
					map.put(AtdConstants.PARAMETER_OPERATION_TYPE, OPERATION_TYPE_EXPORT_FORM_DEFINITION);
					
					return new ModelAndView(FORM_VIEW, map);
				}
			}
			catch(Exception e)
			{
				log.error("Error in processFormSubmission().", e);
				reloadValues(request, map);
				return new ModelAndView(FORM_VIEW, map);
			}
			
			reloadValues(request, map);
			return new ModelAndView(FORM_VIEW, map);
		}
			
		reloadValues(request, map);
		return new ModelAndView(FORM_VIEW, map);
	}

    /**
     * DWE CHICA-280 4/1/15
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map)
	{
		request.setAttribute(AtdConstants.ATTRIBUTE_SELECTED_FORM_ID, CHOOSE_FORMS_OPTION);
		
		reloadValues(request, map);
		
		return FORM_VIEW;
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
		
		map.put(AtdConstants.PARAMETER_FORMS, forms);
		
		request.setAttribute(AtdConstants.ATTRIBUTE_CHOOSE_FORM_OPTION_CONSTANT, CHOOSE_FORMS_OPTION);
		request.setAttribute(ATTRIBUTES_ALL_FORMS_OPTION_CONSTANT, ALL_FORMS_OPTION);
	}
}
