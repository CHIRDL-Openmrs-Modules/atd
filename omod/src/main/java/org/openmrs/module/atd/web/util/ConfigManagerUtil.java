package org.openmrs.module.atd.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.CreateFormUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttribute;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.web.multipart.MultipartFile;


public class ConfigManagerUtil {

	public static Form loadTeleformFile(MultipartFile multipartFile, String formName) throws Exception {
		Form form = null;
		AdministrationService adminService = Context.getAdministrationService();
		TeleformTranslator translator = new TeleformTranslator();
		String formLoadDir = adminService.getGlobalProperty("atd.formLoadDirectory");
		String filename = multipartFile.getOriginalFilename();
		int index = filename.lastIndexOf(".");
		String extension = filename.substring(index + 1, filename.length());
		// Place the file in the forms to load directory
		File file = new File(formLoadDir, formName + ".xml");
		if (file.exists()) {
			file.delete();
		}
		
		if (extension.equalsIgnoreCase("xml")) {
			multipartFile.transferTo(file);
		} else if (extension.equalsIgnoreCase("fxf") || extension.equalsIgnoreCase("zip") ||
				extension.equalsIgnoreCase("jar")) {
			loadZippedFormFile(adminService, multipartFile, file);
		} else {
			throw new IllegalArgumentException("Unknown file type");
		}
		
		// Load the XML file
		form = translator.templateXMLToDatabaseForm(formName, file.getAbsolutePath());
		return form;
	}
	
	/**
	 * Loads a form via the CSV file format.
	 * 
	 * @param multipartFile MultipartFile object containing the CSV file information.
	 * @param formName The name of the database form.
	 * @return The database Form object.
	 * @throws Exception
	 */
	public static Form loadFormFromCSVFile(MultipartFile multipartFile, String formName) throws Exception {
		Form form = null;
		String formLoadDir = Context.getAdministrationService().getGlobalProperty("atd.formLoadDirectory");
		// Place the file in the forms to load directory
		File file = new File(formLoadDir, formName + ".csv");
		if (file.exists()) {
			file.delete();
		}
		
		multipartFile.transferTo(file);
		
		// Load the CSV file
		form = CreateFormUtil.createFormFromCSVFile(new FileInputStream(file));
		return form;
	}
	
	public static void createFormDirectories(String formName, List<String> locations, boolean faxableForm, 
	                                         boolean scannableForm, String installationDirectory) throws Exception {
		if (installationDirectory == null) {
			throw new Exception("atd.installationDirectory not specified.");
		}
		
		for (String locName : locations) {
			// Create the merge directory
			File mergeDir = new File(installationDirectory + File.separator + "merge" + 
				File.separator + locName + File.separator + formName + File.separator + "Pending");
			mergeDir.mkdirs();
			
			if (scannableForm) {
				// Create the scan directory
				File scanDir = new File(installationDirectory + File.separator + "scan" + 
					File.separator + locName + File.separator + formName + "_SCAN");
				scanDir.mkdirs();
				
				// Create the images directory
				File imageDir = new File(installationDirectory + File.separator + "images" + 
					File.separator + locName + File.separator + formName);
				imageDir.mkdirs();
			} else if (faxableForm) {
				// Create the scan directory
				File scanDir = new File(installationDirectory + File.separator + "scan" + 
					File.separator + "Fax" + File.separator + formName + "_SCAN");
				scanDir.mkdirs();
				
				// Create the images directory
				File imageDir = new File(installationDirectory + File.separator + "images" + 
					File.separator + "Fax" + File.separator + formName);
				imageDir.mkdirs();
			}
		}
	}
	
	public static void deleteFormDirectories(String formName, List<String> locations, boolean faxableForm, 
	                                         boolean scannableForm) throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		String installationDirectory = adminService.getGlobalProperty("atd.installationDirectory");
		
		for (String locName : locations) {
			// Create the merge directory
			File mergePendingDir = new File(installationDirectory + File.separator + "merge" + 
				File.separator + locName + File.separator + formName + File.separator + "Pending");
			File mergeDir = new File(installationDirectory + File.separator + "merge" + 
				File.separator + locName + File.separator + formName);
			if (mergePendingDir.exists()) {
				mergePendingDir.delete();
			}
			
			if (mergeDir.exists()) {
				mergeDir.delete();
			}
			
			if (scannableForm) {
				// Delete the scan directory
				File scanDir = new File(installationDirectory + File.separator + "scan" + 
					File.separator + locName + File.separator + formName + "_SCAN");
				if (scanDir.exists()) {
					scanDir.delete();
				}
				
				// Delete the images directory
				File imageDir = new File(installationDirectory + File.separator + "images" + 
					File.separator + locName + File.separator + formName);
				if (imageDir.exists()) {
					imageDir.delete();
				}
			} else if (faxableForm) {
				// Delete the scan directory
				File scanDir = new File(installationDirectory + File.separator + "scan" + 
					File.separator + "Fax" + File.separator + formName + "_SCAN");
				if (scanDir.exists()) {
					scanDir.delete();
				}
				
				// Delete the images directory
				File imageDir = new File(installationDirectory + File.separator + "images" + 
					File.separator + "Fax" + File.separator + formName);
				if (imageDir.exists()) {
					imageDir.delete();
				}
			}
		}
	}
	
	public static String loadFormScoringConfigFile(MultipartFile xmlFile, String formName) throws Exception {
		AdministrationService adminService = Context.getAdministrationService();
		String installationDirectory = adminService.getGlobalProperty("atd.installationDirectory");
		String xmlLoadDir = installationDirectory + "\\config\\" + formName + " scoring config";
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
			ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
			LocationTagAttribute attr = chirdlutilbackportsService.getLocationTagAttribute(form.getName());
			if (attr != null) {
				chirdlutilbackportsService.deleteLocationTagAttribute(attr);
			}
		}
	}
	
	private static void loadZippedFormFile(AdministrationService adminService, MultipartFile multipartFile, File outFile) 
	throws IOException {
		File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".zip");
		OutputStream outStream = null;
		InputStream inStream = null;
		try {
			multipartFile.transferTo(tempFile);
			ZipFile zipFile = new ZipFile(tempFile);
			String formFilename = adminService.getGlobalProperty("atd.TeleformFormFileName");
			ZipEntry entry = zipFile.getEntry(formFilename);
			if (entry == null) {
				throw new IOException("No " + formFilename + " file found in: " + multipartFile.getOriginalFilename());
			}
			
			inStream = zipFile.getInputStream(entry);
			outStream = new FileOutputStream(outFile);
			byte buf[]=new byte[1024];
		    int len;
		    while((len = inStream.read(buf)) > 0) {
		    	outStream.write(buf,0,len);
		    }
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
			if (outStream != null) {
				outStream.close();
			}
			if (inStream != null) {
				inStream.close();
			}
		}
	}
}