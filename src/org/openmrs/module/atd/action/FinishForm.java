/**
 * 
 */
package org.openmrs.module.atd.action;

import java.util.HashMap;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 * @author Steve McKee
 * 
 */
public class FinishForm implements ProcessStateAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction,
	 *      org.openmrs.Patient,
	 *      org.openmrs.module.atd.hibernateBeans.PatientState,
	 *      java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient,
		PatientState patientState, HashMap<String, Object> parameters){
		FormInstance formInstance = patientState.getFormInstance();
		if(formInstance == null) {
			Integer sessionId = patientState.getSessionId();
			PatientState stateWithFormId = Util.getPrevProducePatientStateByAction(patientState, sessionId);

			if (stateWithFormId != null) {
				formInstance = stateWithFormId.getFormInstance();
			}
			
			if (formInstance != null) {
				patientState.setFormInstance(formInstance);
				Context.getService(ChirdlUtilBackportsService.class).updatePatientState(patientState);
			}
		}
		
		StateManager.endState(patientState);
	}

	/**
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#changeState(org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {
		// ProcessAction changes the state
	}

}
