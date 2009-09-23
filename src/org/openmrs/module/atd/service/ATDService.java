package org.openmrs.module.atd.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;



import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.ParameterHandler;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Program;
import org.openmrs.module.atd.hibernateBeans.Session;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateMapping;
import org.openmrs.module.dss.DssElement;
import org.openmrs.module.dss.DssManager;

/**
 * ATD related services
 * 
 * @author Tammy Dugan
 */
public interface ATDService
{
	
	public Form teleformXMLToDatabaseForm(String formName,
			String templateXMLFilename);

	public boolean tableExists(String tableName);

	public void executeSql(String sql);
	
	public boolean consume(InputStream customInput, int formInstanceId,
			int formId,Patient patient, int encounterId,
			Map<String,Object> baseParameters,
			String rulePackagePrefix,ParameterHandler parameterHandler,
			List<FormField> fieldsToConsume
			)throws Exception;
	
	public boolean produce(Patient patient,
			int formInstanceId, OutputStream customOutput, 
			int formId, DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,String rulePackagePrefix,
			boolean generateJITS);
	
	public boolean produce(Patient patient,
			int formInstanceId, OutputStream customOutput, 
			int formId, Integer encounterId,
			Map<String,Object> baseParameters,
			String rulePackagePrefix,
			boolean generateJITS);
	
	public FormInstance addFormInstance(Integer formId);
	
	public Result evaluateRule(String ruleEvalString,Patient patient,
			Map<String, Object> baseParameters, String rulePackagePrefix);
	
	public void addPatientATD(int patientId, Integer formId, DssElement dssElement,
			Integer formInstanceId, Integer encounterId) throws APIException;
	
	public PatientATD getPatientATD(int formInstanceId, int fieldId, int formId);
	
	public FormAttributeValue getFormAttributeValue(Integer formId, String formAttributeName);
	
	public ArrayList<TeleformFileState> fileProcessed(ArrayList<TeleformFileState> tfstates);

	/**
	 * Get state by state name
	 * 
	 * @param initialState
	 * 
	 * @param stateMappingId state name
	 * @return state with given state name
	 */
	public StateMapping getStateMapping(State initialState,Program program);
	
	public Session addSession();
	
	public Session updateSession(Session session);
	
	public Session getSession(int sessionId);
	
	public Session getSessionByEncounter(int encounterId);
	
	public PatientState addPatientState(Patient patient,State initialState, int sessionId,Integer formId);
	
	public PatientState updatePatientState(PatientState patientState);

	public PatientState getPrevPatientStateByAction(
			int sessionId, int patientStateId,String action);
	
	public List<PatientState> getPatientStatesWithForm(int sessionId);
	
	public List<PatientState> getUnfinishedPatientStatesAllPatients(Date optionalDateRestriction);
	
	public List<PatientState> getUnfinishedPatientStateByStateName(String state,Date optionalDateRestriction);
	
	public PatientState getLastUnfinishedPatientState(Integer sessionId);

	public List<PatientState> getLastPatientStateAllPatients(Date optionalDateRestriction, Integer programId);
	
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
	
	public List<Session> getSessionsByEncounter(Integer encounterId);
	
	public List<Integer> getFormInstancesByEncounterId(String formName, Integer encounter_id);
	
	public List<PatientState> getPatientStateByEncounterState(Integer encounterId,
			Integer stateId);
	}