/**
 * 
 */
package org.openmrs.module.atd.action;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.atd.datasource.TeleformExportXMLDatasource;
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
	
	private Log log = LogFactory.getLog(this.getClass());
	
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
		
		TeleformExportXMLDatasource xmlDatasource = (TeleformExportXMLDatasource) logicService.getLogicDataSource("xml");
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
						xmlDatasource.deleteParsedFile(formInstance);
					}
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
			log.error(e);
		} finally {
			StateManager.endState(patientState);
		}
	}
	
	public void changeState(PatientState patientState, HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}
	
}
