package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
/**
 * 
 * @author wang417
 * controller for chooseLocation.form
 */
public class ChooseLocationController extends SimpleFormController {

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
		map.put("formId", request.getParameter("formIdStr"));
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
		List<Location> locationsList = new ArrayList<Location>();
		Map<Integer, List<LocationTag>> locationTagsMap = new HashMap<Integer, List<LocationTag>>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		String formIdStr = (String)request.getParameter("formId");
		map.put("formIdStr", formIdStr);
		LocationService locationService = Context.getLocationService();
		List<Location> locations = locationService.getAllLocations(false);
		for (Location currLoc : locations){
			locationsList.add(currLoc);
            Set<LocationTag> tags = currLoc.getTags();
            List<LocationTag> locationTagsList = new ArrayList<LocationTag>();
            for (LocationTag tag : tags){
            	locationTagsList.add(tag);
            }
            locationTagsMap.put(currLoc.getId(), locationTagsList);
		}
		map.put("locationsList", locationsList);
		map.put("locationTagsMap", locationTagsMap);
		return map;
	}
	
}
