/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.FileOutputStream;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.StateManager;
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.Session;
import org.openmrs.module.atd.hibernateBeans.State;
import org.openmrs.module.atd.hibernateBeans.StateAction;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutil.util.Util;

/**
 * @author tmdugan
 *
 */
public class ProduceJIT implements ProcessStateAction
{
	private Log log = LogFactory.getLog(this.getClass());

	/* (non-Javadoc)
	 * @see org.openmrs.module.atd.action.ProcessStateAction#processAction(org.openmrs.module.atd.hibernateBeans.StateAction, org.openmrs.Patient, org.openmrs.module.atd.hibernateBeans.PatientState, java.util.HashMap)
	 */
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState, HashMap<String, Object> parameters)
			throws Exception
	{
		Integer formId = patientState.getFormId();
		if (formId != null)
		{
			writeJIT(formId, patient,
					patientState);
		}
	}

	private void writeJIT(Integer formId,
			Patient patient,PatientState patientState){
		ATDService atdService = Context.getService(ATDService.class);
		Integer locationTagId = patientState.getLocationTagId();
		Integer locationId = patientState.getLocationId();
		
		Integer sessionId = patientState.getSessionId();
		Session session = atdService.getSession(sessionId);
		Integer encounterId = session.getEncounterId();
		
		try
		{
			FormInstance formInstance = atdService.addFormInstance(formId,locationId);
			patientState.setFormInstance(formInstance);
			String mergeDirectory = IOUtil
					.formatDirectoryName(org.openmrs.module.atd.util.Util
							.getFormAttributeValue(formId, "pendingMergeDirectory",
									locationTagId,locationId));
			String mergeFilename = mergeDirectory + formInstance.toString() + ".xml";
			FileOutputStream output = new FileOutputStream(mergeFilename);
			atdService.produce(patient, formInstance, output,
					encounterId, null, null,false,locationTagId,patientState.getSessionId());
			output.flush();
			output.close();
			StateManager.endState(patientState);
		} catch (Exception e)
		{
			State currState = atdService.getStateByName("ErrorState");
			atdService.addPatientState(patient,
					currState, patientState.getSessionId(),locationTagId,locationId);
			this.log.error("Error writing JIT for formID: "+formId);
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));
		}
	}
	public void changeState(PatientState patientState,
			HashMap<String, Object> parameters) {
		//deliberately empty because processAction changes the state
	}

}
