/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.chirdlutil.util.ChirdlUtilConstants;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
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
public class Reprint implements ProcessStateAction
{
	private Log log = LogFactory.getLog(this.getClass());

	/* (non-Javadoc)
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.atd.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState, HashMap<String, Object> parameters)
	{
		//lookup the patient again to avoid lazy initialization errors
		PatientService patientService = Context.getPatientService();
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);
		
		Integer locationTagId = patientState.getLocationTagId();

		ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);
		Integer sessionId = patientState.getSessionId();
		
		Session session = chirdlutilbackportsService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		Integer locationId = patientState.getLocationId();
		
		String formName = null;
		if(parameters != null){
			formName = (String) parameters.get("formName");
		}
		if(formName == null){
			formName = patientState.getState().getFormName();
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
			State currState = chirdlutilbackportsService.getStateByName("ErrorState");
			chirdlutilbackportsService.addPatientState(patient,
					currState, sessionId,locationTagId,locationId, null);
			log.error(formName+
					" locationTagAttribute does not exist for locationTagId: "+
					locationTagId+" locationId: "+locationId);
			return;
		}
		
		PatientState patientStateProduce = 
			org.openmrs.module.atd.util.Util.getProducePatientStateByEncounterFormAction(encounterId, formId);

		if (patientStateProduce != null)
		{
			FormInstance formInstance = patientStateProduce.getFormInstance();
			if(parameters == null){
				parameters = new HashMap<String,Object>();
			}
			parameters.put("formInstance", formInstance);
			patientState.setFormInstance(formInstance);
			chirdlutilbackportsService.updatePatientState(patientState);
			FormAttributeValue fav = Context.getService(ChirdlUtilBackportsService.class).getFormAttributeValue(
				formInstance.getFormId(), "mobileOnly", locationTagId, locationId);
			if (fav == null || !"true".equalsIgnoreCase(fav.getValue())) {

				// DWE CHICA-821 Made changes below to check both the merge and pending directory. Previous functionality only checked the merge directory
				String mergeDirectory = org.openmrs.module.chirdlutilbackports.util.Util.getFormAttributeValue(formInstance.getFormId(),
						ChirdlUtilConstants.FORM_ATTR_DEFAULT_MERGE_DIRECTORY, locationTagId, formInstance.getLocationId());
				
				if (mergeDirectory != null)
				{
					File[] fileDirectories = {new File(mergeDirectory), new File(mergeDirectory, ChirdlUtilConstants.FILE_PENDING + File.separator)};
					
					outerLoop:
					for(File fileDirectory : fileDirectories) // Check the merge and pending directory
					{
						if(fileDirectory.exists())
						{
							for (String fileName : fileDirectory.list())
							{
								if (fileName.startsWith(formInstance.toString() + "."))
								{
									fileName = fileDirectory.getAbsolutePath() + File.separator + fileName;
									IOUtil.renameFile(fileName, fileName.substring(0,
											fileName.lastIndexOf("."))
											+ ChirdlUtilConstants.FILE_EXTENSION_XML);
									StateManager.endState(patientState);
									State currState = patientState.getState();
									BaseStateActionHandler.changeState(patient, sessionId, currState,
										stateAction,parameters,locationTagId,locationId);
									break outerLoop;
								}
							}
						}
					}
				} else
				{
					log.error("Reprint failed for patient: "
							+ patient.getPatientId()
							+ " because merge directory was null.");
				}
			} else {
				StateManager.endState(patientState);
				State currState = patientState.getState();
				BaseStateActionHandler.changeState(patient, sessionId, currState,
					stateAction,parameters,locationTagId,locationId);
			}
		}

	}

	/**
	 * @see org.openmrs.module.chirdlutilbackports.action.ProcessStateAction#changeState(org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}

}
