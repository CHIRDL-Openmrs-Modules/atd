package org.openmrs.module.atd.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
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
 * controller for chooseLocation.form
 */
@Controller
@RequestMapping(value = "module/atd/chooseLocation.form")
public class ChooseLocationController {
    
    /** Form view */
    private static final String FORM_VIEW = "/module/atd/chooseLocation";
    
    /** Success view */
    private static final String SUCCESS_VIEW = "configFormAttributeValue.form";
    
    /** Parameters */
    private static final String PARAMETER_LOCATION_TAGS_MAP = "locationTagsMap";
    private static final String PARAMETER_LOCATIONS_LIST = "locationsList";
    private static final String OPERATION_SUCCESS_FORM_VIEW = "operationSuccess.form";
    private static final String PARAMETER_SUCCESS_VIEW_NAME = "successViewName";
    private static final String PARAMETER_FORM_ID_STRING = "formIdStr";
    private static final String PARAMETER_POSITIONS = "positions";
    private static final String PARAMETER_NO_POSITION_SELECTED = "NoPositionSelected";
    private static final String PARAMETER_POSITIONS_APPLICABLE = "positions_applicable";
    
    /**
     * Parses the locations provided by the client and load them into a ModelMap for the next view.
     * 
     * @param request The HTTP request information
     * @return The name of the next view
     */
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request) {
		HashMap<String, Object> map = new HashMap<>();
		String[] positionStrs = request.getParameterValues(PARAMETER_POSITIONS_APPLICABLE);
		if(positionStrs==null || positionStrs.length==0){
			map.put(PARAMETER_NO_POSITION_SELECTED, ChirdlUtilConstants.GENERAL_INFO_TRUE);
			return new ModelAndView(
					new RedirectView(AtdConstants.FORM_VIEW_CHOOSE_LOCATION_FORM), map);
		}
		
		map.put(PARAMETER_POSITIONS, positionStrs);
		map.put(ChirdlUtilConstants.PARAMETER_FORM_ID, request.getParameter(PARAMETER_FORM_ID_STRING));
		map.put(AtdConstants.PARAMETER_SELECTED_FORM_NAME, request.getParameter(AtdConstants.PARAMETER_SELECTED_FORM_NAME));
		// Success view will depend on which page the user came from
		map.put(PARAMETER_SUCCESS_VIEW_NAME, OPERATION_SUCCESS_FORM_VIEW); 
		return new ModelAndView(new RedirectView(SUCCESS_VIEW), map);
	}
	
    /**
     * Form initialization method.
     * 
     * @param request The HTTP request information
     * @param map The map to populate for return to the client
     * @return The form view name
     */
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) {
		List<Location> locationsList = new ArrayList<>();
		Map<Integer, List<LocationTag>> locationTagsMap = new HashMap<>();
		String formIdStr = request.getParameter(ChirdlUtilConstants.PARAMETER_FORM_ID);
		String selectedFormName = request.getParameter(AtdConstants.PARAMETER_SELECTED_FORM_NAME);
		map.put(PARAMETER_FORM_ID_STRING, formIdStr);
		map.put(AtdConstants.PARAMETER_SELECTED_FORM_NAME, selectedFormName);
		LocationService locationService = Context.getLocationService();
		List<Location> locations = locationService.getAllLocations(false);
		for (Location currLoc : locations){
			locationsList.add(currLoc);
            Set<LocationTag> tags = currLoc.getTags();
            List<LocationTag> locationTagsList = new ArrayList<>();
            for (LocationTag tag : tags){
            	locationTagsList.add(tag);
            }
            
            locationTagsMap.put(currLoc.getId(), locationTagsList);
		}
		
		map.put(PARAMETER_LOCATIONS_LIST, locationsList);
		map.put(PARAMETER_LOCATION_TAGS_MAP, locationTagsMap);
		return FORM_VIEW;
	}
	
}
