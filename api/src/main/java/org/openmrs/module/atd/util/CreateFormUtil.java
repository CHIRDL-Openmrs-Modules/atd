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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformTranslator;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

/**
 * Utility class for importing concepts from a csv file
 * 
 * @author Tammy Dugan
 */
public class CreateFormUtil {
	
	private static final Logger log = LoggerFactory.getLogger(CreateFormUtil.class);
	
	public static Collection<Form> createFormsFromCSVFile(InputStream inputStream) {
		HashMap<String, HashMap<String,FormDescriptor>> formNameFieldNameDescriptorMap = new HashMap<String, HashMap<String,FormDescriptor>> ();
		HashMap<String, HashMap<String,FormField>> formNameFieldNameFormFieldMap = new HashMap<String, HashMap<String,FormField>>();
		ConceptService conceptService = Context.getConceptService();
		TeleformTranslator translator = new TeleformTranslator();
		FormService formService = Context.getFormService();
		HashMap<String,Form> forms = new HashMap<String,Form>();
		
		try {
			
			List<FormDescriptor> formDescriptors = getFormInfo(inputStream);
			
			for (FormDescriptor fieldDescriptor : formDescriptors) {
				String formName = fieldDescriptor.getFormName();
				Form form = forms.get(formName);

				if (form == null) {
					form = new Form();
					form.setName(formName);
					form.setVersion("0.1");
					form.setCreator(Context.getAuthenticatedUser());
					form.setDateCreated(new java.util.Date());
					form.setUuid(UUID.randomUUID().toString());
					String formDescription = fieldDescriptor.getFormDescription();
					form.setDescription(formDescription);

					forms.put(formName, form);
				}
				
				String conceptName = fieldDescriptor.getConceptName();
				Concept concept = conceptService.getConceptByName(conceptName);
				String defaultValue = fieldDescriptor.getDefaultValue();
				String fieldName = fieldDescriptor.getFieldName();
				
				Field field = new Field();
				field.setConcept(concept);
				field.setDefaultValue(defaultValue);
				field.setName(fieldName);
				field.setCreator(Context.getAuthenticatedUser());
				FieldType fieldType = translator.getFieldType(fieldDescriptor.getFieldType());
				field.setFieldType(fieldType);
				field.setDateCreated(new java.util.Date());
				field.setUuid(UUID.randomUUID().toString());
				
				FormField formField = new FormField();
				Integer fieldNumber=null;
                try {
	                fieldNumber = Integer.parseInt(fieldDescriptor.getFieldNumber());
                }
                catch (Exception e) {
                }
				formField.setFieldNumber(fieldNumber);
				formField.setField(field);
				formField.setCreator(Context.getAuthenticatedUser());
				formField.setDateCreated(new java.util.Date());
				formField.setUuid(UUID.randomUUID().toString());
				
				form.addFormField(formField);
				
				HashMap<String,FormField> fieldNameFormFieldMap = formNameFieldNameFormFieldMap.get(formName);
				if(fieldNameFormFieldMap == null){
					fieldNameFormFieldMap = new HashMap<String,FormField>();
					formNameFieldNameFormFieldMap.put(formName, fieldNameFormFieldMap);
				}
				
				HashMap<String,FormDescriptor> fieldNameDescriptorMap = formNameFieldNameDescriptorMap.get(formName);
				if(fieldNameDescriptorMap == null){
					fieldNameDescriptorMap = new HashMap<String,FormDescriptor>();
					formNameFieldNameDescriptorMap.put(formName, fieldNameDescriptorMap);
				}
				
				fieldNameFormFieldMap.put(fieldName, formField);
				fieldNameDescriptorMap.put(fieldName, fieldDescriptor);
				
				formService.saveForm(form);
				
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		for(HashMap<String,FormDescriptor> fieldNameDescriptorMap:formNameFieldNameDescriptorMap.values()){
			
			//link all the answers to the parent
			for (FormDescriptor currDescriptor : fieldNameDescriptorMap.values()) {
				String parentName = currDescriptor.getParentFieldName();
				String childName = currDescriptor.getFieldName();
				String formName = currDescriptor.getFormName();
				
				if (parentName != null && parentName.length() > 0) {
					parentName = parentName.trim();
					
					HashMap<String,FormField> fieldNameFormFieldMap = formNameFieldNameFormFieldMap.get(formName);
					
					FormField parentField = fieldNameFormFieldMap.get(parentName);
					FormField childField = fieldNameFormFieldMap.get(childName);
					
					if (parentField != null) {
						
						childField.setParent(parentField);
						formService.saveFormField(childField);
					}
				}
			}
		}
		
		return forms.values();
	}
	
	public static Form createFormFromCSVFile(InputStream inputStream) throws IllegalArgumentException {
		Collection<Form> forms = createFormsFromCSVFile(inputStream);
		if (forms == null || forms.size() == 0) {
			log.error("There are no forms found in the CSV file");
			return null;
		} else if (forms.size() > 1) {
			log.error("There are multiple forms in the CSV file, and this is not allowed.");
			throw new IllegalArgumentException("There are multiple forms in the CSV file, and this is not allowed.");
		}
		
		return forms.iterator().next();
	}
	
	/**
	 * Get the list of appointments for the next business day.
	 * 
	 * @return List of Appointment objects
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<FormDescriptor> getFormInfo(InputStream inputStream) throws FileNotFoundException, IOException {
		
		List<FormDescriptor> list = null;
		try {
			InputStreamReader inStreamReader = new InputStreamReader(inputStream);
			CSVReader reader = new CSVReader(inStreamReader, ',');
			HeaderColumnNameTranslateMappingStrategy<FormDescriptor> strat = new HeaderColumnNameTranslateMappingStrategy<FormDescriptor>();
			
			Map<String, String> map = new HashMap<String, String>();
			
			map.put("form_name", "formName");
			map.put("form_description", "formDescription");
			map.put("field_name", "fieldName");
			map.put("field_type", "fieldType");
			map.put("concept_name", "conceptName");
			map.put("default_value", "defaultValue");
			map.put("field_number", "fieldNumber");
			map.put("parent_field_name", "parentFieldName");
			
			strat.setType(FormDescriptor.class);
			strat.setColumnMapping(map);
			
			CsvToBean<FormDescriptor> csv = new CsvToBean<FormDescriptor>();
			list = csv.parse(strat, reader);
			
			if (list == null) {
				return new ArrayList<FormDescriptor>();
			}
		}
		catch (Exception e) {
			
			log.error("Error parsing csv file", e);
		}
		finally {
			inputStream.close();
		}
		return list;
	}
	
}
