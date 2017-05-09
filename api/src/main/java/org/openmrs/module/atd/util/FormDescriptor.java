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

import org.openmrs.Concept;

/**
 * Bean containing the information provided from the concept import file.
 * 
 * @author Tammy Dugan
 */
public class FormDescriptor {
	
	private String formName = null;
	private String formDescription = null;
	private String fieldName = null;
	private String fieldType = null;
	private String conceptName = null;
	private String defaultValue = null;
	private String fieldNumber = null;
	private String parentFieldName = null;
	
	/**
	 * Constructor Method
	 */
	public FormDescriptor() {
	}

	
    /**
     * @return the formName
     */
    public String getFormName() {
    	return this.formName;
    }

	
    /**
     * @param formName the formName to set
     */
    public void setFormName(String formName) {
    	this.formName = formName;
    }

	
    /**
     * @return the formDescription
     */
    public String getFormDescription() {
    	return this.formDescription;
    }

	
    /**
     * @param formDescription the formDescription to set
     */
    public void setFormDescription(String formDescription) {
    	this.formDescription = formDescription;
    }

	
    /**
     * @return the fieldName
     */
    public String getFieldName() {
    	return this.fieldName;
    }

	
    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
    	this.fieldName = fieldName;
    }

	
    /**
     * @return the fieldType
     */
    public String getFieldType() {
    	return this.fieldType;
    }

	
    /**
     * @param fieldType the fieldType to set
     */
    public void setFieldType(String fieldType) {
    	this.fieldType = fieldType;
    }

	
    /**
     * @return the conceptName
     */
    public String getConceptName() {
    	return this.conceptName;
    }

	
    /**
     * @param conceptName the conceptName to set
     */
    public void setConceptName(String conceptName) {
    	this.conceptName = conceptName;
    }

	
    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
    	return this.defaultValue;
    }

	
    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
    	this.defaultValue = defaultValue;
    }

	
    /**
     * @return the fieldNumber
     */
    public String getFieldNumber() {
    	return this.fieldNumber;
    }

	
    /**
     * @param fieldNumber the fieldNumber to set
     */
    public void setFieldNumber(String fieldNumber) {
    	
    	this.fieldNumber = fieldNumber;

    }

	
    /**
     * @return the parentFieldName
     */
    public String getParentFieldName() {
    	return this.parentFieldName;
    }

	
    /**
     * @param parentFieldName the parentFieldName to set
     */
    public void setParentFieldName(String parentFieldName) {
    	this.parentFieldName = parentFieldName;
    }
	
	
}
