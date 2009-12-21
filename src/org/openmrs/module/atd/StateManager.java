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
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Program;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateAction;
import org.openmrs.module.atd.hibernateBeans.StateMapping;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.Util;

/**
 * @author Tammy Dugan
 * 
 */
public class StateManager
{
	private static Log log = LogFactory.getLog(StateManager.class);
		
	/**
	 * Changes from the current state to the next state
	 * as determined by the atd_state_mapping table
	 * @param patient Patient that is being processed
	 * @param sessionId current atd session for the Patient
	 * @param currState current state of the atd process
	 * @return
	 */
	public static PatientState changeState(Patient patient, Integer sessionId,
			State currState, Program program,HashMap<String,Object> parameters,
			Integer locationTagId,Integer locationId,StateActionHandler stateActionHandler)
	{
		AdministrationService adminService = Context.getAdministrationService();
		ATDService atdService = Context
				.getService(ATDService.class);
		PatientState patientState = null;
		//look program back up since we are crossing sessions
		program = atdService.getProgram(program.getProgramId());
		final State END_STATE = program.getEndState();

		StateMapping currMapping = null;

		final State START_STATE = program.getStartState();
		
		// start at the initial state or move to the next one
		if (currState == null)
		{
			currState = START_STATE;
		} else
		{
			currMapping = atdService.getStateMapping(currState,program);
			if(currMapping != null){
				currState = currMapping.getNextState();
			}
		}

		currMapping = atdService.getStateMapping(currState,program);

		patientState = atdService.addPatientState(patient, currState,
				sessionId,locationTagId,locationId);

		if (currMapping != null)
		{
			processStateAction(currState.getAction(), patient, patientState,
					program,parameters,stateActionHandler);
		} else
		{
			endState(patientState);
		}

		if (END_STATE != null && currState.getName().equalsIgnoreCase(END_STATE.getName()))
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
						FormInstance formInstance = state.getFormInstance();
						
						if(formInstance != null){
							xmlDatasource.deleteParsedFile(formInstance);
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
			HashMap<String,Object> parameters,StateActionHandler stateActionHandler)
	{
		if (stateAction == null)
		{
			endState(patientState);
			changeState(patient, patientState.getSessionId(), patientState
					.getState(),program,parameters,patientState.getLocationTagId(),
					patientState.getLocationId(),stateActionHandler);
			return;
		}
		stateActionHandler.processAction(stateAction,patient,patientState,parameters);
	}
	
	public static PatientState runState(Patient patient, Integer sessionId,
			State currState,HashMap<String,Object> parameters,
			Integer locationTagId,Integer locationId,
			StateActionHandler stateActionHandler)
	{
		ATDService atdService = Context.getService(ATDService.class);
		StateAction stateAction = currState.getAction();
		PatientState patientState = atdService.addPatientState(patient,
				currState, sessionId,locationTagId,locationId);

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

}
