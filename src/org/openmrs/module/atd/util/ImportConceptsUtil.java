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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

/**
 * Utility class for importing concepts from a csv file
 * 
 * @author Tammy Dugan
 */
public class ImportConceptsUtil {
	
	private static Log log = LogFactory.getLog(ImportConceptsUtil.class);
	
	public static void createConceptsFromFile(InputStream inputStream) {
		int conceptsCreated = 0;
		List<ConceptDescriptor> conceptDescriptorsToLink = new ArrayList<ConceptDescriptor>();
		ConceptService conceptService = Context.getConceptService();
		
		try {
			
			List<ConceptDescriptor> conceptDescriptors = getConcepts(inputStream);
			
			for (ConceptDescriptor currTerm : conceptDescriptors) {
				Concept newConcept = null;
				
				if (currTerm.getDatatype().equalsIgnoreCase("Numeric")) {
					newConcept = new ConceptNumeric();
				} else {
					newConcept = new Concept();
				}
				ConceptName conceptName = new ConceptName();
				conceptName.setLocale(new Locale("en"));
				conceptName.setName(currTerm.getName().trim());
				conceptName.setDateCreated(new java.util.Date());
				
				newConcept.addName(conceptName);
				if (newConcept instanceof ConceptNumeric) {
					((ConceptNumeric) newConcept).setUnits(currTerm.getUnits());
				}
				
				ConceptClass conceptClass = conceptService.getConceptClassByName(currTerm.getConceptClass());
				
				newConcept.setConceptClass(conceptClass);
				String datatype = currTerm.getDatatype();
				
				ConceptDatatype conceptDatatype = conceptService.getConceptDatatypeByName(datatype);
				newConcept.setDatatype(conceptDatatype);
				newConcept.setDateCreated(new java.util.Date());
				ConceptDescription conceptDescription = new ConceptDescription();
				conceptDescription.setDescription(currTerm.getDescription().replace(Util.ESCAPE_BACKSLASH, "\\")); // DWE CHICA-330 Replace special character that was escaped during the export
				conceptDescription.setLocale(new Locale("en"));
				newConcept.addDescription(conceptDescription);
				conceptDescriptorsToLink.add(currTerm);
				Concept concept = conceptService.getConceptByName(conceptName.getName());
				
				if (concept != null) {
					currTerm.setConceptId(concept.getConceptId());
					log.error("Could not create concept: " + conceptName.getName() + ". It already exists.");
					
					//update the description, units, or class if they have changed
					String oldDescription = null;
					if(concept.getDescription() != null){
						oldDescription = concept.getDescription().getDescription();
					}
					String oldUnits = null;
					if (concept instanceof ConceptNumeric) {
						oldUnits = ((ConceptNumeric) concept).getUnits();
					}
					String oldConceptClass = concept.getConceptClass().getName();
					String newDescription = currTerm.getDescription();
					String newUnits = currTerm.getUnits();
					String newConceptClass = currTerm.getConceptClass();
					
					if (newDescription != null && newDescription.length() > 0 && !newDescription.equals(oldDescription)) {
						concept.addDescription(new ConceptDescription(newDescription, new Locale("en")));
						log.info("updated concept: " + conceptName.getName() + ". Changed description to "+newDescription);
					}
					if (newConceptClass != null && newConceptClass.length() > 0 && !newConceptClass.equalsIgnoreCase(oldConceptClass)) {
						ConceptClass conceptClassObject = conceptService.getConceptClassByName(newConceptClass);
						concept.setConceptClass(conceptClassObject);
						log.info("updated concept: " + conceptName.getName() + ". Changed concept class to "+newConceptClass);

					}
					if (concept instanceof ConceptNumeric&&newUnits != null && newUnits.length() > 0 && !newUnits.equals(oldUnits)) {
						((ConceptNumeric) concept).setUnits(newUnits); 
						log.info("updated concept: " + conceptName.getName() + ". Changed units to "+newUnits);
					}
					
					conceptService.saveConcept(concept);
					continue;
				}else{			
					conceptService.saveConcept(newConcept);
					currTerm.setConceptId(newConcept.getConceptId());
					conceptsCreated++;
				}
				
				if (conceptsCreated % 500 == 0) {
					log.info("Context.clear session");
					Context.clearSession();
				}
			}
		}
		catch (Exception e) {
			log.error("Error creating concepts from file", e);
		}
		int conceptAnswersCreated = 0;
		
		//link all the answers to the parent
		for (ConceptDescriptor currDescriptor : conceptDescriptorsToLink) {
			String parentName = currDescriptor.getParentConcept();
			
			if (parentName != null) {
				parentName = parentName.trim();
				Concept parentConcept = conceptService.getConceptByName(parentName);
				if (parentConcept != null) {
					UserContext context = Context.getUserContext();
					Concept childConcept = conceptService.getConceptByName(currDescriptor.getName());
					boolean foundMatch = false;
					Collection<ConceptAnswer> answers = parentConcept.getAnswers();
					for (ConceptAnswer answer : answers) {
						if (answer.getAnswerConcept().getName().getName().equalsIgnoreCase(childConcept.getName().getName())) {
							foundMatch = true;
							break;//don't create a duplicate answer
						}
					}
					
					if (!foundMatch) {
						ConceptAnswer conceptAnswer = new ConceptAnswer();
						conceptAnswer.setAnswerConcept(childConcept);
						conceptAnswer.setConcept(parentConcept);
						conceptAnswer.setCreator(context.getAuthenticatedUser());
						conceptAnswer.setUuid(UUID.randomUUID().toString());
						conceptAnswer.setDateCreated(new java.util.Date());
						
						parentConcept.addAnswer(conceptAnswer);
						conceptService.saveConcept(parentConcept);
					}
					
					conceptAnswersCreated++;
					
					if (conceptAnswersCreated % 500 == 0) {
						log.info("Context.clear session");
						Context.clearSession();
					}
				}
			}
		}
		
		log.info("Number concepts created: " + conceptsCreated);
		
	}
	
	/**
	 * Get the list of appointments for the next business day.
	 * 
	 * @return List of Appointment objects
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static List<ConceptDescriptor> getConcepts(InputStream inputStream) throws FileNotFoundException, IOException {
		
		List<ConceptDescriptor> list = null;
		try {
			InputStreamReader inStreamReader = new InputStreamReader(inputStream);
			CSVReader reader = new CSVReader(inStreamReader, ',');
			HeaderColumnNameTranslateMappingStrategy<ConceptDescriptor> strat = new HeaderColumnNameTranslateMappingStrategy<ConceptDescriptor>();
			
			Map<String, String> map = new HashMap<String, String>();
			
			map.put("name", "name");
			map.put("concept class", "conceptClass");
			map.put("datatype", "datatype");
			map.put("description", "description");
			map.put("parent concept", "parentConcept");
			map.put("units", "units");
			
			strat.setType(ConceptDescriptor.class);
			strat.setColumnMapping(map);
			
			CsvToBean<ConceptDescriptor> csv = new CsvToBean<ConceptDescriptor>();
			list = csv.parse(strat, reader);
			
			if (list == null) {
				return new ArrayList<ConceptDescriptor>();
			}
		}
		catch (Exception e) {
			log.error("Error parsing concept file", e);
		}
		finally {
			inputStream.close();
		}
		return list;
	}
	
}
