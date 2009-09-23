/**
 * 
 */
package org.openmrs.module.atd;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.atd.datasource.TeleformExportXMLDatasource;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Program;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateAction;
import org.openmrs.module.atd.hibernateBeans.StateMapping;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.dss.util.Util;

/**
 * @author Tammy Dugan
 * 
 */
public class StateManager
{
	private static Log log = LogFactory.getLog(StateManager.class);
	
	private static StateActionHandler stateActionHandler = null;
	
	/**
	 * Changes from the current state to the next state
	 * as determined by the atd_state_mapping table
	 * @param patient Patient that is being processed
	 * @param sessionId current atd session for the Patient
	 * @param currState current state of the atd process
	 * @return
	 * @throws Exception
	 */
	public static PatientState changeState(Patient patient, Integer sessionId,
			State currState, Program program,HashMap<String,Object> parameters) throws Exception
	{
		AdministrationService adminService = Context.getAdministrationService();
		ATDService atdService = Context
				.getService(ATDService.class);
		PatientState patientState = null;
		Integer formId = null;
		final String FINAL_STATE = adminService.getGlobalProperty("atd.finalState");

		StateMapping currMapping = null;

		// start at the initial state or move to the next one
		if (currState == null)
		{
			currState = atdService.getStateByName(adminService.getGlobalProperty("atd.initialState"));
		} else
		{
			currMapping = atdService.getStateMapping(currState,program);
			if(currMapping != null){
				currState = currMapping.getNextState();
			}
		}

		currMapping = atdService.getStateMapping(currState,program);

		formId = currState.getFormId();

		patientState = atdService.addPatientState(patient, currState,
				sessionId, formId);

		if (currMapping != null)
		{
			processStateAction(currState.getAction(), patient, patientState,
					program,parameters);
		} else
		{
			endState(patientState);
		}

		if (FINAL_STATE != null && currState.getName().equalsIgnoreCase(FINAL_STATE))
		{
			LogicService logicService = Context.getLogicService();

			TeleformExportXMLDatasource xmlDatasource = 
				(TeleformExportXMLDatasource) logicService
					.getLogicDataSource("xml");
			try
			{
				Integer purgeXMLDatasourceProperty=null;
				try
				{
					purgeXMLDatasourceProperty = Integer
							.parseInt(adminService
									.getGlobalProperty("atd.purgeXMLDatasource"));
				} catch (Exception e)
				{
				}

				//purge parsed xml at the end of the session
				if (purgeXMLDatasourceProperty!=null&&
						purgeXMLDatasourceProperty == 2)
				{
					List<PatientState> formStates = atdService
							.getPatientStatesWithForm(sessionId);
					for (PatientState state : formStates)
					{
						Integer formInstanceId = state.getFormInstanceId();
						formId = state.getFormId();
						if(formInstanceId != null){
							xmlDatasource.deleteParsedFile(formInstanceId, formId);
						}
					}
				}
			} catch (Exception e)
			{
				log.error(e.getMessage());
				log.error(Util.getStackTrace(e));
			}
		}
		
		return patientState;
	}

	private static void processStateAction(StateAction stateAction,
			Patient patient, PatientState patientState,Program program,
			HashMap<String,Object> parameters)
			throws Exception
	{
		if (stateAction == null)
		{
			endState(patientState);
			changeState(patient, patientState.getSessionId(), patientState
					.getState(),program,parameters);
			return;
		}
		stateActionHandler.processAction(stateAction,patient,patientState,parameters);
	}
	
	public static PatientState runState(Patient patient, Integer sessionId,
			State currState,HashMap<String,Object> parameters)throws Exception
	{
		ATDService atdService = Context.getService(ATDService.class);
		Integer formId = currState.getFormId();
		StateAction stateAction = currState.getAction();
		PatientState patientState = atdService.addPatientState(patient,
				currState, sessionId, formId);

		stateActionHandler.processAction(stateAction, patient, patientState,parameters);
		return patientState;
	}

	public static void endState(PatientState patientState)
	{
		ATDService atdService = Context
				.getService(ATDService.class);
		patientState.setEndTime(new java.util.Date());
		atdService.updatePatientState(patientState);// set the end time for
							// the initial state
	}

	public static StateActionHandler getStateActionHandler()
	{
		return stateActionHandler;
	}

	public static void setStateActionHandler(StateActionHandler stateActionHandler)
	{
		StateManager.stateActionHandler = stateActionHandler;
	}
}
