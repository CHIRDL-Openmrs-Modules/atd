package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.atd.util.CreateFormUtil;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class CreateFormFromFileController extends SimpleFormController {
	
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
	protected Map referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		
		return map;
	}
	
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	                                             BindException errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String view = getFormView();
		
		try {
			// Load the Teleform file.
			if (request instanceof MultipartHttpServletRequest) {
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile dataFile = multipartRequest.getFile("dataFile");
				if (dataFile != null && !dataFile.isEmpty()) {
					String filename = dataFile.getOriginalFilename();
					int index = filename.lastIndexOf(".");
					if (index < 0) {
						map.put("incorrectExtension", true);
						return new ModelAndView(view, map);
					}
					
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("csv")) {
						map.put("incorrectExtension", true);
						return new ModelAndView(view, map);
					}
					
					//filename
					CreateFormUtil.createFormsFromCSVFile(dataFile.getInputStream());
					
				} else {
					map.put("missingFile", true);
					return new ModelAndView(view, map);
				}
			}
		}
		catch (Exception e) {
			log.error("Error while processing uploaded file from request", e);
			map.put("failedFileUpload", true);
			return new ModelAndView(view, map);
		}
		
		view = getSuccessView();
		return new ModelAndView(new RedirectView(view), map);
	}
}
