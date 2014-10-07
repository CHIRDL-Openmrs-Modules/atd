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
		if(parameters != null){
			formName = (String) parameters.get("formName");
			Object param2Object = parameters.get("trigger");
			if (param2Object != null && param2Object instanceof String){
				trigger = (String) param2Object;
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
			currState = chirdlutilbackportsService.getStateByName("ErrorState");
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
		parameters.put("formInstance", formInstance);
		patientState.setFormInstance(formInstance);
		chirdlutilbackportsService.updatePatientState(patientState);
		Integer formInstanceId = formInstance.getFormInstanceId();
		
		//If triggered by a force print, save as a form instance attibute value for later reference
		try {
			if (trigger != null && trigger.equalsIgnoreCase("forcePrint")){
				FormInstanceAttributeValue fiav = new FormInstanceAttributeValue();
				FormInstanceAttribute attr = chirdlutilbackportsService
					.getFormInstanceAttributeByName("trigger");
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
		
		produceForm(formId, locationId, locationTagId, encounterId, sessionId, formInstance, patientState, patient, 
			formName, atdService);

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
	                           String formName, ATDService atdService) {
		String mergeDirectory = IOUtil.formatDirectoryName(org.openmrs.module.chirdlutilbackports.util.Util
		        .getFormAttributeValue(formId, "defaultMergeDirectory", locationTagId, locationId));
		
		String outputTypeString = org.openmrs.module.chirdlutilbackports.util.Util.getFormAttributeValue(formId,
		    "outputType", locationTagId, locationId);
		
		if (outputTypeString == null) {
			outputTypeString = "teleformXML";
		}
		
		StringTokenizer tokenizer = new StringTokenizer(outputTypeString,",");
		HashMap<String, OutputStream> outputs = new HashMap<String, OutputStream>();
		int maxDssElements = Util.getMaxDssElements(formId, locationTagId, locationId);
		
		while (tokenizer.hasMoreTokens()) {
			String mergeFilename = null;
			try {
				String currToken = tokenizer.nextToken().trim();
				
				if (currToken.equalsIgnoreCase("teleformXML")) {
					File pendingDir = new File(mergeDirectory, "Pending");
					pendingDir.mkdirs();
					mergeFilename = pendingDir.getAbsolutePath() + File.separator + formInstance.toString() + ".xml";
				}
				if (currToken.equalsIgnoreCase("pdf")) {
					File pdfDir = new File(mergeDirectory, "pdf");
					pdfDir.mkdirs();
					mergeFilename =pdfDir.getAbsolutePath() + File.separator + formInstance.toString() + ".pdf";
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
		
	}
}
