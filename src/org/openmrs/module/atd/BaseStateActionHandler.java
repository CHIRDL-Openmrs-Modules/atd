/**
 * 
 */
package org.openmrs.module.atd;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleClassLoader;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.atd.action.ProcessStateAction;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.StateAction;

/**
 * @author tmdugan
 */
public class BaseStateActionHandler implements StateActionHandler {
	
	private static Log log = LogFactory.getLog(BaseStateActionHandler.class);
	
	private static BaseStateActionHandler stateActionHandler = null;
	
	public static BaseStateActionHandler getInstance() {
		if (stateActionHandler == null) {
			stateActionHandler = new BaseStateActionHandler();
		}
		return stateActionHandler;
	}
	
	protected BaseStateActionHandler() {
		
	}
	
	protected ProcessStateAction loadProcessStateAction(StateAction stateAction) {
		
		ProcessStateAction processStateAction = null;
		
		try {
			String stateActionClass = stateAction.getActionClass();
			Collection<ModuleClassLoader> moduleClassLoaders = ModuleFactory.getModuleClassLoaders();
			Class classInstatiation = null;
			for(ModuleClassLoader moduleClassLoader:moduleClassLoaders){
				try {
	                classInstatiation = Class.forName(stateActionClass,true,moduleClassLoader);
	                if(classInstatiation != null){
	                	break;
	                }
                }
                catch (Exception e) {
                }
			}
			if (stateActionClass == null) {
				return null;
			}
			//class the initialization method is in
			processStateAction = (ProcessStateAction) classInstatiation.newInstance();
		}
		catch (Exception e) {
			log.error(e.getMessage());
			log.error(org.openmrs.module.chirdlutil.util.Util.getStackTrace(e));
		}
		return processStateAction;
	}
	
	public synchronized void processAction(StateAction stateAction, Patient patient, PatientState patientState,
	                                       HashMap<String, Object> parameters) {
		if (stateAction == null) {
			return;
		}
		
		// lookup the patient again to avoid lazy initialization errors
		PatientService patientService = Context.getPatientService();
		Integer patientId = patient.getPatientId();
		patient = patientService.getPatient(patientId);
		
		ProcessStateAction processStateAction = loadProcessStateAction(stateAction);
		
		if (processStateAction != null) {
			processStateAction.processAction(stateAction, patient, patientState, parameters);
		}
	}
	
}
