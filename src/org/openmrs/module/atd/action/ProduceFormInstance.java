/**
 * 
 */
package org.openmrs.module.atd.action;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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
import org.openmrs.module.chirdlutil.util.IOUtil;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.StateManager;
import org.openmrs.module.chirdlutilbackports.action.ProcessStateAction;
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
		long startTime = System.currentTimeMillis();
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
		if(parameters != null){
			formName = (String) parameters.get("formName");
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
			currState = chirdlutilbackportsService.getStateByName("ErrorState");
			chirdlutilbackportsService.addPatientState(patient,
					currState, sessionId,locationTagId,locationId);
			log.error(formName+
					" locationTagAttribute does not exist for locationTagId: "+
					locationTagId+" locationId: "+locationId);
			return;
		}
		
		FormService formService = Context.getFormService();
		Form form = formService.getForm(formId);
		startTime = System.currentTimeMillis();
		
		// write the form
		FormInstance formInstance = chirdlutilbackportsService.addFormInstance(formId,
				locationId);
		
		if(parameters == null){
			parameters = new HashMap<String,Object>();
		}
		parameters.put("formInstance", formInstance);
		patientState.setFormInstance(formInstance);
		chirdlutilbackportsService.updatePatientState(patientState);
		Integer formInstanceId = formInstance.getFormInstanceId();
		String mergeDirectory = IOUtil
				.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
						.getFormAttributeValue(formId, "defaultMergeDirectory",
								locationTagId, locationId));

		String mergeFilename = mergeDirectory + "Pending/"+formInstance.toString() + ".xml";
		int maxDssElements = Util
				.getMaxDssElements(formId, locationTagId, locationId);
		
		try {
			FileOutputStream output = new FileOutputStream(mergeFilename);
			startTime = System.currentTimeMillis();
			atdService.produce(output, patientState, patient, encounterId, formName, maxDssElements, sessionId);
			startTime = System.currentTimeMillis();
			output.flush();
			output.close();
		}
		catch (IOException e) {
			log.error("Could not produce merge xml for file: "+mergeFilename, e);
		}

		StateManager.endState(patientState);
		System.out.println("Produce: Total time to produce "+form.getName()+"(" + Thread.currentThread().getName() + "): "+
			(System.currentTimeMillis()-totalTime));
		BaseStateActionHandler.changeState(patient, sessionId, currState, stateAction, parameters,
				locationTagId, locationId);
		startTime = System.currentTimeMillis();
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

}
