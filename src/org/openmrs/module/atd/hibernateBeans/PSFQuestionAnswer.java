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
package org.openmrs.module.atd.hibernateBeans;


/**
 * Bean to hold the question and answer for the PSF form.
 * 
 * @author Steve McKee
 */
public class PSFQuestionAnswer {

	private String question;
	private String answer;
	private Integer formInstanceId;
	private Integer formId;
	private Integer locationId;
	private Integer encounterId;
	
	/**
     * @return the question
     */
    public String getQuestion() {
    	return question;
    }
	
    /**
     * @param question the question to set
     */
    public void setQuestion(String question) {
    	this.question = question;
    }
	
    /**
     * @return the answer
     */
    public String getAnswer() {
    	return answer;
    }
	
    /**
     * @param answer the answer to set
     */
    public void setAnswer(String answer) {
    	this.answer = answer;
    }
	
    /**
     * @return the formInstanceId
     */
    public Integer getFormInstanceId() {
    	return formInstanceId;
    }
	
    /**
     * @param formInstanceId the formInstanceId to set
     */
    public void setFormInstanceId(Integer formInstanceId) {
    	this.formInstanceId = formInstanceId;
    }
	
    /**
     * @return the formId
     */
    public Integer getFormId() {
    	return formId;
    }
	
    /**
     * @param formId the formId to set
     */
    public void setFormId(Integer formId) {
    	this.formId = formId;
    }
	
    /**
     * @return the locationId
     */
    public Integer getLocationId() {
    	return locationId;
    }
	
    /**
     * @param locationId the locationId to set
     */
    public void setLocationId(Integer locationId) {
    	this.locationId = locationId;
    }
    
    /**
     * @return the encounterId
     */
    public Integer getEncounterId() {
    	return encounterId;
    }

    /**
     * @param encounterId the encounterId to set
     */
    public void setEncounterId(Integer encounterId) {
    	this.encounterId = encounterId;
    }
}
