package org.openmrs.module.atd.web.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.atd.util.AtdConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(value = "module/atd/importConceptsFromFile.form")
public class ImportConceptsFromFileController { 
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Form view */
    private static final String FORM_VIEW = "/module/atd/importConceptsFromFile";
    
    @RequestMapping(method = RequestMethod.GET)
    protected String initForm(HttpServletRequest request, ModelMap map) throws Exception {

		return FORM_VIEW;
	}
	
    @RequestMapping(method = RequestMethod.POST)
    protected ModelAndView processSubmit(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		return new ModelAndView(new RedirectView(AtdConstants.FORM_VIEW_CONFIG_MANAGER_SUCCESS), map);
	}
}
