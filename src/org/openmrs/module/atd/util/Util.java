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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.LanguageAnswers;
import org.openmrs.module.atd.xmlBeans.Language;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 *
 */
public class Util {
	
	private static Log log = LogFactory.getLog(Util.class);
	
	public static int getMaxDssElements(Integer formId, Integer locationTagId, Integer locationId) {
		String propertyValue = null;
		int maxDssElements = 0;
		
		propertyValue = org.openmrs.module.chirdlutilbackports.util.Util.getFormAttributeValue(formId, "numPrompts",
		    locationTagId, locationId);
		
		try {
			maxDssElements = Integer.parseInt(propertyValue);
		}
		catch (NumberFormatException e) {}
		
		return maxDssElements;
	}
	
	/**
	 * 
	 * @param patient
	 * @param currConcept
	 * @param encounterId
	 * @param value
	 * @param formInstance
	 * @param ruleId
	 * @param locationTagId
	 * @param usePrintedTimestamp
	 * @param formFieldId - DWE CHICA-437 the form field id, pass null if the id is not available
	 * @return
	 */
	public static Obs saveObsWithStatistics(Patient patient, Concept currConcept, int encounterId, String value,
	                                         FormInstance formInstance, Integer ruleId, Integer locationTagId,
	                                         boolean usePrintedTimestamp, Integer formFieldId) {
		
		String formName = null;
		if (formInstance != null) {
			if (formInstance.getFormId() == null) {
				log.error("Could not find form for statistics update");
				return null;
			}
			
			FormService formService = Context.getFormService();
			Form form = formService.getForm(formInstance.getFormId());
			formName = form.getName();
		}
		
		ATDService atdService = Context.getService(ATDService.class);
		
		Date obsDate = new Date();
		
		if (formInstance != null) {
			
			Integer formInstanceId = formInstance.getFormInstanceId();
			Integer locationId = formInstance.getLocationId();
			
			//set the observation time as the time the form is printed
			if (usePrintedTimestamp) {
				List<Statistics> stats = atdService.getStatByFormInstance(formInstanceId, formName, locationId);
				if (stats != null && stats.size() > 0) {
					obsDate = stats.get(0).getPrintedTimestamp();
				}else{
					//this happens if no prompts were printed on the form
					//There is no printed timestamp for the non-prioritized fields 
					//like physical exam, etc, that are consumed so the encounter date time is used
					//This situation often happens when the sick visit checkbox is checked
					EncounterService encounterService = Context.getEncounterService();
					Encounter encounter = encounterService.getEncounter(encounterId);
					if(encounter != null){
						obsDate = encounter.getEncounterDatetime();
					}
				}
			}
			
			if (obsDate == null) {
				obsDate = new Date();
			}
		}
		
		Obs obs = org.openmrs.module.chirdlutil.util.Util.saveObs(patient, currConcept, encounterId, value, obsDate);
		Integer obsId = null;
		
		if(obs != null){
			obsId = obs.getObsId();
		}
		
		if (formInstance != null) {
			
			Integer formInstanceId = formInstance.getFormInstanceId();
			Integer locationId = formInstance.getLocationId();
			if (ruleId != null) {
				List<Statistics> statistics = atdService.getStatByIdAndRule(formInstanceId, ruleId, formName, locationId);
				
				if (statistics != null && statistics.size() > 0) {
					Statistics stat = statistics.get(0);
					
					if (stat.getObsvId() == null) {
						stat.setObsvId(obsId);
						stat.setFormFieldId(formFieldId);
						atdService.updateStatistics(stat);
					} else {
						stat = new Statistics(stat);
						stat.setObsvId(obsId);
						stat.setFormFieldId(formFieldId);
						atdService.createStatistics(stat);
					}
				}
			} else {
				List<Statistics> statistics = atdService.getStatByFormInstance(formInstanceId, formName, locationId);
				Statistics stat = new Statistics();
				
				stat.setAgeAtVisit(org.openmrs.module.chirdlutil.util.Util.adjustAgeUnits(patient.getBirthdate(), null));
				stat.setEncounterId(encounterId);
				stat.setFormInstanceId(formInstanceId);
				stat.setLocationTagId(locationTagId);
				stat.setFormName(formName);
				stat.setObsvId(obsId);
				stat.setPatientId(patient.getPatientId());
				stat.setRuleId(ruleId);
				stat.setLocationId(locationId);
				stat.setFormFieldId(formFieldId);
				if (statistics != null && statistics.size() > 0) {
					Statistics oldStat = statistics.get(0);
					stat.setPrintedTimestamp(oldStat.getPrintedTimestamp());
					stat.setScannedTimestamp(oldStat.getScannedTimestamp());
				}else{
					EncounterService encounterService = Context.getEncounterService();
					Encounter encounter = encounterService.getEncounter(encounterId);
					if(encounter != null){
						stat.setPrintedTimestamp(encounter.getEncounterDatetime());
					}
					stat.setScannedTimestamp(new Date());
				}
				atdService.createStatistics(stat);
			}
		}
		
		return obs;
	}
	
	public static Field pickFieldLanguage(Field currField, HashMap<String, FormField> childFields,
	                                      HashMap<String, Field> langFieldsToConsume,
	                                      HashMap<String, HashMap<String, FormField>> formFieldsMap) {
		String fieldName = currField.getId();
		
		//field name in config file matches the preferred language
		//field name
		childFields = formFieldsMap.get(fieldName);
		Field matchingField = null;
		
		if (childFields != null) {
			
			//see which of the child fields is in the language list
			for (String currChildFieldName : childFields.keySet()) {
				
				matchingField = langFieldsToConsume.get(currChildFieldName);
				if (matchingField != null) {
					break;
				}
			}
		}
		if (matchingField == null) {
			matchingField = currField;
		}
		return matchingField;
	}
	
	public static HashMap<String, Field> getLanguageFieldsToConsume(
	                                                                HashMap<String, org.openmrs.module.atd.xmlBeans.Field> fieldMap,
	                                                                FormInstance formInstance,
	                                                                LanguageAnswers answersByLanguage) {
		TeleformTranslator translator = new TeleformTranslator();
		FormService formService = Context.getFormService();
		Integer formId = formInstance.getFormId();
		Form databaseForm = formService.getForm(formId);
		if (databaseForm == null) {
			log.error("Could not consume teleform export xml because form " + formId + " does not exist in the database");
			return null;
		}
		
		HashMap<String, HashMap<String, Field>> languageToFieldnames = new HashMap<String, HashMap<String, Field>>();
		
		populateFieldNameArrays(languageToFieldnames, answersByLanguage);
		
		HashMap<String, Integer> languageToNumAnswers = new HashMap<String, Integer>();
		
		for (FormField currField : databaseForm.getFormFields()) {
			FieldType currFieldType = currField.getField().getFieldType();
			// only process export fields
			if (currFieldType != null && currFieldType.equals(translator.getFieldType("Export Field"))) {
				String fieldName = currField.getField().getName();
				
				for (String currLanguage : languageToFieldnames.keySet()) {
					
					HashMap<String, Field> currLangMap = languageToFieldnames.get(currLanguage);
					
					if (currLangMap.get(currField.getField().getName()) != null) {
						String value = null;
						if (fieldMap.get(fieldName) != null) {
							value = fieldMap.get(fieldName).getValue();
						}
						if (value != null) {
							if (languageToNumAnswers.get(currLanguage) == null) {
								languageToNumAnswers.put(currLanguage, 0);
							}
							
							languageToNumAnswers.put(currLanguage, languageToNumAnswers.get(currLanguage) + 1);
						}
					}
					
				}
			}
			
		}
		
		int maxNumAnswers = -1;
		String maxLanguage = null;
		
		for (String language : languageToNumAnswers.keySet()) {
			Integer compareNum = languageToNumAnswers.get(language);
			
			if (compareNum != null && compareNum > maxNumAnswers) {
				maxNumAnswers = compareNum;
				maxLanguage = language;
			}
		}
		if (maxNumAnswers > 0) {
			return languageToFieldnames.get(maxLanguage);
		} else {
			return languageToFieldnames.get("English");
		}
	}
	
	public static void populateFieldNameArrays(HashMap<String, HashMap<String, Field>> languages, 
	                                           LanguageAnswers answersByLanguage) {
		
		
		if (answersByLanguage != null) {
			ArrayList<Language> xmlLanguages = answersByLanguage.getLanguages();
			
			for (Language currLanguage : xmlLanguages) {
				String languageName = currLanguage.getName();
				if (languageName != null) {
					HashMap<String, Field> currLanguageFields = new HashMap<String, Field>();
					languages.put(languageName, currLanguageFields);
					
					ArrayList<org.openmrs.module.atd.xmlBeans.Field> fields = currLanguage.getFields();
					for (org.openmrs.module.atd.xmlBeans.Field currField : fields) {
						if (currField.getId() != null) {
							currLanguageFields.put(currField.getId(), currField);
						}
					}
				}
			}
		}
	}
	
	public static PatientState getPrevProducePatientStateByAction (PatientState patientState, Integer sessionId) {
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		PatientState stateWithFormId = chirdlutilbackportsService.getPrevPatientStateByAction(sessionId, 
			patientState.getPatientStateId(),"PRODUCE FORM INSTANCE");
		if (stateWithFormId == null) {
			return chirdlutilbackportsService.getPrevPatientStateByAction(sessionId, 
				patientState.getPatientStateId(),"PRODUCE ELECTRONIC FORM INSTANCE");
		}
		
		return stateWithFormId;
	}
	
	public static PatientState getProducePatientStateByFormInstanceAction (FormInstance formInstance) {
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		PatientState patientState = chirdlutilbackportsService.getPatientStateByFormInstanceAction(formInstance, 
			"PRODUCE FORM INSTANCE");
		if (patientState == null) {
			return chirdlutilbackportsService.getPatientStateByFormInstanceAction(formInstance, 
				"PRODUCE ELECTRONIC FORM INSTANCE");
		}
		
		return patientState;
	}
	
	public static PatientState getProducePatientStateByEncounterFormAction(Integer encounterId, Integer formId) {
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		PatientState patientStateProduce = chirdlutilbackportsService.getPatientStateByEncounterFormAction(encounterId, 
			formId, "PRODUCE FORM INSTANCE");
		if (patientStateProduce == null) {
			return patientStateProduce = chirdlutilbackportsService.getPatientStateByEncounterFormAction(encounterId, 
				formId, "PRODUCE ELECTRONIC FORM INSTANCE");
		}
		
		return patientStateProduce;
	}
	
	/**
	 * Returns PatientATD objects based on the form instance and field Id information provided.
	 * 
	 * @param formInstance Form Instance object.
	 * @param fieldIds Field Ids to find.
	 * 
	 * @return Map of fieldId to PatientATD object.
	 */
	public static Map<Integer, PatientATD> getPatientATDs(FormInstance formInstance, List<Integer> fieldIds) {
		Map<Integer, PatientATD> patdMap = new HashMap<Integer, PatientATD>();
		List<PatientATD> patds = Context.getService(ATDService.class).getPatientATDs(formInstance, fieldIds);
		if (patds == null) {
			return patdMap;
		}
		
		for (PatientATD patd : patds) {
			patdMap.put(patd.getFieldId(), patd);
		}
		
		return patdMap;
	}
}
