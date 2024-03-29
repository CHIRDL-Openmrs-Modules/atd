/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.atd.ruleLibrary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.atd.datasource.FormDatasource;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.atd.xmlBeans.Choose;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.FormConfig;
import org.openmrs.module.atd.xmlBeans.Geq;
import org.openmrs.module.atd.xmlBeans.Eq;
import org.openmrs.module.atd.xmlBeans.If;
import org.openmrs.module.atd.xmlBeans.LanguageAnswers;
import org.openmrs.module.atd.xmlBeans.Mean;
import org.openmrs.module.atd.xmlBeans.Plus;
import org.openmrs.module.atd.xmlBeans.Score;
import org.openmrs.module.atd.xmlBeans.Scores;
import org.openmrs.module.atd.xmlBeans.Value;
import org.openmrs.module.chirdlutil.util.XMLUtil;

/**
 * Calculates a person's age in years based from their date of birth to the index date
 */
public class CheckIncompleteScoringJit implements Rule {
	
	private static final Logger log = LoggerFactory.getLogger(CheckIncompleteScoringJit.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	@Override
    public Result eval(LogicContext context, Integer patientId, Map<String, Object> parameters) throws LogicException {
		
		PatientService patientService = Context.getPatientService();
		Patient patient = patientService.getPatient(patientId);
		FormInstance formInstance = (FormInstance) parameters.get("formInstance");
		Integer locationTagId = (Integer) parameters.get("locationTagId");
		Integer locationId = null;
		Integer formId = null;
		
		if (formInstance != null) {
			locationId = formInstance.getLocationId();
			formId = formInstance.getFormId();
		}
		
		ChirdlUtilBackportsService chirdlUtilBackportsService = Context.getService(ChirdlUtilBackportsService.class);
		
		//parse the scan xml
		LogicService logicService = Context.getLogicService();
		FormDatasource xmlDatasource = (FormDatasource) logicService.getLogicDataSource("form");
		HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap = xmlDatasource.getFormFields(formInstance);
		
		if(fieldMap == null){
			return Result.emptyResult();
		}
		
		//map fields to languages
		FormAttributeValue scorableFormConfigAttrVal = chirdlUtilBackportsService.getFormAttributeValue(formId, "scorableFormConfigFile",
		    locationTagId, locationId);
		
		String scorableFormConfigFile = null;
		if (scorableFormConfigAttrVal != null) {
			scorableFormConfigFile = scorableFormConfigAttrVal.getValue();
		}
		if (scorableFormConfigFile == null) {
			log.error("Could not find scorableFormConfigFile for locationId: {} and locationTagId: {}", locationId, locationTagId);
			return Result.emptyResult();
		}
		LanguageAnswers answersByLanguage = null;
		
		FormConfig formConfig = null;
		try (InputStream input = new FileInputStream(scorableFormConfigFile)){
			formConfig = (FormConfig) XMLUtil.deserializeXML(FormConfig.class, input);
			answersByLanguage = formConfig.getLanguageAnswers();
		}
		catch (IOException e1) {
			log.error("Error loading scoring config file {}", scorableFormConfigFile, e1);
			return Result.emptyResult();
		}
		HashMap<String, Field> langFieldsToConsume = org.openmrs.module.atd.util.Util.getLanguageFieldsToConsume(fieldMap, formInstance,
		    answersByLanguage);
		
		HashMap<String, HashMap<String, FormField>> formFieldsMap = new HashMap<>();
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formId);
		
		//make a map of child to parent fields. This is used when figuring out
		//whether to score the spanish or english side
		//we assume the configuration file always configures using the english fields
		HashMap<String, FormField> childFields = null;
		for (org.openmrs.FormField currFormField : form.getFormFields()) {
			FormField parentField = currFormField.getParent();
			if (parentField != null) {
				String fieldName = currFormField.getField().getName();
				String parentName = parentField.getField().getName();
				childFields = formFieldsMap.get(parentName);
				if (childFields == null) {
					childFields = new HashMap<>();
					formFieldsMap.put(parentName, childFields);
				}
				
				childFields.put(fieldName, currFormField);
			}
		}
		//parse the form configuration file
		if (scorableFormConfigAttrVal != null) {
			
			try {
				boolean incomplete = false;
				Scores scores = formConfig.getScores();
				PatientState openJITIncompleteState = null;

				//compute each score and save it to a concept in the database
				for (Score score : scores.getScores()) {
					Integer numBlankFieldsPerScore = 0;
					Integer maxNumBlankFieldsPerScore = 2;
					String maxNumBlankString = score.getMaxBlankFieldsAllowed();
					
					if(maxNumBlankString != null){
						maxNumBlankString = maxNumBlankString.trim();
						
						if(maxNumBlankString.length()>0){
							try{
								maxNumBlankFieldsPerScore = Integer.parseInt(maxNumBlankString);
							}catch(Exception e){
								
							}
						}
					}
					
					Value value = score.getValue(); //value that should be saved to the concept
					Plus plus = value.getPlus();
					Mean mean = value.getMean();
					
					//compute the sum
					if (plus != null) {
						
						List<Choose> choices = plus.getChooses();
						
						//process conditional logic
						if (choices != null) {
							
							for (Choose choose : choices) {
								If ifObject = choose.getIf();
								
								if (ifObject != null) {
									Geq geq = ifObject.getGeq();
									Eq eq = ifObject.getEq();
									
									if (geq != null||eq != null) {
										
										Field fieldOperand = null;
										String cnOperand = null;
										
										if(geq != null){
											fieldOperand = geq.getField();
											cnOperand = geq.getResult();
										}
										if(eq != null){
											fieldOperand = eq.getField();
											cnOperand = eq.getResult();
										}
										
										if (fieldOperand != null && cnOperand != null) {
											Field matchingField = Util.pickFieldLanguage(fieldOperand, langFieldsToConsume, 
											    formFieldsMap);
											if (matchingField != null) {
												org.openmrs.module.atd.xmlBeans.Field scorableFormField = fieldMap
												        .get(matchingField.getId());
												if (scorableFormField!=null&&
														scorableFormField.getValue() == null) {
													numBlankFieldsPerScore++;
												}
											}
										}
									}
									
								}
							}
						}
						List<Field> fields = plus.getFields();
						
						//sum the fields
						if (fields != null) {
							
							Integer computeSumResult =  computeSum(fields, langFieldsToConsume, fieldMap,
							    formFieldsMap);
							numBlankFieldsPerScore+=computeSumResult;
						}
					}
					//compute the average
					if (mean != null) {
						
						List<Field> fields = mean.getFields();
						Integer computeSumResult = computeSum(fields, langFieldsToConsume, fieldMap,
						    formFieldsMap);
						numBlankFieldsPerScore += computeSumResult;
						
					}
					Integer sessionId = (Integer) parameters.get("sessionId");
					
					//see if an incomplete state exists for the JIT
					State currState = chirdlUtilBackportsService.getStateByName("JIT_incomplete");
					List<PatientState> patientStates = chirdlUtilBackportsService.getPatientStateByFormInstanceState(formInstance, currState);
										
					//look for an open JIT_incomplete state
					for(PatientState patientState:patientStates){
						if(patientState.getEndTime()==null){
							openJITIncompleteState = patientState;
							break;
						}
					}
					//if the form is incomplete store an incomplete state
					if (numBlankFieldsPerScore > maxNumBlankFieldsPerScore) {
						
						//add a JIT_incomplete state if there is no open JIT_incomplete states
						if (openJITIncompleteState == null) {
							chirdlUtilBackportsService.addPatientState(patient, currState, sessionId, locationTagId, locationId, formInstance);
						}else{
							//update the start time if the state already exists
							openJITIncompleteState.setStartTime(new java.util.Date());
							chirdlUtilBackportsService.updatePatientState(openJITIncompleteState);
						}
						incomplete = true;
					}
				}
				
				if(!incomplete){
					//if a JIT_incomplete state exists, make sure the state is ended
					if (openJITIncompleteState != null) {
						openJITIncompleteState.setEndTime(new java.util.Date());
						chirdlUtilBackportsService.updatePatientState(openJITIncompleteState);
					}
				}
			}
			catch (Exception e) {
				log.error("Error computing JIT scores", e);
			}
		}
		
		return Result.emptyResult();
		
	}
	
	private Integer computeSum(List<Field> fields, HashMap<String, Field> langFieldsToConsume,
	                           HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap,
	                           HashMap<String, HashMap<String, FormField>> formFieldsMap) {
		
		Integer numBlankScoringFields = 0;
		if(fields == null||fieldMap==null)
		{
			return numBlankScoringFields;
		}
		for (Field currField : fields) {
			
			Field matchingField = Util.pickFieldLanguage(currField, langFieldsToConsume, formFieldsMap);
			org.openmrs.module.atd.xmlBeans.Field scorableFormField = fieldMap.get(matchingField.getId());
			
			if (scorableFormField.getValue() == null) {
				numBlankScoringFields++;
			}
		}

		return numBlankScoringFields;
	}
	
	/**
	 * @see org.openmrs.logic.rule.Rule#getParameterList()
	 */
	@Override
    public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.rule.Rule#getDependencies()
	 */
	@Override
    public String[] getDependencies() {
		return new String[] { "%%patient.birthdate" };
	}
	
	/**
	 * @see org.openmrs.logic.rule.Rule#getTTL()
	 */
	@Override
    public int getTTL() {
		return 60 * 60 * 24; // 1 day
	}
	
	/**
	 * @see org.openmrs.logic.rule.Rule#getDatatype(String)
	 */
	@Override
    public Datatype getDefaultDatatype() {
		return Datatype.NUMERIC;
	}
	
}
