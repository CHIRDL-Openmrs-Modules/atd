/**
 * 
 */
package org.openmrs.module.atd;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.TeleformFileMonitor;
import org.openmrs.module.atd.TeleformFileState;
import org.openmrs.module.chirdlutil.threadmgmt.ChirdlRunnable;
import org.openmrs.module.chirdlutilbackports.BaseStateActionHandler;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.PatientState;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.State;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.StateAction;
import org.openmrs.module.chirdlutilbackports.service.ChirdlUtilBackportsService;

/**
 * @author tmdugan
 * 
 */
public class InitializeOldStates implements ChirdlRunnable
{
	private Log log = LogFactory.getLog(this.getClass());

	public InitializeOldStates()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		log.info("Started execution of " + getName() + "("+ Thread.currentThread().getName() + ", " + 
			new Timestamp(new Date().getTime()) + ")");
		Context.openSession();
		try
		{
			AdministrationService adminService = Context
					.getAdministrationService();
			Context.authenticate(adminService
					.getGlobalProperty("scheduler.username"), adminService
					.getGlobalProperty("scheduler.password"));
			Integer processedStates = 0;

			LocationService locationService = Context.getLocationService();

			List<Location> locations = locationService.getAllLocations();
			
			for (Location location : locations)
			{
				Calendar todaysDate = Calendar.getInstance();
				todaysDate.set(Calendar.HOUR_OF_DAY, 0);
				todaysDate.set(Calendar.MINUTE, 0);
				todaysDate.set(Calendar.SECOND, 0);
				Date currDate = todaysDate.getTime();
				ChirdlUtilBackportsService chirdlutilbackportsService = Context.getService(ChirdlUtilBackportsService.class);

				Set<LocationTag> tags = location.getTags();
				
				if(tags != null){
				
					for(LocationTag tag:tags){
						
						Integer locationTagId = tag.getLocationTagId();
						Integer locationId = location.getLocationId();
						
				List<PatientState> unfinishedStatesToday = chirdlutilbackportsService
						.getUnfinishedPatientStatesAllPatients(null,locationTagId,locationId);

				BaseStateActionHandler handler = BaseStateActionHandler
						.getInstance();

				for (PatientState currPatientState : unfinishedStatesToday)
				{
					if (currPatientState.getStartTime().compareTo(currDate) >= 0)
					{
						continue;
					}
					State state = currPatientState.getState();
					if (state != null)
					{
						StateAction stateAction = state.getAction();
					Patient patient = currPatientState.getPatient();

						try
						{
						if (stateAction!=null&&(stateAction.getActionName().equalsIgnoreCase(
								"CONSUME FORM INSTANCE")))
						{
								TeleformFileState teleformFileState = TeleformFileMonitor
										.addToPendingStatesWithoutFilename(
												currPatientState.getFormInstance());
								teleformFileState.addParameter("patientState",
									currPatientState);
						}
						HashMap<String,Object> parameters = new HashMap<String,Object>();
						parameters.put("formInstance", currPatientState.getFormInstance());
						handler.processAction(stateAction, patient,
								currPatientState, parameters);
						} catch (Exception e)
						{
							log.error(e.getMessage());
							log.error(org.openmrs.module.chirdlutil.util.Util
									.getStackTrace(e));
						}
					}
					if (processedStates % 100 == 0)
					{
						this.log.info("Old states loaded: " + processedStates);
					}
					processedStates++;
				}
			}
				}
			}
			this.log.info("Final number old states loaded: " + processedStates);
		} catch (Exception e)
		{
		} finally
		{
			Context.closeSession();
			log.info("Finished execution of " + getName() + "("+ Thread.currentThread().getName() + ", " + 
				new Timestamp(new Date().getTime()) + ")");
		}
	}

	/**
     * @see org.openmrs.module.chirdlutil.threadmgmt.ChirdlRunnable#getName()
     */
    public String getName() {
	    
	    return "Initialize Old States";
    }

	/**
     * @see org.openmrs.module.chirdlutil.threadmgmt.ChirdlRunnable#getPriority()
     */
    public int getPriority() {
	    // TODO Auto-generated method stub
	    return ChirdlRunnable.PRIORITY_FIVE;
    }

}
