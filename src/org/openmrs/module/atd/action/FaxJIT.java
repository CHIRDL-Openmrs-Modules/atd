/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

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
		
		Session session = chirdlutilbackportsService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		
		FormInstance formInstance = (FormInstance) parameters.get("formInstance");
		Integer formId = formInstance.getFormId();
		
		Integer locationTagId = patientState.getLocationTagId();
		Integer locationId = patientState.getLocationId();
		
		try {
			// see if the form needs to be faxed
			FormAttributeValue formAttrVal = chirdlutilbackportsService.getFormAttributeValue(formId, "auto-fax", 
				locationTagId, locationId);
			if (formAttrVal != null && "true".equals(formAttrVal.getValue())) {
				// get the clinic fax number
				LocationAttributeValue locAttrVal = chirdlutilbackportsService.getLocationAttributeValue(locationId,
				    "clinicFaxNumber");
				if (locAttrVal != null && locAttrVal.getValue() != null && locAttrVal.getValue().trim().length() > 0) {
					String clinicFaxNumber = locAttrVal.getValue();
					FormAttributeValue tiffLocVal = chirdlutilbackportsService.getFormAttributeValue(formId, 
						"imageDirectory", locationTagId, locationId);
					if (tiffLocVal != null && tiffLocVal.getValue() != null && tiffLocVal.getValue().trim().length() > 0) {
						Integer formInstId = formInstance.getFormInstanceId();
						String filename = locationId + "-" + formId + "-" + formInstId;
						String imageLocDir = tiffLocVal.getValue();
						File imageFile = IOUtil.searchForImageFile(filename, imageLocDir);
						// check to see if the file exists
						if (imageFile.exists()) {
							String from = Context.getAdministrationService().getGlobalProperty("atd.outgoingFaxFrom");
							String to = "Clinical Staff";
							// try to get the provider's name
							HashMap<String, Object> params = new HashMap<String, Object>();
							params.put("encounterId",encounterId);
							Result result = atdService.evaluateRule("providerName", patient, params);
							if (!(result instanceof EmptyResult)) {
								to = result.toString();
							}
							
							// get the form name
							FormAttributeValue displayNameVal = chirdlutilbackportsService.getFormAttributeValue(formId, 
								"displayName", locationTagId, locationId);
							String formName = null;
							if (displayNameVal != null && displayNameVal.getValue() != null && 
									displayNameVal.getValue().trim().length() > 0) {
								formName = displayNameVal.getValue();
							} else {
								FormService formService = Context.getFormService();
								Form form = formService.getForm(formId);
								if (form != null) {
									formName = form.getName();
								}
							}
							
							FaxUtil.faxFile(imageFile, from, to, clinicFaxNumber, patient, formName);
						} else {
							String message = "Error locating form to auto-fax - Location: " + locationId + " Form: "
							        + formId + " File: " + imageFile.getAbsolutePath();
							logError(sessionId, message, null);
						}
					} else {
						String message = "Location: " + locationId + " Form: " + formId
						        + " is set to auto-fax, but the image directory cannot be found for the form.";
						logError(sessionId, message, null);
					}
				} else {
					String message = "Location: " + locationId + " Form: " + formId
					        + " is set to auto-fax, but no clinicFaxNumber exists.";
					logError(sessionId, message, null);
				}
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
