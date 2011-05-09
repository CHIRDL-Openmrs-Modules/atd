package org.openmrs.module.atd.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.hibernateBeans.ATDError;
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
	public FormInstance addFormInstance(Integer formId, Integer locationId);
	
	/**
	 * Adds a new row to atd_patient_atd_element
	 * @param patientATD new patientATD to add
	 */
	public PatientATD addPatientATD(PatientATD patientATD);
	
	/**
	 * Looks up a row in the atd_patient_atd_element table by form_id, form_instance_id,
	 * and field_id
	 * @param fieldId unique id for the field on the openmrs form
	 * @param formId unique id for an openmrs form
	 * @return PatientATD row from the atd_patient_atd_element table
	 */
	public PatientATD getPatientATD(FormInstance formInstance, int fieldId);

	/**
	 * Returns the value of a form attribute from the atd_form_attribute_value table
	 * @param formId id of the form to find an attribute for
	 * @param formAttributeName name of the form attribute
	 * @return FormAttributeValue value of the attribute for the given form
	 */
	public FormAttributeValue getFormAttributeValue(Integer formId, String formAttributeName,Integer locationTagId, Integer locationId);
	
	/**
	 * Get state by name
	 * 
	 * @param initialState name of state
	 * @return state with given name
	 */
	public StateMapping getStateMapping(State initialState,Program program);
	
	public List<PatientState> getUnfinishedPatientStatesAllPatients(Date optionalDateRestriction,Integer locationTagId,Integer locationId);
	
	public Session addSession(Session session);
	
	public Session updateSession(Session session);
	
	public Session getSession(int sessionId);
	
	public PatientState addUpdatePatientState(PatientState patientState);
	
	public List<PatientState> getPatientStatesWithForm(int sessionId);
	
	public PatientState getPrevPatientStateByAction(
			int sessionId, int patientStateId,String action);
	
	public StateAction getStateActionByName(String action);
	
	public List<PatientState> getUnfinishedPatientStateByStateName(String state,Date optionalDateRestriction, Integer locationTagId, Integer locationId);

	public PatientState getLastUnfinishedPatientState(Integer sessionId);

	public PatientState getLastPatientState(Integer sessionId);
	
	public List<PatientState> getLastPatientStateAllPatients(
			Date optionalDateRestriction,Integer programId,String startStateName, Integer locationTagId, Integer locationId);
	
	public State getStateByName(String stateName);
	
	public Program getProgramByNameVersion(String name,String version);
	
	public Program getProgram(Integer programId);

	public PatientState getPatientStateByEncounterFormAction(Integer encounterId, Integer formId, String action);
	
	public PatientState getPatientStateByFormInstanceAction(FormInstance formInstance,String action);

	public List<FormAttributeValue> getFormAttributesByName(String attributeName);
	
	public ArrayList<String> getFormAttributesByNameAsString(String attributeName);
	
	public List<State> getStatesByActionName(String actionName);
	
	public State getState(Integer stateId);
	
	public void updatePatientStates(Date thresholdDate);
	
	public PatientState getPatientState(Integer patientStateId);
	
	public List<PatientState> getPatientStateBySessionState(Integer sessionId,
			Integer stateId);
	
	public List<PatientState> getAllRetiredPatientStatesWithForm(Date thresholdDate);
	
	public List<Session> getSessionsByEncounter(int encounterId);
	
	public List<PatientState> getPatientStatesWithFormInstances(String formName, Integer encounterId);
		
	public List<PatientState> getPatientStateByEncounterState(Integer encounterId,
			Integer stateId);
	
	public void saveError(ATDError error);
	
	public List<ATDError> getATDErrorsByLevel(String errorLevel,Integer sessionId);

	public Integer getErrorCategoryIdByName(String name);
	
	public Program getProgram(Integer locationTagId,Integer locationId);
	
	public List<FormAttributeValue> getFormAttributeValuesByValue(String value);
	
	public List<PatientState> getUnfinishedPatientStateByStateSession(
		String stateName,Integer sessionId);
	
	public List<PatientState> getPatientStateByFormInstanceState(FormInstance formInstance, State state);
	
	public List<PatientState> getPatientStatesByFormInstance(FormInstance formInstance, boolean isRetired);

	public List<PatientState> getPatientStatesBySession(Integer sessionId,boolean isRetired);

	/**
	 * Populates all form fields in a form with data found in form fields from other forms.
	 * 
	 * @param formId The form ID for the form to have its fields auto-populated.
	 * @throws DAOException
	 */
	public void prePopulateNewFormFields(Integer formId) throws DAOException;
	
	/**
	 * Populates fields in a form having no current metadata with data found in form fields from other forms.
	 * 
	 * @param formId The form ID for the form to have its empty fields auto-populated.
	 * @throws DAOException
	 */
	public void populateEmtptyFormFields(Integer formId) throws DAOException;
	
	public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
	                                   String installationDirectory, String serverName, boolean scannableForm, 
	                                   boolean scorableForm, String scoreConfigLoc, Integer numPrioritizedFields,
	                                   Integer copyPrinterConfigFormId) throws DAOException;
	
	public void purgeFormAttributeValues(Integer formId) throws DAOException;
	
	public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId) throws DAOException;
	
	public void savePrinterConfigurations(FormPrinterConfig printerConfig) throws DAOException;
	
	public void copyFormAttributeValues(Integer fromFormId, Integer toFormId) throws DAOException;
	
	public void setClinicUseAlternatePrinters(List<Integer> locationIds, Boolean useAltPrinters) throws DAOException;
	
	public Boolean isFormEnabledAtClinic(Integer formId, Integer locationId) throws DAOException;
}
