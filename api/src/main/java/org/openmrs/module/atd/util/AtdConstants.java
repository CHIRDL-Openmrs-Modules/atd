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

import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceTag;

/**
 * Constants class
 */
public class AtdConstants {
	
	/*
	 * Constants for cache
	 */
	public static final String CACHE_FORM_DRAFT = "formDraft";
	public static final Class<FormInstanceTag> CACHE_FORM_DRAFT_KEY_CLASS = FormInstanceTag.class;
    public static final Class<Records> CACHE_FORM_DRAFT_VALUE_CLASS = Records.class;
	/*
	 * 
	 */
    
    /** Form views */
	public static final String FORM_VIEW_CONFIG_MANAGER_SUCCESS = "configurationManagerSuccess.form";
	public static final String FORM_VIEW_CONFIG_MANAGER = "configurationManager.form";
	public static final String FORM_CONFIG_FORM_VIEW = "/module/atd/configForm";
	public static final String FORM_VIEW_CONFIG_SUCCESS = "configFormAttributeValue.form";
	public static final String FORM_VIEW_CREATE_FORM_MLM_SUCCESS = "mlmForm.form";
	public static final String FORM_ENABLE_LOCATIONS_FORM_VIEW = "/module/atd/enableLocationsForm";
	public static final String FORM_ENABLE_FORM_VIEW = "/module/atd/enableForm";
}
