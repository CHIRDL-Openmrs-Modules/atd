package org.openmrs.module.atd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
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
import org.openmrs.module.atd.datasource.FormDatasource;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.xmlBeans.Record;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.module.chirdlutil.util.XMLUtil;
import org.openmrs.module.chirdlutil.xmlBeans.serverconfig.ImageForm;
import org.openmrs.module.chirdlutil.xmlBeans.serverconfig.ImageMerge;
import org.openmrs.module.chirdlutil.xmlBeans.serverconfig.ServerConfig;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.dss.DssElement;
import org.openmrs.module.dss.DssManager;
import org.openmrs.module.dss.hibernateBeans.Rule;
import org.openmrs.module.dss.hibernateBeans.RuleEntry;
import org.openmrs.module.dss.service.DssService;
import org.openmrs.util.OpenmrsUtil;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


/**
 * Converts teleform xml to openmrs forms and
 * serializes openmrs forms to teleform xml
 * 
 * @author Tammy Dugan
 */
public class TeleformTranslator
{

	private static final String RESULT_DELIM = "^^";
	private static final Logger log = LoggerFactory.getLogger(TeleformTranslator.class);
	
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
		try (InputStream transformInput = new FileInputStream(inputFilename);
	            OutputStream transformOutput = new FileOutputStream(outputFilename);
	            InputStream xslt = new FileInputStream(xsltFilename))
		{
			

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
	 * Takes an instance of a form and serializes as XML to an output stream.
	 * 
	 * @param formInstance The form instance to serialize.
	 * @param output The output stream where the XML will be written.
	 * @param patient The patient owner of the form.
	 * @param dssManager DssManager containing all the form field information to be written.
	 * @param encounterId The patient's encounter ID.
	 * @param baseParameters Map containing parameters.
	 * @param locationTagId The location tag ID.
	 * @param sessionId The session ID.
	 */
	public void formToTeleformXML(FormInstance formInstance, OutputStream output, Patient patient, DssManager dssManager,
	                              Integer encounterId, Map<String, Object> baseParameters, Integer locationTagId,
	                              Integer sessionId) {
		
		LinkedHashMap<String, String> fieldNameResult = getMergeFieldResults(formInstance, patient, dssManager, 
			encounterId, baseParameters, locationTagId, sessionId);
		String resultString = null;
		//create xml
		Record record = new Record();
		Records records = new Records(record);
		
		for (String fieldName : fieldNameResult.keySet()) {
			org.openmrs.module.atd.xmlBeans.Field currXMLField = null;
			currXMLField = new org.openmrs.module.atd.xmlBeans.Field(fieldName);
			record.addField(currXMLField);
			resultString = fieldNameResult.get(fieldName);
			currXMLField.setValue(resultString);
		}
		
		try {
			XMLUtil.serializeXML(records, output);
		}
		catch (Exception e) {
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
		finally {
			try {
				output.flush();
				output.close();
			}
			catch (IOException e) {
				log.error("Error generated", e);
			}
		}
	}
	
	/**
	 * Merges form field information to a PDF file.
	 * 
	 * @param templatePDF The location of the PDF template file.
	 * @param formInstance The instance of a form that will have its metadata written to the PDF file.
	 * @param output The output stream where the merged PDF file will be written.
	 * @param patient The patient owner of the form.
	 * @param dssManager DssManager containing all the information for the merge fields.
	 * @param encounterId The encounter ID.
	 * @param baseParameters Map containing parameters.
	 * @param locationTagId The location tag ID.
	 * @param sessionId The session ID.
	 */
	public void formToPDF(String templatePDF, FormInstance formInstance, OutputStream output,
	                      Patient patient, DssManager dssManager, Integer encounterId, Map<String, Object> baseParameters,
	                      Integer locationTagId, Integer sessionId) {
		try {
	        PdfReader reader = new PdfReader(templatePDF);
	        PdfStamper stamper = new PdfStamper(reader, output);
	        AcroFields form = stamper.getAcroFields();
	        LinkedHashMap<String, String> fieldNameResult = getMergeFieldResults(formInstance, patient, dssManager, 
	        	encounterId, baseParameters, locationTagId, sessionId);
	        String resultString = null;
	        
	        for (String fieldName : fieldNameResult.keySet()) {
	        	resultString = fieldNameResult.get(fieldName);			
	        	form.setField(fieldName, resultString);
	        }
	        
	        postProcessPDF(reader, formInstance, stamper, locationTagId);
	        
	        stamper.close();
	        reader.close();
        }
        catch (Exception e) {
	        log.error("Error generating PDF version of form: " + formInstance, e);
        }
	}
	
	/**
	 * Performs any post-processing on a PDF merge form.
	 * 
	 * @param reader The PdfReader object created from the template PDF file.
	 * @param formInstance FormInstance object containing the form instance information for the form being merged.
	 * @param stamper The PdfStamper used to place objects in the PDF file.
	 * @param locationTagId The location tag identifier.
	 */
	private void postProcessPDF(PdfReader reader, FormInstance formInstance, PdfStamper stamper, Integer locationTagId) {
		Integer formId = formInstance.getFormId();
		Form form = Context.getFormService().getForm(formId);
		mergePDFImages(form, formInstance, locationTagId, stamper);
	}
	
	/**
	 * Merges any needed images to the PDF form.
	 * 
	 * @param form The database form containing the information about the PDF form.
	 * @param formInstance The instance of the form to have data merged.
	 * @param locationTagId The locationTagId of the location tag requesting the merge.
	 * @param stamper The PDF stamper used to merge the images.
	 */
	private void mergePDFImages(Form form, FormInstance formInstance, Integer locationTagId, PdfStamper stamper) {
		try {
			// Merge any images need to the form.
			FormAttributeValue fav = Context.getService(ChirdlUtilBackportsService.class).getFormAttributeValue(
				form.getFormId(), ChirdlUtilConstants.FORM_ATTR_REQUIRES_PDF_IMAGE_MERGE, locationTagId, 
				formInstance.getLocationId());
			if (fav == null || fav.getValue() == null || 
					!ChirdlUtilConstants.FORM_ATTR_VAL_TRUE.equalsIgnoreCase(fav.getValue())) {
				return;
			}
			
			ServerConfig serverConfig = Util.getServerConfig();
			if (serverConfig == null) {
				log.warn("Server config file cannot be found.  No PDF image processing will take place.");
				return;
			}
			
			ImageForm imageForm = serverConfig.getPDFImageForm(form.getName());
			if (imageForm == null) {
				return;
			}
			
			AcroFields fields = stamper.getAcroFields();
			List<ImageMerge> imageMerges = imageForm.getImageMerges();
			if (imageMerges == null) {
				return;
			}
			
			for (ImageMerge imageMerge : imageMerges) {
				String fieldName = imageMerge.getFieldName();
				String filePath = fields.getField(fieldName);
				if (filePath == null || filePath.trim().length() == 0) {
					continue;
				}
				
				Image image = Image.getInstance(filePath);
				image.setRotationDegrees(imageMerge.getRotation());
				image.setAbsolutePosition(imageMerge.getPositionX(), imageMerge.getPositionY());
				PdfContentByte content = stamper.getUnderContent(imageMerge.getPageNumber());
				content.addImage(image);
			}
		} catch (Exception e) {
			log.error("Error post-processing PDF form: " + form.getName(), e);
		}
	}

	/**
	 * Merges results into the form fields.
	 * 
	 * @param formInstance FormInstance used to identify the form.
	 * @param patient Patient who goes with the form
	 * @param dssManager manages prioritized rule evaluation
	 * @param encounterId id for the encounter that goes with this form instance
	 * @param baseParameters An optional map with additional parameters for rule execution.
	 * @param locationTagId The location tag identifier.
	 * @param sessionId The session identifier.
	 * @return LinkedHashMap with the form field name as the key and a String as the value being the value for the form 
	 * field.
	 */
	public LinkedHashMap<String,String> getMergeFieldResults(FormInstance formInstance, Patient patient,
			DssManager dssManager, Integer encounterId,Map<String,Object> baseParameters, Integer locationTagId,
			Integer sessionId)
	{
		FormService formService = Context.getFormService();
		String threadName = Thread.currentThread().getName();
		long startTime = System.currentTimeMillis();
		Form form = formService.getForm(formInstance.getFormId());
		if(form == null) 
		{
			log.error("Could not convert database form "+formInstance.getFormId()+
					" to teleform merge xml. The form does not exist in the database.");

			return null;
		}
		String resultString = null;

		ATDService atdService = Context.getService(ATDService.class);
		
		String defaultValue = null;
		FieldType priorMergeType = this.getFieldType("Prioritized Merge Field");
		FieldType mergeType = this.getFieldType("Merge Field");
		
		Integer dssMergeCounter = null; //index of dss element (by type)
		String mode = "PRODUCE";

		LinkedHashMap<FormField,Object> fieldToResult = 
			new LinkedHashMap<FormField,Object>();
		
		EncounterService encounterService = Context.getEncounterService();
		Encounter encounter = encounterService.getEncounter(encounterId);
		Integer locationId = encounter.getLocation().getLocationId();
		LocationService locationService = Context.getLocationService();
		Location location = locationService.getLocation(locationId);
		String locationName = null;
		if(location != null){
			locationName = location.getName();
		}
		
		HashMap<String,Integer> dssMergeCounters = 
			new HashMap<String,Integer>();
		
		startTime = System.currentTimeMillis();
		List<FormField> formFields = form.getOrderedFormFields();//get all fields for the form
		System.out.println("formToTeleformOutputStream(" + threadName + "): get ordered form fields: "+
			(System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		//iterate through all the form fields
		long totalPopulateDssElementTime = 0;
		long totalEvaluateRule = 0;
		for(FormField currFormField:formFields)
		{
			//-----------start set rule parameters
			HashMap<String,Object> parameters = new HashMap<String,Object>();

			parameters.put("sessionId", sessionId);
			parameters.put("formInstance", formInstance);
			parameters.put("locationTagId", locationTagId);
			parameters.put("locationId",locationId);
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
			
			parameters.put("formFieldId", currFormField.getFormFieldId()); // DWE CHICA-437 Get the form field id here so that it can be used to determine if obs records should be voided when rules are evaluated
			
			//-----------end set rule parameters


			fieldToResult.put(currFormField, null);//initially map field with no result
			
			Field currField = currFormField.getField();
			
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
					String elementString = ((ConceptName) concept.getNames().toArray()[0]).getName();
					parameters.put("concept", elementString);
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
				
				//We need to set the max number of prioritized elements to generate
				if (dssManager.getMaxDssElementsByType(ruleType) == 0) {
					ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
					LocationTagAttributeValue locTagAttrValue = chirdlutilbackportsService.getLocationTagAttributeValue(
					    locationTagId, ruleType, locationId);
					
					Integer formId = null;
					
					if (locTagAttrValue != null) {
						String value = locTagAttrValue.getValue();
						if (value != null) {
							try {
								formId = Integer.parseInt(value);
							}
							catch (Exception e) {}
						}
					}
					String propertyValue = org.openmrs.module.chirdlutilbackports.util.Util.getFormAttributeValue(formId, "numPrompts",
					    locationTagId, locationId);
					
					try {
						int maxDssElements = Integer.parseInt(propertyValue);
						dssManager.setMaxDssElementsByType(ruleType, maxDssElements);
					}
					catch (NumberFormatException e) {}
				}
				dssManager.populateDssElements(ruleType, false, parameters);
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
							patient,parameters);
					totalEvaluateRule+=System.currentTimeMillis()-startTime1;
					fieldToResult.put(currFormField, result);
				}
			}
		}
		
		System.out.println("formToTeleformOutputStream(" + threadName + "): total totalEvaluateRule: "+
			totalEvaluateRule);
		System.out.println("formToTeleformOutputStream(" + threadName + "): total totalPopulateDssElementTime: "+
			totalPopulateDssElementTime);

		System.out.println("formToTeleformOutputStream(" + threadName + "): time iterate through form fields: "+
			(System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		
		LinkedHashMap<String,String> fieldNameResult = new LinkedHashMap<String,String>();
		
		//process Results
		for(FormField currFormField:fieldToResult.keySet())
		{
			resultString = null;
			
			Field currField = currFormField.getField();
			String fieldName = currField.getName();
			Object fieldResult = fieldToResult.get(currFormField);
			
			if(fieldResult == null)
			{
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
		
			//look at results after the first elements to see if there
			//see if there are any value@field values that need to be parsed
			if (fieldResult instanceof Result) {
				
				Result results = (Result) fieldResult;
				
				if(results.size()>1){
									
					for(int i=1;i<results.size();i++){
						Result currResult = results.get(i);
						mapResult(currResult,fieldNameResult);
					}
					
				}	
			}
		}
		
		startTime = System.currentTimeMillis();
		
		//Process rules with null priority that have @ value in write action
		//These rules directly write results to a specific field
		DssService dssService = Context.getService(DssService.class);
		List<RuleEntry> nonPriorRuleEntries = dssService.getNonPrioritizedRuleEntries(form.getName());
		
		long totalRunRule = 0;
		for (RuleEntry currRuleEntry : nonPriorRuleEntries)
		{
			Rule currRule = currRuleEntry.getRule();
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("sessionId", sessionId);
			parameters.put("formInstance", formInstance);
			parameters.put("locationTagId", locationTagId);
			parameters.put("locationId", locationId);
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

			if (currRule.checkAgeRestrictions(patient))
			{
				currRule.setParameters(parameters);
				long startTime1 = System.currentTimeMillis();
				Result result = dssService.runRule(patient, currRule);
				totalRunRule+=(System.currentTimeMillis()-startTime1);
				for (Result currResult : result)
				{
					mapResult(currResult,fieldNameResult);
				}
			}
		}
		System.out.println("formToTeleformOutputStream(" + threadName + "): total run rule: "+totalRunRule);
		System.out.println("formToTeleformOutputStream(" + threadName + "): non prioritized fields: "+
		(System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		
		return fieldNameResult;
	}
	
	private void mapResult(Result result,
	                       LinkedHashMap<String, String> fieldNameResult){
		String resultString = getResultString(result);
		StringTokenizer tokenizer = new StringTokenizer(resultString, RESULT_DELIM);
		while (tokenizer.hasMoreTokens()) {
			String currResult = tokenizer.nextToken();

			int atIndex = currResult.indexOf("@");
			if (atIndex >= 0 && atIndex + 1 < currResult.length()) {
				String fieldName = currResult.substring(atIndex + 1, currResult.length()).trim();
				currResult = currResult.substring(0, atIndex).trim();
				if(currResult.length()>0){
					fieldNameResult.put(fieldName, currResult);
				}
			}
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
		FormDatasource formDatasource = 
			(FormDatasource) logicService
				.getLogicDataSource("form");
		
		Records records = formDatasource.parseTeleformXmlFormat(input);
		
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
			currField.setCreator(Context.getAuthenticatedUser());
			currField.setDateCreated(new Date());
			currField.setUuid(UUID.randomUUID().toString());
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
	 * Method to build a result string with "^^" as the delimiter.
	 * 
	 * @param result The Result object to transform to a String.
	 * @return String representing the result object provided.
	 */
	private String getResultString(Result result) {
		if (result == null) {
			return "";
		} else if (result.size() < 1) {
			return result.toString();
		}
		
		StringBuffer s = new StringBuffer();
		for (Result r : result) {
			if (s.length() > 0) {
				s.append(RESULT_DELIM);
			}
			
			s.append(getResultString(r));
		}
		
		return s.toString();
	}
}
