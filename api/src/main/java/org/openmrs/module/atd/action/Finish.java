/**
 * 
 */
package org.openmrs.module.atd.action;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.atd.datasource.FormDatasource;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 * @author tmdugan
 */
public class Finish implements ProcessStateAction {
	
	private static final Logger log = LoggerFactory.getLogger(Finish.class);
	
	/* (non-Javadoc)
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.atd.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient, PatientState patientState,
	                          HashMap<String, Object> parameters) {
		//lookup the patient again to avoid lazy initialization errors

		PatientService patientService = Context.getPatientService();
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);

		ChirdlUtilBackportsService chirdlUtilBackports = Context.getService(ChirdlUtilBackportsService.class);
		AdministrationService adminService = Context.getAdministrationService();
		Integer sessionId = patientState.getSessionId();
		
		LogicService logicService = Context.getLogicService();
		
		FormDatasource xmlDatasource = (FormDatasource) logicService.getLogicDataSource("form");
		try {
			Integer purgeXMLDatasourceProperty = null;
			try {
				purgeXMLDatasourceProperty = Integer.parseInt(adminService.getGlobalProperty("atd.purgeXMLDatasource"));
			}
			catch (Exception e) {}
			
			//purge parsed xml at the end of the session
			if (purgeXMLDatasourceProperty != null && purgeXMLDatasourceProperty == 2) {
				List<PatientState> formStates = chirdlUtilBackports.getPatientStatesWithForm(sessionId);
				for (PatientState state : formStates) {
					FormInstance formInstance = state.getFormInstance();
					
					if (formInstance != null) {
						xmlDatasource.deleteForm(formInstance);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Error occurred in getting form data source", e);
		} finally {
			StateManager.endState(patientState);
		}
	}
	
	public void changeState(PatientState patientState, HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}
	
}
