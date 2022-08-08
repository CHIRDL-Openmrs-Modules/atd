package org.openmrs.module.atd.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.AtdConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "module/atd/enableForm.form")
public class EnableFormController {

	/** Logger for this class and subclasses */
	private static final Logger log = LoggerFactory.getLogger(EnableFormController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {

		FormService formService = Context.getFormService();
		List<Form> forms = formService.getAllForms(false);
		
		map.put("forms", forms);
		
		return AtdConstants.FORM_ENABLE_FORM_VIEW;
	}

}
