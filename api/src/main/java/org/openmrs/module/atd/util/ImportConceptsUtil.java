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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
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
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.ConceptDescriptor;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

/**
 * Utility class for importing concepts from a csv file
 * 
 * @author Tammy Dugan
 * 
 * DWE CHICA-426 Implemented Runnable interface 
 * and added supporting code to allow the import to be run in a separate thread
 */
public class ImportConceptsUtil implements Runnable, Serializable{
	
	private static final String LOG_UPDATED_CONCEPT = "updated concept: ";
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ImportConceptsUtil.class);
	private transient InputStream inputStream;
	private boolean isImportComplete = false;
	private boolean importStarted = false;
	private boolean errorOccurred = false;
	private int conceptsCreated = 0;
	private int conceptAnswersCreated = 0;
	private int totalRowsFound = 0;
	private int currentRow = 0;
	private boolean isImportCancelled = false;
	
	/**
	 * Constructor for ImportConcepts
	 * @param inputStream
	 */
	public ImportConceptsUtil(InputStream inputStream)
	{
		this.inputStream = inputStream;
	}
	
	/**
	 * @return - true if the import has completed
	 */
	public boolean getIsImportComplete()
	{
		return isImportComplete;
	}
	
	/**
	 * @return - true if an error occurred that prevented the import from starting
	 */
	public boolean getErrorOccurred()
	{
		return errorOccurred;
	}
	
	/**
	 * @return the total number of rows found in the import file
	 */
	public int getTotalRowsFound()
	{
		return totalRowsFound;
	}
	
	/**
	 * @return the current row that is being processed, this number is incremented before any processing 
	 * and will be incremented regardless of any errors that occur. This is simply a value 
	 * to give the user an indication of how far along in the import file the thread is
	 */
	public int getCurrentRow()
	{
		return currentRow;
	}
	
	/**
	 * @return true if the user cancelled the import
	 */
	public boolean getIsImportCancelled()
	{
		return isImportCancelled;
	}
	
	/**
	 * @return true if the import has been started
	 */
	public boolean getImportStarted()
	{
		return importStarted;
	}
	
	/**
	 * Set to true to cancel the import
	 */
	public void setIsImportCancelled(boolean cancelImport)
	{
		this.isImportCancelled = cancelImport;
	}
	
	/**
	 * @return the number of concepts that were successfully created
	 */
	public int getConceptsCreated()
	{
		return conceptsCreated;
	}
	
	/**
	 * @return the number of concept answers that were successfully created
	 */
	public int getConceptAnswersCreated()
	{
		return conceptAnswersCreated;
	}
	
	public void run()
	{
		importStarted = true;
		createConceptsFromFile();
		isImportComplete = true;
	}
	
	/**
	 * Create or update concept records
	 * @param inputStream
	 * 
	 * DWE CHICA-426 Made this private and removed static
	 * Also updated with necessary code to open a new session, keep track of which row in the file is 
	 * currently being processed, track any errors that occur, 
	 * and allow the user to cancel the import 
	 */ 
	private void createConceptsFromFile() {
		try
		{
		List<ConceptDescriptor> conceptDescriptorsToLink = new ArrayList<>();
		ConceptService conceptService = Context.getConceptService();
		
		try {
		    
			List<ConceptDescriptor> conceptDescriptors = getConcepts(inputStream);
			
			totalRowsFound = conceptDescriptors.size();
			
			for (ConceptDescriptor currTerm : conceptDescriptors) {
				if(isImportCancelled)
				{
					break;
				}
				currentRow++;
				Concept newConcept = null;
				
				if (currTerm.getDatatype().equalsIgnoreCase(ChirdlUtilConstants.CONCEPT_DATATYPE_NUMERIC)) {
					newConcept = new ConceptNumeric();
				} else {
					newConcept = new Concept();
				}
				ConceptName conceptName = new ConceptName();
				conceptName.setLocale(Context.getLocale());
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
				conceptDescription.setLocale(Context.getLocale()); 
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
						log.info(LOG_UPDATED_CONCEPT + conceptName.getName() + ". Changed description to "+newDescription);
					}
					if (newConceptClass != null && newConceptClass.length() > 0 && !newConceptClass.equalsIgnoreCase(oldConceptClass)) {
						ConceptClass conceptClassObject = conceptService.getConceptClassByName(newConceptClass);
						concept.setConceptClass(conceptClassObject);
						log.info(LOG_UPDATED_CONCEPT + conceptName.getName() + ". Changed concept class to "+newConceptClass);

					}
					if (concept instanceof ConceptNumeric&&newUnits != null && newUnits.length() > 0 && !newUnits.equals(oldUnits)) {
						((ConceptNumeric) concept).setUnits(newUnits); 
						log.info(LOG_UPDATED_CONCEPT + conceptName.getName() + ". Changed units to "+newUnits);
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
			errorOccurred = true;
		}
		
		//link all the answers to the parent
		if(!isImportCancelled)
		{
			try
			{
				totalRowsFound =  conceptDescriptorsToLink.size();
				currentRow = 0;
				for (ConceptDescriptor currDescriptor : conceptDescriptorsToLink) {
					currentRow++;
					if(isImportCancelled)
					{
						break;
					}
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
			}
			catch(Exception e)
			{
				log.error("Error creating concept answers from file", e);
				errorOccurred = true;
			}
		} 
		log.info("Number concepts created: " + conceptsCreated);
		
		}
		catch(ContextAuthenticationException e)
		{
			errorOccurred = true;
			log.error("Error authenticating context.", e);
		}
		
	}
	
	/**
	 * Get the list of ConceptDescriptor objects from the import file
	 * 
	 * @param inputStream
	 * @return List of ConceptDescriptor objects
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	// DWE CHICA-426 Made public so that the progress bar can be initialized with the number of rows found
	public static List<ConceptDescriptor> getConcepts(InputStream inputStream) throws FileNotFoundException, IOException {
		
		List<ConceptDescriptor> list = null;
		try {
			InputStreamReader inStreamReader = new InputStreamReader(inputStream);
			CSVReader reader = new CSVReader(inStreamReader, ',');
			HeaderColumnNameTranslateMappingStrategy<ConceptDescriptor> strat = new HeaderColumnNameTranslateMappingStrategy<>();
			
			Map<String, String> map = new HashMap<>();
			
			map.put("name", "name");
			map.put("concept class", "conceptClass");
			map.put("datatype", "datatype");
			map.put("description", "description");
			map.put("parent concept", "parentConcept");
			map.put("units", "units");
			
			strat.setType(ConceptDescriptor.class);
			strat.setColumnMapping(map);
			
			CsvToBean<ConceptDescriptor> csv = new CsvToBean<>();
			list = csv.parse(strat, reader);
			
			if (list == null) {
				return new ArrayList<>();
			}
		}
		catch (Exception e) {
			log.error("Error parsing concept file", e);
			return new ArrayList<>();
		}
		finally {
			inputStream.close();
		}
		return list;
	}
	
}
