/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.File;
import java.util.HashMap;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.atd.TeleformFileMonitor;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.atd.util.Util;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.IOUtil;

/**
 * @author tmdugan
 *
 */
public class WaitForPrint implements ProcessStateAction
{

	/* (non-Javadoc)
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.atd.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState, HashMap<String, Object> parameters)
	{
		//lookup the patient again to avoid lazy initialization errors
		PatientService patientService = Context.getPatientService();
		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);
		
		Integer locationTagId = patientState.getLocationTagId();
	
		FormInstance formInstance = (FormInstance) parameters.get("formInstance");
		if(formInstance == null){
		
			Integer sessionId = patientState.getSessionId();
			PatientState stateWithFormId = Util.getPrevProducePatientStateByAction(patientState, sessionId);
			
			formInstance = patientState.getFormInstance();
	
			if(formInstance == null&&stateWithFormId != null)
			{
				formInstance = stateWithFormId.getFormInstance();
			}
		}
		patientState.setFormInstance(formInstance);
		chirdlutilbackportsService.updatePatientState(patientState);
		
		String mergeDirectory = IOUtil
				.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
						.getFormAttributeValue(formInstance.getFormId(),
								"defaultMergeDirectory", locationTagId,
								formInstance.getLocationId()));
		
		// Check the output type
		FormAttributeValue fav = chirdlutilbackportsService.getFormAttributeValue(
			formInstance.getFormId(), ChirdlUtilConstants.FORM_ATTR_OUTPUT_TYPE, locationTagId, formInstance.getLocationId());
		String outputType = null;
		if (fav == null || fav.getValue() == null || fav.getValue().trim().length() == 0) {
			outputType = Context.getAdministrationService().getGlobalProperty(
				ChirdlUtilConstants.GLOBAL_PROP_DEFAULT_OUTPUT_TYPE);
		} else {
			String[] outputTypes = fav.getValue().split(ChirdlUtilConstants.GENERAL_INFO_COMMA);
			outputType = outputTypes[0];
		}
		
		String mergeFilename = "";
		if (ChirdlUtilConstants.FORM_ATTR_VAL_PDF.equalsIgnoreCase(outputType) || 
				ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_PDF.equalsIgnoreCase(outputType)) {
			File pdfDir = new File(mergeDirectory, ChirdlUtilConstants.FILE_PDF);
			File mergeFile = new File(pdfDir, formInstance.toString() + ChirdlUtilConstants.FILE_EXTENSION_PDF);
			mergeFilename = mergeFile.getAbsolutePath();
		} else if (ChirdlUtilConstants.FORM_ATTR_VAL_TELEFORM_XML.equalsIgnoreCase(outputType)) {
			File mergeFile = new File(mergeDirectory, formInstance.toString() + ".20");
			mergeFilename = mergeFile.getAbsolutePath();
		}
		
		TeleformFileState teleformFileState = TeleformFileMonitor.addToPendingStatesWithFilename(formInstance, mergeFilename);
		teleformFileState.addParameter("patientState", patientState);
	}

	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {
		StateManager.endState(patientState);
		BaseStateActionHandler.changeState(patientState.getPatient(), patientState
				.getSessionId(), patientState.getState(),
				patientState.getState().getAction(),parameters,
				patientState.getLocationTagId(),
				patientState.getLocationId());
		
	}

}
