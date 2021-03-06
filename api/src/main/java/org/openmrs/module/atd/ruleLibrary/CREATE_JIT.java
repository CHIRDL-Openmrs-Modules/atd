package org.openmrs.module.atd.ruleLibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Session;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

public class CREATE_JIT implements Rule
{
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList()
	{
		return null;
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getDependencies()
	 */
	public String[] getDependencies()
	{
		return new String[]
		{};
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.rule.Rule#getTTL()
	 */
	public int getTTL()
	{
		return 0; // 60 * 30; // 30 minutes
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype()
	{
		return Datatype.CODED;
	}

	/**
	 * *
	 * 
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, java.lang.Integer, java.util.Map)
	 */
	public Result eval(LogicContext context, Integer patientId,
			Map<String, Object> parameters) throws LogicException
	{
		PatientService patientService = Context.getPatientService();
		FormService formService = Context.getFormService();
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Patient patient = patientService.getPatient(patientId);
		String formName = (String) parameters.get(ChirdlUtilConstants.PARAMETER_1);
		Object triggerObject = parameters.get(ChirdlUtilConstants.PARAMETER_2);
		Object autoPrintObject = parameters.get(ChirdlUtilConstants.PARAMETER_3);
		Object ignoreJitCreatedObject = parameters.get(ChirdlUtilConstants.PARAMETER_4); 
		
		Integer sessionId = (Integer) parameters.get(ChirdlUtilConstants.PARAMETER_SESSION_ID);
		FormInstanceTag formInstTag = null;
		if (sessionId == null) {
			return Result.emptyResult();
		}
		
		Integer locationTagId = (Integer) parameters.get(ChirdlUtilConstants.PARAMETER_LOCATION_TAG_ID); 
		FormInstance formInstance = (FormInstance) parameters.get(ChirdlUtilConstants.PARAMETER_FORM_INSTANCE);
		Integer locationId = formInstance.getLocationId();
		State currState = getCreateState(formName, locationTagId, locationId);
		if (currState == null) {
			return Result.emptyResult();
		}
			
		try {
			
			HashMap<String,Object> actionParameters = new HashMap<String,Object>();
			if (triggerObject != null && triggerObject instanceof String){
				actionParameters.put(ChirdlUtilConstants.PARAMETER_TRIGGER, (String) triggerObject);
			}
			
			if (autoPrintObject != null && autoPrintObject instanceof String){
				actionParameters.put(ChirdlUtilConstants.PARAMETER_AUTO_PRINT, (String) autoPrintObject);
			}
			
			if (ignoreJitCreatedObject == null || !(ignoreJitCreatedObject instanceof String)
							|| !((String)ignoreJitCreatedObject).trim().equalsIgnoreCase(ChirdlUtilConstants.GENERAL_INFO_TRUE)){
						
				Session session = chirdlUtilBackportsService.getSession(sessionId);
				PatientState patientState = org.openmrs.module.atd.util.Util.getProducePatientStateByEncounterFormAction(session.getEncounterId(), formService.getForm(formName).getFormId());
							
				if (patientState != null){
					return Result.emptyResult();

				}
						
			}
						
			actionParameters.put(ChirdlUtilConstants.PARAMETER_FORM_NAME, formName);
        	PatientState patientState = StateManager.runState(patient, sessionId, currState,actionParameters,
        		locationTagId,
        		locationId,
        		BaseStateActionHandler.getInstance());
        	FormInstance formInst = patientState.getFormInstance();
        	if (formInst != null) {
        		formInstTag = new FormInstanceTag(formInst, locationTagId);
        	}
        }
        catch (Exception e) {
            log.error("Error creating JIT",e);
        }
	
		if (formInstTag != null) {
			return new Result(formInstTag.toString());
		}
		
		return Result.emptyResult();
	}

	protected State getCreateState(String formName, Integer locationTagId, Integer locationId) 
	{
		String stateName = ChirdlUtilConstants.STATE_JIT_CREATE;
		Form form = Context.getFormService().getForm(formName);
		if (form == null) {
			log.error("No form found with name: " + formName);
			return null;
		}
		
		// Check to see if the form is mobile only
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		FormAttributeValue fav = 
			chirdlUtilBackportsService.getFormAttributeValue(form.getFormId(), ChirdlUtilConstants.FORM_ATTR_MOBILE_ONLY, 
				locationTagId, locationId);
		if (fav != null && fav.getValue() != null && fav.getValue().equalsIgnoreCase(
			ChirdlUtilConstants.FORM_ATTR_VAL_TRUE)) {
			stateName = ChirdlUtilConstants.STATE_JIT_MOBILE_CREATE;
		}
		
		State currState = chirdlUtilBackportsService.getStateByName(stateName);
		return currState;
	}
}