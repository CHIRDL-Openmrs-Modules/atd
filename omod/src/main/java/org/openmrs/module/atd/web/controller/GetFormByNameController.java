package org.openmrs.module.atd.web.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author wang417
 * Controller for getFormByName.form
 */
@Controller
@RequestMapping(value = "module/atd/getFormByName.form")
public class GetFormByNameController {
    
    /** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(GetFormByNameController.class);
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/getFormByName";

    /** Options */
	protected static final String CHOOSE_FORMS_OPTION = "-1";
	
	/** Parameters */
	private static final String PARAMETER_IO_ERROR = "ioError";
	private static final String PARAMETER_CSV_FILE_ERROR = "csvFileError";
	private static final String PARAMETER_NO_SUCH_NAME = "noSuchName";
	private static final String PARAMETER_SELECTED_OPTION = "selectedOption";
    private static final String PARAMETER_TYPE_NOT_CHOSEN = "typeNotChosen";
    private static final String PARAMETER_EDIT_TYPE = "editType";
    
    /** Error types */
    private static final String ERROR_NOT_MULTIPART = "notMultipart";
    private static final String ERROR_CSV_FILE_EMPTY = "csvFileEmpty";
    private static final String ERROR_NOT_FAV = "notFAV";
    private static final String ERROR_TYPE = "typeError";
    
    /** Operation type */
    private static final String OPERATION_TYPE_IMPORT_FORM_ATTRIBUTE_VALUES = "Import form attribute values";
    
    /** CSV file */
    private static final String CSV_FILE = "csvFile";
    
    /** Edit types */
    private static final String EDIT_TYPE_MANUAL = "manual";
	
	/**
     * Handles submission of the page.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		String editType = request.getParameter(PARAMETER_EDIT_TYPE);
		Map<String, Object> map = new HashMap<>();
		
		// DWE CHICA-280 4/1/15 Made a few minor changes to this method, mainly to handle the formNameSelect drop-down
		// Also cleaned up some of the code to keep the scope of the variables so they are only available if needed
		if(StringUtils.isBlank(editType)){
			map.put(PARAMETER_TYPE_NOT_CHOSEN, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			return new ModelAndView(FORM_VIEW, map);
		}
		map.put(PARAMETER_SELECTED_OPTION, editType);
		if(EDIT_TYPE_MANUAL.equals(editType)){
			return handleManualEditType(request, map);
		} else if(request instanceof MultipartHttpServletRequest){
			return handleCSVFile(request, map);
		}
			
		map.put(PARAMETER_CSV_FILE_ERROR, ERROR_NOT_MULTIPART);
		reloadValues(request, map);
		return new ModelAndView(FORM_VIEW, map);
	}
	
    /**
     * DWE CHICA-331 4/10/15
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
		request.setAttribute(PARAMETER_SELECTED_OPTION, EDIT_TYPE_MANUAL);
		
		reloadValues(request, map);
		
		return FORM_VIEW;
	}
	
	/**
	 * DWE CHICA-331 4/10/15
	 * 
	 * Currently only used to reload the values for the "Form name" drop-down
	 */
	private void reloadValues(HttpServletRequest request, Map<String, Object> map)
	{
		// Reload the values for the "Form name" drop-down
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put(AtdConstants.PARAMETER_FORMS, forms);
		
		request.setAttribute(AtdConstants.ATTRIBUTE_CHOOSE_FORM_OPTION_CONSTANT, CHOOSE_FORMS_OPTION);
	}
	
	/**
	 * Handles the scenario where the user is editing in manual mode.
	 * 
	 * @param request The HTTP request information
	 * @param map Map containing information sent back to the client
	 * @return ModelAndView the view to display
	 */
	private ModelAndView handleManualEditType(HttpServletRequest request, Map<String, Object> map) 
	{
	    String selectedFormId = request.getParameter(AtdConstants.PARAMETER_FORM_NAME_SELECT);
        if(selectedFormId != null)
        {
            try
            {
                Integer formId = Integer.valueOf(selectedFormId);
                
                // Verify that this is a valid formId
                FormService fs =Context.getFormService();
                Form form = fs.getForm(formId);
                
                if(form != null)
                {
                    map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, form.getId());
                    map.put(AtdConstants.PARAMETER_SELECTED_FORM_NAME, form.getName());
                    return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CHOOSE_LOCATION_FORM),map);
                }
                
                map.put(PARAMETER_NO_SUCH_NAME, ChirdlUtilConstants.GENERAL_INFO_TRUE);
                reloadValues(request, map);
                return new ModelAndView(FORM_VIEW, map);
                
            }
            catch(Exception e)
            {
                log.error("Error in processFormSubmission().", e);
                reloadValues(request, map);
                return new ModelAndView(FORM_VIEW, map);
            }     
        }
        
        reloadValues(request, map);
        return new ModelAndView(FORM_VIEW, map);
	}
	
	/**
	 * Handles the processing of the uploaded CSV file.
	 * 
	 * @param request The HTTP request information
     * @param map Map containing information sent back to the client
     * @return ModelAndView the view to display
	 */
	private ModelAndView handleCSVFile(HttpServletRequest request, Map<String, Object> map) {
	    ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
        List<FormAttributeValueDescriptor> favdList=null;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile csvFile = multipartRequest.getFile(CSV_FILE);
        if (csvFile == null || csvFile.isEmpty()) {
            map.put(PARAMETER_CSV_FILE_ERROR, ERROR_CSV_FILE_EMPTY);
            reloadValues(request, map);
            return new ModelAndView(FORM_VIEW, map);
        }
        
        String filename = csvFile.getOriginalFilename();
        int index = filename.lastIndexOf(ChirdlUtilConstants.GENERAL_INFO_PERIOD);
        if (index < 0) {
            map.put(PARAMETER_CSV_FILE_ERROR, ERROR_TYPE);
            reloadValues(request, map);
            return new ModelAndView(FORM_VIEW, map);
        }
        String extension = filename.substring(index, filename.length());
        if (!extension.equalsIgnoreCase(ChirdlUtilConstants.FILE_EXTENSION_CSV)) {
            map.put(PARAMETER_CSV_FILE_ERROR, ERROR_TYPE);
            reloadValues(request, map);
            return new ModelAndView(FORM_VIEW, map);
        }
        try (InputStream input = csvFile.getInputStream()){
            favdList = Util.getFormAttributeValueDescriptorFromCSV(input);
        }catch(Exception e){
            log.error("Error retrieving form attribute values from CSV file", e);
            map.put(PARAMETER_IO_ERROR, ChirdlUtilConstants.GENERAL_INFO_TRUE);
            reloadValues(request, map);
            return new ModelAndView(FORM_VIEW, map);
        }
        
        List<FormAttributeValue>  favList = Util.getFormAttributeValues(favdList);
        if(!favdList.isEmpty() && favList.isEmpty()){
            map.put(PARAMETER_CSV_FILE_ERROR, ERROR_NOT_FAV);
            reloadValues(request, map);
            return new ModelAndView(FORM_VIEW, map);
        }
        if(favList!=null){
            for(FormAttributeValue fav: favList){
                cubService.saveFormAttributeValue(fav);
            }
        }
        map.put(AtdConstants.PARAMETER_OPERATION_TYPE, OPERATION_TYPE_IMPORT_FORM_ATTRIBUTE_VALUES);
        return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_OPERATION_SUCCESS),map);
	}
}
