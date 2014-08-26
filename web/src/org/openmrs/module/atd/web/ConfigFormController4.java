package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.View;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.web.util.ConfigManagerUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutil.log.LoggingConstants;
import org.openmrs.module.chirdlutil.log.LoggingUtil;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConfigFormController4 extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	private Map<String, Object> formAttributesValueMap;
	private Map<String, List<String>> formAttributesValueEnumMap;
	private Integer iFormId;
	private List<Location> locationsList;
	private Set<Integer> locationsIdSet;
	private Map<Integer, List<LocationTag>> locationTagsMap;
	private List<FormAttribute> editableFormAttributes;
/*
	private void rollBack() {
		LocationService locationService = Context.getLocationService();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		for (FormAttribute fa : editableFormAttributes) {
			for (ArrayList<Object> position : positions) {
				Location currLoc = (Location) position.get(0);
				LocationTag tag = (LocationTag) position.get(1);
				cubService.saveFormAttributeValue(iFormId, fa.getName(), tag.getId(), currLoc.getId(), (String) formAttributesValueMap.get(fa.getFormAttributeId() + "#$#" + currLoc.getId() + "#$#" + tag.getId()));
				
			}
		}
	}

	private Map<String, Object> keepInputBack(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		for(FormAttribute fa: editableFormAttributes){
		for (ArrayList<Object> position : positions) {
			Location currLoc = (Location) position.get(0);
			LocationTag tag = (LocationTag) position.get(1);
			map.put(fa.getFormAttributeId() + "#$#" + currLoc.getId() + "#$#" + tag.getId(), request.getParameter(fa.getFormAttributeId() + "#$#" + currLoc.getId() + "#$#" + tag.getId()));
		}
		}
		return map;
	}
*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject
	 * (javax.servlet.http.HttpServletRequest)
	 */
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
		Map<String, Object> map = new HashMap<String, Object>();
		FormService formService = Context.getFormService();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			return new ModelAndView(new RedirectView("configurationManager.form"));
		}
		String successViewName = getSuccessView();
		String gobackViewName = this.getFormView();
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
			/*for getting the value that is applied for all positions and storing it */
			/*
			String feedbackAllPositionValue = request.getParameter("inpt_"+fa.getFormAttributeId()+"#$#ALL#$#ALL");
			feedbackAllPositionValue = feedbackAllPositionValue.trim();
			if(feedbackAllPositionValue !=null && !feedbackAllPositionValue.equals("")){
				for(Location currLoc : locationsList){
					for (LocationTag tag : locationTagsMap.get(currLoc.getId())) {
						cubService.saveFormAttributeValue(iFormId, fa.getName(), tag.getId(), currLoc.getId(), feedbackAllPositionValue);
					}
				}
			}
			*/
		}
		
		map.put("formId", iFormId.toString());
		successViewName = getSuccessView();
		return new ModelAndView(new RedirectView(successViewName), map);
		
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		//empty and initialize positions every time when this page is redirected to. 
		locationsList = new ArrayList<Location>();
		locationsIdSet = new HashSet<Integer>();
		locationTagsMap = new HashMap<Integer, List<LocationTag>>();
		formAttributesValueEnumMap = new HashMap<String, List<String>>();
		formAttributesValueMap = new HashMap<String, Object>();
		//editableFormAttributes = new ArrayList<FormAttribute>();
		Map<String, Object> map = new HashMap<String, Object>();
		// positionStrs are position array. for position string, it is composed
		// of locationId + "#$#"+ locationTagId
		String[] positionStrs = request.getParameterValues("positions");
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
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
		String formId = request.getParameter("formId");
		Integer iFormId = Integer.parseInt(formId);
		this.iFormId = iFormId;
		//get editable attribute
		editableFormAttributes = getEditableFormAttributes();
		
		
		
		//get current attributes value info for each position and attributes
		String back = request.getParameter("redirectBack");
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
						formAttributesValueMap.put(efa.getFormAttributeId()+"#$#"+currLoc.getId()+"#$#"+tag.getId(), theValue);
					}
				}
			}
		}

		//get attribute value enumeration info for each attribute
		for(FormAttribute efa: editableFormAttributes){
			formAttributesValueEnumMap.put(efa.getFormAttributeId().toString(), cubService.getCurrentFormAttributeValueStrCollection(efa));
		}
		
		map.put("locationsList", locationsList);
		map.put("locationTagsMap", locationTagsMap);
		map.put("formAttributesValueEnumMap", formAttributesValueEnumMap);
		map.put("editableFormAttributes", editableFormAttributes);
		map.put("formAttributesValueMap", formAttributesValueMap);
		map.put("formId", formId);
		map.put("formName", request.getParameter("formName"));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		return map;
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
}
