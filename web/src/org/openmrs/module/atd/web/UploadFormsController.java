package org.openmrs.module.atd.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.dss.util.IOUtil;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class UploadFormsController extends SimpleFormController
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
	protected Map referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		TeleformTranslator translator = new TeleformTranslator();
		AdministrationService adminService = Context.getAdministrationService();
		String property = adminService.getGlobalProperty("atd.formLoadDirectory");
		if(property == null){
			throw new Exception("You must set the atd.formLoadDirectory global property to upload forms.");
		}
		String baseDirectory = IOUtil.formatDirectoryName(property);

		int length = 0;
		
		if (request.getParameter("createForms") == null)
		{
			String[] fileExtensions = new String[2];
			fileExtensions[0] = ".xml";
			fileExtensions[1] = ".XML";
			File[] files = IOUtil.getFilesInDirectory(baseDirectory,fileExtensions);
			ArrayList<String> fileNames = new ArrayList<String>();
			
			if (files != null)
			{
				length = files.length;
				
				for(File file:files){
					fileNames.add(file.getName());
				}
			}
			
			map.put("files", fileNames.toArray());
		} else
		{
			length = Integer.parseInt(request.getParameter("numFiles"));
			String filename = null;
			String fileLocation = null;
			ArrayList<Form> newForms = new ArrayList<Form>();

			for (int i = 0; i < length; i++)
			{
				filename = request.getParameter("filename" + (i+1));
				fileLocation = request.getParameter("fileLocation" + (i+1));
				if (filename != null && fileLocation != null)
				{
					Form newForm = translator.templateXMLToDatabaseForm(
							filename, baseDirectory + fileLocation);
					newForms.add(newForm);
				}
			}
			map.put("createForms", true);
			map.put("newForms", newForms);
		}

		map.put("length", length);
		return map;
	}

}
