package org.openmrs.module.atd.web;

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
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author wang417
 * controller for configFormAttributeValue.form
 */
public class ConfigFormAttributeValueController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	protected static final String ESCAPE_BACKSLASH = "\\\\";


	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}
	/*
	 * There are two kinds of input text: form attribute value for all positions and these for each different position. For the former one, leaving the text field blank
	 * would not delete the current value in server for all positions but for the latter ones it does. users may set some specific values to positions that they think identical
	 * in position-specific text fields and leave all-positions field blank, but once user input some values in all-position field, it will override the values of each specific-position field.
	 * When the page is new loaded, the position-specific text fields reflect the current value of each position but all-positions field is always blank.
	 * */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
		String formId = request.getParameter("formId");
		Integer iFormId = Integer.parseInt(formId);
		// DWE CHICA-332 The success view needs to be different depending on which page the user came from
		String successViewName = request.getParameter("successViewName");
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			// Delete the form when coming from the create/replace form pages
			if(successViewName.equalsIgnoreCase("replaceRetireForm.form") || successViewName.equalsIgnoreCase("mlmForm.form"))
			{
				ConfigManagerUtil.deleteForm(iFormId);
			}
			return new ModelAndView(new RedirectView("configurationManager.form"));
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
					String inputName = "inpt_" + fa.getFormAttributeId() + "#$#" + currLoc.getId() + "#$#" + tag.getId();
					String favId = fa.getFormAttributeId() + "#$#" + currLoc.getId() + "#$#" + tag.getId();
					String feedbackValueStr = request.getParameter(inputName);
					if (feedbackValueStr != null && !feedbackValueStr.equals("")) {
						if (!feedbackValueStr.equals(formAttributesValueMap.get(favId))) {
							log.info(fa.getFormAttributeId() + " " + currLoc.getLocationId() + " " + tag.getLocationTagId() + "  value is: " + feedbackValueStr);
						}
						cubService.saveFormAttributeValue(iFormId, fa.getName(), tag.getId(), currLoc.getId(), feedbackValueStr);
					} else {
						FormAttributeValue currentStoredValue = (FormAttributeValue) formAttributesValueMap.get(fa.getFormAttributeId() + "#$#" + currLoc.getId() + "#$#" + tag.getId());
						if (currentStoredValue != null && !currentStoredValue.equals("")) {
							cubService.deleteFormAttributeValue((FormAttributeValue) currentStoredValue);
						}
					}
				}
			}
		}
		
		
		if(successViewName.equalsIgnoreCase("replaceRetireForm.form"))
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

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		//empty and initialize positions every time when this page is redirected to. 
		List<Location> locationsList = new ArrayList<Location>();
		Set<Integer> locationsIdSet = new HashSet<Integer>();
		Map<Integer, List<LocationTag>> locationTagsMap = new HashMap<Integer, List<LocationTag>>();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		Map<String, Object> map = new HashMap<String, Object>();
		
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
		Map<String, List<String>> formAttributesValueEnumMap = getFormAttributesValueEnumMap(editableFormAttributes,cubService);
		String[] positionStrs = request.getParameterValues("positions");
		
		map.put("positionStrs", positionStrs);
		map.put("locationsList", locationsList);
		map.put("locationTagsMap", locationTagsMap);
		map.put("formAttributesValueEnumMap", formAttributesValueEnumMap);
		map.put("editableFormAttributes", editableFormAttributes);
		map.put("formAttributesValueMap", formAttributesValueMap);
		map.put("formId", formId);
		map.put("selectedFormName", request.getParameter("selectedFormName"));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		map.put("successViewName", request.getParameter("successViewName")); // Success view will depend on which page the user came from
		return map;
	}
	
	private Map<String, Object> getFormAttributesValueMap(List<Location> locationsList, Map<Integer, List<LocationTag>> locationTagsMap,List<FormAttribute> editableFormAttributes, Integer iFormId, HttpServletRequest request, ChirdlUtilBackportsService cubService){
		Map<String, Object> formAttributesValueMap = new HashMap<String, Object>();
		String back = request.getParameter("redirectBack");
		//get current attributes value info for each position and attributes
		if("true".equalsIgnoreCase(back)){
			for (Location currLoc: locationsList) {
				for(LocationTag tag: locationTagsMap.get(currLoc.getId())){
					for(FormAttribute efa: editableFormAttributes){
						String favId = efa.getFormAttributeId()+"#$#"+currLoc.getId()+"#$#"+tag.getId();
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
						if(theValue != null && theValue.getValue() != null)
						{
							theValue.setValue(theValue.getValue().replace("\\", ESCAPE_BACKSLASH));
						}
						formAttributesValueMap.put(efa.getFormAttributeId()+"#$#"+currLoc.getId()+"#$#"+tag.getId(), theValue);
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
			for(int i = 0; i <= valueList.size() -1; i++)
			{
				String temp = valueList.get(i);
				valueList.set(i, temp.replace("\\", ESCAPE_BACKSLASH));
			}
			formAttributesValueEnumMap.put(efa.getFormAttributeId().toString(), valueList);
		}
		return formAttributesValueEnumMap;
	}
	
	
	private boolean checkParameter(HttpServletRequest request, String parameterName) {
		boolean positive = false;
		String parameter = request.getParameter(parameterName);
		if ("true".equalsIgnoreCase(parameter)) {
			positive = true;
		}

		return positive;
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
		// of locationId + "#$#"+ locationTagId
		String[] positionStrs = request.getParameterValues("positions");
		LocationService locationService = Context.getLocationService();
		if (positionStrs != null) {
			StringTokenizer st = null;
			for (String p : positionStrs) {
				st = new StringTokenizer(p,"#$#");
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
