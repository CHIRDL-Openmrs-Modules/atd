package org.openmrs.module.atd.web;

import java.util.HashMap;
import java.util.List;
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
import org.openmrs.module.atd.RuleBuilderForm;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class RuleBuilderFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		RuleBuilderForm rule = (RuleBuilderForm)object;
		
		if (context != null) {
			if (rule.getUsername().length() > 0) {
				if (rule.getUsername().length() < 3) {
					errors.rejectValue("username", "error.username.weak");
				}
				if (rule.getUsername().charAt(0) < 'A' || rule.getUsername().charAt(0) > 'z') {
					errors.rejectValue("username", "error.username.invalid");
				}
				
			}
			if (rule.getUsername().length() > 0)
			
			if (!rule.getOldPassword().equals("")) {
				if (rule.getNewPassword().equals(""))
					errors.rejectValue("newPassword", "error.password.weak");
				else if (!rule.getNewPassword().equals(rule.getConfirmPassword())) {
					errors.rejectValue("newPassword", "error.password.match");
					errors.rejectValue("confirmPassword", "error.password.match");
				}
			}
	
			if (!rule.getSecretQuestionPassword().equals("")) {
				if (!rule.getSecretAnswerConfirm().equals(rule.getSecretAnswerNew())) {
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
			RuleBuilderForm rule = (RuleBuilderForm)obj;
			
			Map<String, String> properties = user.getUserProperties();
			
			properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, rule.getDefaultLocation());
			properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, rule.getDefaultLocale());
			properties.put(OpenmrsConstants.USER_PROPERTY_SHOW_RETIRED, rule.getShowRetiredMessage().toString());
			properties.put(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE, rule.getVerbose().toString());
			
			if (!rule.getOldPassword().equals("")) {
				try {
					String password = rule.getNewPassword();
					
					//check password strength
					if (password.length() > 0) {
						if (password.length() < 6)
							errors.reject("error.password.length");
						if (StringUtils.isAlpha(password))
							errors.reject("error.password.characters");
						if (password.equals(user.getUsername()) || password.equals(user.getSystemId()))
							errors.reject("error.password.weak");
						if (password.equals(rule.getOldPassword()) && !errors.hasErrors())
							errors.reject("error.password.different");
					}
					
					if (!errors.hasErrors()) {
						us.changePassword(rule.getOldPassword(), password);
						if (properties.containsKey(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD))
							properties.remove(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
					}
				}
				catch (APIException e) {
					errors.rejectValue("oldPassword", "error.password.match");
				}
			}
			
			if (!rule.getSecretQuestionPassword().equals("") && !errors.hasErrors()) {
				try {
					user.setSecretQuestion(rule.getSecretQuestionNew());
					us.changeQuestionAnswer(rule.getSecretQuestionPassword(), rule.getSecretQuestionNew(), rule.getSecretAnswerNew());
				}
				catch (APIException e) {
					errors.rejectValue("secretQuestionPassword", "error.password.match");
				}
			}
			
			if (rule.getUsername().length() > 0 && !errors.hasErrors()) {
				Context.addProxyPrivilege("View Users");
				if (us.hasDuplicateUsername(user)) {
					errors.rejectValue("username", "error.username.taken");
				}
			}
			
			if (!errors.hasErrors()) {
				user.setUsername(rule.getUsername());
				user.setUserProperties(properties);
				
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
				us.saveUser(user,null);
				Context.removeProxyPrivilege(OpenmrsConstants.PRIV_EDIT_USERS);
				
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "options.saved");
			}
			else {
				return super.processFormSubmission(request, response, rule, errors);
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
		
		RuleBuilderForm rule = new RuleBuilderForm();
		
		if (context != null && Context.isAuthenticated()) {
			User user = Context.getAuthenticatedUser();

			Map<String, String> props = user.getUserProperties();
			rule.setDefaultLocation(props.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION));
			rule.setDefaultLocale(props.get(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE));
			rule.setShowRetiredMessage(new Boolean(props.get(OpenmrsConstants.USER_PROPERTY_SHOW_RETIRED)));
			rule.setVerbose(new Boolean(props.get(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE)));
			rule.setUsername(user.getUsername());
			rule.setSecretQuestionNew(user.getSecretQuestion());
		}
		
		return rule;
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
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (context != null && Context.isAuthenticated()) {
			LocationService locationService = Context.getLocationService();
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
			setUpSearchRules(map,request);
			
		}
		
		return map;
	} 
	
	private void setUpSearchRules(Map<String, Object> map,HttpServletRequest request)
	{
		Rule rule = new Rule();

		String title = request.getParameter("title");
		if (title == null || title.length() == 0)
		{
			title = "";
		} else
		{
			rule.setTitle(title);
		}
		map.put("title", title);
		
		String author = request.getParameter("author");
		if (author == null || author.length() == 0)
		{
			author = "";
		} else
		{
			rule.setAuthor(author);
		}
		map.put("author", author);

		String keywords = request.getParameter("keywords");
		if (keywords == null || keywords.length() == 0)
		{
			keywords = "";
		} else
		{
			rule.setKeywords(keywords);
		}
		map.put("keywords", keywords);

		String ruleType = request.getParameter("ruleType");
		if (ruleType == null || ruleType.length() == 0)
		{
			ruleType = "";
		} else
		{
			rule.setRuleType(ruleType);
		}
		map.put("ruleType", ruleType);
		
		String action = request.getParameter("action");
		if (action == null || action.length() == 0)
		{
			action = "";
		} else
		{
			rule.setAction(action);
		}
		map.put("action", action);

		String logic = request.getParameter("logic");
		if (logic == null || logic.length() == 0)
		{
			logic = "";
		} else
		{
			rule.setLogic(logic);
		}
		map.put("logic", logic);
		
		String data = request.getParameter("data");
		if (data == null || data.length() == 0)
		{
			data = "";
		} else
		{
			rule.setData(data);
		}
		map.put("data", data);
		
		String links = request.getParameter("links");
		if (links == null || links.length() == 0)
		{
			links = "";
		} else
		{
			rule.setLinks(links);
		}
		map.put("links", links);
		
		String citations = request.getParameter("citations");
		if (citations == null || citations.length() == 0)
		{
			citations = "";
		} else
		{
			rule.setCitations(citations);
		}
		map.put("citations", citations);
		
		String explanation = request.getParameter("explanation");
		if (explanation == null || explanation.length() == 0)
		{
			explanation = "";
		} else
		{
			rule.setExplanation(explanation);
		}
		map.put("explanation", explanation);
		
		String purpose = request.getParameter("purpose");
		if (purpose == null || purpose.length() == 0)
		{
			purpose = "";
		} else
		{
			rule.setPurpose(purpose);
		}
		map.put("purpose", purpose);
		
		String specialist = request.getParameter("specialist");
		if (specialist == null || specialist.length() == 0)
		{
			specialist = "";
		} else
		{
			rule.setSpecialist(specialist);
		}
		map.put("specialist", specialist);
		
		String institution = request.getParameter("institution");
		if (institution == null || institution.length() == 0)
		{
			institution = "";
		} else
		{
			rule.setInstitution(institution);
		}
		map.put("institution", institution);
		
		String classFilename = request.getParameter("classFilename");
		if (classFilename == null || classFilename.length() == 0)
		{
			classFilename = "";
		} else
		{
			rule.setClassFilename(classFilename);
		}
		map.put("classFilename", classFilename);
		
		boolean runSearch = false;

		if (request.getParameter("runSearch") != null
				&& request.getParameter("runSearch").length() > 0)
		{
			runSearch = true;
		}

		if (runSearch)
		{
			DssService dssService = Context
					.getService(DssService.class);
			List<Rule> rules = dssService.getRules(rule,true,true,null);
			map.put("rules", rules);
		}
		map.put("runSearch", runSearch);

	}
}