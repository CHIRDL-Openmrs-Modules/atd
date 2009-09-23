/**
 * 
 */
package org.openmrs.module.atd;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;
import org.openmrs.module.dss.util.Util;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * @author Vibha Anand
 * 
 */

public class ATDStatesCron extends AbstractTask
{
	private Log log = LogFactory.getLog(this.getClass());

	
	private Date startDate;
	private Date lastRunDate;
	private Date thresholdDate;
	private TaskDefinition taskConfig;
	private int retireStatesPriorToDays = -1; // in days

	@Override
	public void initialize(TaskDefinition config)
	{
		Context.openSession();
		if (Context.isAuthenticated() == false)
			authenticate();
		
		this.taskConfig = config;
		init();
		Context.closeSession();
	}

	@Override
	public void execute()
	{
		Context.openSession();
		
		try
		{
			
			if (Context.isAuthenticated() == false)
				authenticate();
			
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
			ss.scheduleTask(teleformTaskDefinition); //start the TeleformMonitor task after the states are retired
			
			log.info("ATD States were retired last on: " + this.lastRunDate.toString());
			
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
		this.log.info("Initializing Cron job for ATD states...");
		
		
		try
		{
			startDate = GregorianCalendar.getInstance().getTime();
			AdministrationService adminService = Context.getAdministrationService();
			// configurable time in days before today's date and time, convert it to a negative number
			retireStatesPriorToDays = -Integer.parseInt(adminService.getGlobalProperty("atd.retireStatesPeriod"));  // in days
		
		} catch (Exception e)
		{
			log.error(e.getMessage());
			log.error(Util.getStackTrace(e));
		}
		this.log.info("Finished initializing Cron job for ATD states.");
	}
	
	@Override
	public void shutdown()
	{
		super.shutdown();
		

	}

}
