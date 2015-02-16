package org.openmrs.module.atd.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConfigurationManagerController extends SimpleFormController {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {
		return "testing";
	}
}
