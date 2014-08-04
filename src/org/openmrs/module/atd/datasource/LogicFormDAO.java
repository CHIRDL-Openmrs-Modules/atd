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
package org.openmrs.module.atd.datasource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;

/**
 *
 */
public interface LogicFormDAO {

    public List<FormResult> getFormResults(Cohort who, LogicCriteria logicCriteria);

	public FormInstance setFormFields(HashMap<String, Field> fieldMap,FormInstance formInstance,Integer locationTagId);
	public FormInstance parseTeleformXmlFormat(InputStream input,FormInstance formInstance,Integer locationTagId);
    public Records parseTeleformXmlFormat(InputStream input);  
    public HashMap<String,Field> getFormFields(FormInstance formInstance);
	public void deleteForm(FormInstance formInstance);
	public void clearForms() ;
}
