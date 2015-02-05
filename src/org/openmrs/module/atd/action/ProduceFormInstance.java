/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.print.PrintException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.hibernateBeans.Statistics;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.PrintServices;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Session;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 * @author tmdugan
 * 
 */
public class ProduceFormInstance implements ProcessStateAction
{
	private static Log log = LogFactory.getLog(ProduceFormInstance.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction,
	 *      org.openmrs.Patient,
	 *      org.openmrs.module.atd.hibernateBeans.PatientState,
	 *      java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState, HashMap<String, Object> parameters)
	{
		long totalTime = System.currentTimeMillis();
		//lookup the patient again to avoid lazy initialization errors
		PatientService patientService = Context.getPatientService();
		ATDService atdService = Context.getService(ATDService.class);
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);
		
		Integer locationTagId = patientState.getLocationTagId();
		Integer locationId = patientState.getLocationId();
		
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		State currState = patientState.getState();
		Integer sessionId = patientState.getSessionId();
		
		Session session = chirdlutilbackportsService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		String formName = null;
		String trigger = null;
		String autoPrintStr = null;
		if(parameters != null){
			formName = (String) parameters.get(ChirdlUtilConstants.PARAMETER_FORM_NAME);
			Object param2Object = parameters.get(ChirdlUtilConstants.PARAMETER_TRIGGER);
			if (param2Object != null && param2Object instanceof String){
				trigger = (String) param2Object;
			}
			
			Object param3Object = parameters.get(ChirdlUtilConstants.PARAMETER_AUTO_PRINT);
			if (param3Object != null && param3Object instanceof String){
				autoPrintStr = (String) param3Object;
			}
		}
		
		if(formName == null){
			formName = currState.getFormName();
		}
		LocationTagAttributeValue locTagAttrValue = 
			chirdlutilbackportsService.getLocationTagAttributeValue(locationTagId, formName, locationId);
		
		Integer formId = null;
		
		if(locTagAttrValue != null){
			String value = locTagAttrValue.getValue();
			if(value != null){
				try
				{
					formId = Integer.parseInt(value);
				} catch (Exception e)
				{
				}
			}
		}
		
		if(formId == null){
			//open an error state
			currState = chirdlutilbackportsService.getStateByName(ChirdlUtilConstants.STATE_ERROR_STATE);
			chirdlutilbackportsService.addPatientState(patient,
					currState, sessionId,locationTagId,locationId, null);
			log.error(formName+
					" locationTagAttribute does not exist for locationTagId: "+
					locationTagId+" locationId: "+locationId);
			return;
		}
		
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formId);
		
		// write the form
		FormInstance formInstance = chirdlutilbackportsService.addFormInstance(formId,
				locationId);
		
		if(parameters == null){
			parameters = new HashMap<String,Object>();
		}
		parameters.put(ChirdlUtilConstants.PARAMETER_FORM_INSTANCE, formInstance);
		patientState.setFormInstance(formInstance);
		chirdlutilbackportsService.updatePatientState(patientState);
		Integer formInstanceId = formInstance.getFormInstanceId();
		
		//If triggered by a force print, save as a form instance attibute value for later reference
		try {
			if (trigger != null && trigger.equalsIgnoreCase(ChirdlUtilConstants.FORM_INST_ATTR_VAL_FORCE_PRINT)){
				FormInstanceAttributeValue fiav = new FormInstanceAttributeValue();
				FormInstanceAttribute attr = chirdlutilbackportsService
					.getFormInstanceAttributeByName(ChirdlUtilConstants.FORM_INST_ATTR_TRIGGER);
				if (attr != null){
					System.out.println("Attribute is not null");
					fiav.setFormInstanceAttributeId(attr.getFormInstanceAttributeId());
					fiav.setFormId(formInstance.getFormId());
					fiav.setFormInstanceId(formInstance.getFormInstanceId());
					fiav.setLocationId(locationId);
					fiav.setValue(trigger);
					chirdlutilbackportsService.saveFormInstanceAttributeValue(fiav);
				}
			}
		}catch (Exception e){
			log.error("Error when saving the form attribute for trigger. ",e);
		}
		
		Boolean autoPrint = null;
		if (autoPrintStr != null && autoPrintStr.trim().length() > 0) {
			autoPrint = Boolean.valueOf(autoPrintStr);
		}
		
		produceForm(formId, locationId, locationTagId, encounterId, sessionId, formInstance, patientState, patient, 
			formName, atdService, autoPrint);

		StateManager.endState(patientState);
		System.out.println("Produce: Total time to produce "+form.getName()+"(" + Thread.currentThread().getName() + "): "+
			(System.currentTimeMillis()-totalTime));
		BaseStateActionHandler.changeState(patient, sessionId, currState, stateAction, parameters,
				locationTagId, locationId);
		// update statistics
		List<Statistics> statistics = atdService.getStatByFormInstance(
				formInstanceId, formName, locationId);

		for (Statistics currStat : statistics)
		{
			currStat.setPrintedTimestamp(patientState.getEndTime());
			atdService.updateStatistics(currStat);
		}		
	}

	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}

	protected void produceForm(Integer formId, Integer locationId, Integer locationTagId, Integer encounterId,
	                           Integer sessionId, FormInstance formInstance, PatientState patientState, Patient patient,
	                           String formName, ATDService atdService, Boolean autoPrint) {
		String mergeDirectory = IOUtil.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
		        .getFormAttributeValue(formId, ChirdlUtilConstants.FORM_ATTR_DEFAULT_MERGE_DIRECTORY, locationTagId, 
		        	locationId));
		
		String outputTypeString = org.openmrs.module.chirdlutilbackports.util.Util.getFormAttributeValue(formId,
			ChirdlUtilConstants.FORM_ATTR_OUTPUT_TYPE, locationTagId, locationId);
		
		if (outputTypeString == null) {
			outputTypeString = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_DEFAULT_OUTPUT_TYPE);
			if (outputTypeString == null) {
				outputTypeString = ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_XML;
			}
		}
		
		StringTokenizer tokenizer = new StringTokenizer(outputTypeString, ChirdlUtilConstants.GENERAL_INFO_COMMA);
		HashMap<String, OutputStream> outputs = new HashMap<String, OutputStream>();
		int maxDssElements = Util.getMaxDssElements(formId, locationTagId, locationId);
		File pdfFile = null;
		while (tokenizer.hasMoreTokens()) {
			String mergeFilename = null;
			try {
				String currToken = tokenizer.nextToken().trim();
				
				if (currToken.equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_XML) || 
						currToken.equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_PDF)) {
					File pendingDir = new File(mergeDirectory, ChirdlUtilConstants.FILE_PENDING);
					pendingDir.mkdirs();
					mergeFilename = pendingDir.getAbsolutePath() + File.separator + formInstance.toString() + 
							ChirdlUtilConstants.FILE_EXTENSION_XML;
				}
				if (currToken.equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_PDF)) {
					File pdfDir = new File(mergeDirectory, ChirdlUtilConstants.FORM_ATTR_VAL_PDF);
					pdfDir.mkdirs();
					mergeFilename =pdfDir.getAbsolutePath() + File.separator + formInstance.toString() + 
							ChirdlUtilConstants.FILE_EXTENSION_PDF;
					pdfFile = new File(mergeFilename);
				}
				
				if (mergeFilename != null) {
					FileOutputStream output = new FileOutputStream(mergeFilename);
					
					outputs.put(currToken, output);
				} else {
					log.error("mergeFilename is null");
				}
				
			}
			catch (IOException e) {
				log.error("Could not produce merge xml for file: " + mergeFilename, e);
			}
		}
		
		atdService.produce(outputs, patientState, patient, encounterId, formName, maxDssElements, sessionId);
		
		for (OutputStream output : outputs.values()) {
			try {
				output.flush();
				output.close();
			}
			catch (IOException e) {
				
			}
		}
		
		if (Boolean.TRUE.equals(autoPrint) && pdfFile != null) {
			autoPrintForm(formId, locationId, locationTagId, pdfFile);
		}
	}
	
	/**
	 * Auto-prints a form.  It will print to the default printer unless the alternate printer is being used.
	 * 
	 * @param formId The form identifier.
	 * @param locationId The location identifier.
	 * @param locationTagId The location tag identifier.
	 * @param formToPrint The form to print.
	 */
	protected void autoPrintForm (Integer formId, Integer locationId, Integer locationTagId, File formToPrint) {
		String printerName = null;
		FormAttributeValue altPrinterVal = Context.getService(ChirdlUtilBackportsService.class).getFormAttributeValue(
			formId, ChirdlUtilConstants.FORM_ATTR_USE_ALTERNATE_PRINTER, locationTagId, locationId);
		if (altPrinterVal == null || altPrinterVal.getValue() == null || 
				altPrinterVal.getValue().trim().equalsIgnoreCase(ChirdlUtilConstants.FORM_ATTR_VAL_FALSE)) {
			FormAttributeValue printerVal = Context.getService(ChirdlUtilBackportsService.class).getFormAttributeValue(
				formId, ChirdlUtilConstants.FORM_ATTR_DEFAULT_PRINTER, locationTagId, locationId);
			if (printerVal == null || printerVal.getValue() == null || printerVal.getValue().trim().length() == 0) {
				log.error("Form was set to auto-print, but there is no default printer found.  Form ID: " + formId + 
					" Location ID: " + locationId + " Location Tag ID: " + locationTagId);
				return;
			}
			
			printerName = printerVal.getValue().trim();
		} else {
			FormAttributeValue printerVal = Context.getService(ChirdlUtilBackportsService.class).getFormAttributeValue(
				formId, ChirdlUtilConstants.FORM_ATTR_ALTERNATE_PRINTER, locationTagId, locationId);
			if (printerVal == null || printerVal.getValue() == null || printerVal.getValue().trim().length() == 0) {
				log.error("Form was set to auto-print to the alternate printer, but there is no alternate printer found.  Form ID: " + formId + 
					" Location ID: " + locationId + " Location Tag ID: " + locationTagId);
				return;
			}
			
			printerName = printerVal.getValue().trim();
		}
		
		try {
	        PrintServices.printFile(printerName, formToPrint);
        }
        catch (IOException e) {
	        log.error("Error printing PDF: " + formToPrint.getAbsolutePath() + " to printer " + printerName, e);
        }
        catch (PrintException e) {
	        log.error("Error printing PDF: " + formToPrint.getAbsolutePath() + " to printer " + printerName, e);
        }
	}
}
