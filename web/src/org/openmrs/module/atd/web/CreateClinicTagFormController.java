package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class CreateClinicTagFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		return "testing";
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("clinicName", request.getParameter("clinicName"));
		String clinicDescription = request.getParameter("clinicDescription");
		if (clinicDescription != null) {
			map.put("clinicDescription", clinicDescription);
		}
		
		String tagInfo = request.getParameter("tagInfo");
		if (tagInfo != null) {
			map.put("tagInfo", tagInfo);
		}
		
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String clinicName = request.getParameter("clinicName");
		String clinicDescription = request.getParameter("clinicDescription");
		map.put("clinicName", clinicName);
		map.put("clinicDescription", clinicDescription);
		String tagName = request.getParameter("tagName");
		String tagDescription = request.getParameter("tagDescription");
		String view = getFormView();
		String tagInfo = request.getParameter("tagInfo");
		if (tagInfo != null) {
			map.put("tagInfo", tagInfo);
		}
		
		// Check to see if tag name was specified.
		if (tagName == null || tagName.trim().length() == 0) {
			map.put("missingName", true);
			map.put("tagDescription", tagDescription);
			return new ModelAndView(view, map);
		}
		
		tagName = tagName.trim();
		map.put("tagName", tagName);		
		// Check to see if the tag name is already specified.
		if (findMatch(tagInfo, tagName)) {
			map.put("duplicateName", true);
			map.put("tagDescription", tagDescription);
			return new ModelAndView(view, map);
		}
		
		if (tagDescription != null) {
			tagDescription = tagDescription.trim();
		}
		
		if (tagInfo == null || tagInfo.trim().length() == 0) {
			tagInfo = tagName + "^^" + tagDescription;
		} else {
			tagInfo += "||" + tagName + "^^" + tagDescription;
		} 
		
		map.put("tagInfo", tagInfo);
		if ("false".equals(request.getParameter("addAdditionalTag"))) {
			// Create the clinic
			if (!createClinic(clinicName, clinicDescription, tagInfo)) {
				map.put("failedCreateClinic", true);
				return new ModelAndView(view, map);
			}
			
			view = getSuccessView();
			return new ModelAndView(new RedirectView(view), map);
		}
		
		map.remove("tagName");
		map.remove("tagDescription");
		return new ModelAndView(view, map);
	}
	
	private boolean findMatch(String tagInfo, String tagName) {
		// Tags are separated by the || characters and the tag name and tag description are separated by the ^^ characters
		if (tagInfo == null || tagName == null) {
			return false;
		}
		
		StringTokenizer tagTokenizer = new StringTokenizer(tagInfo, "||");
		while (tagTokenizer.hasMoreTokens()) {
			StringTokenizer detailTokenizer = new StringTokenizer(tagTokenizer.nextToken(), "^^");
			String name = detailTokenizer.nextToken();
			if (tagName.equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean createClinic(String name, String description, String tags) {
		Location location = new Location();
		location.setName(name);
		location.setDateCreated(new Date());
		if (description != null && description.trim().length() > 0) {
			location.setDescription(description.trim());
		}
		
		if (tags != null) {
			LocationService locationService = Context.getLocationService();
			List<LocationTag> addedTags = new ArrayList<LocationTag>();
			try {
				StringTokenizer tagTokenizer = new StringTokenizer(tags, "||");
				while (tagTokenizer.hasMoreTokens()) {
					StringTokenizer detailTokenizer = new StringTokenizer(tagTokenizer.nextToken(), "^^");
					String tagName = detailTokenizer.nextToken();
					String tagDescription = null;
					if (detailTokenizer.hasMoreTokens()) {
						tagDescription = detailTokenizer.nextToken();
					}
					
					LocationTag tag = new LocationTag();
					tag.setName(tagName);
					tag.setDateCreated(new Date());
					if (tagDescription != null && tagDescription.trim().length() > 0) {
						tag.setDescription(tagDescription.trim());
					}
					
					tag = locationService.saveLocationTag(tag);
					location.addTag(tag);
					addedTags.add(tag);
				}
				
				locationService.saveLocation(location);
			} catch (Exception e) {
				log.error("Error creating new location", e);
				// Clean up any tags that were added.
				for (LocationTag tag : addedTags) {
					locationService.purgeLocationTag(tag);
				}
				
				return false;
			}
		}
		
		return true;
	}
}

