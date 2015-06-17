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

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;
import org.openmrs.module.atd.hibernateBeans.PatientATD;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.LanguageAnswers;
import org.openmrs.module.atd.xmlBeans.Language;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

/**
 * 
 * @author wang417
 * A util tool to help proceed format, I/O, showing views of data, and form javabeans
 */
public class Util {
	
	private static Log log = LogFactory.getLog(Util.class);
	protected static final String ESCAPE_BACKSLASH = "&#092";
	
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

	/**
	 * read csv input stream and create FormAttributeValue objects according to it.
	 * @param input the inputStream including the data source
	 * @return a list of new created FormAttributeValueDescriptor objects.
	 * @throws Exception 
	 */
	public static List<FormAttributeValueDescriptor> getFormAttributeValueDescriptorFromCSV(InputStream input) throws Exception{
		List<FormAttributeValueDescriptor> favdList = null;
		try{
			InputStreamReader inStreamReader = new InputStreamReader(input);
			CSVReader reader = new CSVReader(inStreamReader, ',');
			HeaderColumnNameTranslateMappingStrategy<FormAttributeValueDescriptor> strat = new HeaderColumnNameTranslateMappingStrategy<FormAttributeValueDescriptor>();
			Map<String, String> map = new HashMap<String, String>();
			map.put("form_name", "formName");
			map.put("location_name", "locationName");
			map.put("location_tag_name", "locationTagName");
			map.put("attribute_name", "attributeName");
			map.put("attribute_value", "attributeValue");
			strat.setType(FormAttributeValueDescriptor.class);
			strat.setColumnMapping(map);
			CsvToBean<FormAttributeValueDescriptor> csv = new CsvToBean<FormAttributeValueDescriptor>();
			favdList = csv.parse(strat, reader);
		}
		catch(Exception e){
			log.error(e);
			e.printStackTrace();
			throw e;
		}
		return favdList;
	}
	
	/**
	 * Create FormAttributeValue object from a FormAttributeValueDescriptor object
	 * @param favdList data source
	 * @return a list of new created FormAttributeValue objects
	 */
	public static List<FormAttributeValue> getFormAttributeValues(List<FormAttributeValueDescriptor> favdList) {
		List<FormAttributeValue> favList = new ArrayList<FormAttributeValue>();
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		FormService fs = Context.getFormService();
		LocationService locationService = Context.getLocationService();
		if (favdList != null) {
			for (FormAttributeValueDescriptor favd : favdList) {
				/*
				 * create FormAttribbuteValue object by
				 * FormAttributeVlaueDescriptor
				 */
				FormAttributeValue fav = new FormAttributeValue();
				Form form = fs.getForm(favd.getFormName());
				if(form==null) continue;
				Integer formId = form.getId();
				Location loc = locationService.getLocation(favd.getLocationName());
				if(loc==null) continue;
				Integer locationId = loc.getId();
				LocationTag locTag = locationService.getLocationTagByName(favd.getLocationTagName());
				if(locTag==null) continue;
				Integer locationTagId = locTag.getId();
				FormAttribute fv = cubService.getFormAttributeByName(favd.getAttributeName());
				if(fv==null) continue;
				Integer faId = fv.getFormAttributeId();
				fav.setFormId(formId);
				fav.setFormAttributeId(faId);
				fav.setLocationId(locationId);
				fav.setLocationTagId(locationTagId);
				fav.setValue(favd.getAttributeValue());
				favList.add(fav);
			}
		}

		return favList;
	}
	
	/**
	 * Create FormAttributeValueDescriptor object from a FormAttributeValue object
	 * @param fav data source
	 * @return the new created FormAttributeValueDescriptor object
	 */
	public static FormAttributeValueDescriptor getFormAttributeValue(FormAttributeValue fav){
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		FormService fs =Context.getFormService();
		LocationService locationService = Context.getLocationService();
		Form form = fs.getForm(fav.getFormId());
		if(form==null){
			return null;
		}
		String formName = form.getName();
		FormAttribute fa = cubService.getFormAttributeById(fav.getFormAttributeId());
		if(fa==null){
			return null;
		}
		String faName = fa.getName();
		Location location = locationService.getLocation(fav.getLocationId());
		if(location==null){
			return null;
		}
		String locationName = location.getName();
		LocationTag locationTag = locationService.getLocationTag(fav.getLocationTagId());
		if(locationTag==null){
			return null;
		}
		String locationTagName = locationTag.getName();
		FormAttributeValueDescriptor favd = new FormAttributeValueDescriptor(formName, locationName, locationTagName, faName, fav.getValue());
		return favd;
	}
	
	/**
	 * export all form attribute value info in system outside as csv file format
	 * @param writer a writer object containing outputstream destination
	 * @param favdList the data source
	 * @throws IOException 
	 */
	public static void exportFormAttributeValueAsCSV(Writer writer, List<FormAttributeValueDescriptor> favdList) throws IOException{
		CSVWriter csvWriter=null;
		try {
			csvWriter = new CSVWriter(writer);
			String[] columnNames = new String[5];
			columnNames[0]="form_name";
			columnNames[1]="location_name";
			columnNames[2]="location_tag_name";
			columnNames[3]="attribute_name";
			columnNames[4]="attribute_value";
			csvWriter.writeNext(columnNames);
			for(FormAttributeValueDescriptor favd: favdList){
				String[] item = new String[5];
				item[0] = favd.getFormName();
				item[1] = favd.getLocationName();
				item[2] = favd.getLocationTagName();  
				item[3] = favd.getAttributeName();
				item[4] = favd.getAttributeValue();
				csvWriter.writeNext(item);
			}
			csvWriter.close();
			
		} catch (IOException e) {
			log.error(e);
			throw e;
		}

	}
	
	/**
	 * export all concept info in system outside as csv file format
	 * @param writer a writer object containing outputstream destination
	 * @param cdList the data source
	 * @throws IOException 
	 */
	public static void exportAllConceptsAsCSV(Writer writer, List<ConceptDescriptor> cdList) throws IOException{
		CSVWriter csvWriter=null;
		try {
			csvWriter = new CSVWriter(writer);
			String[] columnNames = new String[7];
			columnNames[0]="name";
			columnNames[1]="concept class";
			columnNames[2]="datatype";
			columnNames[3]="description";
			columnNames[4]="concept_id";
			columnNames[5]="units";
			columnNames[6]="parent concept";
			csvWriter.writeNext(columnNames);
			for(ConceptDescriptor cd: cdList){
				String[] item = new String[7];
				item[0] = cd.getName();
				item[1] = cd.getConceptClass();
				item[2] = cd.getDatatype();  
				
				// DWE CHICA-330 Replace special character that will cause the import to fail
				String description = cd.getDescription();
				item[3] = description.replace("\\", ESCAPE_BACKSLASH);
				
				item[4] = Integer.toString(cd.getConceptId());
				item[5] = cd.getUnits();
				item[6] = cd.getParentConcept();
				csvWriter.writeNext(item);
				csvWriter.flush();
			}
			csvWriter.close();
			
		} catch (IOException e) {
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * export all form definition in system outside as csv file format
	 * @param writer a writer object containing outputstream destination
	 * @param fddList the data source
	 * @throws IOException 
	 * 
	 */
	public static void exportAllFormDefinitionCSV(Writer writer, List<FormDefinitionDescriptor> fddList) throws IOException{
		try{
		CSVWriter csvWriter=null;
		csvWriter = new CSVWriter(writer);
		String[] columnNames = new String[8];
		
		// DWE CHICA-280 4/7/15 Changed the column names to match what the import is expecting
		columnNames[0]="form_name";
		columnNames[1]="form_description";
		columnNames[2]="field_name";
		columnNames[3]="field_type";
		columnNames[4]="concept_name";
		columnNames[5]="default_value";
		columnNames[6]="field_number";
		columnNames[7]="parent_field_name";
		csvWriter.writeNext(columnNames);
		for(FormDefinitionDescriptor fdd: fddList){
			String[] item = new String[8];
			item[0] = fdd.getFormName();
			item[1] = fdd.getFormDescription();
			item[2] = fdd.getFieldName();
			item[3] = fdd.getFieldType();
			item[4] = fdd.getConceptName();
			item[5] = fdd.getDefaultValue();
			item[6] = Integer.toString(fdd.getFieldNumber());
			item[7] = fdd.getParentFieldName();
			csvWriter.writeNext(item);
		}
		csvWriter.close();
		}catch(IOException e){
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * read the csv file and parse the information of it to a list of FormAttributeValue objects
	 * @param input an InputStream Object
	 * @return a list of FormAttributeValue objects
	 * @throws Exception 
	 */
	public static List<FormAttributeValue> getFormAttributeValuesFromCSV(InputStream input) throws Exception{
		List<FormAttributeValue> favList = new ArrayList<FormAttributeValue>();
		List<FormAttributeValueDescriptor> favdList = null;
		ChirdlUtilBackportsService cubService = Context.getService(ChirdlUtilBackportsService.class);
		FormService fs =Context.getFormService();
		LocationService locationService = Context.getLocationService();
		try {
			favdList = getFormAttributeValueDescriptorFromCSV(input);
			
			if (favdList != null) {
				for(FormAttributeValueDescriptor favd: favdList){
					/*create FormAttribbuteValue object by FormAttributeVlaueDescriptor*/
					FormAttributeValue fav = new FormAttributeValue();
					Integer formId = fs.getForm(favd.getFormName()).getId();
					Integer locationId = locationService.getLocation(favd.getLocationName()).getId();
					Integer locationTagId = locationService.getLocationTagByName(favd.getLocationTagName()).getId();
					Integer faId = cubService.getFormAttributeByName(favd.getAttributeName()).getFormAttributeId();
					fav.setFormId(formId);
					fav.setFormAttributeId(faId);
					fav.setLocationId(locationId);
					fav.setLocationTagId(locationTagId);
					fav.setValue(favd.getAttributeValue());
					favList.add(fav);
				}
			}
		}
		catch (Exception e) {
			log.error(e);
			throw e;
		}
		return favList;
	}
	
	/**
	 * DWE CHICA-430 This was missing in the atd module
	 * This has also been updated to match the voidObsForConcept() method that was 
	 * modified as part of CHICA-437 in the chica module
	 * @param concept
	 * @param encounterId
	 * @param formFieldId - the form field id or null if one is not used
	 */
	public static void voidObsForConcept(Concept concept,Integer encounterId, Integer formFieldId)
	{
		EncounterService encounterService = Context.getService(EncounterService.class);
		Encounter encounter = (Encounter) encounterService.getEncounter(encounterId);
		ObsService obsService = Context.getObsService();
		
		// DWE CHICA-437 Before voiding obs records, we need to check to see if there is an 
		// existing atd_statistics record. This is needed for cases where a form may have more
		// than one field that uses the same concept and the same rule. Checking the atd_statistics
		// table will allow us to determine if the obs record is for the specified field
		List<Obs> obs = new ArrayList<Obs>();
		if(formFieldId != null)
		{
			// Get a list of obs records that have a related atd_statistics record.
			// This will give us a list of obs records that can be voided below
			ATDService atdService = Context.getService(ATDService.class);
			obs = atdService.getObsWithStatistics(encounter.getEncounterId(), concept.getConceptId(), formFieldId, false);
		}
		else
		{
			// Use previously existing functionality for cases where we don't have a formFieldId
			// Examples of this can be seen in calculatePercentiles()
			List<org.openmrs.Encounter> encounters = new ArrayList<org.openmrs.Encounter>();
			encounters.add(encounter);
			List<Concept> questions = new ArrayList<Concept>();
			
			questions.add(concept);
			obs = obsService.getObservations(null, encounters, questions, null, null, null, null,
					null, null, null, null, false);
		}
		
		for(Obs currObs:obs){
			obsService.voidObs(currObs, "voided due to rescan");
		}
	}
}
