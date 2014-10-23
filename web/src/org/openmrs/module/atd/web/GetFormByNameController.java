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
import org.openmrs.module.atd.util.CreateFormUtil;
import org.openmrs.module.atd.util.FormAttributeValueDescriptor;
import org.openmrs.module.atd.util.FormDescriptor;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


public class GetFormByNameController extends SimpleFormController {
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing"; 
	}
	
	
	

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		String editType = request.getParameter("editType");
		Map<String, Object> map = new HashMap<String, Object>();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		FormService fs =Context.getFormService();
		List<FormAttributeValueDescriptor> favdList=null;
		String view = null;
		String backView = getFormView();
		if(editType==null || editType.equals("")){
			map.put("typeNotChosen", "true");
			return new ModelAndView(backView, map);
		}
		if(editType.equals("manual")){
			view = "chooseLocation.form";
			String formName = request.getParameter("formName");
			Form form = fs.getForm(formName);
			if(form==null){
				map.put("noSuchName", "true");
				return new ModelAndView(backView, map);
			}
			map.put("formId", form.getId());
			return new ModelAndView(new RedirectView(view),map);  
		}else{
			view = "showFormAttributeValue.form";
			if(request instanceof MultipartHttpServletRequest){
				MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
				MultipartFile csvFile = multipartRequest.getFile("csvFile");
				if(csvFile!=null && !csvFile.isEmpty()){
					String filename = csvFile.getOriginalFilename();
					InputStream input = csvFile.getInputStream();
					int index = filename.lastIndexOf(".");
					if (index < 0) {
						map.put("csvFileError", "typeError");
						return new ModelAndView(backView, map);
					}
					String extension = filename.substring(index + 1, filename.length());
					if (!extension.equalsIgnoreCase("csv")) {
						map.put("csvFileError", "typeError");
						return new ModelAndView(backView, map);
					}
					try{
						favdList = Util.getFormAttributeValueDescriptorFromCSV(input);
					}catch(Exception e){
						map.put("ioError", true);
						return new ModelAndView(backView, map);
					}
					//mv.addObject("favdList", favdList);
					//request.getSession().setAttribute("favdList", favdList);
					List<FormAttributeValue>  favList = Util.getFormAttributeValues(favdList);
					if(favList!=null){
						for(FormAttributeValue fav: favList){
							cubService.saveFormAttributeValue(fav);
						}
					}
					
					return new ModelAndView(new RedirectView(getSuccessView()),map);
				}else{
					map.put("csvFileError", "csvFileEmpty");
					return new ModelAndView(backView, map);
				}
				
			}else{
				map.put("csvFileError", "notMultipart");
				return new ModelAndView(backView, map);
			}
		}
	}
}
