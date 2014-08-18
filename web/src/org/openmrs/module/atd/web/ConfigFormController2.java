package org.openmrs.module.atd.web;

import java.util.ArrayList;
import java.util.HashMap;
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

public class ConfigFormController2 extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	private Map<String, Object> map;
	private Integer iFormId;
	private ArrayList<ArrayList<Object>> positions;

	private void rollBack() {
		LocationService locationService = Context.getLocationService();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		for (ArrayList<Object> position : positions) {
			Location currLoc = (Location) position.get(0);
			LocationTag tag = (LocationTag) position.get(1);
			cubService.saveFormAttributeValue(iFormId, "forcePrintable", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "forcePrintable"));
			cubService.saveFormAttributeValue(iFormId, "auto-fax", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "auto-fax"));
			cubService.saveFormAttributeValue(iFormId, "displayName", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "displayname"));
			cubService.saveFormAttributeValue(iFormId, "mobileOnly", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "mobileOnly"));
			cubService.saveFormAttributeValue(iFormId, "ageMin", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAge"));
			cubService.saveFormAttributeValue(iFormId, "ageMinUnits", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAgeUnit"));
			cubService.saveFormAttributeValue(iFormId, "ageMax", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAge"));
			cubService.saveFormAttributeValue(iFormId, "ageMaxUnits", tag.getId(), currLoc.getId(), (String) map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAgeUnit"));

		}
	}

	private Map<String, Object> keepInputBack(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (ArrayList<Object> position : positions) {
			Location currLoc = (Location) position.get(0);
			LocationTag tag = (LocationTag) position.get(1);
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "displayName", request.getParameter("inpt_displayname#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "forcePrintable", request.getParameter("inpt_fprintable#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "auto-fax", request.getParameter("inpt_faxable#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "mobileOnly", request.getParameter("inpt_mobileOnly#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAge", request.getParameter("inpt_miniAge#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAge", request.getParameter("inpt_miniAgeUnit#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAge", request.getParameter("inpt_maxAge#$#" + currLoc.getId() + "#$#" + tag.getId()));
			map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAgeUnit", request.getParameter("inpt_maxAgeUnit#$#" + currLoc.getId() + "#$#" + tag.getId()));

		}

		return map;
	}

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

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		FormService formService = Context.getFormService();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		// String formIdStr = request.getParameter("formId");
		// Integer formId = Integer.parseInt(formIdStr);
		String cancel = request.getParameter("cancelProcess");
		if ("true".equalsIgnoreCase(cancel)) {
			return new ModelAndView(new RedirectView("configurationManager.form"));
		}
		String successViewName = getSuccessView();
		String gobackViewName = this.getFormView();
		LocationService locationService = Context.getLocationService();
		List<Location> locations = locationService.getAllLocations();

		// check if we need pre validation or roll-back.

		for (ArrayList<Object> position: positions) {
			Location currLoc = (Location)position.get(0);
			LocationTag tag = (LocationTag)position.get(1);
				String displayName = request.getParameter("inpt_displayname#$#" + currLoc.getId() + "#$#" + tag.getId());
				String faxable = request.getParameter("inpt_faxable#$#" + currLoc.getId() + "#$#" + tag.getId());
				String forcePrintable = request.getParameter("inpt_fprintable#$#" + currLoc.getId() + "#$#" + tag.getId());
				String mobileOnly = request.getParameter("inpt_mobileOnly#$#" + currLoc.getId() + "#$#" + tag.getId());
				String miniAge = request.getParameter("inpt_miniAge#$#" + currLoc.getId() + "#$#" + tag.getId());
				String miniAgeUnit = request.getParameter("inpt_miniAgeUnit#$#" + currLoc.getId() + "#$#" + tag.getId());
				String maxAge = request.getParameter("inpt_maxAge#$#" + currLoc.getId() + "#$#" + tag.getId());
				String maxAgeUnit = request.getParameter("inpt_maxAgeUnit#$#" + currLoc.getId() + "#$#" + tag.getId());

				int success = 0;
				if (displayName != null) {

					success = cubService.saveFormAttributeValue(iFormId, "displayName", tag.getId(), currLoc.getId(), displayName);
					if (success != 1) {

					}
				}
				if (faxable != null && !faxable.equals("")) {
					if (faxable.equalsIgnoreCase("notSet")) {
						if (this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "auto-fax") != null) {
							cubService.deleteFormAttributeValue((FormAttributeValue) this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "auto-fax"));
						}
					} else {
						cubService.saveFormAttributeValue(iFormId, "auto-faxable", tag.getId(), currLoc.getId(), faxable);
					}

				}
				if (forcePrintable != null && !forcePrintable.equals("")) {
					if (forcePrintable.equalsIgnoreCase("notSet")) {
						if (this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "forcePrintable") != null) {
							cubService.deleteFormAttributeValue((FormAttributeValue) this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "forcePrintable"));
						}
					} else {
						cubService.saveFormAttributeValue(iFormId, "forcePrintable", tag.getId(), currLoc.getId(), forcePrintable);

					}
				}
				if (mobileOnly != null && !mobileOnly.equals("")) {
					if (mobileOnly.equalsIgnoreCase("notSet")) {
						if (this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "mobileOnly") != null) {
							cubService.deleteFormAttributeValue((FormAttributeValue) this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "mobileOnly"));
						}
					} else {
						cubService.saveFormAttributeValue(iFormId, "mobileOnly", tag.getId(), currLoc.getId(), mobileOnly);
					}
				}
				if (miniAge != null && !miniAge.equals("")) {
					try {
						Integer.parseInt(miniAge);
					} catch (NumberFormatException e) {
						this.rollBack();
						Map<String, Object> backMap = this.keepInputBack(request);
						return new ModelAndView(new RedirectView(gobackViewName), backMap);
					}
					cubService.saveFormAttributeValue(iFormId, "ageMin", tag.getId(), currLoc.getId(), miniAge);
				}
				if (miniAgeUnit != null && !miniAgeUnit.equals("")) {
					if (!miniAgeUnit.equals("yo") && !miniAgeUnit.equals("mo") && !miniAgeUnit.equals("wk") && !miniAgeUnit.equals("do")) {
						if (this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAgeUnit") != null) {
							cubService.deleteFormAttributeValue((FormAttributeValue) this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAgeUnit"));
						}
					} else {
						cubService.saveFormAttributeValue(iFormId, "ageMinUnits", tag.getId(), currLoc.getId(), miniAgeUnit);
					}
				}
				if (maxAge != null && !maxAge.equals("")) {
					try {
						Integer.parseInt(maxAge);
					} catch (NumberFormatException e) {
						this.rollBack();
						Map<String, Object> backMap = this.keepInputBack(request);
						return new ModelAndView(new RedirectView(gobackViewName), backMap);
					}
					cubService.saveFormAttributeValue(iFormId, "ageMax", tag.getId(), currLoc.getId(), maxAge);

				}
				if (maxAgeUnit != null && !maxAgeUnit.equals("")) {
					if (!maxAgeUnit.equals("yo") && !maxAgeUnit.equals("mo") && !maxAgeUnit.equals("wk") && !maxAgeUnit.equals("do")) {
						if (this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAgeUnit") != null) {
							cubService.deleteFormAttributeValue((FormAttributeValue) this.map.get(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAgeUnit"));
						}
					} else {
						success = cubService.saveFormAttributeValue(iFormId, "ageMaxUnits", tag.getId(), currLoc.getId(), maxAgeUnit);
						if (success != 1) {
							// TODO: return to the page and give an alert
						}
					}
				}

			

		}

		map.put("formId", iFormId.toString());
		successViewName = getSuccessView();
		return new ModelAndView(new RedirectView(successViewName), map);
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		//empty and initialize positions every time when this page is redirected to. 
		positions = new ArrayList<ArrayList<Object>>();
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
				ArrayList<Object> position = new ArrayList<Object>();
				try {
					// parse and store Location
					if (st.hasMoreTokens()) {
						String strLocId = st.nextToken();
						Location loc = locationService.getLocation(Integer.parseInt(strLocId));
						position.add(loc);
					} else {
						continue;
					}
					// parse and store LocationTag
					if (st.hasMoreTokens()) {
						String strLocTagId = st.nextToken();
						LocationTag locTag = locationService.getLocationTag(Integer.parseInt(strLocTagId));
						position.add(locTag);
					} else {
						continue;
					}
				} catch (NumberFormatException e) {
					continue;
				}
				positions.add(position);
			}
		}
		String formId = request.getParameter("formId");
		Integer iFormId = Integer.parseInt(formId);
		this.iFormId = iFormId;
		//get attributes info for each position
		for (ArrayList<Object> position: positions) {	
				Location currLoc = (Location)position.get(0);
				LocationTag tag = (LocationTag)position.get(1);
				FormAttributeValue forcePrintable = cubService.getFormAttributeValue(iFormId, "forcePrintable", tag.getId(), currLoc.getId());
				FormAttributeValue autoFax = cubService.getFormAttributeValue(iFormId, "auto-fax", tag.getId(), currLoc.getId());
				FormAttributeValue displayname = cubService.getFormAttributeValue(iFormId, "displayName", tag.getId(), currLoc.getId());
				FormAttributeValue mobileOnly = cubService.getFormAttributeValue(iFormId, "mobileOnly", tag.getId(), currLoc.getId());
				FormAttributeValue ageMin = cubService.getFormAttributeValue(iFormId, "ageMin", tag.getId(), currLoc.getId());
				FormAttributeValue ageMinUnits = cubService.getFormAttributeValue(iFormId, "ageMinUnits", tag.getId(), currLoc.getId());
				FormAttributeValue ageMax = cubService.getFormAttributeValue(iFormId, "ageMax", tag.getId(), currLoc.getId());
				FormAttributeValue ageMaxUnits = cubService.getFormAttributeValue(iFormId, "ageMaxUnits", tag.getId(), currLoc.getId());
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "forcePrintable", forcePrintable);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "auto-fax", autoFax);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "displayname", displayname);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "mobileOnly", mobileOnly);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAge", ageMin);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "miniAgeUnit", ageMinUnits);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAge", ageMax);
				map.put(currLoc.getId() + "#$#" + tag.getId() + "#$#" + "maxAgeUnit", ageMaxUnits);
			
		}
		map.put("positions", positions);
		map.put("formId", formId);
		map.put("formName", request.getParameter("formName"));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		this.map = map;
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
}
