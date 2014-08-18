package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class chooseLocationController extends SimpleFormController {
	private String formIdStr;

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		String[] positionStrs = request.getParameterValues("positions_applicable");
		if(positionStrs==null || positionStrs.length==0){
			String gobackViewName = this.getFormView();
			map.put("NoPositionSelected", "true");
			return new ModelAndView(
					new RedirectView(gobackViewName), map);
		}
		map.put("positions", positionStrs);
		map.put("formId", formIdStr);
		String successViewName = this.getSuccessView();
		return new ModelAndView(new RedirectView(successViewName), map);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}
	
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		ArrayList<ArrayList<Object>> positions = new ArrayList<ArrayList<Object>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		formIdStr = (String)request.getParameter("formId");
		LocationService locationService = Context.getLocationService();
		List<Location> locations = locationService.getAllLocations();
		for (Location currLoc : locations){
            Set<LocationTag> tags = currLoc.getTags();
            for (LocationTag tag : tags){
            	ArrayList<Object> position = new ArrayList<Object>();
            	position.add(currLoc);
            	position.add(tag);
            	positions.add(position);
            }   
		}
		map.put("positions", positions);
		return map;
	}
	
}
