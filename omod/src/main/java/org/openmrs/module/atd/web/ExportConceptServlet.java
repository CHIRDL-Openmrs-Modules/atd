package org.openmrs.module.atd.web;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.ConceptDescriptor;

/**
 * DWE CHICA-330 4/20/15
 * Added a separate servlet for use with ajax and jquery data tables
 */
public class ExportConceptServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static final String LOAD_TABLE_PARAM = "loadTable";
	private static final String INCLUDE_RETIRED_PARAM = "includeRetired";
	private static final String CONCEPT_CLASS_PARAM = "conceptClassSelect";
	private static final String CONTENT_TYPE_JSON = "application/json";

	// Data table specific parameters
	private static final String DRAW_PARAM = "draw";
	private static final String START_PARAM = "start";
	private static final String LENGTH_PARAM = "length";
	private static final String SEARCH_PARAM = "search[value]";
	private static final String ORDER_BY_COLUMN_PARAM = "order[0][column]";
	private static final String ORDER_BY_ASC_DESC_PARAM = "order[0][dir]";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		if(request.getParameter(LOAD_TABLE_PARAM) != null)
		{
			// Get the data table parameters for server-side processing
			String start = request.getParameter(START_PARAM);
			String length = request.getParameter(LENGTH_PARAM);
			String draw = request.getParameter(DRAW_PARAM);
			String searchValue = request.getParameter(SEARCH_PARAM);
			boolean includeRetired = Boolean.valueOf(request.getParameter(INCLUDE_RETIRED_PARAM));
			String conceptClass = request.getParameter(CONCEPT_CLASS_PARAM) != null ? request.getParameter(CONCEPT_CLASS_PARAM) : "-1";
			String orderByColumnNumber = request.getParameter(ORDER_BY_COLUMN_PARAM) != null ? request.getParameter(ORDER_BY_COLUMN_PARAM) : "5"; // Default to conceptId column number
			String ascDesc = request.getParameter(ORDER_BY_ASC_DESC_PARAM) != null ? request.getParameter(ORDER_BY_ASC_DESC_PARAM) : "ASC";
			String orderByColumn = request.getParameter("columns[" + orderByColumnNumber + "][data]") != null ? request.getParameter("columns[" + orderByColumnNumber + "][data]") : "conceptId";
			
			// Queries to populate the total number of records, the results list, and the total number of records with filter applied
			List<ConceptDescriptor> results = new ArrayList<ConceptDescriptor>();
			ATDService atdService = Context.getService(ATDService.class);
			int total = 0;
			int totalRecordsWithFilter = 0;
			try
			{
				results = atdService.getConceptDescriptorList(Integer.parseInt(start), Integer.parseInt(length), searchValue, includeRetired, Integer.parseInt(conceptClass), orderByColumn, ascDesc, false);
				total = atdService.getCountConcepts("", includeRetired, -1, false);
				totalRecordsWithFilter = atdService.getCountConcepts(searchValue, includeRetired, Integer.parseInt(conceptClass), false);
				
				// Make sure an error didn't occur in the query
				if(total == -1 || totalRecordsWithFilter == -1)
				{
					results = new ArrayList<ConceptDescriptor>();
				}
			}
			catch(NumberFormatException nfe)
			{
				results = new ArrayList<ConceptDescriptor>();
				total = 0;
				totalRecordsWithFilter = 0;
			}
			
			// Create a DataTableObject object that will be used with ObjectMapper
			// to create a JSON string for the data table
			DataTableObject dt = new DataTableObject();
			dt.setData(results);
			dt.setRecordsTotal(total); // If it is displayed, this is the X in "(filtered from X total entries)"
			dt.setRecordsFiltered(totalRecordsWithFilter); // this is the X in "Showing n to n of X entries"
			dt.setDraw(Integer.parseInt(draw));
			
			StringWriter w = new StringWriter();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(w, dt); // Convert to JSON
			//EXAMPLE {"draw":1,"recordsTotal":27257,"recordsFiltered":27257,"data":[{"name":"value","description":"value","units":"value","conceptId":value,"conceptClass":"value","datatype":"value","parentConcept":"value"}]}
			
			response.setContentType(CONTENT_TYPE_JSON);
			response.getWriter().write(w.toString());		
		}	
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}
	
	/**
	 * Private class used to create the JSON string that the jquery datatable is expecting
	 * Pass this object into the jackson mapper.writeValue() method
	 * This could be modified to use a generic list type so that it could be used in other areas of the code
	 * 
	 * NOTE: This is compatible with DataTables 1.10+, DataTables 1.9- send and receive a different set of parameters
	 */
	private class DataTableObject
	{
		// *********************************** Data Tables 1.10+ *****************************
		int draw;
		int recordsTotal;
		int recordsFiltered; 
		List<ConceptDescriptor> data;

		public int getDraw()
		{
			return draw;
		}

		public void setDraw(int draw)
		{
			this.draw = draw;
		}

		public int getRecordsTotal() 
		{
			return recordsTotal;
		}

		public void setRecordsTotal(int recordsTotal) 
		{
			this.recordsTotal = recordsTotal;
		}

		public int getRecordsFiltered() 
		{
			return recordsFiltered;
		}

		public void setRecordsFiltered(int recordsFiltered)
		{
			this.recordsFiltered = recordsFiltered;
		}

		public List<ConceptDescriptor> getData() 
		{
			return data;
		}

		public void setData(List<ConceptDescriptor> data)
		{
			this.data = data;
		}
	}
}
