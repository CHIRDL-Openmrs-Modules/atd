package org.openmrs.module.atd.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.Util;
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
	private static final String CONCEPTS_FILENAME = "concepts.csv";
	private static final String CONTENT_TYPE_CSV = "text/csv";
	protected final static String ALL_CONCEPT_CLASSES_OPTION = "-1";
	private static final String EXPORT_ALL_PARAM = "exportAll";
	private static final String SELECTED_IDS_PARAM = "selectedIdsField";
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		response.setContentType(CONTENT_TYPE_CSV);
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", CONCEPTS_FILENAME));

		ATDService atdService = Context.getService(ATDService.class);
		List<ConceptDescriptor> cdList = new ArrayList<ConceptDescriptor>();

		if(request.getParameter(EXPORT_ALL_PARAM) != null) // Exporting all records
		{
			cdList = atdService.getConceptDescriptorList(-1, -1, "", true, Integer.parseInt(ALL_CONCEPT_CLASSES_OPTION), "", "ASC", false);	
		}
		else if(request.getParameter(SELECTED_IDS_PARAM) != null) // Exporting specifically selected records
		{
			// Get a list of all concept descriptors and create a map
			// Then loop over the selected Ids and locate it in the map
			// This may seem inefficient but is significantly faster than looking each one up individually 
			List<ConceptDescriptor> tempList = atdService.getConceptDescriptorList(-1, -1, "", true, Integer.parseInt(ALL_CONCEPT_CLASSES_OPTION), "", "ASC", false);
			Map<String, ConceptDescriptor> conceptMap = new HashMap<String, ConceptDescriptor>();
			for(ConceptDescriptor cd : tempList)
			{
				// Combine the conceptId and parentConceptId to use as the key
				conceptMap.put(cd.getConceptId() + "_" + cd.getParentConceptId(), cd);
			}

			// Split the string to get an array of combined Ids,
			// which is the conceptId_parentConceptId
			String selectedIdString = request.getParameter(SELECTED_IDS_PARAM);
			String [] selectedIds = selectedIdString.split(","); 
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
			map.put("error", "serverError");
			return new ModelAndView(getFormView(), map);
		}

		map.put("operationType", "export concept as csv file");
		return new ModelAndView(new RedirectView(getSuccessView()), map);
	}
	
	/**
	 * DWE CHICA-330 4/30/15
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		List<ConceptClass> conceptClassList = new ArrayList<ConceptClass>();
		ConceptService cs = Context.getConceptService();
		
		conceptClassList = cs.getAllConceptClasses();
		conceptClassList.add(0, new ConceptClass()); // Add a place holder for the "- All Concept Classes -" option
		
		map.put("conceptClassList", conceptClassList);
		request.setAttribute("allConceptClassesOptionConstant", ALL_CONCEPT_CLASSES_OPTION);
		
		return map;
	}
}

