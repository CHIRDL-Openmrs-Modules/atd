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
package org.openmrs.module.atd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Record;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutil.threadmgmt.ChirdlRunnable;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;


/**
 * Create a form XML if one does not exist in the scan directory and add the field
 * information to it. If fields already exist, the results will be overwritten by the new data
 * provided.
 * 
 * @author Steve McKee
 */
public class FormWriter implements ChirdlRunnable {
	
	private Log log = LogFactory.getLog(this.getClass());
	private FormInstance formInstance;
	private Integer locationTagId;
	private List<Field> fieldsToAdd;
	private List<String> fieldsToRemove; // DWE CHICA-430
	
	/**
	 * Constructor method.
	 * 
	 * @param formInstance FormInstance object containing form ID, location ID, and form instance ID.
	 * @param locationTagId The location tag ID.
	 * @param fieldsToAdd The fields to add to the scan XML file.
	 * @param fieldsToRemove - DWE CHICA-430 The fields to remove from the scan XML file.
	 */
	public FormWriter(FormInstance formInstance, Integer locationTagId, List<Field> fieldsToAdd, List<String> fieldsToRemove) {
		this.formInstance = formInstance;
		this.locationTagId = locationTagId;
		this.fieldsToAdd = fieldsToAdd;
		this.fieldsToRemove = fieldsToRemove;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		this.log.info("Started execution of " + getName() + "(" + Thread.currentThread().getName() + ", "
		        + new Timestamp(new Date().getTime()) + ")");
        try {
            Integer locationId = this.formInstance.getLocationId();
            Integer formId = this.formInstance.getFormId();
            String scanDirectory = IOUtil.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
                    .getFormAttributeValue(formId, "defaultExportDirectory", this.locationTagId, locationId));
            if (scanDirectory == null) {
                this.log.info("No defaultExportDirectory found for Form: " + formId + " Location ID: " + locationId
                        + " Location Tag ID: " + this.locationTagId + ".  No scan XML file will be created.");
                return;
            }

            File file = new File(scanDirectory, this.formInstance.toString() + ".20");
            Records records = null;
            if (file.exists()) {
                records = parseTeleformXmlFormat(file);
                records = addFields(records, this.fieldsToAdd, this.fieldsToRemove); // DWE CHICA-430 Added fieldsToRemove
            } else {
                Record record = new Record();
                records = new Records(record);
                for (Field field : this.fieldsToAdd) {
                    record.addField(field);
                }
            }

            serializeTeleformXmlFormat(records, file);
        } catch (ContextAuthenticationException e) {
            this.log.error("Error authenticating user", e);
        } finally {
            this.log.info("Finished execution of " + getName() + "(" + Thread.currentThread().getName() + ", "
                    + new Timestamp(new Date().getTime()) + ")");
        }
	}
	
	/**
	 * @see org.openmrs.module.chirdlutil.threadmgmt.ChirdlRunnable#getName()
	 */
	@Override
	public String getName() {
		return "Form Writer (Form: " + this.formInstance.getFormId() + " Location ID: " + this.formInstance.getLocationId() + 
				" Location Tag ID: " + this.locationTagId + ")";
	}
	
	/**
	 * @see org.openmrs.module.chirdlutil.threadmgmt.ChirdlRunnable#getPriority()
	 */
	@Override
	public int getPriority() {
		return ChirdlRunnable.PRIORITY_FIVE;
	}
	
	/**
	 * Adds new fields or updates existing fields for the Records object.
	 * 
	 * @param records The Records object that the new fields will added/updated.
	 * @param fields The fields to add or update.
	 * @param fieldsToRemove - DWE CHICA-430 The fields to remove
	 * @return A new Records object with the added/updated fields.
	 */
	private Records addFields(Records records, List<Field> fields, List<String> fieldsToRemove) {
		Map<String, Field> fieldIdToFieldMap = new HashMap<>();
		Record record = records.getRecord();
		if (record == null) {
			return records;
		}
		
		List<Field> currentFields = record.getFields();
		if (currentFields == null) {
			return records;
		}
		
		// Index the current fields.
		for (Field field : currentFields) {
			String id = field.getId();
			// DWE CHICA-430 Check to see if the field is in the remove list, 
			// don't add it to the map if it is in the remove list
			if(!fieldsToRemove.contains(id))
			{
				fieldIdToFieldMap.put(id, field);
			}
		}
		
		// Add the new fields.  Replace the values of existing fields as needed.
		for (Field field : fields) {
			String id = field.getId();
			Field currentField = fieldIdToFieldMap.get(id);
			if (currentField == null) {
				fieldIdToFieldMap.put(id, field);
			} else {
				currentField.setValue(field.getValue());
			}
		}
		
		// Create the new records object.
		Record newRecord = new Record();
		Records newRecords = new Records(newRecord);
		
		for (Field field : fieldIdToFieldMap.values()) {
			newRecord.addField(field);
		}
		
		return newRecords;
	}
	
	/**
	 * Takes the scan XML and de-serializes it to a Records object.
	 * 
	 * @param input The File to serialize.
	 * @return The Records object representation of the de-serialized XML.
	 */
	private Records parseTeleformXmlFormat(File input) {
		Records records = null;
		try {
			records = (Records) XMLUtil.deserializeXML(Records.class, new FileInputStream(input));
		}
		catch (IOException e) {
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
		return records;
	}
	
	/**
	 * Serializes a Records object to a File.
	 * 
	 * @param records The Records object to serialize to disk.
	 * @param output The File where the records will be serialized.
	 */
	private void serializeTeleformXmlFormat(Records records, File output) {
		try {
			XMLUtil.serializeXML(records, new FileOutputStream(output));
		}
		catch (IOException e) {
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
	}
	
}
