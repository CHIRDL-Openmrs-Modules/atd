package org.openmrs.module.atd.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
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
import org.openmrs.module.atd.datasource.TeleformExportXMLDatasource;
import org.openmrs.module.atd.db.ATDDAO;
import org.openmrs.module.atd.hibernateBeans.PSFQuestionAnswer;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.BadScansFileFilter;
import org.openmrs.module.atd.util.ConceptDescriptor;
import org.openmrs.module.atd.util.FormDefinitionDescriptor;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.dss.DssElement;
import org.openmrs.module.dss.DssManager;
import org.openmrs.module.dss.hibernateBeans.Rule;
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
		TeleformExportXMLDatasource xmlDatasource = (TeleformExportXMLDatasource) logicService.getLogicDataSource("xml");
		try {
			formInstance = xmlDatasource.parse(customInput, formInstance, locationTagId);
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
		
		HashMap<String, Field> fieldMap = xmlDatasource.getParsedFile(formInstance);
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
					parameterHandler.addParameters(parameters,rule);
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
		DssManager dssManager = new DssManager(patient);
		return this.produce(patient, formInstance, customOutput,
				dssManager, encounterId,baseParameters,
				locationTagId,sessionId);
	}
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,
			 Integer locationTagId,Integer sessionId)
	{
		
		AdministrationService adminService = Context.getAdministrationService();
		boolean mergeToTable=false;
		try
		{
			mergeToTable = Boolean.parseBoolean(
					adminService.getGlobalProperty("atd.mergeToTable"));
		} catch (Exception e1)
		{
		}
		FormService formService = Context.getFormService();
		
		if(!mergeToTable)
		{
			try
			{
				this.createMergeXML(customOutput, patient,formInstance,
						dssManager,encounterId,baseParameters,
						locationTagId,sessionId);
			} catch (Exception e)
			{
				this.log.error("Error creating merge xml");
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
				return false;
			}
		}
		
		if(mergeToTable)
		{
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try
			{
				this.createMergeXML(output, patient,formInstance,
						dssManager,encounterId,baseParameters,
						locationTagId,sessionId);
			} catch (Exception e)
			{
				this.log.error("Error creating merge xml");
				this.log.error(e.getMessage());
				this.log.error(Util.getStackTrace(e));
				return false;
			}
			if(formInstance != null){
				String formname = formService.getForm(formInstance.getFormId()).getName();
				this.createMergeTable(output.toString(),
					formInstance.getFormInstanceId(), formname);
			}
		}
		
		this.saveDssElementsToDatabase(patient,formInstance,
				dssManager,encounterId);
		return true;
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
			for (int i=0,pos=0; i < dssElements.size(); i++,pos++)
			{
				DssElement currDssElement = dssElements.get(i);
				atdService.addPatientATD(patientId, formInstance,
						currDssElement,encounterId);
			}
		}
	}
	
	private int createMergeXML(OutputStream output, 
			Patient patient, FormInstance formInstance, 
			DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,
			Integer locationTagId,
			Integer sessionId
			)
	{
		TeleformTranslator translator = new TeleformTranslator();
		translator.formToTeleformOutputStream(formInstance, output,
				patient,dssManager,encounterId,
				baseParameters,
				locationTagId,sessionId);
		return formInstance.getFormId();
	}
	
	private void createMergeTable(String inputXML, 
			Integer formInstanceId, String formname)
	{
		AdministrationService adminService = Context.getAdministrationService();
		String xsltFilename = 
			adminService.getGlobalProperty("atd.convertMergeXMLToTableFile");
		if(xsltFilename == null){
			this.log.error("No xslt filename. You need to set global property atd.convertMergeXMLToTableFile. Could not convert teleform xml to table for form: "+
					formname +" with id: "+formInstanceId+".");
			return;
		}
		TeleformTranslator translator = new TeleformTranslator();
		try
		{
			translator.teleformXMLToTable(inputXML,
					xsltFilename,formname,
					formInstanceId);
		} catch (Exception e)
		{		
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
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
	
	public void addPatientATD(int patientId, FormInstance formInstance, DssElement dssElement,
			Integer encounterId) throws APIException
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
		
		getATDDAO().addPatientATD(patientATD);
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
        ((TeleformExportXMLDatasource) Context.getLogicService().getLogicDataSource("xml")).clearParsedFile();
        
    }
    
    public void prePopulateNewFormFields(Integer formId) {
	    getATDDAO().prePopulateNewFormFields(formId);
    }
    
    public void populateEmtptyFormFields(Integer formId) throws APIException {
	    getATDDAO().populateEmtptyFormFields(formId);
    }

    public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
                                       String installationDirectory, String serverName, boolean faxableForm, 
                                       boolean scannableForm, boolean scorableForm, String scoreConfigLoc, 
                                       Integer numPrioritizedFields, Integer copyPrinterConfigFormId) {
	    getATDDAO().setupInitialFormValues(formId, formName, locationNames, installationDirectory, serverName, faxableForm, 
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
	
	public void createStatistics(Statistics statistics)
	{
		getATDDAO().addStatistics(statistics);
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
	
	public List<Statistics> getAllStatsByEncounterForm(Integer encounterId,String formName){
		return getATDDAO().getAllStatsByEncounterForm(encounterId, formName);
	}

	public List<Statistics> getStatsByEncounterFormNotPrioritized(Integer encounterId,String formName){
		return getATDDAO().getStatsByEncounterFormNotPrioritized(encounterId, formName);
	}
	
	/**
	 * @should testPSFProduce
	 * @should testPWSProduce
	 */
	public void produce(OutputStream output, PatientState state,
			Patient patient, Integer encounterId, String dssType,
			int maxDssElements,Integer sessionId)
	{
		ATDService atdService = Context
				.getService(ATDService.class);

		DssManager dssManager = new DssManager(patient);
		dssManager.setMaxDssElementsByType(dssType, maxDssElements);
		HashMap<String, Object> baseParameters = new HashMap<String, Object>();

		FormInstance formInstance = state.getFormInstance();
		atdService.produce(patient, formInstance, output, dssManager,
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
		Rule rule = dssService.getRule(ruleId);
		
		Statistics statistics = new Statistics();
		statistics.setAgeAtVisit(Util.adjustAgeUnits(patient.getBirthdate(), null));
		statistics.setPriority(rule.getPriority());
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
        	log.equals("Please specify a value for the global property 'atd.defaultTifImageDirectory'");
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
            fileLoc.delete();
            
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
	 * Auto generated method comment
	 * 
	 * @param patientId
	 * @return
	 */
    public PatientIdentifier getPatientMRN(Integer patientId){
		return getATDDAO().getPatientMRN(patientId);
	}

	/**
	 * @see org.openmrs.module.atd.service.ATDService#getPSFQuestionAnswers(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
    public List<PSFQuestionAnswer> getPSFQuestionAnswers(Integer formInstanceId, Integer locationId, Integer patientId) {
	    return getATDDAO().getPSFQuestionAnswers(formInstanceId, locationId, patientId);
    }

	@Override
	public List<ConceptDescriptor> getAllConceptsAsDescriptor() {
		return this.dao.getAllConceptAsDescriptor();
	}

	@Override
	public List<FormDefinitionDescriptor> getAllFormDefinitionAsDescriptor() {
		return this.dao.getAllFormDefinitionAsDescriptor();
	}

	@Override
	public List<FormDefinitionDescriptor> getFormDefinitionAsDescriptor(Integer formId) {
		return this.dao.getFormDefinitionAsDescriptor(formId);
	}
	
	
}
