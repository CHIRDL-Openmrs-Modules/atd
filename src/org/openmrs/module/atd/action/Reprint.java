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
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.Session;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.LocationTagAttributeValue;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;
import org.openmrs.module.chirdlutil.service.ChirdlUtilService;
import org.openmrs.module.chirdlutil.util.IOUtil;

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
		ChirdlUtilService chirdlUtilService = Context.getService(ChirdlUtilService.class);

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
					currState, sessionId,locationTagId,locationId);
			log.error(formName+
					" locationTagAttribute does not exist for locationTagId: "+
					locationTagId+" locationId: "+locationId);
			return;
		}
		PatientState patientStateProduce = chirdlutilbackportsService.getPatientStateByEncounterFormAction(
				encounterId, formId,"PRODUCE FORM INSTANCE");

		if (patientStateProduce != null)
		{
			FormInstance formInstance = patientStateProduce.getFormInstance();
			if(parameters == null){
				parameters = new HashMap<String,Object>();
			}
			parameters.put("formInstance", formInstance);
			patientState.setFormInstance(formInstance);
			chirdlutilbackportsService.updatePatientState(patientState);
			String mergeDirectory = IOUtil
			.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
					.getFormAttributeValue(formInstance.getFormId(),
							"defaultMergeDirectory",locationTagId,formInstance.getLocationId()));
			if (mergeDirectory != null)
			{
				File dir = new File(mergeDirectory);
				for (String fileName : dir.list())
				{
					if (fileName.startsWith(formInstance.toString() + "."))
					{
						fileName = mergeDirectory + "/" + fileName;
						IOUtil.renameFile(fileName, fileName.substring(0,
								fileName.lastIndexOf("."))
								+ ".xml");
						StateManager.endState(patientState);
						State currState = patientState.getState();
						BaseStateActionHandler.changeState(patient, sessionId, currState,
							stateAction,parameters,locationTagId,locationId);
						break;
					}
				}
			} else
			{
				log.error("Reprint failed for patient: "
						+ patient.getPatientId()
						+ " because merge directory was null.");
			}
		}

	}

	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}

}
