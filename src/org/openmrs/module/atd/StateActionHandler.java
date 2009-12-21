/**
 * 
 */
package org.openmrs.module.atd;

import java.util.HashMap;

import org.openmrs.Patient;
import org.openmrs.module.atd.hibernateBeans.PatientState;
import org.openmrs.module.atd.hibernateBeans.StateAction;

/**
 * @author tmdugan
 *
 */
public interface StateActionHandler
{
	public void processAction(StateAction stateAction, Patient patient,
			PatientState patientState,HashMap<String,Object> parameters);
}
