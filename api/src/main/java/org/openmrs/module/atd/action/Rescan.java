/**
 * 
 */
package org.openmrs.module.atd.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Session;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;

/**
 * @author tmdugan
 *
 */
public class Rescan implements ProcessStateAction
{

	/* (non-Javadoc)
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.atd.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState, HashMap<String, Object> parameters)
	{	
		//lookup the patient again to avoid lazy initialization errors
		PatientService patientService = Context.getPatientService();
		ATDService atdService = Context.getService(ATDService.class);
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);
		
		Integer locationTagId = patientState.getLocationTagId();
		Integer locationId = patientState.getLocationId();
		
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		State currState = patientState.getState();
		Integer sessionId = patientState.getSessionId();

		FormInstance formInstance = (FormInstance) parameters.get("formInstance");
		
		patientState.setFormInstance(formInstance);
		chirdlutilbackportsService.updatePatientState(patientState);

		Session session = chirdlutilbackportsService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formInstance.getFormId());
		String formName = form.getName();
		List<Statistics> stats = null;
		
		Boolean voidObs = (Boolean) parameters.get("voidObs");
		
		boolean shouldVoidObs = true;
		
		if(voidObs != null && !voidObs.booleanValue()){
			shouldVoidObs = false;
		}
		
		if (shouldVoidObs) {
			// void all obs for form
			stats = atdService.getStatsByEncounterForm(encounterId, formName);
			
			ObsService obsService = Context.getObsService();
			for (Statistics currStat : stats) {
				Integer obsId = currStat.getObsvId();
				Obs obs = obsService.getObs(obsId);
				obsService.voidObs(obs, "voided due to rescan");
			}
		}

		StateManager.endState(patientState);
		BaseStateActionHandler.changeState(patient, sessionId, currState, stateAction, parameters, locationTagId,
		    locationId);
	}
	protected void voidObsForConcept(String conceptName,Integer encounterId){
		EncounterService encounterService = Context.getService(EncounterService.class);
		Encounter encounter = encounterService.getEncounter(encounterId);
		ObsService obsService = Context.getObsService();
		List<org.openmrs.Encounter> encounters = new ArrayList<org.openmrs.Encounter>();
		encounters.add(encounter);
		List<Concept> questions = new ArrayList<Concept>();
		
		ConceptService conceptService = Context.getConceptService();
		Concept concept = conceptService.getConcept(conceptName);
		questions.add(concept);
		List<Obs> obs = obsService.getObservations(null, encounters, questions, null, null, null, null,
				null, null, null, null, false);
		
		for(Obs currObs:obs){
			obsService.voidObs(currObs, "voided due to rescan");
		}
	}
	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters){
		StateAction stateAction = patientState.getState().getAction();
		processAction(stateAction,patientState.getPatient(),patientState,parameters);

	}
}
