package org.openmrs.module.atd.ruleLibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttributeValue;
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
		Patient patient = patientService.getPatient(patientId);
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		String formName = (String) parameters.get("param1");
		Object param2Object = parameters.get("param2");
		
		Integer sessionId = (Integer) parameters.get("sessionId");
		if(sessionId != null){
			Integer locationTagId = (Integer) parameters.get("locationTagId"); 
			FormInstance formInstance = (FormInstance) parameters.get("formInstance");
			Integer locationId = formInstance.getLocationId();
			State currState = chirdlUtilBackportsService.getStateByName("JIT_create");
			
			try {
				HashMap<String,Object> actionParameters = new HashMap<String,Object>();
				if (param2Object != null && param2Object instanceof String){
					String trigger = (String) param2Object;
					actionParameters.put("trigger", trigger);
				}
				actionParameters.put("formName", formName);
            	StateManager.runState(patient, sessionId, currState,actionParameters,
            		locationTagId,
            		locationId,
            		BaseStateActionHandler.getInstance());
            }
            catch (Exception e) {
	            log.error("",e);
            }
		}	
		return Result.emptyResult();
	}
}