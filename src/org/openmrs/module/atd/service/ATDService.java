package org.openmrs.module.atd.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;



import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.ParameterHandler;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.atd.hibernateBeans.ATDError;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * ATD related services
 * 
 * @author Tammy Dugan
 */
@Transactional
public interface ATDService
{
	
	public Form teleformXMLToDatabaseForm(String formName,
			String templateXMLFilename);

	public boolean tableExists(String tableName);

	public void executeSql(String sql);
	
	public boolean consume(InputStream customInput, FormInstance formInstance,
			Patient patient, int encounterId,
			Map<String,Object> baseParameters,
			String rulePackagePrefix,ParameterHandler parameterHandler,
			List<FormField> fieldsToConsume,
			Integer locationTagId, Integer sessionId
			);
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,String rulePackagePrefix,
			Integer locationTagId,Integer sessionId);
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			Integer encounterId,
			Map<String,Object> baseParameters,
			String rulePackagePrefix,
			Integer locationTagId,Integer sessionId);
	
	public FormInstance addFormInstance(Integer formId, Integer locationId);
	
	public Result evaluateRule(String ruleEvalString,Patient patient,
			Map<String, Object> baseParameters, String rulePackagePrefix);
	
	public void addPatientATD(int patientId, FormInstance formInstance, DssElement dssElement,
			Integer encounterId) throws APIException;
	
	public PatientATD getPatientATD(FormInstance formInstance, int fieldId);
	
	public FormAttributeValue getFormAttributeValue(Integer formId, String formAttributeName,
			Integer locationTagId,Integer locationId);
	
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
	
	public PatientState addPatientState(Patient patient,State initialState, int sessionId,Integer locationTagId,Integer locationId);
	
	public PatientState updatePatientState(PatientState patientState);

	public PatientState getPrevPatientStateByAction(
			int sessionId, int patientStateId,String action);
	
	public List<PatientState> getPatientStatesWithForm(int sessionId);
	
	public List<PatientState> getUnfinishedPatientStatesAllPatients(Date optionalDateRestriction,Integer locationTagId,Integer locationId);
	
	public List<PatientState> getUnfinishedPatientStateByStateName(String state,Date optionalDateRestriction,Integer locationTagId,Integer locationId);
	
	public PatientState getLastUnfinishedPatientState(Integer sessionId);

	public PatientState getLastPatientState(Integer sessionId);
	
	public List<PatientState> getLastPatientStateAllPatients(Date optionalDateRestriction, Integer programId,
			String startStateName, Integer locationTagId, Integer locationId);
	
	public State getStateByName(String stateName);
	
	public Program getProgramByNameVersion(String name,String version);
	
	public Program getProgram(Integer programId);
	
	public PatientState getPatientStateByEncounterFormAction(Integer encounterId, Integer formId, String action);

	public PatientState getPatientStateByFormInstanceAction(FormInstance formInstance,String action);

	public ArrayList<String> getExportDirectories();
	
	public List<State> getStatesByActionName(String actionName);
	
	public State getState(Integer stateId);
	
	public void updatePatientStates(Date thresholdDate);
	
	public PatientState getPatientState(Integer patientStateId);
	
	public List<PatientState> getPatientStateBySessionState(Integer sessionId,
			Integer stateId);
	
	public List<PatientState> getAllRetiredPatientStatesWithForm(Date thresholdDate);
	
	public List<Session> getSessionsByEncounter(Integer encounterId);
	
	public List<PatientState> getPatientStatesWithFormInstances(String formName, Integer encounterId);
	
	public List<PatientState> getPatientStateByEncounterState(Integer encounterId,
			Integer stateId);
	
	public void saveError(ATDError error);
	
	public List<ATDError> getATDErrorsByLevel(String errorLevel,Integer sessionId);
	
	public Integer getErrorCategoryIdByName(String name);
	
	public Program getProgram(Integer locationTagId,Integer locationId);
	
	public List<FormAttributeValue> getFormAttributeValuesByValue(String value);
		
	public void cleanCache();
	
	public List<PatientState> getUnfinishedPatientStateByStateSession(
		String stateName,Integer sessionId);
	
	public List<PatientState> getPatientStateByFormInstanceState(FormInstance formInstance, State state);
	
	public List<PatientState> getPatientStatesByFormInstance(FormInstance formInstance, boolean isRetired);
	
	public void unretireStatesBySessionId(Integer sessionId);

	public List<PatientState> getPatientStatesBySession(Integer sessionId,boolean isRetired);
	
	public void copyFormMetadata(Integer fromFormId, Integer toFormId);
	
	public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
	                                   String defaultDriveLetter, String serverName, boolean scannableForm, 
	                                   boolean scorableForm, String scoreConfigLoc, Integer numPrioritizedFields,
	                                   Integer copyPrinterConfigFormId);
	
	public void purgeFormAttributeValues(Integer formId);
	
	public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId);
	
	public void savePrinterConfigurations(FormPrinterConfig printerConfig);
	
	public void copyFormAttributeValues(Integer fromFormId, Integer toFormId);

}