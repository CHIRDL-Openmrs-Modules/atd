package org.openmrs.module.atd.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.cache.Cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.FormPrinterConfig;
import org.openmrs.module.atd.ParameterHandler;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.atd.datasource.FormDatasource;
import org.openmrs.module.atd.db.ATDDAO;
import org.openmrs.module.atd.hibernateBeans.PSFQuestionAnswer;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.AtdConstants;
import org.openmrs.module.atd.util.BadScansFileFilter;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.chirdlutilbackports.cache.ApplicationCacheManager;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.dss.DssElement;
import org.openmrs.module.dss.DssManager;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.hibernateBeans.RuleEntry;
import org.openmrs.module.dss.service.DssService;

/**
 * ATD related service implementations
 * 
 * @author Tammy Dugan
 */
public class ATDServiceImpl implements ATDService
{
	private Log log = LogFactory.getLog(this.getClass());
	private ATDDAO dao;

	/**
	 * Empty constructor
	 */
	public ATDServiceImpl()
	{
	}

	public ArrayList<TeleformFileState> fileProcessed(ArrayList<TeleformFileState> tfstates)
	{
		return tfstates;
	}
	
	public TeleformFileState fileProcessed(TeleformFileState tfstate)
	{
		return tfstate;
	}
	
	/**
	 * Gets the dao object for this service. The dao
	 * object allows access to database methods.
	 * 
	 * @return ATDDAO object allowing access to database methods
	 */
	public ATDDAO getATDDAO()
	{
		return this.dao;
	}

	/**
	 * Sets the dao object for this service.
	 * @param dao database access object for this service
	 */
	public void setATDDAO(ATDDAO dao)
	{
		this.dao = dao;
	}
	
	public boolean consume(InputStream customInput, 
			FormInstance formInstance,
			Patient patient, int encounterId,
			Map<String, Object> baseParameters,
			ParameterHandler parameterHandler,
			List<FormField> fieldsToConsume,
			Integer locationTagId, Integer sessionId
			)
	{

		ATDService atdService = 
			Context.getService(ATDService.class);
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		TeleformTranslator translator = new TeleformTranslator();
		FormService formService = Context.getFormService();
		Form databaseForm = null;
		if(formInstance != null){
			 databaseForm = formService.getForm(formInstance.getFormId());
		}
		if(databaseForm == null)
		{
			String errMessage = "Could not consume teleform export xml because form ";
			if(formInstance != null){
				errMessage+=formInstance.getFormId();
			}
			errMessage+=" does not exist in the database";
			log.error(errMessage);
			return false;
		}
		
		//parse the xml
		LogicService logicService = Context.getLogicService();
		FormDatasource xmlDatasource = (FormDatasource) logicService.getLogicDataSource("form");
		try {
			formInstance = xmlDatasource.parseTeleformXmlFormat(customInput, formInstance, locationTagId);
		}
		catch (Exception e) {
			this.log.error("Error parsing file to be consumed");
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
			return false;
		}
		
		if (formInstance == null) {
			log.error("Form instance came back null");
			return false;
		}
		
		HashMap<String, Field> fieldMap = xmlDatasource.getFormFields(formInstance);
		// check that the medical record number in the xml file and the medical
		// record number of the patient match
		String patientMedRecNumber = patient.getPatientIdentifier().getIdentifier();
		String xmlMedRecNumber = null;
		Integer locationId = formInstance.getLocationId();
		Integer formId = formInstance.getFormId();
		Integer formInstanceId = formInstance.getFormInstanceId();
		String medRecNumberTag = org.openmrs.module.chirdlutilbackports.util.Util.getFormAttributeValue(formId,
		    "medRecNumberTag", locationTagId, locationId);
		
		//MRN
		if (medRecNumberTag != null && fieldMap.get(medRecNumberTag) != null) {
			xmlMedRecNumber = fieldMap.get(medRecNumberTag).getValue();
		}
		
		//Compare form MRNs to patient medical record number
		if (!Util.extractIntFromString(patientMedRecNumber).equalsIgnoreCase(Util.extractIntFromString(xmlMedRecNumber))) {
			org.openmrs.module.chirdlutilbackports.hibernateBeans.Error noMatch = new org.openmrs.module.chirdlutilbackports.hibernateBeans.Error(
			        "Fatal", "MRN Validity", "Patient MRN" + " does not match MRN bar code ", "\r\n Form instance id: "
			                + formInstanceId + "\r\n Patient MRN: " + patientMedRecNumber + " \r\n MRN barcode: "
			                + xmlMedRecNumber, new Date(), sessionId);
			chirdlutilbackportsService.saveError(noMatch);
			return false;
			
		}
		
		LinkedHashMap<FormField,String> formFieldToValue =
			new LinkedHashMap<FormField,String>();
	
		if(fieldsToConsume == null){
			fieldsToConsume = databaseForm.getOrderedFormFields();
		}
		
		//map form field definition from database
		//to result for that field in the xml that is being consumed
		for (FormField currField : fieldsToConsume)
		{
			FieldType currFieldType = currField.getField().getFieldType();
			// only process export fields
			if (currFieldType != null
					&& currFieldType.equals(translator
							.getFieldType("Export Field")))
			{
				String fieldName = currField.getField().getName();
				if (fieldMap.get(fieldName) != null)
				{
					String value = fieldMap.get(fieldName).getValue();

					formFieldToValue.put(currField, value);
				}
			}
		}
						
		String mode = "CONSUME";
		
		LinkedHashMap<String,LinkedHashMap<String,Rule>> rulesToRunByField = 
			new LinkedHashMap<String,LinkedHashMap<String,Rule>>();
		
		//find consume rules to execute
		//set parameters for rules
		for(FormField currField:formFieldToValue.keySet())
		{
			String fieldName = currField.getField().getName();
			Concept currConcept = currField.getField().getConcept();
			String ruleName = currField.getField().getDefaultValue();
			LinkedHashMap<String,Rule> rulesToRun = null;
			Map<String, Object> parameters = new HashMap<String,Object>();

			FormField parentField = currField.getParent();
			
			//if parent field is not null look at parent
			//field for rule to execute
			Rule rule = null;
			if(parentField != null)
			{
				FieldType currFieldType = currField.getField().getFieldType();

				if(currFieldType.equals(translator
						.getFieldType("Prioritized Merge Field")))
				{
					ruleName = null;//no rule to execute unless patientATD finds one	
				}
				PatientATD patientATD = 
					atdService.getPatientATD(formInstance, 
							parentField.getField().getFieldId());
				
				if(patientATD != null)
				{
					rule = patientATD.getRule();
					ruleName = rule.getTokenName();
				}
			}
			String lookupFieldName = null;
			
			if(parentField != null){
				lookupFieldName = parentField.getField().getName();
			}else{
				lookupFieldName = fieldName;
			}
			if(ruleName != null)
			{
				rulesToRun = rulesToRunByField.get(lookupFieldName);
				if(rulesToRun == null)
				{
					rulesToRun  = 
						new LinkedHashMap<String,Rule>();
					rulesToRunByField.put(lookupFieldName, rulesToRun);
				}
				
				Rule ruleLookup = rulesToRun.get(ruleName);
				
				if(ruleLookup == null)
				{
					if(rule != null){
						ruleLookup = rule;
					}else{
						ruleLookup = new Rule();
						ruleLookup.setTokenName(ruleName);
					}
					ruleLookup.setParameters(parameters);
					rulesToRun.put(ruleName, ruleLookup);
				}else
				{
					parameters = ruleLookup.getParameters();
				}
			}
			
			//------------start set rule parameters
			parameters.put("sessionId", sessionId);
			parameters.put("formInstance", formInstance);
			parameters.put("locationTagId",locationTagId);
			Integer formfieldId = currField.getFormFieldId();
			parameters.put("formFieldId", formfieldId); // CHICA-765 added form field id
			EncounterService encounterService = Context.getEncounterService();
			Encounter encounter = encounterService.getEncounter(encounterId);
			locationId = encounter.getLocation().getLocationId();
			parameters.put("locationId", locationId);
			LocationService locationService = Context.getLocationService();
			Location location = locationService.getLocation(locationId);
			String locationName = null;
			if(location != null){
				locationName = location.getName();
			}
			parameters.put("location", locationName);
			parameters.put("mode", mode);
			parameters.put("encounterId", encounterId);
			if(rule != null)
			{
				parameters.put("ruleId", rule.getRuleId());
			}
			
			if (currConcept != null)
			{
				try
				{
					String elementString = ((ConceptName) currConcept.getNames().toArray()[0]).getName();
					parameters.put("concept", elementString);
				} catch (Exception e)
				{
					parameters.put("concept", null);
				}
			}else
			{
				parameters.put("concept", null);
			}
			
			if(fieldName != null)
			{
				parameters.put("fieldName", lookupFieldName);
			}
			//----------end set rule parameters
		}
		
		//add child fields as parameters to parent field rules
		HashMap<String,Integer> childIndex = new HashMap<String,Integer>();
		
		for(FormField currField:formFieldToValue.keySet())
		{
			LinkedHashMap<String,Rule> rulesToRun = null;
			Map<String, Object> parameters = new HashMap<String,Object>();
			FormField parentField = currField.getParent();

			//look for parentField
			if(parentField != null)
			{
				FieldType parentFieldType = parentField.getField().getFieldType();

				String parentRuleName = parentField.getField().getDefaultValue();
				String parentFieldName = parentField.getField().getName();

				if(parentFieldType.equals(translator
						.getFieldType("Prioritized Merge Field")))
				{
					parentRuleName = null;//no rule to execute unless patientATD finds one	
				}
				PatientATD patientATD = 
					atdService.getPatientATD(formInstance, 
							parentField.getField().getFieldId());
				
				if(patientATD != null)
				{
					Rule rule = patientATD.getRule();
					parentRuleName = rule.getTokenName();
				}
				//if there is a parent rule, add a parameter for the child's fieldname
				//add the parent rule if it is not in rules to run
				if(parentRuleName!=null)
				{
					rulesToRun = rulesToRunByField.get(parentFieldName);
					if(rulesToRun == null)
					{
						rulesToRun  = 
							new LinkedHashMap<String,Rule>();
						rulesToRunByField.put(parentFieldName, rulesToRun);
					}
					
					Rule ruleLookup = rulesToRun.get(parentRuleName);
					
					if(ruleLookup == null)
					{
						ruleLookup = new Rule();
						ruleLookup.setParameters(parameters);
						ruleLookup.setTokenName(parentRuleName);
						rulesToRun.put(parentRuleName, ruleLookup);
					}else
					{
						parameters = ruleLookup.getParameters();
					}
					
					String childFieldName = currField.getField().getName();
					Integer index = childIndex.get(parentFieldName);
					if(index == null)
					{
						index = 0;
					}
					parameters.put("child"+index, childFieldName);
					childIndex.put(parentFieldName,++index);
				}
			}
		}
		
		//run all the consume rules
		String formType = org.openmrs.module.chirdlutil.util.Util.getFormType(formInstance.getFormId(), locationTagId, locationId); // CHICA-1234 Look up the formType
		for(LinkedHashMap<String, Rule> rulesToRun: 
			rulesToRunByField.values())
		{
			for(String currRuleName: rulesToRun.keySet())
			{
				Rule rule = rulesToRun.get(currRuleName);
				Map<String, Object> parameters = 
					rule.getParameters();
					
				if(baseParameters != null)
				{
					parameters.putAll(baseParameters);
				}
				if(parameterHandler != null){
					parameterHandler.addParameters(parameters, formType); // CHICA-1234 Added formType parameter
				}
				this.evaluateRule(currRuleName, patient, parameters);
			}
		}
		
		return true;
	}
	
	public boolean produce(Patient patient, FormInstance formInstance,
			OutputStream customOutput, Integer encounterId,
			Integer locationTagId,Integer sessionId,State produceState)
	{
		return this.produce(patient,formInstance,customOutput,encounterId,null,
				locationTagId,sessionId);
	}

	public boolean produce(Patient patient, FormInstance formInstance,
			OutputStream customOutput, DssManager dssManager,
			Integer encounterId, Integer locationTagId,
			Integer sessionId)
	{
		return this.produce(patient, formInstance, customOutput, 
				dssManager, encounterId, null,
				locationTagId,sessionId);
	}
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			Integer encounterId,
			Map<String,Object> baseParameters,
			Integer locationTagId,
			Integer sessionId)
	{
		HashMap<String, OutputStream> outputs = new HashMap<String,OutputStream>();
		outputs.put(ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_XML, customOutput);
		return  produce(patient, formInstance, outputs,
            encounterId, baseParameters, locationTagId,sessionId);
	}
	
	public boolean produce(Patient patient, FormInstance formInstance, Map<String, OutputStream> outputs,
	                       Integer encounterId, Map<String, Object> baseParameters, Integer locationTagId, Integer sessionId) {
		DssManager dssManager = new DssManager(patient);
	
		return this.produce(patient, formInstance, outputs, dssManager, encounterId, baseParameters, locationTagId,
		    sessionId);
	}        	
	
	public boolean produce(Patient patient, FormInstance formInstance, Map<String,OutputStream> outputs, DssManager dssManager,
	                       Integer encounterId, Map<String, Object> baseParameters, Integer locationTagId, Integer sessionId) {
		
			try {
				this.createMergeFile(outputs, patient, formInstance, dssManager, encounterId, baseParameters,
				    locationTagId, sessionId);
			}
			catch (Exception e) {
				this.log.error("Error creating merge xml");
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
				return false;
			}

		this.saveDssElementsToDatabase(patient, formInstance, dssManager, encounterId);
		return true;
	}
	
	public boolean produce(Patient patient, FormInstance formInstance, OutputStream customOutput, DssManager dssManager,
	                       Integer encounterId, Map<String, Object> baseParameters, Integer locationTagId, Integer sessionId) {
		HashMap<String, OutputStream> outputs = new HashMap<String,OutputStream>();
		outputs.put(ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_XML, customOutput);
		return produce(patient, formInstance, outputs, dssManager, encounterId, baseParameters, locationTagId, sessionId);
	}
	
	private void saveDssElementsToDatabase(Patient patient,
			FormInstance formInstance, DssManager dssManager,
			Integer encounterId)
	{
		Integer patientId = patient.getPatientId();
		
		HashMap<String,ArrayList<DssElement>> dssElementsByType = 
			dssManager.getDssElementsByType();
		ATDService atdService = Context.getService(ATDService.class);
		
		if(dssElementsByType == null)
		{
			return;
		}
		Iterator<ArrayList<DssElement>> iter = dssElementsByType.values()
				.iterator();
		ArrayList<DssElement> dssElements = null;

		while (iter.hasNext())
		{
			dssElements = iter.next();
			for (int i=0; i < dssElements.size(); i++)
			{
				DssElement currDssElement = dssElements.get(i);
				atdService.addPatientATD(formInstance,
						currDssElement,encounterId, patientId);
			}
		}
	}
	
	private int createMergeFile(Map<String,OutputStream> outputs, Patient patient, FormInstance formInstance, DssManager dssManager,
	                           Integer encounterId, Map<String, Object> baseParameters, Integer locationTagId,
	                           Integer sessionId) {
		TeleformTranslator translator = new TeleformTranslator();
		Integer formId = formInstance.getFormId();
		FormService formService = Context.getFormService();
		String formName = formService.getForm(formId).getName();
		AdministrationService adminService = Context.getAdministrationService();
		
		for(String outputType:outputs.keySet()){
			outputType = outputType.trim();
			OutputStream output = outputs.get(outputType);
			if (outputType.equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_XML) || 
					outputType.equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_PDF)) {
				translator.formToTeleformXML(formInstance, output, patient, dssManager, encounterId, baseParameters,
				    locationTagId, sessionId);
			}
			
			if (outputType.equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_PDF)) {
				String templateDirectory = adminService.getGlobalProperty(
					ChirdlUtilConstants.GLOBAL_PROP_PDF_TEMPLATE_DIRECTORY);
				if (templateDirectory == null || templateDirectory.trim().length() == 0) {
					log.error("Value cannot be found for global property: atd.pdfTemplateDirectory.  No pdf "
							+ "merge file will be created for form: " + formName);
					continue;
				}
				
				File pdfTemplate = new File(templateDirectory, formName + ChirdlUtilConstants.FILE_PDF_TEMPLATE);
				translator.formToPDF(pdfTemplate.getAbsolutePath(), formInstance, output, patient, dssManager,
				    encounterId, baseParameters, locationTagId, sessionId);
			}
		}
	
		return formInstance.getFormId();
	}

	public Form teleformXMLToDatabaseForm(String formName, String templateXMLFilename)
	{
		TeleformTranslator translator = new TeleformTranslator();
		return translator.templateXMLToDatabaseForm(formName, templateXMLFilename);
	}

	public Result evaluateRule(String ruleEvalString,Patient patient,
			Map<String, Object> baseParameters)
	{
		DssService dssService = Context.getService(DssService.class);
		Result result = Result.emptyResult();
		StringTokenizer tokenizer = 
			new StringTokenizer(ruleEvalString,">");
		ArrayList<String> ruleTokens = new ArrayList<String>();
		Map<String, Object> parameters = null;
		
		while(tokenizer.hasMoreTokens())
		{
			ruleTokens.add(tokenizer.nextToken().trim());
		}
		parameters = new HashMap<String, Object>();
		if(baseParameters != null){
			parameters.putAll(baseParameters);
		}
			
		for (String ruleToken : ruleTokens)
		{
			if(ruleToken.contains("\""))
			{
				tokenizer  = new StringTokenizer(ruleToken.substring(1,ruleToken.length()-1),",");
				int position = 0;
				while(tokenizer.hasMoreTokens())
				{
					parameters.put("param" + position, tokenizer.nextToken().trim());
					position++;
				}
				continue;
			}
			
			Rule rule = new Rule();
			
			rule.setTokenName(ruleToken);
			rule.setParameters(parameters);
			
			result = 
				dssService.runRule(patient, rule);
			parameters = new HashMap<String, Object>();
			if(baseParameters != null){
				parameters.putAll(baseParameters);
			}
			
			if (result != null)
			{
				if (result.size() > 0)
				{
					for (int i = 0; i < result.size(); i++)
					{
						parameters.put("param" + i, result.get(i));
					}
				} else
				{
					parameters.put("param0", result);
				}
			}
		}
		if(result != null)
		{
			if(result.size() > 0)
			{
				return result.get(0);
			}
			
			return result;	
		}
		return null;
	}
	
	@Override
    public PatientATD addPatientATD(FormInstance formInstance, DssElement dssElement,
			Integer encounterId, Integer patientId) throws APIException
	{
		PatientATD patientATD = new PatientATD();
		patientATD.setCreationTime(new java.util.Date());
		patientATD.setFieldId((Integer) dssElement.getParameter("fieldId"));
		patientATD.setFormInstance(formInstance);
		patientATD.setPatientId(patientId);
		patientATD.setEncounterId(encounterId);
		Rule rule = new Rule();
		rule.setRuleId(dssElement.getRuleId());
		patientATD.setRule(rule);
		
		Result result = dssElement.getResult();
		if(result != null)
		{
			if(result.get(0) != null)
			{
				patientATD.setText(result.get(0).toString());
			}else
			{
				patientATD.setText(result.toString());
			}
		}
		
		return getATDDAO().addPatientATD(patientATD);
	}
	
	public PatientATD getPatientATD(FormInstance formInstance, int fieldId)
	{
		return getATDDAO().getPatientATD(formInstance,fieldId);
	}
		
	public void updatePatientStates(Date thresholdDate){
		getATDDAO().updatePatientStates(thresholdDate);
	}
	
    public void cleanCache() {
        log.info("Clear the cache if any");
        // parsedFile belongs to ATD, deal in there
        ((FormDatasource) Context.getLogicService().getLogicDataSource("form")).clearForms();
        
    }
    
    public void prePopulateNewFormFields(Integer formId) {
	    getATDDAO().prePopulateNewFormFields(formId);
    }

    public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
                                       String installationDirectory, boolean faxableForm, boolean scannableForm, 
                                       boolean scorableForm, String scoreConfigLoc, Integer numPrioritizedFields, 
                                       Integer copyPrinterConfigFormId) {
	    getATDDAO().setupInitialFormValues(formId, formName, locationNames, installationDirectory, faxableForm, 
	    	scannableForm, scorableForm, scoreConfigLoc, numPrioritizedFields, copyPrinterConfigFormId);
    }

	public void purgeFormAttributeValues(Integer formId) throws APIException {
		getATDDAO().purgeFormAttributeValues(formId);
	}

    public FormPrinterConfig getPrinterConfigurations(Integer formId, Integer locationId) throws APIException {
	    return getATDDAO().getPrinterConfigurations(formId, locationId);
    }
    
    public void savePrinterConfigurations(FormPrinterConfig printerConfig) throws APIException {
    	getATDDAO().savePrinterConfigurations(printerConfig);
    }

    public void copyFormAttributeValues(Integer fromFormId, Integer toFormId) throws APIException {
	    getATDDAO().copyFormAttributeValues(fromFormId, toFormId);
    }
    
    public void setClinicUseAlternatePrinters(List<Integer> locationIds, Boolean useAltPrinters) throws APIException {
    	getATDDAO().setClinicUseAlternatePrinters(locationIds, useAltPrinters);
    }
    
    public Boolean isFormEnabledAtClinic(Integer formId, Integer locationId) throws APIException {
    	return getATDDAO().isFormEnabledAtClinic(formId, locationId);
    }
    public void updateStatistics(Statistics statistics)
	{
    	getATDDAO().updateStatistics(statistics);
	}

	public Statistics createStatistics(Statistics statistics)
	{
		return getATDDAO().addStatistics(statistics);
	}
	
	public List<Statistics> getStatByIdAndRule(int formInstanceId,int ruleId,String formName, 
		Integer locationId)	{
		return getATDDAO().getStatByIdAndRule(formInstanceId,ruleId,formName,locationId);
	}
	
	public List<Statistics> getStatByFormInstance(int formInstanceId,String formName, 
		Integer locationId){
		return getATDDAO().getStatByFormInstance(formInstanceId,formName, locationId);
	}
	
	public List<Statistics> getStatsByEncounterForm(Integer encounterId,String formName){
		return getATDDAO().getStatsByEncounterForm(encounterId, formName);
	}

	public List<Statistics> getStatsByEncounterFormNotPrioritized(Integer encounterId,String formName){
		return getATDDAO().getStatsByEncounterFormNotPrioritized(encounterId, formName);
	}
	
	/**
	 * @should testPSFProduce
	 * @should testPWSProduce
	 */
	public void produce(OutputStream output, PatientState state, Patient patient, Integer encounterId, String dssType,
	                    int maxDssElements, Integer sessionId) {
		HashMap<String, OutputStream> outputs = new HashMap<String, OutputStream>();
		outputs.put("teleformXML", output);
		produce(outputs, state, patient, encounterId, dssType, maxDssElements, sessionId);
	}
	public void produce(Map<String,OutputStream> outputs, PatientState state,
	        			Patient patient, Integer encounterId, String dssType,
	        			int maxDssElements,Integer sessionId)
	{
		ATDService atdService = Context
				.getService(ATDService.class);

		DssManager dssManager = new DssManager(patient);
		dssManager.setMaxDssElementsByType(dssType, maxDssElements);
		HashMap<String, Object> baseParameters = new HashMap<String, Object>();

		FormInstance formInstance = state.getFormInstance();
		atdService.produce(patient, formInstance, outputs, dssManager,
				encounterId, baseParameters,state.getLocationTagId(),sessionId);
		Integer formInstanceId = formInstance.getFormInstanceId();
		Integer locationId = formInstance.getLocationId();
		Integer formId = formInstance.getFormId();
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formId);
		String formName = form.getName();
		this.saveStats(patient, formInstanceId, dssManager, encounterId,state.getLocationTagId(),
			locationId,formName);
	}
	
	private void saveStats(Patient patient, Integer formInstanceId, DssManager dssManager, Integer encounterId,
	                       Integer locationTagId, Integer locationId, String formName) {
		HashMap<String, ArrayList<DssElement>> dssElementsByType = dssManager.getDssElementsByType();
		EncounterService encounterService = Context.getService(EncounterService.class);
		Encounter encounter = (Encounter) encounterService.getEncounter(encounterId);
		String type = null;
		
		if (dssElementsByType == null) {
			return;
		}
		Iterator<String> iter = dssElementsByType.keySet().iterator();
		ArrayList<DssElement> dssElements = null;
		
		while (iter.hasNext()) {
			type = iter.next();
			dssElements = dssElementsByType.get(type);
			for (int i = 0; i < dssElements.size(); i++) {
				DssElement currDssElement = dssElements.get(i);
				
				this.addStatistics(patient, currDssElement, formInstanceId, i, encounter, formName, locationTagId,
				    locationId);
			}
		}
	}
	
	private void addStatistics(Patient patient, DssElement currDssElement, Integer formInstanceId, int questionPosition,
	                           Encounter encounter, String formName, Integer locationTagId, Integer locationId) {
		DssService dssService = Context.getService(DssService.class);
		Integer ruleId = currDssElement.getRuleId();
		
		// Try to get rule entry to determine priority
		Integer priority = null;
		RuleEntry ruleEntry = dssService.getRuleEntry(ruleId, formName);
		if (ruleEntry != null) {
			priority = ruleEntry.getPriority();
		}
		
		Statistics statistics = new Statistics();
		statistics.setAgeAtVisit(Util.adjustAgeUnits(patient.getBirthdate(), null));
		statistics.setPriority(priority);
		statistics.setFormInstanceId(formInstanceId);
		statistics.setLocationTagId(locationTagId);
		statistics.setPosition(questionPosition + 1);
		
		statistics.setRuleId(ruleId);
		statistics.setPatientId(patient.getPatientId());
		statistics.setFormName(formName);
		statistics.setEncounterId(encounter.getEncounterId());
		statistics.setLocationId(locationId);
		
		ATDService atdService = Context.getService(ATDService.class);
		
		atdService.createStatistics(statistics);
	}

    
    public List<URL> getBadScans(String locationName) {
        AdministrationService adminService = Context.getAdministrationService();
        String imageDirStr = adminService.getGlobalProperty("atd.defaultTifImageDirectory");
        if (imageDirStr == null || imageDirStr.length() == 0) {
        	log.error("Please specify a value for the global property 'atd.defaultTifImageDirectory'");
        	return new ArrayList<URL>();
        }
        
        File imageDirectory = new File(imageDirStr, locationName);
        if (!imageDirectory.exists()) {
        	log.error("Cannot find directory: " + imageDirStr + File.separator + locationName);
        	return new ArrayList<URL>();
        }
        
        List<URL> badScans = new ArrayList<URL>();
        String ignoreExtensions = adminService.getGlobalProperty("atd.badScansExcludedExtensions");
        List<String> extensionList = new ArrayList<String>();
        if (ignoreExtensions != null) {
        	StringTokenizer tokenizer = new StringTokenizer(ignoreExtensions, ",");
        	while (tokenizer.hasMoreTokens()) {
        		extensionList.add(tokenizer.nextToken());
        	}
        }
        
        FilenameFilter fileFilter = new BadScansFileFilter(new Date(), extensionList);
        return getBadScans(imageDirectory, badScans, fileFilter);
    }
    
    public void moveBadScan(String url, boolean formRescanned) throws Exception {
        try {
        	URL urlLoc = new URL(url);
            File fileLoc = new File(urlLoc.getFile());
            if (!fileLoc.exists()) {
            	log.warn("Bad scan does not exist: " + fileLoc.getAbsolutePath());
            	return;
            }
            
            File parentFile = fileLoc.getParentFile();
            File rescannedScansDir = null;
            if (formRescanned) {
            	rescannedScansDir = new File(parentFile, "rescanned bad scans");
            } else {
            	rescannedScansDir = new File(parentFile, "ignored bad scans");
            }
            
            if (!rescannedScansDir.exists()) {
            	rescannedScansDir.mkdirs();
            }
            
            String filename = fileLoc.getName();
            File newLoc = new File(rescannedScansDir, filename);
            if (newLoc.exists()) {
            	int i = 1;
            	int index = filename.indexOf("."); 
            	String name = null;
            	String extension = "";
            	if (index >= 0) {
            		name = filename.substring(0, index);
            		extension = filename.substring(index, filename.length());
            	} else {
            		name = filename;
            	}
            	newLoc = new File(rescannedScansDir, name + "_" + i++ + extension);
            	while (newLoc.exists() && i < 1000) {
            		newLoc = new File(rescannedScansDir, name + "_" + i++ + extension);
            	}
            }
            
            IOUtil.copyFile(fileLoc.getAbsolutePath(), newLoc.getAbsolutePath());
            if (!fileLoc.delete()) {
                log.error("Unable to delete file: " + fileLoc.getAbsolutePath());
            }
            
            // log the event
            String description = null;
            if (formRescanned) {
            	description = "User attempted to rescan a bad scan: " + newLoc.getAbsolutePath();
            } else {
            	description = "User ignored a bad scan: " + newLoc.getAbsolutePath();
            }
            
            org.openmrs.module.chirdlutilbackports.hibernateBeans.Error event = new org.openmrs.module.chirdlutilbackports.hibernateBeans.Error("Info", "Bad Scans", description, null, new Date(), null);
            ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
            chirdlutilbackportsService.saveError(event);
        }
        catch (Exception e) {
            log.error("Error moving bad scan", e);
            throw e;
        }
    }
    
    private List<URL> getBadScans(File imageDirectory, List<URL> badScans, FilenameFilter fileFilter) {
		File[] files = imageDirectory.listFiles(fileFilter);
		if (files != null) {
			for (File foundFile : files) {
				if (foundFile.isDirectory()) {
					getBadScans(foundFile, badScans, fileFilter);
				} else {
    				try {
    					URI foundUri = foundFile.toURI();
                        URL foundUrl = foundUri.toURL();
                        badScans.add(foundUrl);
                    }
                    catch (MalformedURLException e) {
                        log.error("Error converting file to URL", e);
                    }
				}
			}
		}
    	
    	return badScans;
    }
    
    /**
	 * This is a method I added to get around lazy initialization errors with patient.getIdentifier() in rules
	 * 
	 * @param patientId
	 * @return
	 */
    public PatientIdentifier getPatientMRN(Integer patientId){
		return getATDDAO().getPatientMRN(patientId);
	}

	/**
	 * @see org.openmrs.module.atd.service.ATDService#getPatientFormQuestionAnswers(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.String)
	 */
    public List<PSFQuestionAnswer> getPatientFormQuestionAnswers(Integer formInstanceId, Integer locationId, Integer patientId, String patientForm) {
	    return getATDDAO().getPatientFormQuestionAnswers(formInstanceId, locationId, patientId, patientForm);
    }

	/**
	 * @see org.openmrs.module.atd.service.ATDService#updatePatientATD(org.openmrs.module.atd.hibernateBeans.PatientATD)
	 */
    public PatientATD updatePatientATD(PatientATD patientATD) throws APIException {
	    return getATDDAO().addPatientATD(patientATD);
    }

	/**
	 * @see org.openmrs.module.atd.service.ATDService#getPatientATDs(org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance, java.util.List)
	 */
    public List<PatientATD> getPatientATDs(FormInstance formInstance, List<Integer> fieldIds) {
	    return getATDDAO().getPatientATDs(formInstance, fieldIds);
    }

	public List<FormDefinitionDescriptor> getAllFormDefinitions() throws SQLException {
		return this.dao.getAllFormDefinitions();
	}

	public List<FormDefinitionDescriptor> getFormDefinition(Integer formId) throws SQLException {
		return this.dao.getFormDefinition(formId);
	}
	
	/**
	 * DWE CHICA-332 4/16/15
	 * 
	 * @see org.openmrs.module.atd.service.ATDService#getFormAttributeValueLocationsAndTagsMap(Integer)
	 */
	@Override
	public HashMap<Integer, List<Integer>> getFormAttributeValueLocationsAndTagsMap(Integer formId)
	{
		return this.dao.getFormAttributeValueLocationsAndTagsMap(formId);
	}
	
	/**
	 * DWE CHICA-330 4/22/15 
	 * 
	 * @see org.openmrs.module.atd.service.ATDService#getConceptDescriptorList(int, int, String, boolean, int, String, String, boolean)
	 */
	public List<ConceptDescriptor> getConceptDescriptorList(int start, int length, String searchValue, boolean includeRetired, int conceptClassId, String orderByColumn, String ascDesc, boolean exactMatchSearch)
	{
		return this.dao.getConceptDescriptorList(start, length, searchValue, includeRetired, conceptClassId, orderByColumn, ascDesc, exactMatchSearch);
	}
	
	/**
	 * DWE CHICA-330 4/23/15 
	 * 
	 * @see org.openmrs.module.atd.service.ATDService#getCountConcepts(String, boolean, int, boolean)
	 */
	public int getCountConcepts(String searchValue, boolean includeRetired, int conceptClassId, boolean exactMatchSearch)
	{
		return this.dao.getCountConcepts(searchValue, includeRetired, conceptClassId, exactMatchSearch);
	}
	
	/**
     * DWE CHICA-437
     * @see org.openmrs.module.atd.service.ATDService#getObsWithStatistics(Integer, Integer, Integer, boolean)
     */
    public List<Obs> getObsWithStatistics(Integer encounterId, Integer conceptId, Integer formFieldId, boolean includeVoidedObs)
    {
    	return getATDDAO().getObsWithStatistics(encounterId, conceptId, formFieldId, includeVoidedObs);
    }
  
    /**
     * Checks to see if at least one box is checked for this rule and encounter 
     * in the atd_statistics table
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
    public boolean oneBoxChecked(Integer encounterId, Integer ruleId){
    	return getATDDAO().oneBoxChecked(encounterId, ruleId);
    }
    
    /**
     * 
     * Look up the Statistics record by encounter_id and rule_id.
     * This checks to see if the rule fired for a certain encounter
     * 
     * @param encounterId
     * @param ruleId
     * @return
     */
    public List<Statistics> getStatsByEncounterRule(Integer encounterId, Integer ruleId){
    	return getATDDAO().getStatsByEncounterRule(encounterId, ruleId);
    }
    
    public boolean ruleFiredForEncounter(Integer encounterId, Integer ruleId){
    	List<Statistics> stats = getStatsByEncounterRule(encounterId,ruleId);
    	if(stats != null && stats.size()>0){
    		return true;
    	}
    	return false;
    }

	/**
	 * @see org.openmrs.module.atd.service.ATDService#getFormRecords(org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag)
	 */
	public Records getFormRecords(FormInstanceTag formInstanceTag) throws APIException {
		if (formInstanceTag == null || formInstanceTag.getFormId() == null || formInstanceTag.getFormInstanceId() == null || 
				formInstanceTag.getLocationId() == null || formInstanceTag.getLocationTagId() == null) {
			String message = "Invalid parameters.  A non-null formInstance must be provided with non-null attributes. " + 
					"formInstanceTag: " + formInstanceTag;
			log.error(message);
			throw new APIException(message);
		}
		
		ApplicationCacheManager appCacheManager = ApplicationCacheManager.getInstance();
		Cache<FormInstanceTag, Records> cache = appCacheManager.getCache(
			AtdConstants.CACHE_FORM_DRAFT, AtdConstants.CACHE_FORM_DRAFT_KEY_CLASS, AtdConstants.CACHE_FORM_DRAFT_VALUE_CLASS);
		Records records = null;
		if (cache != null) {
			records = cache.get(formInstanceTag);
		}
		
		File mergeFile = null;
		if (records == null) {
			mergeFile = IOUtil.getMergeFile(formInstanceTag);
		}
		
		if (mergeFile != null && mergeFile.exists()) {
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(mergeFile);
				records = (Records) XMLUtil.deserializeXML(Records.class, inputStream);
			} catch (Exception e) {
				String message = "Error loading merge file for form instance: " + formInstanceTag.toString();
				log.error(message, e);
				throw new APIException(message);
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				}
				catch (IOException e) {
					// No need to bother the client with this error
					log.error("Error closing input stream", e);
				}
			}
			
			if (records != null && cache != null) {
				cache.put(formInstanceTag, records);
			}
		}
		
		return records;
	}

	/**
	 * @see org.openmrs.module.atd.service.ATDService#saveFormRecordsDraft(org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag, org.openmrs.module.atd.xmlBeans.Records)
	 */
	public void saveFormRecordsDraft(FormInstanceTag formInstanceTag, Records records) throws APIException {
		if (formInstanceTag == null || formInstanceTag.getFormId() == null || formInstanceTag.getFormInstanceId() == null || 
				formInstanceTag.getLocationId() == null || formInstanceTag.getLocationTagId() == null) {
			String message = "Invalid parameters.  A non-null formInstance must be provided with non-null attributes. " + 
					"formInstanceTag: " + formInstanceTag;
			log.error(message);
			throw new APIException(message);
		}
		
		ApplicationCacheManager appCacheManager = ApplicationCacheManager.getInstance();
		Cache<FormInstanceTag, Records> cache = appCacheManager.getCache(
			AtdConstants.CACHE_FORM_DRAFT, AtdConstants.CACHE_FORM_DRAFT_KEY_CLASS, AtdConstants.CACHE_FORM_DRAFT_VALUE_CLASS);
		if (cache != null) {
			cache.put(formInstanceTag, records);
		} else {
			String message = "Cache " + AtdConstants.CACHE_FORM_DRAFT + " not configured.  Cannot save form draft for form ID: " + 
					formInstanceTag.getFormId() + " form instance ID: " + formInstanceTag.getFormInstanceId() + " location ID: " + 
					formInstanceTag.getLocationId() + " location tag ID: " + formInstanceTag.getLocationTagId();
			log.error(message);
			throw new APIException(message);
		}
	}

	/**
	 * @see org.openmrs.module.atd.service.ATDService#saveFormRecords(org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag, org.openmrs.module.atd.xmlBeans.Records)
	 */
	public void saveFormRecords(FormInstanceTag formInstanceTag, Records records) throws APIException {
		if (formInstanceTag == null || formInstanceTag.getFormId() == null || formInstanceTag.getFormInstanceId() == null || 
				formInstanceTag.getLocationId() == null || formInstanceTag.getLocationTagId() == null) {
			String message = "Invalid parameters.  A non-null formInstance must be provided with non-null attributes. " + 
					"formInstanceTag: " + formInstanceTag;
			log.error(message);
			throw new APIException(message);
		}
		
		ApplicationCacheManager appCacheManager = ApplicationCacheManager.getInstance();
		Cache<FormInstanceTag, Records> cache = appCacheManager.getCache(
			AtdConstants.CACHE_FORM_DRAFT, AtdConstants.CACHE_FORM_DRAFT_KEY_CLASS, AtdConstants.CACHE_FORM_DRAFT_VALUE_CLASS);
		
		if (records == null) {
			// remove the data from the cache
			if (cache != null) {
				cache.remove(formInstanceTag);
			}
			
			return;
		}
		
		Integer formId = formInstanceTag.getFormId();
		Integer formInstanceId = formInstanceTag.getFormInstanceId();
		Integer locationId = formInstanceTag.getLocationId();
		Integer locationTagId = formInstanceTag.getLocationTagId();
		String exportDirectory = IOUtil.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
	        .getFormAttributeValue(formId, ChirdlUtilConstants.FORM_ATTR_DEFAULT_EXPORT_DIRECTORY, locationTagId, locationId));
		String defaultMergeDirectory = IOUtil.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
		        .getFormAttributeValue(formId, ChirdlUtilConstants.FORM_ATTR_DEFAULT_MERGE_DIRECTORY, locationTagId, locationId));
		
		FormInstance formInstance = new FormInstance(locationId, formId, formInstanceId);
		// write the xml for the export file
		// use xmle extension to represent form completion through electronic means.
		String exportFilename = exportDirectory + formInstance.toString() + ChirdlUtilConstants.FILE_EXTENSION_XMLE;
		
		OutputStream output = null;
		try {
			output = new FileOutputStream(exportFilename);
			XMLUtil.serializeXML(records, output);
			
			// remove the data from the cache
			if (cache != null) {
				cache.remove(formInstanceTag);
			}
		} catch (FileNotFoundException e) {
			String message = "Cannot find export form " + exportFilename + " for form ID: " + formId + " form instance ID: " + 
					formInstanceId + " location ID: " + locationId + " location tag ID: " + locationTagId;
			log.error(message, e);
			throw new APIException(message);
		} catch (IOException e) {
			String message = "Error writing export form " + exportFilename + " for form ID: " + formId + " form instance ID: " + 
					formInstanceId + " location ID: " + locationId + " location tag ID: " + locationTagId;
			log.error(message, e);
			throw new APIException(message);
		} finally {
			if (output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException e) {
					// This isn't super important.  No need to push the exception up to the client.
					log.error("Error flushing and closing output stream for form ID: " + formId + " form instance ID: " + formInstanceId + 
						" location ID: " + locationId + " location tag ID: " + locationTagId, e);
				}
			}
		}
		
		// rename the merge file to trigger state change
		String newMergeFilename = defaultMergeDirectory + formInstance.toString() + ChirdlUtilConstants.FILE_EXTENSION_20;
		File newFile = new File(newMergeFilename);
		if (!newFile.exists()) {
			try {
				File mergeFile = IOUtil.getMergeFile(formInstanceTag);
				IOUtil.copyFile(mergeFile.getAbsolutePath(), newMergeFilename);
				IOUtil.deleteFile(mergeFile.getAbsolutePath());
			} catch (Exception e) {
				// No need to push this exception back to the client.  It's just clean up issues.
				String message = "Error renaming merge file for form ID: " + formId + " form instance ID: " + formInstanceId + 
						" location ID: " + locationId + " location tag ID: " + locationTagId;
				log.error(message, e);
			}
		}
	}
	
	/**
     * @see org.openmrs.module.atd.service.ATDService#getFormNamesByFormAttribute(java.util.List, String, boolean)
     */
    public List<String> getFormNamesByFormAttribute(List<String> formAttrNames, String formAttrValue, boolean isRetired) throws APIException
    {
    	return getATDDAO().getFormNamesByFormAttribute(formAttrNames, formAttrValue, isRetired);
    }
}
