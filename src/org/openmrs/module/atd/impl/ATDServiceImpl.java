package org.openmrs.module.atd.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.openmrs.Encounter;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Patient;
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
import org.openmrs.module.atd.hibernateBeans.ATDError;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Program;
import org.openmrs.module.atd.hibernateBeans.Session;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateMapping;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.chirdlutil.util.Util;
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
			String rulePackagePrefix,
			ParameterHandler parameterHandler,
			List<FormField> fieldsToConsume,
			Integer locationTagId, Integer sessionId
			)
	{
		long startTime = System.currentTimeMillis();
		long totalTime = System.currentTimeMillis();
		ATDService atdService = 
			Context.getService(ATDService.class);
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
		TeleformExportXMLDatasource xmlDatasource = 
			(TeleformExportXMLDatasource) logicService
				.getLogicDataSource("xml");
		try
		{
			formInstance = xmlDatasource.parse(customInput,
					 formInstance,locationTagId);
		} catch (Exception e)
		{
			this.log.error("Error parsing file to be consumed");
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
			return false;
		}
		HashMap<String,Field> fieldMap = xmlDatasource.getParsedFile(formInstance);
		
		LinkedHashMap<FormField,String> formFieldToValue =
			new LinkedHashMap<FormField,String>();
	
		if(fieldsToConsume == null){
			fieldsToConsume = databaseForm.getOrderedFormFields();
		}
		
		startTime = System.currentTimeMillis();
		
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

		startTime = System.currentTimeMillis();
						
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
			Integer locationId = encounter.getLocation().getLocationId();
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
					parameters.put("concept", currConcept.getName().getName());
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

		startTime = System.currentTimeMillis();
		
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

		startTime = System.currentTimeMillis();
		
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
				parameterHandler.addParameters(parameters,rule);
				this.evaluateRule(currRuleName, patient, parameters,rulePackagePrefix);
			}
		}
		
		return true;
	}
	
	public boolean produce(Patient patient, FormInstance formInstance,
			OutputStream customOutput, Integer encounterId,
			Integer locationTagId,Integer sessionId,State produceState)
	{
		return this.produce(patient,formInstance,customOutput,encounterId,null,null,
				locationTagId,sessionId);
	}

	public boolean produce(Patient patient, FormInstance formInstance,
			OutputStream customOutput, DssManager dssManager,
			Integer encounterId, Integer locationTagId,
			Integer sessionId)
	{
		return this.produce(patient, formInstance, customOutput, 
				dssManager, encounterId, null,null,
				locationTagId,sessionId);
	}
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			Integer encounterId,
			Map<String,Object> baseParameters,
			String rulePackagePrefix,
			Integer locationTagId,
			Integer sessionId)
	{
		DssManager dssManager = new DssManager(patient);
		return this.produce(patient, formInstance, customOutput,
				dssManager, encounterId,baseParameters,
				rulePackagePrefix, locationTagId,sessionId);
	}
	
	public boolean produce(Patient patient,
			FormInstance formInstance, OutputStream customOutput, 
			DssManager dssManager,Integer encounterId,
			Map<String,Object> baseParameters,String rulePackagePrefix,
			 Integer locationTagId,Integer sessionId)
	{
		long totalTime = System.currentTimeMillis();
		long startTime = System.currentTimeMillis();
		
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
						rulePackagePrefix,locationTagId,sessionId);
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
						rulePackagePrefix,locationTagId,sessionId);
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
		
		startTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
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
			String rulePackagePrefix,Integer locationTagId,
			Integer sessionId
			)
	{
		TeleformTranslator translator = new TeleformTranslator();
		translator.formToTeleformOutputStream(formInstance, output,
				patient,dssManager,encounterId,
				baseParameters,rulePackagePrefix,
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
	
	public boolean tableExists(String tableName)
	{
		return getATDDAO().tableExists(tableName);	
	}
	
	public void executeSql(String sql)
	{
		getATDDAO().executeSql(sql);
	}
	
	public FormInstance addFormInstance(Integer formId,Integer locationId){
		return getATDDAO().addFormInstance(formId,locationId);
	}

	public Result evaluateRule(String ruleEvalString,Patient patient,
			Map<String, Object> baseParameters,String rulePackagePrefix)
	{
		AdministrationService adminService = Context.getAdministrationService();
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
			
			String defaultPackagePrefix = Util.formatPackagePrefix(
					adminService.getGlobalProperty("atd.defaultPackagePrefix"));
			result = 
				dssService.runRule(patient, rule,
						defaultPackagePrefix,rulePackagePrefix);
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
			/*
			 *  This is deliberate as per Tammy - to get one result per field of atd document
			 *  Vibha- added rulePackagePrefix to qualify the above statement, otherwise you are evaluating rules in other packages
			 */
			
			if(result.size() > 0 && rulePackagePrefix == null)
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
	
	public FormAttributeValue getFormAttributeValue(Integer formId, String formAttributeName,
			Integer locationTagId,Integer locationId)
	{
		return getATDDAO().getFormAttributeValue(formId,formAttributeName,locationTagId,locationId);
	}
	
	/**
	 * Get state mapping by initial state name
	 * 
	 * @param stateMappingId state mapping unique id
	 * @return state with given state name
	 */
	public StateMapping getStateMapping(State initialState,Program program)  {
		return getATDDAO().getStateMapping(initialState,program);
	}
	
	public Session addSession() {
		
		Session session = new Session();
		
		return getATDDAO().addSession(session);
	}
	
	public Session getSession(int sessionId)
	{
		return getATDDAO().getSession(sessionId);
	}
		
	public Session updateSession(Session session) {
		
		return getATDDAO().updateSession(session);
	}
	
	public PatientState addPatientState(Patient patient,State initialState,
			int sessionId,Integer locationTagId,Integer locationId)
	{
		PatientState patientState = new PatientState();
		patientState.setStartTime(new java.util.Date());
		patientState.setPatient(patient);
		patientState.setState(initialState);
		patientState.setSessionId(sessionId);
		patientState.setLocationId(locationId);
		patientState.setLocationTagId(locationTagId);
		return getATDDAO().addUpdatePatientState(patientState);
	}
	
	public PatientState updatePatientState(PatientState patientState)
	{
		return getATDDAO().addUpdatePatientState(patientState);
	}
		
	public List<PatientState> getPatientStatesWithForm(int sessionId){
		return getATDDAO().getPatientStatesWithForm(sessionId);
	}
	
	public PatientState getPrevPatientStateByAction(
			int sessionId, int patientStateId,String action)
	{
		return getATDDAO().getPrevPatientStateByAction(
				sessionId, patientStateId,action);
	}
	
	public List<PatientState> getUnfinishedPatientStatesAllPatients(Date optionalDateRestriction,
			Integer locationTagId,Integer locationId)
	{
		return getATDDAO().getUnfinishedPatientStatesAllPatients(optionalDateRestriction,locationTagId,locationId);
	}
	
	public List<PatientState> getUnfinishedPatientStateByStateName(String state,Date optionalDateRestriction,
			Integer locationTagId, Integer locationId){
		return getATDDAO().getUnfinishedPatientStateByStateName(state,optionalDateRestriction,locationTagId,locationId);
	}
	public PatientState getLastUnfinishedPatientState(Integer sessionId){
		return getATDDAO().getLastUnfinishedPatientState(sessionId);
	}
	
	public PatientState getLastPatientState(Integer sessionId){
		return getATDDAO().getLastPatientState(sessionId);
	}
	
	public List<PatientState> getLastPatientStateAllPatients(Date optionalDateRestriction, 
			Integer programId,String startStateName,Integer locationTagId, Integer locationId){
		return getATDDAO().getLastPatientStateAllPatients(optionalDateRestriction, programId,startStateName, locationTagId,locationId);
	}
	public State getStateByName(String stateName){
		return getATDDAO().getStateByName(stateName);
	}
	
	public Program getProgramByNameVersion(String name,String version){
		return getATDDAO().getProgramByNameVersion(name, version);
	}
	
	public Program getProgram(Integer programId){
		return getATDDAO().getProgram(programId);
	}
	
	public PatientState getPatientStateByEncounterFormAction(Integer encounterId, Integer formId, String action){
		return getATDDAO().getPatientStateByEncounterFormAction(encounterId, formId, action);
	}
	
	public PatientState getPatientStateByFormInstanceAction(FormInstance formInstance,String action){

		return getATDDAO().getPatientStateByFormInstanceAction(formInstance, action);
	}
	public ArrayList<String> getFormAttributesByName(String attributeName){
		return getATDDAO().getFormAttributesByName(attributeName);
	}
	
	public List<State> getStatesByActionName(String actionName){
		return getATDDAO().getStatesByActionName(actionName);
	}
	
	public State getState(Integer stateId){
		return getATDDAO().getState(stateId);
	}
	
	public void updatePatientStates(Date thresholdDate){
		getATDDAO().updatePatientStates(thresholdDate);
	}
	public PatientState getPatientState(Integer patientStateId){
		return getATDDAO().getPatientState(patientStateId);
	}
	
	public List<PatientState> getPatientStateBySessionState(Integer sessionId,
			Integer stateId){
		return getATDDAO().getPatientStateBySessionState(sessionId,stateId);
	}
	
	public List<PatientState> getAllRetiredPatientStatesWithForm(Date thresholdDate){
		return getATDDAO().getAllRetiredPatientStatesWithForm(thresholdDate);
	}
	
	public List<Session> getSessionsByEncounter(Integer encounterId){
		return getATDDAO().getSessionsByEncounter(encounterId);
	}
	
	public List<PatientState> getPatientStatesWithFormInstances(String formName, Integer encounterId){
		return getATDDAO().getPatientStatesWithFormInstances(formName, encounterId);
	}

	public List<PatientState> getPatientStateByEncounterState(Integer encounterId,
			Integer stateId){
		return getATDDAO().getPatientStateByEncounterState(encounterId,stateId);
	}
	
	public void saveError(ATDError error){
		getATDDAO().saveError(error);
	}
	
	public List<ATDError> getATDErrorsByLevel(String errorLevel,Integer sessionId){
		return getATDDAO().getATDErrorsByLevel(errorLevel, sessionId);
	}
	
	public Integer getErrorCategoryIdByName(String name){
		return getATDDAO().getErrorCategoryIdByName(name);
	}
	
	public Program getProgram(Integer locationTagId,Integer locationId){
		return getATDDAO().getProgram(locationTagId, locationId);
	}

	public List<FormAttributeValue> getFormAttributeValuesByValue(String value){
		return getATDDAO().getFormAttributeValuesByValue(value);
	}
	
	public List<PatientState> getUnfinishedPatientStateByStateSession(
		String stateName,Integer sessionId){
		return getATDDAO().getUnfinishedPatientStateByStateSession(stateName, sessionId);
	}
	
    public void cleanCache() {
        log.info("Clear the cache if any");
        // parsedFile belongs to ATD, deal in there
        ((TeleformExportXMLDatasource) Context.getLogicService().getLogicDataSource("xml")).clearParsedFile();
        
    }
    
	public List<PatientState> getPatientStateByFormInstanceState(FormInstance formInstance, State state) {
		return getATDDAO().getPatientStateByFormInstanceState(formInstance, state);
	}
	public List<PatientState> getPatientStatesByFormInstance(FormInstance formInstance, boolean isRetired) {
		return getATDDAO().getPatientStatesByFormInstance(formInstance, isRetired);
	}
	
	public List<PatientState> getPatientStatesBySession(Integer sessionId,boolean isRetired){
		return getATDDAO().getPatientStatesBySession(sessionId, isRetired);
	}

	
	public void unretireStatesBySessionId(Integer sessionId){
		List<PatientState> patientStates = 
			this.getPatientStatesBySession(sessionId,true);
		
		for(PatientState patientState:patientStates){
			patientState.setRetired(false);
			patientState.setDateRetired(null);
			this.updatePatientState(patientState);
		}
	}

    public void copyFormMetadata(Integer fromFormId, Integer toFormId) {
	    getATDDAO().copyFormMetadata(fromFormId, toFormId);
    }

    public void setupInitialFormValues(Integer formId, String formName, List<String> locationNames, 
                                       String defaultDriveLetter, String serverName, boolean scannableForm, 
                                       boolean scorableForm, String scoreConfigLoc, Integer numPrioritizedFields,
                                       Integer copyPrinterConfigFormId) {
	    getATDDAO().setupInitialFormValues(formId, formName, locationNames, defaultDriveLetter, serverName, scannableForm,
	    	scorableForm, scoreConfigLoc, numPrioritizedFields, copyPrinterConfigFormId);
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
}
