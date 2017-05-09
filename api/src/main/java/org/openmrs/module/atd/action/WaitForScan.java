/**
 * 
 */
package org.openmrs.module.atd.action;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformFileMonitor;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 * @author tmdugan
 * 
 */
public class WaitForScan implements ProcessStateAction {

	private static Log log = LogFactory.getLog(WaitForScan.class);

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
		// lookup the patient again to avoid lazy initialization errors
		PatientService patientService = Context.getPatientService();
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);

		FormInstance formInstance = (FormInstance) parameters.get("formInstance");
		if(formInstance == null){

		Integer sessionId = patientState.getSessionId();
		PatientState stateWithFormId = Util.getPrevProducePatientStateByAction(patientState, sessionId);

		formInstance = patientState.getFormInstance();

		if (formInstance == null && stateWithFormId != null) {
			formInstance = stateWithFormId.getFormInstance();
		}
		}
		patientState.setFormInstance(formInstance);
		chirdlutilbackportsService.updatePatientState(patientState);
		TeleformFileState teleformFileState = TeleformFileMonitor
				.addToPendingStatesWithoutFilename(formInstance);
		teleformFileState.addParameter("patientState", patientState);
	}

	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {

		StateManager.endState(patientState);
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		ATDService atdService = Context.getService(ATDService.class);

		try {

			FormInstance formInstance = patientState.getFormInstance();
			
			if(formInstance == null){
			Integer sessionId = patientState.getSessionId();
			PatientState stateWithFormId = Util.getPrevProducePatientStateByAction(patientState, sessionId);

			if (stateWithFormId != null) {
				formInstance = stateWithFormId.getFormInstance();
			}
			}
			Integer formId = formInstance.getFormId();
			FormService formService = Context.getFormService();
			Form form = formService.getForm(formId);
			String formName = form.getName();
			
			List<Statistics> statistics = atdService.getStatByFormInstance(
				formInstance.getFormInstanceId(), formName, patientState.getLocationId());

			for (Statistics currStat : statistics) {
				currStat.setScannedTimestamp(patientState.getEndTime());
				atdService.updateStatistics(currStat);
			}

			BaseStateActionHandler.changeState(patientState.getPatient(), patientState.getSessionId(),
					patientState.getState(), patientState.getState()
							.getAction(), parameters, patientState
							.getLocationTagId(), patientState.getLocationId());
		} catch (Exception e) {
			log.error("",e);
			log.error(org.openmrs.module.chirdlutil.util.Util.getStackTrace(e));
		}
	}

}
