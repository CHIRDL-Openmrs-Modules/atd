package org.openmrs.module.atd.web;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.FormAttributeValueDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 
 * @author wang417
 * Controller for getFormByName.form
 */
public class GetFormByNameController extends SimpleFormController {
	protected final Log log = LogFactory.getLog(getClass());
	protected final static String CHOOSE_FORMS_OPTION = "-1";
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing"; 
	}
	
	
	

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		String editType = request.getParameter("editType");
		Map<String, Object> map = new HashMap<String, Object>();
		
		// DWE CHICA-280 4/1/15 Made a few minor changes to this method, mainly to handle the formNameSelect drop-down
		// Also cleaned up some of the code to keep the scope of the variables so they are only available if needed
		String view = null;
		String backView = getFormView();
		if(editType==null || editType.equals("")){
			map.put("typeNotChosen", "true");
			return new ModelAndView(backView, map);
		}
		map.put("selectedOption", editType);
		if(editType.equals("manual")){
			view = "chooseLocation.form";
			String selectedFormId = request.getParameter("formNameSelect");
			if(selectedFormId != null)
			{
				try
				{
					int formId = Integer.parseInt(selectedFormId);
					
					// Verify that this is a valid formId
					FormService fs =Context.getFormService();
					Form form = fs.getForm(formId);
					
					if(form != null)
					{
						map.put("formId", form.getId());
						map.put("selectedFormName", form.getName());
						return new ModelAndView(new RedirectView(view),map);
					}
					else
					{
						map.put("noSuchName", "true");
						reloadValues(request, map);
						return new ModelAndView(backView, map);
					}
					
				}
				catch(Exception e)
				{
					log.error("Error in processFormSubmission().", e);
					reloadValues(request, map);
					return new ModelAndView(backView, map);
				}	  
			}
			else
			{
				reloadValues(request, map);
				return new ModelAndView(backView, map);
			}
		}else{
			if(request instanceof MultipartHttpServletRequest){
				ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
				List<FormAttributeValueDescriptor> favdList=null;
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile csvFile = multipartRequest.getFile("csvFile");
				if(csvFile!=null && !csvFile.isEmpty()){
					String filename = csvFile.getOriginalFilename();
					InputStream input = csvFile.getInputStream();
					int index = filename.lastIndexOf(".");
					if (index < 0) {
						map.put("csvFileError", "typeError");
						reloadValues(request, map);
						return new ModelAndView(backView, map);
					}
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("csv")) {
						map.put("csvFileError", "typeError");
						reloadValues(request, map);
						return new ModelAndView(backView, map);
					}
					try{
						favdList = Util.getFormAttributeValueDescriptorFromCSV(input);
					}catch(Exception e){
						map.put("ioError", true);
						reloadValues(request, map);
						return new ModelAndView(backView, map);
					}
					
					List<FormAttributeValue>  favList = Util.getFormAttributeValues(favdList);
					if(favdList.size()!=0 && favList.size()==0){
						map.put("csvFileError", "notFAV");
						reloadValues(request, map);
						return new ModelAndView(backView, map);
					}
					if(favList!=null){
						for(FormAttributeValue fav: favList){
							cubService.saveFormAttributeValue(fav);
						}
					}
					map.put("operationType", "Import form attribute values");
					return new ModelAndView(new RedirectView(getSuccessView()),map);
				}else{
					map.put("csvFileError", "csvFileEmpty");
					reloadValues(request, map);
					return new ModelAndView(backView, map);
				}
				
			}else{
				map.put("csvFileError", "notMultipart");
				reloadValues(request, map);
				return new ModelAndView(backView, map);
			}
		}
	}
	
	/**
	 * DWE CHICA-331 4/10/15
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		request.setAttribute("selectedFormId", CHOOSE_FORMS_OPTION);
		request.setAttribute("selectedOption", "manual");
		
		reloadValues(request, map);
		
		return map;
	}
	
	/**
	 * DWE CHICA-331 4/10/15
	 * 
	 * Currently only used to reload the values for the "Form name" drop-down
	 */
	private void reloadValues(HttpServletRequest request, Map<String, Object> map)
	{
		// Reload the values for the "Form name" drop-down
		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put("forms", forms);
		
		request.setAttribute("chooseFormOptionConstant", CHOOSE_FORMS_OPTION);
	}
}
