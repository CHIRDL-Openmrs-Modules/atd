package org.openmrs.module.atd.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.springframework.web.multipart.MultipartFile;


public class ConfigManagerUtil {

	public static Form loadTeleformXmlFile(MultipartFile xmlFile, String formName) throws Exception {
		Form form = null;
		AdministrationService adminService = Context.getAdministrationService();
		TeleformTranslator translator = new TeleformTranslator();
		String formLoadDir = adminService.getGlobalProperty("atd.formLoadDirectory");
		// Place the file in the forms to load directory
		InputStream in = xmlFile.getInputStream();
		File file = new File(formLoadDir, formName + ".xml");
		if (file.exists()) {
			file.delete();
		}
		
		OutputStream out = new FileOutputStream(file);
		int nextChar;
		try {
			while ((nextChar = in.read()) != -1) {
				out.write(nextChar);
			}
			
			// Load the XML file
			form = translator.templateXMLToDatabaseForm(formName, file.getAbsolutePath());
		}
		finally {
			if (in != null) {
				in.close();
			}
			
			if (out != null) {
				out.close();
			}
		}
		
		return form;
	}
	
	public static void createFormDirectories(String formName, List<String> locations, boolean scannableForm, String driveLetter) 
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
	
	public static void deleteFormDirectories(String formName, List<String> locations, boolean scannableForm) throws Exception {
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
	
	public static String loadFormScoringConfigFile(MultipartFile xmlFile, String formName) throws Exception {
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
	
	public static void deleteForm(Integer formId) {
		FormService formService = Context.getFormService();
		ATDService atdService = Context.getService(ATDService.class);
		//delete the form attribute values
		atdService.purgeFormAttributeValues(formId);
		
		Form form = formService.getForm(formId);
		if (form != null) {
			//delete the form
			formService.purgeForm(form);
			
			//delete the orphaned fields
			for(FormField currFormField: form.getFormFields()) {
				formService.purgeField(currFormField.getField());
			}
			
			// delete from Chirdl Util tables
			ChirdlUtilService chirdlService = Context.getService(ChirdlUtilService.class);
			LocationTagAttribute attr = chirdlService.getLocationTagAttribute(form.getName());
			if (attr != null) {
				chirdlService.deleteLocationTagAttribute(attr);
			}
		}
	}
}
