package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.DataForm;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class DataFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		DataForm data = (DataForm)object;
		
		if (context != null) {
			if (data.getUsername().length() > 0) {
				if (data.getUsername().length() < 3) {
					errors.rejectValue("username", "error.username.weak");
				}
				if (data.getUsername().charAt(0) < 'A' || data.getUsername().charAt(0) > 'z') {
					errors.rejectValue("username", "error.username.invalid");
				}
				
			}
			if (data.getUsername().length() > 0)
			
			if (!data.getOldPassword().equals("")) {
				if (data.getNewPassword().equals(""))
					errors.rejectValue("newPassword", "error.password.weak");
				else if (!data.getNewPassword().equals(data.getConfirmPassword())) {
					errors.rejectValue("newPassword", "error.password.match");
					errors.rejectValue("confirmPassword", "error.password.match");
				}
			}
	
			if (!data.getSecretQuestionPassword().equals("")) {
				if (!data.getSecretAnswerConfirm().equals(data.getSecretAnswerNew())) {
					errors.rejectValue("secretAnswerNew", "error.options.secretAnswer.match");
					errors.rejectValue("secretAnswerConfirm", "error.options.secretAnswer.match");
				}
			}
			
			// TODO catch errors
		}
		
		return super.processFormSubmission(request, response, object, errors); 
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		String view = getFormView();
		
		if (context == null || !Context.isAuthenticated()) {
			errors.reject("auth.session.expired");
			return super.processFormSubmission(request, response, obj, errors);
		}

		if (!errors.hasErrors()) {
			User user = Context.getAuthenticatedUser();
			UserService us = Context.getUserService();
			DataForm data = (DataForm)obj;
			
			Map<String, String> properties = user.getUserProperties();
			
			properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, data.getDefaultLocation());
			properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, data.getDefaultLocale());
			properties.put(OpenmrsConstants.USER_PROPERTY_SHOW_RETIRED, data.getShowRetiredMessage().toString());
			properties.put(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE, data.getVerbose().toString());
			
			if (!data.getOldPassword().equals("")) {
				try {
					String password = data.getNewPassword();
					
					//check password strength
					if (password.length() > 0) {
						if (password.length() < 6)
							errors.reject("error.password.length");
						if (StringUtils.isAlpha(password))
							errors.reject("error.password.characters");
						if (password.equals(user.getUsername()) || password.equals(user.getSystemId()))
							errors.reject("error.password.weak");
						if (password.equals(data.getOldPassword()) && !errors.hasErrors())
							errors.reject("error.password.different");
					}
					
					if (!errors.hasErrors()) {
						us.changePassword(data.getOldPassword(), password);
						if (properties.containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD))
							properties.remove(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
					}
				}
				catch (APIException e) {
					errors.rejectValue("oldPassword", "error.password.match");
				}
			}
			
			if (!data.getSecretQuestionPassword().equals("") && !errors.hasErrors()) {
				try {
					user.setSecretQuestion(data.getSecretQuestionNew());
					us.changeQuestionAnswer(data.getSecretQuestionPassword(), data.getSecretQuestionNew(), data.getSecretAnswerNew());
				}
				catch (APIException e) {
					errors.rejectValue("secretQuestionPassword", "error.password.match");
				}
			}
			
			if (data.getUsername().length() > 0 && !errors.hasErrors()) {
				Context.addProxyPrivilege("View Users");
				if (us.hasDuplicateUsername(user)) {
					errors.rejectValue("username", "error.username.taken");
				}
			}
			
			if (!errors.hasErrors()) {
				user.setUsername(data.getUsername());
				user.setUserProperties(properties);
				
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
				us.saveUser(user,null);
				Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
				
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "options.saved");
			}
			else {
				return super.processFormSubmission(request, response, data, errors);
			}
			
			view = getSuccessView();
		}
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    @Override
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		DataForm data = new DataForm();
		
		if (context != null && Context.isAuthenticated()) {
			User user = Context.getAuthenticatedUser();

			Map<String, String> props = user.getUserProperties();
			data.setDefaultLocation(props.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION));
			data.setDefaultLocale(props.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE));
			data.setShowRetiredMessage(new Boolean(props.get(OpenmrsConstants.USER_PROPERTY_SHOW_RETIRED)));
			data.setVerbose(new Boolean(props.get(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE)));
			data.setUsername(user.getUsername());
			data.setSecretQuestionNew(user.getSecretQuestion());
		}
		
		return data;
    }
    
	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		LocationService locationService = Context.getLocationService();
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && Context.isAuthenticated()) {
			
			EncounterService encounterService = Context.getEncounterService();
			
			// set location options
			map.put("locations", locationService.getAllLocations());
			
			// set language/locale options
			map.put("languages", org.openmrs.util.OpenmrsConstants.OPENMRS_LOCALES());
			
			String resetPassword = (String)httpSession.getAttribute("resetPassword");
			if (resetPassword==null)
				resetPassword = "";
			else
				httpSession.removeAttribute("resetPassword");
			map.put("resetPassword", resetPassword);
			
		}
		
		return map;
	} 
}