package org.openmrs.module.atd.web.controller;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author wang417
 * controller for configFormAttributeValue.form
 */
@Controller
@RequestMapping(value = "module/atd/configFormAttributeValue.form")
public class ConfigFormAttributeValueController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view */
	private static final String FORM_VIEW = "/module/atd/configFormAttributeValue";
	
	/** Success form view */
	private static final String SUCCESS_FORM_VIEW_MLM = "mlmForm.form";
	private static final String SUCCESS_FORM_VIEW_REPLACE_RETIRE = "replaceRetireForm.form";

	/*
	 * There are two kinds of input text: form attribute value for all positions and these for each different position. For the former one, leaving the text field blank
	 * would not delete the current value in server for all positions but for the latter ones it does. users may set some specific values to positions that they think identical
	 * in position-specific text fields and leave all-positions field blank, but once user input some values in all-position field, it will override the values of each specific-position field.
	 * When the page is new loaded, the position-specific text fields reflect the current value of each position but all-positions field is always blank.
	 * */
	@RequestMapping(method = RequestMethod.POST)
	protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		String formId = request.getParameter("formId");
		Integer iFormId = Integer.parseInt(formId);
		// DWE CHICA-332 The success view needs to be different depending on which page the user came from
		String successViewName = request.getParameter(AtdConstants.PARAMETER_SUCCESS_VIEW_NAME);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			// Delete the form when coming from the create/replace form pages
			if(successViewName.equalsIgnoreCase(SUCCESS_FORM_VIEW_REPLACE_RETIRE) || successViewName.equalsIgnoreCase(SUCCESS_FORM_VIEW_MLM))
			{
				// CHICA-993 Updated to delete based on formId
				// true when successViewName is mlmForm.form (which happens when using the Create Form tool)
				// false when successViewName is replaceRetireForm.form (which happens when using the Replace Form tool)
				ConfigManagerUtil.deleteForm(iFormId, successViewName.equalsIgnoreCase(SUCCESS_FORM_VIEW_MLM));
			}
			return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER));
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		List<Location> locationsList = new ArrayList<Location>();
		Set<Integer> locationsIdSet = new HashSet<Integer>();
		Map<Integer, List<LocationTag>> locationTagsMap = new HashMap<Integer, List<LocationTag>>();
		configPositionInfo(locationsList, locationsIdSet, locationTagsMap, request);
		//get editable attribute
		List<FormAttribute> editableFormAttributes = getEditableFormAttributes();
		
		Map<String, Object> formAttributesValueMap=getFormAttributesValueMap(locationsList,locationTagsMap, editableFormAttributes,iFormId, request,cubService);
		for (FormAttribute fa: editableFormAttributes){
			/*for storing values with each position */
			for (Location currLoc : locationsList) {
				for (LocationTag tag : locationTagsMap.get(currLoc.getId())) {
					String inputName = "inpt_" + fa.getFormAttributeId() + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + currLoc.getId() + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + tag.getId();
					String favId = fa.getFormAttributeId() + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + currLoc.getId() + ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE + tag.getId();
					String feedbackValueStr = request.getParameter(inputName);
					if (feedbackValueStr != null && !feedbackValueStr.equals("")){ 
						FormAttributeValue currentStoredValue = (FormAttributeValue) formAttributesValueMap.get(favId);
						if(currentStoredValue == null || (currentStoredValue != null && !feedbackValueStr.equals(currentStoredValue.getValue()))){
							cubService.saveFormAttributeValue(iFormId, fa.getName(), tag.getId(), currLoc.getId(), feedbackValueStr);
						}					
					} else {
						// We need to delete form attribute value records if the value from the UI is null or empty 					
						FormAttributeValue currentStoredValue = (FormAttributeValue) formAttributesValueMap.get(favId);
						if (currentStoredValue != null && !currentStoredValue.getValue().equals("")) {
							cubService.deleteFormAttributeValue((FormAttributeValue) currentStoredValue);
						}
					}
				}
			}
		}
		
		
		if(successViewName.equalsIgnoreCase(SUCCESS_FORM_VIEW_REPLACE_RETIRE))
		{
			// Need to switch the parameters back around to what this page is expecting
			map.put("formId", request.getParameter("replaceFormId"));
			map.put("newFormId", request.getParameter("formId"));
		}
		else
		{
			map.put("formId", iFormId.toString());
		}
		
		map.put("operationType", "Editing form attributes value");
		return new ModelAndView(new RedirectView(successViewName), map);
		
	}

	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {
		//empty and initialize positions every time when this page is redirected to. 
		List<Location> locationsList = new ArrayList<Location>();
		Set<Integer> locationsIdSet = new HashSet<Integer>();
		Map<Integer, List<LocationTag>> locationTagsMap = new HashMap<Integer, List<LocationTag>>();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		
		// DWE CHICA-332 4/16/15 Making the configFormAttributeValue.form available from the Replace Form page
		// When this page is displayed from the Replace Form page, use newFormId parameter
		String formId = request.getParameter("newFormId");
		if(formId != null)
		{
			// Need to keep track of the "replaceFormId" so that it is available on the retire form page
			map.put("replaceFormId", request.getParameter("formId")); 
		}
		
		if(formId == null)
		{
			// Coming from chooseLocations.form or configForm.form
			formId = request.getParameter("formId");
		}
		
		configPositionInfo(locationsList, locationsIdSet, locationTagsMap, request);
		
		Integer iFormId = Integer.parseInt(formId);
		
		//get editable attribute
		List<FormAttribute> editableFormAttributes = getEditableFormAttributes();
		
		//get current attributes value info for each position and attributes 
		Map<String, Object> formAttributesValueMap = getFormAttributesValueMap(locationsList,locationTagsMap, editableFormAttributes,iFormId, request,cubService);
	
		//get attribute value enumeration info for each attribute
		String[] positionStrs = request.getParameterValues(AtdConstants.PARAMETER_POSITIONS);
		
		map.put("positionStrs", positionStrs);
		map.put(AtdConstants.PARAMETER_LOCATIONS_LIST, locationsList);
		map.put(AtdConstants.PARAMETER_LOCATION_TAGS_MAP, locationTagsMap);
		map.put("editableFormAttributes", editableFormAttributes);
		map.put("formAttributesValueMap", formAttributesValueMap);
		map.put("formId", formId);
		map.put(AtdConstants.PARAMETER_SELECTED_FORM_NAME, request.getParameter(AtdConstants.PARAMETER_SELECTED_FORM_NAME));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		// Success view will depend on which page the user came from
		map.put(AtdConstants.PARAMETER_SUCCESS_VIEW_NAME, request.getParameter(AtdConstants.PARAMETER_SUCCESS_VIEW_NAME)); 
		
		// DWE CHICA-596 Added logging so that we know what is sent back to the client
		// Trying to track down why the page does not display any of the existing values in production
		try{
		StringWriter w = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(w, formAttributesValueMap);
		log.info("Edit form attribute values: " + w.toString());
		}catch(Exception e){
			log.error("Error logging form attribute values map.", e);
		}
		
		return FORM_VIEW;
	}
	
	private Map<String, Object> getFormAttributesValueMap(List<Location> locationsList, Map<Integer, List<LocationTag>> locationTagsMap,List<FormAttribute> editableFormAttributes, Integer iFormId, HttpServletRequest request, ChirdlUtilBackportsService cubService){
		Map<String, Object> formAttributesValueMap = new HashMap<String, Object>();
		String back = request.getParameter("redirectBack");
		//get current attributes value info for each position and attributes
		if("true".equalsIgnoreCase(back)){
			for (Location currLoc: locationsList) {
				for(LocationTag tag: locationTagsMap.get(currLoc.getId())){
					for(FormAttribute efa: editableFormAttributes){
						String favId = efa.getFormAttributeId()+ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE+currLoc.getId()+ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE+tag.getId();
						formAttributesValueMap.put(favId, request.getParameter(favId));
					}
				}
			}
		}else{
			for (Location currLoc: locationsList) {
				for(LocationTag tag: locationTagsMap.get(currLoc.getId())){
					for(FormAttribute efa: editableFormAttributes){
						FormAttributeValue theValue = cubService.getFormAttributeValue(iFormId, efa.getName(), tag.getId(), currLoc.getId());
						//formAttributesValueMap key is ids of formAttributeValue, Location, locationTag
						formAttributesValueMap.put(efa.getFormAttributeId()+ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE+currLoc.getId()+ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE+tag.getId(), theValue);
					}
				}
			}
		}
		return formAttributesValueMap;
	}

	public Map<String, List<String>> getFormAttributesValueEnumMap(List<FormAttribute> editableFormAttributes, ChirdlUtilBackportsService cubService){
		//get attribute value enumeration info for each attribute
		Map<String, List<String>> formAttributesValueEnumMap = new HashMap<String, List<String>>();
		for(FormAttribute efa: editableFormAttributes){
			List<String> valueList = cubService.getCurrentFormAttributeValueStrCollection(efa);
			formAttributesValueEnumMap.put(efa.getFormAttributeId().toString(), valueList);
		}
		return formAttributesValueEnumMap;
	}
	
	private List<FormAttribute> getEditableFormAttributes(){
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		List<FormAttribute> editable = cubService.getAllEditableFormAttributes();
		if(editable==null){
			return new ArrayList<FormAttribute>();
		}
		return editable;
	}
	
	private void configPositionInfo(List<Location> locationsList, Set<Integer> locationsIdSet,Map<Integer, List<LocationTag>> locationTagsMap, HttpServletRequest request){
		// positionStrs are position array. for position string, it is composed
		// of locationId + "_"+ locationTagId
		String[] positionStrs = request.getParameterValues(AtdConstants.PARAMETER_POSITIONS);
		LocationService locationService = Context.getLocationService();
		if (positionStrs != null) {
			StringTokenizer st = null;
			for (String p : positionStrs) {
				st = new StringTokenizer(p,ChirdlUtilConstants.GENERAL_INFO_UNDERSCORE);
				try {
					Location loc = null;
					LocationTag locTag = null;
					// parse and store Location
					if (st.hasMoreTokens()) {
						Integer locId = Integer.parseInt(st.nextToken());
						loc = locationService.getLocation(locId);
					} else {
						continue;
					}
					// parse and store LocationTag
					if (st.hasMoreTokens()) {
						Integer locTagId = Integer.parseInt(st.nextToken());
						locTag = locationService.getLocationTag(locTagId);
					} else {
						continue;
					}
					if(loc!=null && locTag!=null){
						if(!locationsIdSet.contains(loc.getId())){
							locationsList.add(loc);
							List<LocationTag> tagsListCurLocation = new ArrayList<LocationTag>();
							locationTagsMap.put(loc.getId(), tagsListCurLocation);
							locationsIdSet.add(loc.getId());
						}
						locationTagsMap.get(loc.getId()).add(locTag);
					}else{
						continue;
					}
				} catch (NumberFormatException e) {
					continue;
				}
				
			}
		}
	}
}
