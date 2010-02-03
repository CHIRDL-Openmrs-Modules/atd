package org.openmrs.module.atd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.datasource.TeleformExportXMLDatasource;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.xmlBeans.Record;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.dss.DssElement;
import org.openmrs.module.dss.DssManager;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.util.OpenmrsUtil;

/**
 * Converts teleform xml to openmrs forms and
 * serializes openmrs forms to teleform xml
 * 
 * @author Tammy Dugan
 */
public class TeleformTranslator
{

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Converts teleform template xml into an OpenMRS database form
	 * 
	 * @param formName name of the form to be created
	 * @param templateXMLFilename name of the teleform template xml file to
	 *        parse for the form definition
	 * @return Form resulting openmrs Form
	 */
	public Form templateXMLToDatabaseForm(String formName,
			String templateXMLFilename) 
	{
		AdministrationService adminService = Context.getAdministrationService();
		String appDataDirectory = OpenmrsUtil.getApplicationDataDirectory();
		String xsltFilename = 
			adminService.getGlobalProperty("atd.convertTeleformToMergeXMLFile");
		if(xsltFilename == null){
			this.log.error("No xsltFilename found to convert teleform design xml to teleform merge xml file"+
					" for form name: "+formName+". You must set global property atd.convertTeleformToMergeXMLFile.");
			return null;
		}
		
		String tempDir = appDataDirectory + "temporary files/";
		
		File file = new File(tempDir);
		if(!file.exists()){
			file.mkdir();
		}

		String mergeXMLFilename = tempDir + "tempMergeFile.xml";

		teleformTemplateXMLToMergeXML(templateXMLFilename, mergeXMLFilename,
				xsltFilename);
		Form newForm = teleformMergeXMLToForm(mergeXMLFilename,
				formName);

		formToDatabase(newForm);

		return newForm;
	}

	/**
	 * Transforms teleform template xml into teleform merge xml using an xslt
	 * transform
	 * 
	 * @param inputFilename teleform template xml file name
	 * @param outputFilename teleform merge xml file name
	 * @param xsltFilename xslt file name
	 */
	private void teleformTemplateXMLToMergeXML(String inputFilename,
			String outputFilename, String xsltFilename)
	{
		try
		{
			InputStream transformInput = new FileInputStream(inputFilename);
			OutputStream transformOutput = new FileOutputStream(outputFilename);
			InputStream xslt = new FileInputStream(xsltFilename);

			XMLUtil.transformXML(transformInput, transformOutput, xslt,null);
		} catch (Exception e)
		{
			this.log.error("Error transforming template xml to merge xml");
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
	}

	/**
	 * Looks up an openmrs FieldType by name
	 * 
	 * @param fieldTypeName name of the field type
	 * @return FieldType openmrs field type with the given name
	 */
	public FieldType getFieldType(String fieldTypeName)
	{
		FormService formService = Context.getFormService();
		List<FieldType> fieldTypes = formService.getAllFieldTypes();
		Iterator<FieldType> iter = fieldTypes.iterator();
		FieldType currFieldType = null;

		while (iter.hasNext())
		{
			currFieldType = iter.next();
			if (currFieldType.getName().equals(fieldTypeName))
			{
				return currFieldType;
			}
		}
		return null;
	}

	/**
	 * deserializes teleform merge xml into OpenMRS form
	 * 
	 * @param inputFile file containing teleform merge xml
	 * @param formName name of the form to create
	 * @return Form OpenMRS form
	 */
	public Form teleformMergeXMLToForm(String inputFile,String formName)
	{
		String formVersion = "0.1"; // always enter 0.1 for form version

		try
		{
			return teleformInputStreamToForm(new FileInputStream(inputFile),
					formName, formVersion);
		} catch (Exception e)
		{
			this.log.error("Merge xml for file: "+inputFile+
					" could not be converted to an openmrs form");
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
		return null;
	}

	/**
	 * Saves an OpenMRS form to the database
	 * 
	 * @param form OpenMRS form
	 * @return int unique id for the newly created form
	 */
	private int formToDatabase(Form form)
	{
		FormService formService = Context.getFormService();
		formService.saveForm(form);
		return form.getFormId();
	}

	/**
	 * Pulls an OpenMRS form from the database based on a formId
	 * 
	 * @param formId unique identifier for OpenMRS form definition in the
	 *        database
	 * @return Form OpenMRS form from the database matching the formId
	 */
	private Form databaseToForm(int formId)
	{
		FormService formService = Context.getFormService();

		return formService.getForm(formId);
	}

	/**
	 * Turns an OpenMRS form into teleform merge xml
	 * @param form openmrs Form
	 * @param output where to write teleform xml
	 * @param patient Patient who goes with the form
	 * @param formInstanceId unique id for the specific instance of the form
	 * @param dssManager manages prioritized rule evaluation
	 * @param encounterId id for the encounter that goes with this form instance
	 */
	public void formToTeleformOutputStream(FormInstance formInstance, OutputStream output,
			Patient patient,DssManager dssManager,
			Integer encounterId,Map<String,Object> baseParameters,
			String rulePackagePrefix,
			Integer locationTagId,Integer sessionId)
	{
		long totalTime = System.currentTimeMillis();
		long startTime = System.currentTimeMillis();
		Form form = databaseToForm(formInstance.getFormId());
		if(form == null) 
		{
			log.error("Could not convert database form "+formInstance.getFormId()+
					" to teleform merge xml. The form does not exist in the database.");

			return;
		}
		String resultString = null;

		AdministrationService adminService = Context.getAdministrationService();
		ATDService atdService = Context.getService(ATDService.class);
		
		String defaultPackagePrefix = Util.formatPackagePrefix(
				adminService.getGlobalProperty("atd.defaultPackagePrefix"));
		String defaultValue = null;
		FieldType priorMergeType = this.getFieldType("Prioritized Merge Field");
		FieldType mergeType = this.getFieldType("Merge Field");
		
		Integer dssMergeCounter = null; //index of dss element (by type)
		String mode = "PRODUCE";

		LinkedHashMap<FormField,Object> fieldToResult = 
			new LinkedHashMap<FormField,Object>();
		
		//-----------start set rule parameters
		HashMap<String,Object> parameters = new HashMap<String,Object>();

		parameters.put("sessionId", sessionId);
		parameters.put("formInstance", formInstance);
		parameters.put("locationTagId", locationTagId);
		EncounterService encounterService = Context.getEncounterService();
		Encounter encounter = encounterService.getEncounter(encounterId);
		Integer locationId = encounter.getLocation().getLocationId();
		parameters.put("locationId",locationId);
		LocationService locationService = Context.getLocationService();
		Location location = locationService.getLocation(locationId);
		String locationName = null;
		if(location != null){
			locationName = location.getName();
		}
		parameters.put("location", locationName);
		if(encounterId != null)
		{
			parameters.put("encounterId", encounterId);
		}
		
		parameters.put("mode", mode);
		
		if(baseParameters != null)
		{
			parameters.putAll(baseParameters);
		}
		//-----------end set rule parameters
		
		HashMap<String,Integer> dssMergeCounters = 
			new HashMap<String,Integer>();
		
		List<FormField> formFields = form.getOrderedFormFields();//get all fields for the form
		
		FormService formService = Context.getFormService();
		startTime = System.currentTimeMillis();
		//iterate through all the form fields
		long totalPopulateDssElementTime = 0;
		long totalEvaluateRule = 0;
		for(FormField currFormField:formFields)
		{
			fieldToResult.put(currFormField, null);//initially map field with no result
			
			Field currField = currFormField.getField();

			//fix lazy initialization error
			currField = formService.getField(currField.getFieldId());
			
			defaultValue = currField.getDefaultValue();
			if(defaultValue == null)
			{
				continue;
			}
		
			FieldType currFieldType = currField.getFieldType();
			
			if(currFieldType == null)
			{
				fieldToResult.put(currFormField, defaultValue);
				continue;
			}
			
			//---------start set concept rule parameter
			Concept concept = currField.getConcept();
			if (concept != null)
			{
				try
				{
					parameters.put("concept", concept.getName().getName());
				} catch (Exception e)
				{
					parameters.put("concept", null);
				}
			}else
			{
				parameters.put("concept",null);
			}
			//---------end set concept rule parameter
			
			//process prioritized merge type fields
			if(currFieldType.equals(priorMergeType))
			{
				String ruleType = defaultValue;
				dssMergeCounter = dssMergeCounters.get(ruleType);
				if(dssMergeCounter == null)
				{
					dssMergeCounter = 0;
				}
				
				//fill in the dss elements for this type
				//(this will only get executed once even though it is called for each field)
				long startTime1 = System.currentTimeMillis();
				dssManager.populateDssElements(
						ruleType, false,parameters,
						defaultPackagePrefix);
				totalPopulateDssElementTime+=System.currentTimeMillis()-startTime1;
				//get the result for this field
				Result result = processDssElements(dssManager, dssMergeCounter,
						currField.getFieldId(), 
						ruleType);
				
				dssMergeCounter++;
				dssMergeCounters.put(ruleType, dssMergeCounter);
				fieldToResult.put(currFormField, result);
				continue;
			}
			
			//process merge type fields
			if(currFieldType.equals(mergeType))
			{
				//store the leaf index as the result if the
				//current field has a parent
				if(currFormField.getParent() != null)
				{
					fieldToResult.put(currFormField, defaultValue);
				} else
				{
					//produce other rule based fields
					long startTime1 = System.currentTimeMillis();
					Result result = atdService.evaluateRule(defaultValue,
							patient,parameters,rulePackagePrefix);
					totalEvaluateRule+=System.currentTimeMillis()-startTime1;
					fieldToResult.put(currFormField, result);
				}
			}
		}
		
		System.out.println("formToTeleformOutputStream: total totalEvaluateRule: "+
			totalEvaluateRule);
		System.out.println("formToTeleformOutputStream: total totalPopulateDssElementTime: "+
			totalPopulateDssElementTime);

		System.out.println("formToTeleformOutputStream: time iterate through form fields: "+
			(System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		
		LinkedHashMap<String,String> fieldNameResult = new LinkedHashMap<String,String>();
		
		//process Results
		for(FormField currFormField:fieldToResult.keySet())
		{
			resultString = null;
			
			//fix lazy initialization error
			Field currField = formService.getField(currFormField.getField().getFieldId());
			String fieldName = currField.getName();
			Object fieldResult = fieldToResult.get(currFormField);
			
			if(fieldResult == null)
			{
				fieldNameResult.put(fieldName, null);
				continue;
			}
			
			if(fieldResult instanceof Result)
			{
				resultString = ((Result) fieldResult).get(0).toString();
			}else
			{
				//if the field has a parent, process the result as a leaf index
				//of the parent results
				if(currFormField.getParent() == null)
				{
					resultString = (String) fieldResult;
				}else
				{
					try
					{
						int leafPos = Integer.parseInt((String) fieldResult);
						FormField parentField = currFormField.getParent();
						Result parentResult = (Result) fieldToResult.get(parentField);

						if(parentResult != null)
						{
							resultString = parentResult.get(leafPos).toString();
						}
					} catch (NumberFormatException e)
					{
					}
				}
			}
			if (resultString != null)
			{
				//remove everything at or after the @ symbol (i.e. @Spanish)
				int atIndex = resultString.indexOf("@");
				if (atIndex >= 0)
				{
					resultString = resultString.substring(0, atIndex);
				}
			}
			fieldNameResult.put(fieldName, resultString);
		}
		
		startTime = System.currentTimeMillis();
		
		//Process rules with null priority that have @ value in write action
		//These rules directly write results to a specific field
		DssService dssService = Context.getService(DssService.class);
		List<Rule> nonPriorRules = dssService.getNonPrioritizedRules(form.getName());
		
		parameters.put("concept", null);
		long totalRunRule = 0;
		for (Rule currRule : nonPriorRules)
		{
			if (currRule.checkAgeRestrictions(patient))
			{
				currRule.setParameters(parameters);
				long startTime1 = System.currentTimeMillis();
				Result result = dssService.runRule(patient, currRule,
						defaultPackagePrefix, rulePackagePrefix);
				totalRunRule+=(System.currentTimeMillis()-startTime1);
				for (Result currResult : result)
				{
					resultString = currResult.toString();
					int atIndex = resultString.indexOf("@");
					if (atIndex >= 0)
					{
						String fieldName = resultString.substring(atIndex + 1,
								resultString.length()).trim();
						resultString = resultString.substring(0, atIndex)
								.trim();
						fieldNameResult.put(fieldName, resultString);
					}
				}
			}
		}
		System.out.println("formToTeleformOutputStream: total run rule: "+totalRunRule);
		System.out.println("formToTeleformOutputStream: non prioritized fields: "+
		(System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		
		//create xml
		Record record = new Record();
		Records records = new Records(record);
		
		for(String fieldName:fieldNameResult.keySet()){
			org.openmrs.module.atd.xmlBeans.Field currXMLField = null;
			currXMLField = new org.openmrs.module.atd.xmlBeans.Field(fieldName);
			record.addField(currXMLField);
			resultString = fieldNameResult.get(fieldName);
			currXMLField.setValue(resultString);
		}
		
		try
		{
			XMLUtil.serializeXML(records, output);
			output.flush();
			output.close();
			
		} catch (Exception e)
		{
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
	}
	
	private Result processDssElements(DssManager dssManager, int dssMergeCounter, 
			int fieldId,String type)
	{
		DssElement dssElement = dssManager.getDssElement(dssMergeCounter, 
				type);
		
		if(dssElement != null)
		{
			dssElement.addParameter("fieldId", fieldId);
			return dssElement.getResult();
		}
		return null;
	}
	
	/**
	 * Converts teleform merge xml to OpenMRS form object
	 * 
	 * @param input teleform merge xml
	 * @param formName name of form to create
	 * @param formVersion version of form to create
	 * @return Form OpenMRS form
	 */
	private Form teleformInputStreamToForm(InputStream input, String formName,
			String formVersion)
	{
		Form form = new Form(); // make a new empty form
		form.setVersion(formVersion);
		form.setName(formName);
		int length = 0;
		String currFieldName = null;
		Field currField = null;
		FormField currFormField = null;
		int order = 0;

		LogicService logicService = Context.getLogicService();
		TeleformExportXMLDatasource xmlDatasource = 
			(TeleformExportXMLDatasource) logicService
				.getLogicDataSource("xml");
		
		Records records = xmlDatasource.parse(input);
		
		ArrayList<org.openmrs.module.atd.xmlBeans.Field> fields = records
				.getRecord().getFields();

		length = fields.size();

		for (int i = 0; i < length; i++)
		{
			currFormField = new FormField();
			try
			{
				order = Integer.parseInt(fields.get(i).getTaborder());
			} catch (NumberFormatException e)
			{
				order = i;
				order++;
			}
			if(i == length - 1)
			{
				currFormField.setFieldNumber(order+1000);
			}else
			{
				currFormField.setFieldNumber(order);
			}
			currFieldName = fields.get(i).getId();
			currField = new Field();
			currField.setName(currFieldName);
			String type = fields.get(i).getType();
			if(type != null && type.equalsIgnoreCase("Export"))
			{
				currField.setFieldType(this.getFieldType("Export Field"));
			}
			currFormField.setField(currField);
			form.addFormField(currFormField);
		}
		form.setDescription(records.getTitle());
		return form;
	}
	
	/**
	 * Turns teleform xml into a table that is readable by
	 * teleforms merge utility
	 * @param inputXML teleform xml
	 * @param xsltFilename xslt file to convert xml to statements
	 * to create/populate database table
	 * @param formName name of the form
	 * @param formInstanceId unique id for the specific instance of the form
	 * @throws IOException
	 */
	public void teleformXMLToTable(String inputXML,
			String xsltFilename, String formName, Integer formInstanceId)
			throws IOException
	{
		ByteArrayOutputStream transformOutput = null;
		String tableName = "TFORM_" + formName;

		// Check if table exists
		ATDService atdService = Context
		.getService(ATDService.class);
		boolean tableExists = atdService.tableExists(tableName);
		
		//if the table doesn't exist, create it
		if (!tableExists)
		{
			transformOutput = new ByteArrayOutputStream();
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("table_name", tableName);
			parameters.put("form_instance_id", formInstanceId);
			parameters.put("create_table", "true");
			XMLUtil.transformXML(new ByteArrayInputStream(inputXML.toString().getBytes()),
					transformOutput, new FileInputStream(xsltFilename),
					parameters);
			atdService.executeSql(transformOutput.toString());
		}
		
		//insert the data rows
		transformOutput = new ByteArrayOutputStream();
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("table_name", tableName);
		parameters.put("form_instance_id", formInstanceId);
		parameters.put("create_insert", "true");
		XMLUtil.transformXML(new ByteArrayInputStream(inputXML.toString().getBytes()),
				transformOutput, new FileInputStream(xsltFilename),
				parameters);
		atdService.executeSql(transformOutput.toString());
	}
}
