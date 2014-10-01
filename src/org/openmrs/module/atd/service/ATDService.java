package org.openmrs.module.atd.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.ParameterHandler;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.atd.hibernateBeans.PSFQuestionAnswer;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
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
	
	public boolean consume(InputStream customInput, FormInstance formInstance,
			Patient patient, int encounterId,
			Map<String,Object> baseParameters,
			ParameterHandler parameterHandler,
			List<FormField> fieldsToConsume,
			Integer locationTagId, Integer sessionId
			);
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,
			Integer locationTagId,Integer sessionId);
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			Integer encounterId,
			Map<String,Object> baseParameters,
			Integer locationTagId,Integer sessionId);
	
	public Result evaluateRule(String ruleEvalString,Patient patient,
			Map<String, Object> baseParameters);
	
	public void addPatientATD(int patientId, FormInstance formInstance, DssElement dssElement,
			Integer encounterId) throws APIException;
	
	public PatientATD getPatientATD(FormInstance formInstance, int fieldId);
	
	public ArrayList<TeleformFileState> fileProcessed(ArrayList<TeleformFileState> tfstates);
	
	public TeleformFileState fileProcessed(TeleformFileState tfstate);
	
	public void updatePatientStates(Date thresholdDate);
		
	public void cleanCache();
	
	/**
	 * Populates all form fields in a form with data found in form fields from other forms.
	 * 
	 * @param formId The form ID for the form to have its fields auto-populated.
	 * @throws APIException
	 */
	public void prePopulateNewFormFields(Integer formId) throws APIException;
	
	/**
	 * Populates fields in a form having no current metadata with data found in form fields from other forms.
	 * 
	 * @param formId The form ID for the form to have its empty fields auto-populated.
	 * @throws APIException
	 */
	public void populateEmtptyFormFields(Integer formId) throws APIException;
	
	public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
	                                   String installationDirectory, String serverName, boolean faxableForm, 
	                                   boolean scannableForm, boolean scorableForm, String scoreConfigLoc, 
	                                   Integer numPrioritizedFields, Integer copyPrinterConfigFormId) throws APIException;
	
	public void purgeFormAttributeValues(Integer formId) throws APIException;
	
	public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId) throws APIException;
	
	public void savePrinterConfigurations(FormPrinterConfig printerConfig) throws APIException;
	
	public void copyFormAttributeValues(Integer fromFormId, Integer toFormId) throws APIException;
	
	public void setClinicUseAlternatePrinters(List<Integer> locationIds, Boolean useAltPrinters) throws APIException;
	
	public Boolean isFormEnabledAtClinic(Integer formId, Integer locationId) throws APIException;
	
	/**
	 * Retrieves a list of URL objects referencing bad scans found for the provided location.
	 * 
	 * @param locationName The name of the location to search for bad scans.
	 * @return List of URL objects of the bad scans.
	 */
	public List<URL> getBadScans(String locationName);
	
	/**
	 * Moves the provided file to its parent directory named "resolved bad scans".
	 * 
	 * @param url The file (in URL format) to move to the "resolved bad scans" folder.
	 * @param formRescanned Whether or not the form was attempted to be rescanned.  If so, 
	 * the form file will be moved to the rescanned folder.  Otherwise, it will be moved to 
	 * the ignored folder.
	 * 
	 * @throws Exception
	 */
	public void moveBadScan(String url, boolean formRescanned) throws Exception;

	public List<Statistics> getStatsByEncounterForm(Integer encounterId,String formName);
	
	/**
	 * Get all statistics for a given encounter ID and form name whether the there is an observation associated or not.
	 * 
	 * @param encounterId
	 * @param formName
	 * @return List of Statistics objects
	 */
	public List<Statistics> getAllStatsByEncounterForm(Integer encounterId,String formName);

	public List<Statistics> getStatsByEncounterFormNotPrioritized(Integer encounterId,String formName);
	
	public List<Statistics> getStatByIdAndRule(int formInstanceId,int ruleId,String formName,
		Integer locationId);
	
	public List<Statistics> getStatByFormInstance(int formInstanceId,String formName,
		Integer locationId);
	
	public void updateStatistics(Statistics statistics);

	public void createStatistics(Statistics statistics);
	
	public void produce(OutputStream output, PatientState state,
	        			Patient patient, Integer encounterId, String dssType,
	        			int maxDssElements,Integer sessionId);
	
	/**
	 * This is a method I added to get around lazy initialization errors with patient.getIdentifier() in rules
	 * Auto generated method comment
	 * 
	 * @param patientId
	 * @return
	 */
	public PatientIdentifier getPatientMRN(Integer patientId);
	
	/**
	 * Returns the question/answer pair for a PSF form.
	 * 
	 * @param formInstanceId The form instance ID
	 * @param locationId The location ID
	 * @param patientId The patient ID
	 * @return List of PSFQuestionAnswer objects.  This will not return null;
	 */
	public List<PSFQuestionAnswer> getPSFQuestionAnswers(Integer formInstanceId, Integer locationId, Integer patientId);
	
	public List<ConceptDescriptor> getAllConceptsAsDescriptor();
	
	public List<FormDefinitionDescriptor> getAllFormDefinitionAsDescriptor();
	
	public List<FormDefinitionDescriptor> getFormDefinitionAsDescriptor(Integer formId);
}