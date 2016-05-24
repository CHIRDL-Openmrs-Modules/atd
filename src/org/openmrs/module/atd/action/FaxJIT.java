/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
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
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Session;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;



/**
 * @author Steve McKee
 */
public class FaxJIT implements ProcessStateAction {
	
	
	private static final int FAX_PRIORITY_HIGH = 2;
	private static final int FAX_RESOLUTION_HIGH = 1;
	private static Log log = LogFactory.getLog(FaxJIT.class);
	
	
	/**
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient, PatientState patientState,
	                          HashMap<String, Object> parameters) {
		
		// lookup the patient again to avoid lazy initialization errors
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		PatientService patientService = Context.getPatientService();
		ATDService atdService = Context.getService(ATDService.class);
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);
		State currState = patientState.getState();
		Integer sessionId = patientState.getSessionId();
		int priority = FAX_PRIORITY_HIGH;
		int resolution = FAX_RESOLUTION_HIGH;
		
		String password = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_PASSWORD);
		String username = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_USERNAME); 
		String sender = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_SENDER); 
		String defaultRecipient = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_RECIPIENT); 
		String wsdlLocation = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_WSDL_LOCATION); 
		String priorityProperty= Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_PRIORITY); 
		String resolutionPropterty= Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_RESOLUTION); 
		String sendTime = Context.getAdministrationService().getGlobalProperty(ChirdlUtilConstants.GLOBAL_PROP_OUTGOING_FAX_SEND_TIME); 
		
		Session session = chirdlutilbackportsService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		
		FormInstance formInstance = (FormInstance) parameters.get(ChirdlUtilConstants.PARAMETER_FORM_INSTANCE);
		Integer formId = formInstance.getFormId();
		
		Integer locationTagId = patientState.getLocationTagId();
		Integer locationId = patientState.getLocationId();
		
		try {
			//check wsdl location property
			if (StringUtils.isBlank(wsdlLocation)){
				String message = "Unable to fax form instance: " + formInstance.toString() + ", because wsdl location property for fax web service is null or empty.";
				logError(sessionId, message, null);
				return;
			}
			
			// see if the form needs to be faxed
			FormAttributeValue formAttrVal = chirdlutilbackportsService.getFormAttributeValue(formId, ChirdlUtilConstants.FORM_ATTRIBUTE_AUTO_FAX, 
				locationTagId, locationId);
			if (formAttrVal == null || !StringUtils.equalsIgnoreCase(formAttrVal.getValue(), ChirdlUtilConstants.GENERAL_INFO_TRUE)){
				String message = "Error auto-faxing form - Location: " + locationId + " Form: " + formId;
				logError(sessionId, message, null);
				return;
			}
			
			// get the clinic fax number
			LocationAttributeValue locAttrValFaxNumber = chirdlutilbackportsService.getLocationAttributeValue(locationId,
					ChirdlUtilConstants.LOCATION_ATTR_CLINIC_FAX_NUMBER);
			if (locAttrValFaxNumber == null || StringUtils.isBlank(locAttrValFaxNumber.getValue())){
				String message = "Location: " + locationId + " Form: " + formId
				        + " is set to auto-fax, but no clinicFaxNumber exists.";
				logError(sessionId, message, null);
				return;
			}
			//String faxNumber = locAttrValFaxNumber.getValue();
			//for test:
			String faxNumber = "3172780456";
			
			// get the tiff image directory
			FormAttributeValue imageDirectoryAttrValue = chirdlutilbackportsService.getFormAttributeValue(formId, 
					ChirdlUtilConstants.FORM_ATTRIBUTE_IMAGE_DIRECTORY, locationTagId, locationId);
			if (imageDirectoryAttrValue == null || StringUtils.isBlank(imageDirectoryAttrValue.getValue())){
				String message = "Location: " + locationId + " Form: " + formId
				        + " is set to auto-fax, but the image directory cannot be found for the form.";
				logError(sessionId, message, null);
				return;
			}
			
			// check to see if the file exists
			File imageFile = IOUtil.searchForImageFile(formInstance.toString(), imageDirectoryAttrValue.getValue());
		
			if (imageFile.exists()) {
							
				String recipient = defaultRecipient;
				
				// try to get the provider's name
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put(ChirdlUtilConstants.PARAMETER_ENCOUNTER_ID,encounterId);
				Result result = atdService.evaluateRule(ChirdlUtilConstants.RULE_PROVIDER_NAME, patient, params);
				if (!(result instanceof EmptyResult)) {
					recipient = result.toString();
				}
							
				// get the form display name
				FormAttributeValue displayNameVal = chirdlutilbackportsService.getFormAttributeValue(formId, 
									ChirdlUtilConstants.FORM_ATTR_DISPLAY_NAME, locationTagId, locationId);
				String formName = null;
				if (displayNameVal != null && !StringUtils.isBlank(displayNameVal.getValue())) {
					formName = displayNameVal.getValue();
				} else {
					FormService formService = Context.getFormService();
					Form form = formService.getForm(formId);
					if (form != null) {
						formName = form.getName();
					}
				}
				
				if (StringUtils.isNumeric(resolutionPropterty) && !StringUtils.isWhitespace(resolutionPropterty)) {
					resolution = Integer.valueOf(resolutionPropterty);
				}
							
				if (StringUtils.isNumeric(priorityProperty) && !StringUtils.isWhitespace(priorityProperty)) {
					priority = Integer.valueOf(priorityProperty);
				}
							
				FaxUtil.faxFileByWebService(imageFile, wsdlLocation, ChirdlUtilConstants.GENERAL_INFO_EMPTY_STRING, 
						faxNumber, username, password, sender, recipient, patient, formName, resolution, priority, sendTime);
							
			}
		}
		catch (Exception e) {
			String message = "Error auto-faxing form - Location: " + locationId + " Form: " + formId;
			logError(sessionId, message, e);
		}
		finally {
			StateManager.endState(patientState);
			BaseStateActionHandler.changeState(patient, sessionId, currState,
					stateAction,parameters,locationTagId,locationId);
		}
	}
	
	private void logError(Integer sessionId, String message, Throwable e) {
		log.error("Error auto-faxing form");
		log.error(message);
		Error Error = new Error("Error", "General Error", message, null, new Date(), sessionId);
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		chirdlutilbackportsService.saveError(Error);
	}
	
	public void changeState(PatientState patientState, HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}
	
}
