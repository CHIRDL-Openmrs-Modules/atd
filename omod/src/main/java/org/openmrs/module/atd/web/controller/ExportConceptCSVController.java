package org.openmrs.module.atd.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.util.Log;
import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
/**
 * 
 * @author wang417
 * For the page exportConceptCSV.form, exporting concept information as csv files.
 */
@Controller
@RequestMapping(value = "module/atd/exportConceptCSV.form")
public class ExportConceptCSVController {
    
    /** Form view */
    private static final String FORM_VIEW = "/module/atd/exportConceptCSV";
    
    /** Operation types */
    private static final String OPERATION_TYPE_EXPORT_CONCEPT_AS_CSV_FILE = "export concept as csv file";
    
    /** Attributes */
    private static final String ATTRIBUTE_ALL_CONCEPT_CLASSES_OPTION_CONSTANT = "allConceptClassesOptionConstant";
    
    /** Parameters */
    private static final String PARAMETER_CONCEPT_CLASS_LIST = "conceptClassList";
    private static final String PARAMETER_EXPORT_ALL = "exportAll";
    private static final String PARAMETER_SELECTED_IDS = "selectedIdsField";
    
    /** Sorting */
    private static final String SORT_ASC = "ASC";
    
    /** Concepts Filename */
	private static final String CONCEPTS_FILENAME = "concepts.csv";
	
	protected static final String ALL_CONCEPT_CLASSES_OPTION = "-1";
	
	/**
     * Handles submission of the page.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<>();
		response.setContentType(ChirdlUtilConstants.HTTP_CONTENT_TYPE_CSV);
		response.setHeader(ChirdlUtilConstants.HTTP_HEADER_CONTENT_DISPOSITION, 
		    String.format(ChirdlUtilConstants.HTTP_HEADER_ATTACHMENT, CONCEPTS_FILENAME));

		ATDService atdService = Context.getService(ATDService.class);
		List<ConceptDescriptor> cdList = new ArrayList<>();

		if(request.getParameter(PARAMETER_EXPORT_ALL) != null) // Exporting all records
		{
			cdList = atdService.getConceptDescriptorList(-1, -1, ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING, true, 
			    Integer.parseInt(ALL_CONCEPT_CLASSES_OPTION), ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING, SORT_ASC, false);	
		}
		else if(request.getParameter(PARAMETER_SELECTED_IDS) != null) // Exporting specifically selected records
		{
			// Get a list of all concept descriptors and create a map
			// Then loop over the selected Ids and locate it in the map
			// This may seem inefficient but is significantly faster than looking each one up individually 
			List<ConceptDescriptor> tempList = atdService.getConceptDescriptorList(-1, -1, 
			    ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING, true, Integer.parseInt(ALL_CONCEPT_CLASSES_OPTION), 
			    ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING, SORT_ASC, false);
			Map<String, ConceptDescriptor> conceptMap = new HashMap<>();
			for(ConceptDescriptor cd : tempList)
			{
				// Combine the conceptId and parentConceptId to use as the key
				conceptMap.put(cd.getConceptId() + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + cd.getParentConceptId(), cd);
			}

			// Split the string to get an array of combined Ids,
			// which is the conceptId_parentConceptId
			String selectedIdString = request.getParameter(PARAMETER_SELECTED_IDS);
			String [] selectedIds = selectedIdString.split(ChirdlUtilConstants.GENERAL_INFO_COMMA); 
			for(String combinedId : selectedIds)
			{
				ConceptDescriptor cd = conceptMap.get(combinedId);
				if(cd != null)
				{
					cdList.add(cd);
				}
			}
		}

		try
		{
			Util.exportAllConceptsAsCSV(response.getWriter(), cdList);
		}
		catch(IOException e)
		{
		    Log.error("Error exporting concepts as CSV", e);
			map.put(AtdConstants.PARAMETER_ERROR, AtdConstants.ERROR_TYPE_SERVER);
			return new ModelAndView(FORM_VIEW, map);
		}

		map.put(AtdConstants.PARAMETER_OPERATION_TYPE, OPERATION_TYPE_EXPORT_CONCEPT_AS_CSV_FILE);
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_OPERATION_SUCCESS), map);
	}
	
    /**
     * DWE CHICA-330 4/30/15
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map)
	{
		ConceptService cs = Context.getConceptService();
		
		List<ConceptClass> conceptClassList = cs.getAllConceptClasses();
		conceptClassList.add(0, new ConceptClass()); // Add a place holder for the "- All Concept Classes -" option
		
		map.put(PARAMETER_CONCEPT_CLASS_LIST, conceptClassList);
		request.setAttribute(ATTRIBUTE_ALL_CONCEPT_CLASSES_OPTION_CONSTANT, ALL_CONCEPT_CLASSES_OPTION);
		
		return FORM_VIEW;
	}
}

