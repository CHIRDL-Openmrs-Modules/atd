/**
 * 
 */
package org.openmrs.module.atd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.chirdlutil.util.Util;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * @author Vibha Anand
 * 
 */

public class ATDStatesCron extends AbstractTask
{
	private static final Logger log = LoggerFactory.getLogger(ATDStatesCron.class);

	
	private Date lastRunDate;
	private Date thresholdDate;
	private int retireStatesPriorToDays = -1; // in days

	@Override
	public void initialize(TaskDefinition config)
	{
		super.initialize(config);
		Context.openSession();
		init();
		Context.closeSession();
	}

	@Override
	public void execute()
	{
		Context.openSession();
		
		try
		{
			
			if(retireStatesPriorToDays == -1)
			{
				// Moved here from init() because of openSession and closeSession errors with rule token fetch 
				
			}
			
			lastRunDate = GregorianCalendar.getInstance().getTime();
			
			Calendar threshold = GregorianCalendar.getInstance();
			threshold.add(GregorianCalendar.DAY_OF_MONTH, retireStatesPriorToDays);   
			thresholdDate = threshold.getTime();
			
			
			ATDService atdService = Context.getService(ATDService.class);
			SchedulerService ss = Context.getSchedulerService();
			TaskDefinition teleformTaskDefinition = ss.getTaskByName("TeleformMonitor");
			
			ss.shutdownTask(teleformTaskDefinition); //stop the TeleformMonitor task to prevent data corruption
			atdService.updatePatientStates(thresholdDate);  // retires states
			// Clear the cached data
			atdService.cleanCache();
			ss.scheduleTask(teleformTaskDefinition); //start the TeleformMonitor task after the states are retired
			
			
			log.info("ATD States were retired last on: {}", this.lastRunDate.toString());
			
		} catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		} finally
		{
			Context.closeSession();
		}
	}
	
	private void init()
	{
		log.info("Initializing Cron job for ATD states...");
		
		
		try
		{
			AdministrationService adminService = Context.getAdministrationService();
			// configurable time in days before today's date and time, convert it to a negative number
			retireStatesPriorToDays = -Integer.parseInt(adminService.getGlobalProperty("atd.retireStatesPeriod"));  // in days
		
		} catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		log.info("Finished initializing Cron job for ATD states.");
	}
	
	@Override
	public void shutdown()
	{
		super.shutdown();
		

	}

}
