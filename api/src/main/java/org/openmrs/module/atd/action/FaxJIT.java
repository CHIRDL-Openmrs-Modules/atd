/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.FormService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.EmptyResult;
import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.FaxUtil;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Error;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttribute;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstanceAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.ChirdlLocationAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Session;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 * @author Steve McKee
 */
/**
 * Faxes JIT form images using an online fax webservice. 
 * Forms must be designated as auto-fax.
 * @author msheley
 *
 */
public class FaxJIT implements ProcessStateAction {
	
	private static Log log = LogFactory.getLog(FaxJIT.class);
	
	/**
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient, PatientState patientState,
	                          HashMap<String, Object> parameters) {

		// lookup the patient again to avoid lazy initialization errors
		log.info("this is a test");
		try {
			log.info(" This is line 61" + Thread.currentThread().getStackTrace()[0].getLineNumber());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ATDService atdService = Context.getService(ATDService.class);
		FormService formService = Context.getFormService();
		
		State currState = patientState.getState();
		Integer sessionId = patientState.getSessionId();
		int priority = ChirdlUtilConstants.FAX_PRIORITY_NORMAL; //Default
		int resolution = ChirdlUtilConstants.FAX_RESOLUTION_HIGH; //Default
		
		AdministrationService administrationService = Context.getAdministrationService();
		String password = administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_PASSWORD);
		String username = administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_USERNAME); 
		String sender = administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_SENDER); 
		String defaultRecipient = administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_RECIPIENT); 
		
		String wsdlLocation = administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_WSDL_LOCATION); 
		String priorityProperty= administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_PRIORITY); 
		String resolutionPropterty= administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_RESOLUTION); 
		String sendTime = administrationService.getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_SEND_TIME); 
		
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Session session = chirdlutilbackportsService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		FormInstance formInstance = (FormInstance) parameters.get(ChirdlUtilConstants.PARAMETER_FORM_INSTANCE);
		Integer formId = formInstance.getFormId();	
		Integer locationTagId = patientState.getLocationTagId();
		Integer locationId = patientState.getLocationId();
		
		try {
			//check wsdl location property
			if (StringUtils.isBlank(wsdlLocation)){
				String message = "Global property '" + ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_WSDL_LOCATION + "', for the fax web service wsdl location is null or empty.";
				logError(sessionId, message, null);
				return;
			}
			
			// verify form needs to be faxed
			FormAttributeValue formAttrVal = chirdlutilbackportsService.getFormAttributeValue(formId, ChirdlUtilConstants.FORM_ATTRIBUTE_AUTO_FAX, 
				locationTagId, locationId);
			
			Form form = formService.getForm(formId);
			String formName = (form != null) ? form.getName() : "";
			
			//check if auto-fax form attribute value is true
			if (formAttrVal == null || !StringUtils.equalsIgnoreCase(formAttrVal.getValue(), ChirdlUtilConstants.GENERAL_INFO_TRUE)){
				return;
			}
			
			// get the clinic fax number
			ChirdlLocationAttributeValue locAttrValFaxNumber = chirdlutilbackportsService.getLocationAttributeValue(locationId,
					ChirdlUtilConstants.LOCATION_ATTR_CLINIC_FAX_NUMBER);
			if (locAttrValFaxNumber == null || StringUtils.isBlank(locAttrValFaxNumber.getValue())){
				String message = "No clinic fax number exists as a location attribute for location: " + locationId;
				logError(sessionId, message, null);
				return;
			}
			String faxNumber = locAttrValFaxNumber.getValue();
			
			// get the image directory
			FormAttributeValue imageDirectoryAttrValue = chirdlutilbackportsService.getFormAttributeValue(formId, 
					ChirdlUtilConstants.FORM_ATTRIBUTE_IMAGE_DIRECTORY, locationTagId, locationId);
			if (imageDirectoryAttrValue == null || StringUtils.isBlank(imageDirectoryAttrValue.getValue())){
				String message = "Fax image directory attribute, '" + ChirdlUtilConstants.FORM_ATTRIBUTE_IMAGE_DIRECTORY + "', is null or empty for formId: " + formId + " Form Name: " + formName;
				logError(sessionId, message, null);
				return;
			}
			
			//Check that image file exists.
			HashSet<String> extensions = new HashSet<String>();
			extensions.add(ChirdlUtilConstants.FILE_EXTENSION_PDF);
			extensions.add(ChirdlUtilConstants.FILE_EXTENSION_TIF);
			extensions.add(ChirdlUtilConstants.FILE_EXTENSION_TIFF);
			String imageFilename = formInstance.getLocationId() + "-" + formInstance.getFormId() + "-" + formInstance.getFormInstanceId();
			File imageFile = IOUtil.searchForFile(imageFilename, imageDirectoryAttrValue.getValue(), extensions);
		
			if (imageFile.exists()) {
							
				String recipient = defaultRecipient;
				
				// get the provider's name
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put(ChirdlUtilConstants.PARAMETER_ENCOUNTER_ID,encounterId);
				Result result = atdService.evaluateRule(ChirdlUtilConstants.RULE_PROVIDER_NAME, patient, params);
				if (!(result instanceof EmptyResult)) {
					recipient = result.toString();
				}
				
				// get the clinic name
				ChirdlLocationAttributeValue locAttrValueClinicDisplayName = chirdlutilbackportsService.getLocationAttributeValue(locationId,
						ChirdlUtilConstants.LOCATION_ATTR_CLINIC_DISPLAY_NAME);
				String clinic = ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING;
				if (locAttrValueClinicDisplayName != null && !StringUtils.isBlank(locAttrValueClinicDisplayName.getValue())){
					clinic = locAttrValueClinicDisplayName.getValue();
				}
						
				// get the form display name
				FormAttributeValue displayNameVal = chirdlutilbackportsService.getFormAttributeValue(formId, 
									ChirdlUtilConstants.FORM_ATTR_DISPLAY_NAME, locationTagId, locationId);
				
				if (displayNameVal != null && !StringUtils.isBlank(displayNameVal.getValue())) {
					formName = displayNameVal.getValue();
				}
				
				//Method isNumeric() returns true if empty string.  
				//Add check for whitespace to make sure it is truly numeric.
				//Newer version of Apache Commons will fix this.
				if (StringUtils.isNumeric(resolutionPropterty) && !StringUtils.isWhitespace(resolutionPropterty)) {
					resolution = Integer.valueOf(resolutionPropterty);
				}
							
				if (StringUtils.isNumeric(priorityProperty) && !StringUtils.isWhitespace(priorityProperty)) {
					priority = Integer.valueOf(priorityProperty);
				}
					
				String uniqueId = FaxUtil.faxFileByWebService(imageFile, wsdlLocation, ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING, 
						faxNumber, username, password, sender, recipient, clinic, patient, formName, resolution, priority, sendTime);
				//save a form_instance attribute
				//New attribute faxid
				
				
				FormInstanceAttribute attr = chirdlutilbackportsService
						.getFormInstanceAttributeByName(ChirdlUtilConstants.FORM_INSTANCE_ATTR_FAX_ID);
				
				if (attr != null){
					FormInstanceAttributeValue formInstanceAttrValue = new FormInstanceAttributeValue();
					formInstanceAttrValue.setFormInstanceAttributeId(attr.getFormInstanceAttributeId());
					formInstanceAttrValue.setFormId(formInstance.getFormId());
					formInstanceAttrValue.setFormInstanceId(formInstance.getFormInstanceId());
					formInstanceAttrValue.setLocationId(locationId);
					formInstanceAttrValue.setValue(uniqueId);
					chirdlutilbackportsService.saveFormInstanceAttributeValue(formInstanceAttrValue);
				}
				
				log.info("Form " + formName + " was submitted to the fax web service for patient_id: " 
						+ patient.getPatientId() + " clinic: " + clinic + " recipient: " + recipient);
							
			}
		}
		catch (Exception e) {
			String message = "Exception auto-faxing form - Location: " + locationId + " Form: " + formId;
			logError(sessionId, message, e);
		}
		finally {
			StateManager.endState(patientState);
			BaseStateActionHandler.changeState(patient, sessionId, currState,
					stateAction,parameters,locationTagId,locationId);
		}
	}
	
	private void logError(Integer sessionId, String message, Throwable e) {
		String stack = null;
		if (e != null){
			log.error(message, e );
			stack = org.openmrs.module.chirdlutil.util.Util.getStackTrace((Exception) e);
		}
		else {
			log.error(message);
		} 
		Error Error = new Error("Error", "General Error", message, stack, new Date(), sessionId);
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		chirdlutilbackportsService.saveError(Error);
	}
	
	public void changeState(PatientState patientState, HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}
	
}
