package org.openmrs.module.atd.db;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.openmrs.Obs;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.hibernateBeans.PSFQuestionAnswer;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.springframework.transaction.annotation.Transactional;

/**
 * ATD database methods.
 * 
 * @author Tammy Dugan
 */
@Transactional
public interface ATDDAO {

	
	
	
	/**
	 * Adds a new row to atd_patient_atd_element
	 * @param patientATD new patientATD to add
	 */
	public PatientATD addPatientATD(PatientATD patientATD);
	
	public void updatePatientStates(Date thresholdDate);
		
	/**
	 * Looks up a row in the atd_patient_atd_element table by form_id, form_instance_id,
	 * and field_id
	 * @param fieldId unique id for the field on the openmrs form
	 * @param formId unique id for an openmrs form
	 * @return PatientATD row from the atd_patient_atd_element table
	 */
	public PatientATD getPatientATD(FormInstance formInstance, int fieldId);
	
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
	                                   String installationDirectory, String serverName, boolean faxableForm, 
	                                   boolean scannableForm, boolean scorableForm, String scoreConfigLoc, 
	                                   Integer numPrioritizedFields, Integer copyPrinterConfigFormId) throws DAOException;
	
	public void purgeFormAttributeValues(Integer formId) throws DAOException;
	
	public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId) throws DAOException;
	
	public void savePrinterConfigurations(FormPrinterConfig printerConfig) throws DAOException;
	
	public void copyFormAttributeValues(Integer fromFormId, Integer toFormId) throws DAOException;
	
	public void setClinicUseAlternatePrinters(List<Integer> locationIds, Boolean useAltPrinters) throws DAOException;
	
	public Boolean isFormEnabledAtClinic(Integer formId, Integer locationId) throws DAOException;

	public void addStatistics(Statistics statistics);
	
	public void updateStatistics(Statistics statistics);
	
	public List<Statistics> getStatByFormInstance(int formInstanceId,String formName, Integer locationId);

	public List<Statistics> getStatByIdAndRule(int formInstanceId,int ruleId,String formName, Integer locationId);
	
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
	 * @param patientForm The Patient Form Name
	 * @return List of PSFQuestionAnswer objects.  This will not return null.
	 */
	public List<PSFQuestionAnswer> getPatientFormQuestionAnswers(Integer formInstanceId, Integer locationId, Integer patientId, String patientForm);
	
	/**
	 * Returns PatientATD objects based on the form instance and field Id information provided.
	 * 
	 * @param formInstance Form Instance object.
	 * @param fieldIds Field Ids to find.
	 * 
	 * @return List of PatientATD objects matching the criteria provided.
	 */
	public List<PatientATD> getPatientATDs(FormInstance formInstance, List<Integer> fieldIds);
    /**
	 * Get all concept information from database as list of ConceptDescriptor
	 * @return A list of ConceptDescriptor objects
	 * @throws SQLException
	 */
	public List<ConceptDescriptor> getAllConcepts() throws SQLException ;
	
	/**
	 * Get all form definition from database as list of FormDefinitionDescriptor
	 * @return A list of FormDefinitionDescriptor objects
	 * @throws SQLException
	 */
	public List<FormDefinitionDescriptor> getAllFormDefinitions() throws SQLException ;
	
	/**
	 * Get the form definition from database with the form that has id as formId. 
	 * @param The id of the form
	 * @return	A list of DefinitionDescriptor objects
	 * @throws SQLException
	 */
	public List<FormDefinitionDescriptor> getFormDefinition(int formId) throws SQLException;
	
	/**
	 * DWE CHICA-332 4/16/15
	 * 
	 * Given a formId, return a HashMap containing location ids and location tag ids
	 * key: locationId, value: list of location tag ids
	 * 
	 * @param formId
	 * @return the HashMap
	 */
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
	 * @param orderByColumns - order by column
	 * @param ascDesc - order by ASC or DESC
	 * @param exactMatchSearch - true to perform and exact match search using the searchValue parameter
	 * @return
	 */
	public List<ConceptDescriptor> getConceptDescriptorList(int start, int length, String searchValue, boolean includeRetired, int conceptClassId, String orderByColumn, String ascDesc, boolean exactMatchSearch);
	
	/**
	 * DWE CHICA-330 4/23/15 
	 * 
	 * Used for jquery data table server-side processing to return a count of Concept records
	 * 
	 * @param searchValue - pass in an empty string to return the count without the filter
	 * @param includeRetired - flag to include retired concepts
	 * @param conceptClassId - filter by concept class
	 * @param exactMatchSearch - true to perform and exact match search using the searchValue parameter
	 * @return the total number of concept records
	 */
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
    public List<Obs> getObsWithStatistics(Integer encounterId, Integer conceptId, Integer formFieldId, boolean includeVoidedObs);
    
    /**
     * DWE CHICA-612
     * Gets a sorted list of all atd_statistics with the encounterId and formName
     * @param encounterId
     * @param formName
     * @param orderAscDesc - ASC or DESC
     * @return sorted list of Statistics
     */
    public List<Statistics> getAllStatsByEncounterForm(Integer encounterId,String formName, String orderAscDesc);
    
    /**
     * Checks to see if at least one box is checked for this rule and encounter 
     * in the atd_statistics table
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
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
    public List<Statistics> getStatsByEncounterRule(Integer encounterId, Integer ruleId);
    
    /**
     * Looks up the form names by form attribute values
     * 
     * @param formAttrNames
     * @param formAttrValue
	 * @param isRetired
     * @return
     */
    public List<String> getFormNamesByFormAttribute(List<String> formAttrNames, String formAttrValue, boolean isRetired) throws DAOException;
}
