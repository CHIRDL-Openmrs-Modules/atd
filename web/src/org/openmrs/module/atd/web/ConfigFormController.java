package org.openmrs.module.atd.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutil.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConfigFormController extends SimpleFormController
{

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception
	{
		return "testing";
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, 
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String formName = request.getParameter("formName");
		String printerCopy = request.getParameter("printerCopy");
		map.put("printerCopy", printerCopy);
		String view = getFormView();
		
		// Check to see if the user checked any locations
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		boolean found = false;
		List<String> locNames = new ArrayList<String>();
		List<String> selectedLocations = new ArrayList<String>();
		for (Location location : locations) {
			String name = location.getName();
			locNames.add(name);
			Object foundLoc = request.getParameterValues("location_" + name);
			if (foundLoc != null) {
				found = true;
				selectedLocations.add(name);
				map.put("checked_" + name, "true");
			} else {
				map.put("checked_" + name, "false");
			}
		}
		
		map.put("locations", locNames);
		
		String formIdStr = request.getParameter("formId");
		map.put("formId", formIdStr);
        Integer formId = Integer.parseInt(formIdStr);
		
		boolean scannableForm = false;
		String scanChoice = request.getParameter("scannableForm");
		if (scanChoice != null) {
			if ("Yes".equalsIgnoreCase(scanChoice)) {
				scannableForm = true;
			}
		}
		
		map.put("scannableForm", scannableForm);
		boolean scorableForm = false;
		String scoreChoice = request.getParameter("scorableForm");
		if (scoreChoice != null) {
			if ("Yes".equalsIgnoreCase(scoreChoice)) {
				scorableForm = true;
			}
		}
		
		map.put("scorableForm", scorableForm);		
		map.put("formName", formName);
		if (!found) {
			map.put("noLocationsChecked", true);
			return new ModelAndView(view, map);
		}
		
		AdministrationService adminService = Context.getAdministrationService();
		String driveLetter = adminService.getGlobalProperty("atd.driveLetter");
		try {	
			// Create the directories.
			createDirectories(formName, selectedLocations, scannableForm, driveLetter);
		} catch (Exception e) {			
			log.error("Error creating directories", e);
			map.put("failedCreateDirectories", true);
			return new ModelAndView(view, map);
		}
        
        // Copy form configuration
        ATDService atdService = Context.getService(ATDService.class);
        String formIdToCopy = request.getParameter("formToCopy");
        if (formIdToCopy != null && !"No Selection".equalsIgnoreCase(formIdToCopy)) {
        	atdService.copyFormMetadata(Integer.parseInt(formIdToCopy), formId);
        } else {
        	try {
	        	String serverName = adminService.getGlobalProperty("atd.serverName");
	        	String scoreConfigFile = null;
	        	if (request instanceof MultipartHttpServletRequest && scorableForm) {
	                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
	                MultipartFile xmlFile = multipartRequest.getFile("scoringFile");
	                if (xmlFile != null && !xmlFile.isEmpty()) {
	                	scoreConfigFile = loadScoringConfigFile(xmlFile, formName);
	                } else {
	                	map.put("missingScoringFile", true);
	                	// delete the directories
	        			deleteDirectories(formName, locNames, scannableForm);
	        			return new ModelAndView(view, map);
	                }
	            }
	        	
	        	Integer numPrioritizedFields = 0;
	        	String pFieldString = request.getParameter("numPrioritizedFields");
	        	if (pFieldString != null && pFieldString.trim().length() > 0) {
	        		numPrioritizedFields = Integer.parseInt(pFieldString);
	        	}
	        	
	        	FormService formService = Context.getFormService();
	        	Form printerCopyForm = formService.getForm(printerCopy);
	        	atdService.setupInitialFormValues(formId, formName, selectedLocations, driveLetter, 
	        		serverName, scannableForm, scorableForm, scoreConfigFile, numPrioritizedFields,
	        		printerCopyForm.getFormId());
        	}
        	catch (Exception e) {
                log.error("Error while getting scoring file from request", e);
                map.put("failedScoringFileUpload", true);
    			// delete the directories
    			deleteDirectories(formName, locNames, scannableForm);
    			return new ModelAndView(view, map);
            }
        }
        
        try {
        	ChirdlUtilService chirdlService = Context.getService(ChirdlUtilService.class);
        	LocationTagAttribute locTagAttr = new LocationTagAttribute();
        	locTagAttr.setName(formName);
        	locTagAttr = chirdlService.saveLocationTagAttribute(locTagAttr);
        	for (String locName : selectedLocations) {
        		Location loc = locService.getLocation(locName);
        		Set<LocationTag> tags = loc.getTags();
        		Iterator<LocationTag> iter = tags.iterator();
        		while (iter.hasNext()) {
        			LocationTag tag = iter.next();
        			LocationTagAttributeValue attrVal = new LocationTagAttributeValue();
        			attrVal.setLocationId(loc.getLocationId());
        			attrVal.setLocationTagAttributeId(locTagAttr.getLocationTagAttributeId());
        			attrVal.setLocationTagId(tag.getLocationTagId());
        			attrVal.setValue(String.valueOf(formId));
        			chirdlService.saveLocationTagAttributeValue(attrVal);
        		}
        	}
        } catch (Exception e) {
        	log.error("Error while creating data for the Chirdl location tag tables", e);
        	map.put("failedChirdlUpdate", true);
        	// delete the directories
			deleteDirectories(formName, locNames, scannableForm);
			// remove attribute values
			atdService.purgeFormAttributeValues(formId);
			return new ModelAndView(view, map);
        }
		
		view = getSuccessView();
		return new ModelAndView(
			new RedirectView(view), map);
	}

	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		LocationService locService = Context.getLocationService();
		List<Location> locations = locService.getAllLocations(false);
		List<String> locNames = new ArrayList<String>();
		for (Location location : locations) {
			boolean checkLocation = false;
			String name = location.getName();
			String checked = request.getParameter("checked_" + name);
			if (checked != null && "true".equalsIgnoreCase(checked)) {
				checkLocation = true;
			}
			
			locNames.add(name);
			map.put("checked_" + name, checkLocation);
		}
		
		map.put("locations", locNames);
		map.put("formName", request.getParameter("formName"));
		map.put("formId", request.getParameter("formId"));
		map.put("createWizard", checkParameter(request, "createWizard"));
		map.put("numPrioritizedFields", request.getParameter("numPrioritizedFields"));
		return map;
	}
	
	private void createDirectories(String formName, List<String> locations, boolean scannableForm, String driveLetter) 
	throws Exception {
		if (driveLetter == null) {
			throw new Exception("atd.driveLetter not specified.");
		}
		
		for (String locName : locations) {
			// Create the merge directory
			File mergeDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "merge" + 
				File.separator + locName + File.separator + formName + File.separator + "Pending");
			mergeDir.mkdirs();
			
			if (scannableForm) {
				// Create the scan directory
				File scanDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "scan" + 
					File.separator + locName + File.separator + formName + "_SCAN");
				scanDir.mkdirs();
				
				// Create the images directory
				File imageDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "images" + 
					File.separator + locName + File.separator + formName);
				imageDir.mkdirs();
			}
		}
	}
	
	private void deleteDirectories(String formName, List<String> locations, boolean scannableForm) throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		String driveLetter = adminService.getGlobalProperty("atd.driveLetter");
		
		for (String locName : locations) {
			// Create the merge directory
			File mergePendingDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "merge" + 
				File.separator + locName + File.separator + formName + File.separator + "Pending");
			File mergeDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "merge" + 
				File.separator + locName + File.separator + formName);
			if (mergePendingDir.exists()) {
				mergePendingDir.delete();
			}
			
			if (mergeDir.exists()) {
				mergeDir.delete();
			}
			
			if (scannableForm) {
				// Create the scan directory
				File scanDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "scan" + 
					File.separator + locName + File.separator + formName + "_SCAN");
				if (scanDir.exists()) {
					scanDir.delete();
				}
				
				// Create the images directory
				File imageDir = new File(driveLetter + ":" + File.separator + "chica" + File.separator + "images" + 
					File.separator + locName + File.separator + formName);
				if (imageDir.exists()) {
					imageDir.delete();
				}
			}
		}
	}
	
	private String loadScoringConfigFile(MultipartFile xmlFile, String formName) throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		String driveLetter = adminService.getGlobalProperty("atd.driveLetter");
		String xmlLoadDir = driveLetter + ":\\chica\\config\\" + formName + " scoring config";
		File loadDir = new File(xmlLoadDir);
		loadDir.mkdirs();
		String fileName = formName + "_scoring_config.xml";
		
		// Place the file in the forms to load directory
		InputStream in = xmlFile.getInputStream();
		File file = new File(loadDir, fileName);
		if (file.exists()) {
			file.delete();
		}
		
		OutputStream out = new FileOutputStream(file);
		int nextChar;
		try {
			while ((nextChar = in.read()) != -1) {
				out.write(nextChar);
			}
			
		} finally {
			if (in != null) {
				in.close();
			}
			
			if (out != null) {
				out.close();
			}
		}
		
		return file.getAbsolutePath();
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
