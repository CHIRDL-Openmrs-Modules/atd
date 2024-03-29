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
package org.openmrs.module.atd.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Concept;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.atd.datasource.FormDatasource;
import org.openmrs.module.atd.xmlBeans.Choose;
import org.openmrs.module.atd.xmlBeans.Eq;
import org.openmrs.module.atd.xmlBeans.EstimatedScoreValue;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.FormConfig;
import org.openmrs.module.atd.xmlBeans.Geq;
import org.openmrs.module.atd.xmlBeans.If;
import org.openmrs.module.atd.xmlBeans.LanguageAnswers;
import org.openmrs.module.atd.xmlBeans.Mean;
import org.openmrs.module.atd.xmlBeans.Plus;
import org.openmrs.module.atd.xmlBeans.Score;
import org.openmrs.module.atd.xmlBeans.Scores;
import org.openmrs.module.atd.xmlBeans.Then;
import org.openmrs.module.atd.xmlBeans.Value;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 *
 */
public class FormScoringUtil {
	private static final Logger log = LoggerFactory.getLogger(FormScoringUtil.class);

	public static void scoreJit(FormInstance formInstance, Integer locationTagId, Integer encounterId, 
	                            Patient patient) {
		
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer locationId = formInstance.getLocationId();
		Integer formId = formInstance.getFormId();
		
		//parse the scan xml
		LogicService logicService = Context.getLogicService();
		FormDatasource formDatasource = (FormDatasource) logicService.getLogicDataSource("form");
		HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap = formDatasource.getFormFields(formInstance);
		
		//map fields to languages
		FormAttributeValue scorableFormConfigAttrVal = chirdlutilbackportsService.getFormAttributeValue(formId, "scorableFormConfigFile",
		    locationTagId, locationId);
		
		if (fieldMap == null) {
			return;
		}
		
		String scorableFormConfigFile = null;
		
		if (scorableFormConfigAttrVal != null) {
			scorableFormConfigFile = scorableFormConfigAttrVal.getValue();
		}
		
		if (scorableFormConfigFile == null) {
			log.error("Could not find scorableFormConfigFile for locationId: {} and locationTagId: {}", locationId, locationTagId);
			return;
		}
		
		LanguageAnswers answersByLanguage = null;
		FormConfig formConfig = null;
		InputStream input = null;
		try {
			input = new FileInputStream(scorableFormConfigFile);
			formConfig = (FormConfig) XMLUtil.deserializeXML(FormConfig.class, input);
			answersByLanguage = formConfig.getLanguageAnswers();
		}
		catch (IOException e1) {
			log.error("Error scoring JIT", e1);
			return;
		}
		HashMap<String, Field> langFieldsToConsume = Util.getLanguageFieldsToConsume(fieldMap, formInstance,
		    answersByLanguage);
		
		HashMap<String, HashMap<String, FormField>> formFieldsMap = new HashMap<String, HashMap<String, FormField>>();
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
					childFields = new HashMap<String, FormField>();
					formFieldsMap.put(parentName, childFields);
				}
				
				childFields.put(fieldName, currFormField);
			}
		}
		
		//parse the form configuration file
		if (scorableFormConfigAttrVal != null) {
			
			try {
				Scores scores = formConfig.getScores();
				
				//compute each score and save it to a concept in the database
				for (Score score : scores.getScores()) {
					
					//if we have an estimated score to substitute if a field is empty, compute that value here
					EstimatedScoreValue estimatedScoreValue = score.getEstimatedScoreValue();
					Double estimatedScore = null;
					if (estimatedScoreValue != null) {
						estimatedScore = computeValue(estimatedScoreValue.getValue(), childFields, langFieldsToConsume,
						    fieldMap, formFieldsMap,null);
					}
					
					Value value = score.getValue(); //value that should be saved to the concept
					Choose choose = value.getChoose();
					if (choose != null) {
						String result = processChoose(choose, childFields, langFieldsToConsume, fieldMap, formFieldsMap);
						saveScore(score.getConcept(), result, encounterId, patient);
					} else {
						Double scoreTotal = computeValue(value, childFields, langFieldsToConsume, fieldMap, formFieldsMap,
						    estimatedScore);
						
						if (scoreTotal != null) {
							saveScore(score.getConcept(), String.valueOf(scoreTotal), encounterId, patient);
						}
					}
				}
			}
			catch (Exception e) {
				log.error("Error scoring JIT for formInstance {}", formInstance, e);
			}
		}
		
	}
	
	private static String processChoose(Choose choose, HashMap<String, FormField> childFields,
	                                     HashMap<String, Field> langFieldsToConsume,
	                                     HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap,
	                                     HashMap<String, HashMap<String, FormField>> formFieldsMap) {
		boolean ifSatisfied = false;
		If ifObject = choose.getIf();
		Then thenObject = choose.getThen();
		
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
					Field matchingField = Util.pickFieldLanguage(fieldOperand, langFieldsToConsume, formFieldsMap);
					if (matchingField != null && fieldMap != null) {
						org.openmrs.module.atd.xmlBeans.Field scorableFormField = fieldMap
								.get(matchingField.getId());
						
						if (scorableFormField!=null&&
						scorableFormField.getValue() != null) {
							if (geq != null) {
								if (Integer.parseInt(scorableFormField.getValue()) >= Integer.parseInt(cnOperand)) {
									ifSatisfied = true;
								}
							}
							if (eq != null) {
								if (Integer.parseInt(scorableFormField.getValue()) == Integer.parseInt(cnOperand)) {
									ifSatisfied = true;
								}
							}
						}
					}
				}
			}
		}
		
		if (thenObject != null) {
			String result = thenObject.getResult();
			
			if (result != null && ifSatisfied) {
				return result;
			}
			
		}
		return null;
	}
	
	private static Double computeValue(Value value, HashMap<String, FormField> childFields,
	                                   HashMap<String, Field> langFieldsToConsume,
	                                   HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap,
	                                   HashMap<String, HashMap<String, FormField>> formFieldsMap,
	                                   Double estimatedScore) {
		Double scoreTotal = null;
		Plus plus = value.getPlus();
		Mean mean = value.getMean();
		
		//compute the sum
		if (plus != null) {
			List<Choose> choices = plus.getChooses();
			
			//process conditional logic
			if (choices != null) {
				
				for (Choose choose : choices) {
					
					String chooseResult = processChoose(choose, childFields, langFieldsToConsume,
					    fieldMap, formFieldsMap);
					if (chooseResult != null) {
						if (scoreTotal == null) {
							scoreTotal = 0D;
						}
						scoreTotal += Integer.parseInt(chooseResult);
					}
				}
			}
			
			List<Field> fields = plus.getFields();
			
			//sum the fields
			if (fields != null) {
				if (scoreTotal == null) {
					scoreTotal = 0D;
				}
				Double computeSumResult = computeSum(fields, childFields, langFieldsToConsume, 
					fieldMap, formFieldsMap, estimatedScore);
				if (computeSumResult != null) {
					scoreTotal += computeSumResult;
				}
			}
		}
		//compute the average
		if (mean != null && fieldMap != null) {
			scoreTotal = computeMean(mean, childFields, langFieldsToConsume, fieldMap, formFieldsMap,
				estimatedScore);
		}
		return scoreTotal;
	}
	
	private static Double computeMean(Mean mean, HashMap<String, FormField> childFields,
	                                  HashMap<String, Field> langFieldsToConsume,
	                                  HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap,
	                                  HashMap<String, HashMap<String, FormField>> formFieldsMap,
	                                  Double estimatedScore) {
		List<Field> fields = mean.getFields();
		Double computeSumResult = computeSum(fields, childFields, langFieldsToConsume, 
			fieldMap, formFieldsMap, estimatedScore);
		Double scoreTotal = null;
		
		if (computeSumResult != null) {
			
			scoreTotal = computeSumResult;
		}
		
		boolean excludeEmpty = false;
		String excludeEmptyString = mean.getExcludeEmpty();
		if (excludeEmptyString != null) {
			excludeEmptyString = excludeEmptyString.trim();
			if (excludeEmptyString.length() > 0) {
				try {
					excludeEmpty = Boolean.parseBoolean(excludeEmptyString);
				}
				catch (Exception e) {}
			}
		}
		
		Integer numFields = fields.size();
		if (excludeEmpty) {
			numFields = 0;
			
			for (Field field : fields) {
				Field matchingField = Util.pickFieldLanguage(field, langFieldsToConsume, formFieldsMap);
				org.openmrs.module.atd.xmlBeans.Field scorableFormField = fieldMap.get(matchingField.getId());
				if(scorableFormField != null){
					String fieldValue = scorableFormField.getValue();
					if(fieldValue != null&&fieldValue.trim().length()>0){
						numFields++;
					}
				}
			}
		}
		
		if (scoreTotal != null && numFields != 0) {
			return scoreTotal / numFields;
		}
		return scoreTotal;
	}
	
	private static Double computeSum(List<Field> fields, HashMap<String, FormField> childFields,
	                                 HashMap<String, Field> langFieldsToConsume,
	                                 HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap,
	                                 HashMap<String, HashMap<String, FormField>> formFieldsMap,
	                                 Double estimatedScore) {
		Double scoreTotal = null;
		
		for (Field currField : fields) {
			try {
				
				Field matchingField = Util.pickFieldLanguage(currField, langFieldsToConsume, formFieldsMap);
				org.openmrs.module.atd.xmlBeans.Field scorableFormField = fieldMap.get(matchingField.getId());
				
				if (scorableFormField.getValue() != null) {
					if (scoreTotal == null) {
						scoreTotal = 0D;
					}
					scoreTotal += Integer.parseInt(scorableFormField.getValue());
				}else{
					
					//if the field does not have a value and has an attribute value indicating that
					//an estimate of the field should be substituted, then use the value of estimatedScore
					//in the sum
					boolean substituteEstimate = false;
					String substituteEstimateString = currField.getSubstituteEstimate();
					if (substituteEstimateString != null) {
						substituteEstimateString = substituteEstimateString.trim();
						if (substituteEstimateString.length() > 0) {
							try {
								substituteEstimate = Boolean.parseBoolean(substituteEstimateString);
							}
							catch (Exception e) {}
						}
					}
					
					if(substituteEstimate&&estimatedScore != null){
						if (scoreTotal == null) {
							scoreTotal = 0D;
						}
						scoreTotal+=estimatedScore;
					}
				}
				
			}
			catch (Exception e) {}
		}
		return scoreTotal;
	}
	
	private static void saveScore(org.openmrs.module.atd.xmlBeans.Concept xmlConcept, String value, Integer encounterId,
	                              Patient patient) {
		
		if (value == null || value.length() == 0) {
			return;
		}
		
		String conceptName = xmlConcept.getName();
		
		ConceptService conceptService = Context.getConceptService();
		Concept concept = conceptService.getConcept(conceptName);
		if (concept != null) {
			ObsService obsService = Context.getObsService();
			EncounterService encounterService = Context.getEncounterService();
			org.openmrs.Encounter encounter = encounterService.getEncounter(encounterId);
			Obs obs = new Obs(patient, concept, new java.util.Date(), encounter.getLocation());
			String datatypeName = concept.getDatatype().getName();
			
			if (datatypeName.equalsIgnoreCase("Numeric")) {
				try {
					obs.setValueNumeric(Double.parseDouble(value));
				}
				catch (NumberFormatException e) {
					log.error("Could not save value: {} to the database for concept {}"
					        , value, concept.getName().getName());
				}
			} else if (datatypeName.equalsIgnoreCase("Coded")) {
				Concept answer = conceptService.getConceptByName(value);
				if (answer == null) {
					log.error("{} is not a valid concept name. {} will be stored as text.", value, value);
					obs.setValueText(value);
				} else {
					obs.setValueCoded(answer);
				}
			} else {
				obs.setValueText(value);
			}
			obs.setEncounter(encounter);
			obsService.saveObs(obs, "");
		} else {
			log.error("Concept {} does not exist to save score", conceptName);
		}
		
	}
}
