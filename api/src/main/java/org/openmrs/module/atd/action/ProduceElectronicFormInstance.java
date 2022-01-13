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
package org.openmrs.module.atd.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;


/**
 * Action class for producing electronic versions of forms.
 * 
 * @author Steve McKee
 */
public class ProduceElectronicFormInstance extends ProduceFormInstance {
	
	private static final Logger log = LoggerFactory.getLogger(ProduceElectronicFormInstance.class);

	/**
	 * @see org.openmrs.module.atd.action.ProduceFormInstance#produceForm(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance, org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState, org.openmrs.Patient, java.lang.String, org.openmrs.module.atd.service.ATDService)
	 */
	protected void produceForm(Integer formId, Integer locationId, Integer locationTagId, Integer encounterId, 
	                           Integer sessionId, FormInstance formInstance, PatientState patientState, Patient patient, 
	                           String formName, ATDService atdService, Boolean autoPrint) {
		// We don't won't a physical file on the file system.
		// Just save that the form is electronic.
		if (formInstance == null) {
			return;
		}
		
		ChirdlUtilBackportsService service = Context.getService(ChirdlUtilBackportsService.class);
		FormInstanceAttribute fia = service.getFormInstanceAttributeByName("medium");
		if (fia == null) {
			log.error("No form instance attribute with name 'medium' found.");
			return;
		}
		
		FormInstanceAttributeValue fiav = new FormInstanceAttributeValue();
		fiav.setFormId(formId);
		fiav.setFormInstanceAttributeId(fia.getFormInstanceAttributeId());
		fiav.setFormInstanceId(formInstance.getFormInstanceId());
		fiav.setLocationId(locationId);
		fiav.setValue("electronic");
		service.saveFormInstanceAttributeValue(fiav);
	}
}
