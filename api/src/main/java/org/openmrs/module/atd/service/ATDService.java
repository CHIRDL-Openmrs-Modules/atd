package org.openmrs.module.atd.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.annotation.Authorized;
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
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag;
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
	
	@Authorized()
	public Form teleformXMLToDatabaseForm(String formName,
			String templateXMLFilename);
	
	@Authorized()
	public boolean consume(InputStream customInput, FormInstance formInstance,
			Patient patient, int encounterId,
			Map<String,Object> baseParameters,
			ParameterHandler parameterHandler,
			List<FormField> fieldsToConsume,
			Integer locationTagId, Integer sessionId
			);
	
	@Authorized()
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,
			Integer locationTagId,Integer sessionId);
	
	@Authorized()
	public boolean produce(Patient patient,
	           			FormInstance formInstance, Map<String,OutputStream> outputs, 
	           			DssManager dssManager,Integer encounterId,
	           			Map<String,Object> baseParameters,
	           			Integer locationTagId,Integer sessionId);
	          
	@Authorized()
	public boolean produce(Patient patient,
	           			FormInstance formInstance, OutputStream customOutput, 
	           			Integer encounterId,
	           			Map<String,Object> baseParameters,
	           			Integer locationTagId,Integer sessionId);
	        
	@Authorized()
	public boolean produce(Patient patient,
			FormInstance formInstance, Map<String,OutputStream> outputs, 
			Integer encounterId,
			Map<String,Object> baseParameters,
			Integer locationTagId,Integer sessionId);
	
	@Authorized()
	public Result evaluateRule(String ruleEvalString,Patient patient,
			Map<String, Object> baseParameters);
	
	@Authorized()
	public PatientATD addPatientATD(int patientId, FormInstance formInstance, DssElement dssElement,
			Integer encounterId) throws APIException;
	
	/**
	 * Update an existing PatientATD object.
	 * 
	 * @param patientATD The PatientATD object to update.
	 * @return The updated PatientATD object.
	 * @throws APIException
	 */
	@Authorized()
	public PatientATD updatePatientATD(PatientATD patientATD) throws APIException;
	
	@Authorized()
	public PatientATD getPatientATD(FormInstance formInstance, int fieldId);
	
	@Authorized()
	public ArrayList<TeleformFileState> fileProcessed(ArrayList<TeleformFileState> tfstates);
	
	@Authorized()
	public TeleformFileState fileProcessed(TeleformFileState tfstate);
	
	@Authorized()
	public void updatePatientStates(Date thresholdDate);
		
	@Authorized()
	public void cleanCache();
	
	/**
	 * Populates all form fields in a form with data found in form fields from other forms.
	 * 
	 * @param formId The form ID for the form to have its fields auto-populated.
	 * @throws APIException
	 */
	@Authorized()
	public void prePopulateNewFormFields(Integer formId) throws APIException;
	
	@Authorized()
	public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
	                                   String installationDirectory, boolean faxableForm, boolean scannableForm, 
	                                   boolean scorableForm, String scoreConfigLoc, Integer numPrioritizedFields, 
	                                   Integer copyPrinterConfigFormId) throws APIException;
	
	@Authorized()
	public void purgeFormAttributeValues(Integer formId) throws APIException;
	
	@Authorized()
	public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId) throws APIException;
	
	@Authorized()
	public void savePrinterConfigurations(FormPrinterConfig printerConfig) throws APIException;
	
	@Authorized()
	public void copyFormAttributeValues(Integer fromFormId, Integer toFormId) throws APIException;
	
	@Authorized()
	public void setClinicUseAlternatePrinters(List<Integer> locationIds, Boolean useAltPrinters) throws APIException;
	
	@Authorized()
	public Boolean isFormEnabledAtClinic(Integer formId, Integer locationId) throws APIException;
	
	/**
	 * Retrieves a list of URL objects referencing bad scans found for the provided location.
	 * 
	 * @param locationName The name of the location to search for bad scans.
	 * @return List of URL objects of the bad scans.
	 */
	@Authorized()
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
	@Authorized()
	public void moveBadScan(String url, boolean formRescanned) throws Exception;

	@Authorized()
	public List<Statistics> getStatsByEncounterForm(Integer encounterId,String formName);
	
	@Authorized()
	public List<Statistics> getStatsByEncounterFormNotPrioritized(Integer encounterId,String formName);
	
	@Authorized()
	public List<Statistics> getStatByIdAndRule(int formInstanceId,int ruleId,String formName,
		Integer locationId);
	
	@Authorized()
	public List<Statistics> getStatByFormInstance(int formInstanceId,String formName,
		Integer locationId);
	
	@Authorized()
	public void updateStatistics(Statistics statistics);

	@Authorized()
	public Statistics createStatistics(Statistics statistics);
	
	@Authorized()
	public void produce(OutputStream output, PatientState state,
	        			Patient patient, Integer encounterId, String dssType,
	        			int maxDssElements,Integer sessionId);
	
	@Authorized()
	public void produce(Map<String,OutputStream> outputs, PatientState state,
	        			Patient patient, Integer encounterId, String dssType,
	        			int maxDssElements,Integer sessionId);
	
	/**
	 * This is a method I added to get around lazy initialization errors with patient.getIdentifier() in rules
	 * Auto generated method comment
	 * 
	 * @param patientId
	 * @return
	 */
	@Authorized()
	public PatientIdentifier getPatientMRN(Integer patientId);
	
	/**
	 * Returns the question/answer pair for a PSF form.
	 * 
	 * @param formInstanceId The form instance ID
	 * @param locationId The location ID
	 * @param patientId The patient ID
	 * @param patientForm The Patient Form Name
	 * @return List of PSFQuestionAnswer objects.  This will not return null;
	 */
	@Authorized()
	public List<PSFQuestionAnswer> getPatientFormQuestionAnswers(Integer formInstanceId, Integer locationId, Integer patientId, String patientForm);
	
	/**
	 * Returns PatientATD objects based on the form instance and field Id information provided.
	 * 
	 * @param formInstance Form Instance object.
	 * @param fieldIds Field Ids to find.
	 * 
	 * @return List of PatientATD objects matching the criteria provided.
	 */
	@Authorized()
	public List<PatientATD> getPatientATDs(FormInstance formInstance, List<Integer> fieldIds);
	
	/**
	 * Get all form definition in system as list of FormDefinitionDescriptor
	 * @return A list of FormDefinitionDescriptor objects
	 * @throws SQLException
	 */
	@Authorized()
	public List<FormDefinitionDescriptor> getAllFormDefinitions() throws SQLException;
	
	/**
	 * get the form definition in database with the form that has id as formId. 
	 * @param form id
	 * @return A list of DefinitionDescriptor objects
	 * @throws SQLException
	 */
	@Authorized()
	public List<FormDefinitionDescriptor> getFormDefinition(Integer formId) throws SQLException;
	
	/**
	 * DWE CHICA-332 4/16/15
	 * 
	 * Given a formId, return a HashMap containing location ids and location tag ids
	 * key: locationId, value: list of location tag ids
	 * 
	 * @param formId
	 * @return the HashMap
	 */
	@Authorized()
	public HashMap<Integer, List<Integer>> getFormAttributeValueLocationsAndTagsMap(Integer formId);
	
	/**
	 * DWE CHICA-330 4/22/15 
	 * 
	 * Return concepts to populate data table, start and length parameters are used for paging
	 * 
	 * @param start - page number
	 * @param length - number of rows to display in the data table
	 * @param searchValue - search by concept name
	 * @param includeRetired - flag to include retired concepts
	 * @param conceptClassId - filter by concept class
	 * @param orderByColumn - order by column
	 * @param ascDesc - order by ASC or DESC
	 * @param exactMatchSearch - true to perform and exact match search using the searchValue parameter
	 * @return
	 */
	@Authorized()
	public List<ConceptDescriptor> getConceptDescriptorList(int start, int length, String searchValue, boolean includeRetired, int conceptClassId, String orderByColumn, String ascDesc, boolean exactMatchSearch);
	
	/**
	 * DWE CHICA-330 4/23/15 
	 * 
	 * Used for jquery data table server-side processing to return a count of Concept records with filter applied
	 * 
	 * @param searchValue - pass in an empty string to return the count without the filter
	 * @param includeRetired - flag to include retired concepts
	 * @param conceptClassId - filter by concept class
	 * @param exactMatchSearch - true to perform and exact match search using the searchValue parameter
	 * @return the total number of concept records
	 */
	@Authorized()
	public int getCountConcepts(String searchValue, boolean includeRetired, int conceptClassId, boolean exactMatchSearch);

	/**
     * DWE CHICA-437 
     * Gets a list of obs records where there is a related atd_statistics record with the formFieldId
     * 
     * @param encounterId
     * @param conceptId
     * @param formFieldId
     * @param includeVoidedObs
     * @return
     */
	@Authorized()
    public List<Obs> getObsWithStatistics(Integer encounterId, Integer conceptId, Integer formFieldId, boolean includeVoidedObs);

    /**
     * Checks to see if at least one box is checked for this rule and encounter 
     * in the atd_statistics table
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
	@Authorized()
    public boolean oneBoxChecked(Integer encounterId, Integer ruleId);
    
    /**
     * 
     * Look up the Statistics record by encounter_id and rule_id.
     * This checks to see if the rule fired for a certain encounter
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
	@Authorized()
    public List<Statistics> getStatsByEncounterRule(Integer encounterId, Integer ruleId);
    
    /**
     * Returns true if a rule fired for a given encounter
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
	@Authorized()
    public boolean ruleFiredForEncounter(Integer encounterId, Integer ruleId);
    
    /**
     * Returns a Records object containing form field values.  This method will first try to access any form data stored
     * in the cache.  If no data is present, the form will be loaded from the merge file.
     * 
     * @param formInstanceTag FormInstanceTag object used to locate the form data
     * @return Records object containing form information
     * @throws APIException
     */
	@Authorized()
    public Records getFormRecords(FormInstanceTag formInstanceTag) throws APIException;
    
    /**
     * Saves a draft of the form records in the cache.
     * 
     * @param formInstanceTag FormInstanceTag object used to save the draft of the form records
     * @param records Records object containing form information
     * @throws APIException
     */
	@Authorized()
    public void saveFormRecordsDraft(FormInstanceTag formInstanceTag, Records records) throws APIException;
    
    /**
     * Saves the form records.
     * 
     * @param formInstanceTag FormInstanceTag object used to save the form records
     * @param records Records object containing form information
     * @throws APIException
     */
	@Authorized()
    public void saveFormRecords(FormInstanceTag formInstanceTag, Records records) throws APIException;
    
	/**
     * Looks up the form names by Form Attribute Value
     * 
     * @param formAttrNames
	 * @param formAttrValue
	 * @param isRetired
     * @return
     */
	@Authorized()
    public List<String> getFormNamesByFormAttribute(List<String> formAttrNames, String formAttrValue, boolean isRetired) throws APIException;
}
