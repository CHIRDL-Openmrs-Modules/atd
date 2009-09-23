package org.openmrs.module.atd.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.Patient;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Program;
import org.openmrs.module.atd.hibernateBeans.Session;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateAction;
import org.openmrs.module.atd.hibernateBeans.StateMapping;

/**
 * ATD database methods.
 * 
 * @author Tammy Dugan
 */
public interface ATDDAO {

	/**
	 * Returns true if the table already exists in the database
	 * @param tableName name of the table to check
	 * @return boolean true if the table exists in the database
	 */
	public boolean tableExists(String tableName);
	
	/**
	 * Executes an sql string
	 * @param sql sql string to execute
	 */
	public void executeSql(String sql);
	
	/**
	 * Adds a new row to the atd_form_instance table
	 * @param formInstance new form instance to add
	 * @return FormInstance newly added form instance
	 */
	public FormInstance addFormInstance(Integer formId);
	
	/**
	 * Adds a new row to atd_patient_atd
	 * @param patientATD new patientATD to add
	 */
	public PatientATD addPatientATD(PatientATD patientATD);
	
	/**
	 * Looks up a row in the atd_patient_atd table by form_id, form_instance_id,
	 * and field_id
	 * @param formInstanceId unique id for the specific instance of an openmrs form
	 * @param fieldId unique id for the field on the openmrs form
	 * @param formId unique id for an openmrs form
	 * @return PatientATD row from the atd_patient_atd table
	 */
	public PatientATD getPatientATD(int formInstanceId, int fieldId, int formId);

	/**
	 * Returns the value of a form attribute from the atd_form_attribute_value table
	 * @param formId id of the form to find an attribute for
	 * @param formAttributeName name of the form attribute
	 * @return FormAttributeValue value of the attribute for the given form
	 */
	public FormAttributeValue getFormAttributeValue(Integer formId, String formAttributeName);
	
	/**
	 * Get state by name
	 * 
	 * @param initialState name of state
	 * @return state with given name
	 */
	public StateMapping getStateMapping(State initialState,Program program);
	
	public List<PatientState> getUnfinishedPatientStatesAllPatients(Date optionalDateRestriction);
	
	public Session addSession(Session session);
	
	public Session updateSession(Session session);
	
	public Session getSession(int sessionId);
	
	public Session getSessionByEncounter(int encounterId);
	
	public PatientState addUpdatePatientState(PatientState patientState);
	
	public List<PatientState> getPatientStatesWithForm(int sessionId);
	
	public PatientState getPrevPatientStateByAction(
			int sessionId, int patientStateId,String action);
	
	public StateAction getStateActionByName(String action);
	
	public List<PatientState> getUnfinishedPatientStateByStateName(String state,Date optionalDateRestriction);

	public PatientState getLastUnfinishedPatientState(Integer sessionId);

	public List<PatientState> getLastPatientStateAllPatients(
			Date optionalDateRestriction,Integer programId);
	
	public State getStateByName(String stateName);
	
	public Program getProgramByNameVersion(String name,String version);
	
	public PatientState getPatientStateByEncounterFormAction(Integer encounterId, Integer formId, String action);
	
	public PatientState getPatientStateByFormInstanceAction(Integer formId, Integer formInstanceId,String action);

	public ArrayList<String> getExportDirectories();
	
	public List<State> getStatesByActionName(String actionName);
	
	public State getState(Integer stateId);
	
	public void updatePatientStates(Date thresholdDate);
	
	public PatientState getPatientState(Integer patientStateId);
	
	public List<PatientState> getPatientStateBySessionState(Integer sessionId,
			Integer stateId);
	
	public List<PatientState> getAllRetiredPatientStatesWithForm(Date thresholdDate);
	
	public List<Session> getSessionsByEncounter(int encounterId);
	
	public List<Integer> getFormInstancesByEncounterId(String formName, Integer encounter_id);
		
	public List<PatientState> getPatientStateByEncounterState(Integer encounterId,
			Integer stateId);
}
