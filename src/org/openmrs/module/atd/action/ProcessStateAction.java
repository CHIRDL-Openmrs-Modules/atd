/**
 * 
 */
package org.openmrs.module.atd.action;

import java.util.HashMap;

import org.openmrs.Patient;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.StateAction;

/**
 * @author tmdugan
 *
 */
public interface ProcessStateAction
{
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState,HashMap<String,Object> parameters) throws Exception;

	public void changeState(PatientState patientState,
			HashMap<String,Object> parameters) throws Exception;
}
