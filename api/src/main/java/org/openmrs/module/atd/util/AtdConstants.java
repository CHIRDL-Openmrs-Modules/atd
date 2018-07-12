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
    
    /* 
     * Constants for form views 
     */
	public static final String FORM_VIEW_CONFIG_MANAGER_SUCCESS = "configurationManagerSuccess.form";
	public static final String FORM_VIEW_CONFIG_MANAGER = "configurationManager.form";
	public static final String FORM_CONFIG_FORM_VIEW = "/module/atd/configForm";
	public static final String FORM_VIEW_CONFIG_SUCCESS = "configFormAttributeValue.form";
	public static final String FORM_VIEW_CREATE_FORM_MLM_SUCCESS = "mlmForm.form";
	public static final String FORM_ENABLE_LOCATIONS_FORM_VIEW = "/module/atd/enableLocationsForm";
	public static final String FORM_ENABLE_FORM_VIEW = "/module/atd/enableForm";
	public static final String FORM_VIEW_FAXABLE = "/module/atd/faxableForm";
	public static final String FORM_VIEW_OPERATION_SUCCESS = "operationSuccess.form";
	public static final String FORM_VIEW_CHOOSE_LOCATION_FORM = "chooseLocation.form";
	public static final String FORM_VIEW_CONFIG_FORM_ATTRIBUTE_VALUE = "configFormAttributeValue.form";
	/*
	 * 
	 */
	
	/*
	 * Constants for global properties
	 */
	public static final String GLOBAL_PROP_INSTALLATION_DIRECTORY = "atd.installationDirectory";
	/*
	 * 
	 */
	
	/*
	 * Constants for common controller parameters
	 */
	public static final String PARAMETER_APPLICATION = "application";
	public static final String PARAMETER_ERROR = "error";
	public static final String PARAMETER_FORMS = "forms";
	public static final String PARAMETER_OPERATION_TYPE = "operationType";
	public static final String PARAMETER_LOCATIONS = "locations";
	public static final String PARAMETER_EXPORT_TO_CSV = "exportToCSV";
	public static final String PARAMETER_FORM_NAME_SELECT = "formNameSelect";
	public static final String PARAMETER_SELECTED_FORM_NAME = "selectedFormName";
	public static final String PARAMETER_FORM_TO_EDIT = "formToEdit";
	public static final String PARAMETER_LOCATION_TAGS_MAP = "locationTagsMap";
	public static final String PARAMETER_LOCATIONS_LIST = "locationsList";
	public static final String PARAMETER_SUCCESS_VIEW_NAME = "successViewName";
	public static final String PARAMETER_POSITIONS = "positions";
	public static final String PARAMETER_NO_LOCATIONS_CHECKED = "noLocationsChecked";
	public static final String PARAMETER_FAILED_CREATION = "failedCreation";
	public static final String PARAMETER_SPACES_IN_NAME = "spacesInName";
	public static final String PARAMETER_DUPLICATE_NAME = "duplicateName";
	public static final String PARAMETER_MISSING_NAME = "missingName";
	public static final String PARAMETER_PARENT_PREFIX = "parent_";
	public static final String PARAMETER_FIELD_NUMBER_PREFIX = "fieldNumber_";
	public static final String PARAMETER_DEFAULT_VALUE_PREFIX = "defaultValue_";
	public static final String PARAMETER_CONCEPT_PREFIX = "concept_";
	public static final String PARAMETER_FIELD_TYPE_PREFIX = "fieldType_";
	public static final String PARAMETER_NAME_PREFIX = "name_";
	public static final String PARAMETER_FIELD_TYPES = "fieldTypes";
	public static final String PARAMETER_FORM_FIELDS = "formFields";
	/*
	 * 
	 */
	
	/*
	 * Constants for error types
	 */
    public static final String ERROR_TYPE_SERVER = "serverError";
    /*
     * 
     */
    
    /*
     * Constants for HTTP attributes
     */
    public static final String ATTRIBUTE_SELECTED_FORM_ID = "selectedFormId";
    public static final String ATTRIBUTE_CHOOSE_FORM_OPTION_CONSTANT = "chooseFormOptionConstant";
    /*
     * 
     */
}
