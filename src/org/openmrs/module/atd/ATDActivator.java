package org.openmrs.module.atd;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.chirdlutil.util.Util;

/**
 * Checks that all global properties for this module have been
 * initialized
 * 
 * @author Tammy Dugan
 */
public class ATDActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		try
		{
			this.log.info("Starting ATD Module");
			// check that all global properties are set
			AdministrationService adminService = Context
					.getAdministrationService();
			Context.authenticate(adminService
					.getGlobalProperty("scheduler.username"), adminService
					.getGlobalProperty("scheduler.password"));
			Iterator<GlobalProperty> properties = adminService
					.getAllGlobalProperties().iterator();
			GlobalProperty currProperty = null;
			String currValue = null;
			String currName = null;

			while (properties.hasNext())
			{
				currProperty = properties.next();
				currName = currProperty.getProperty();
				if (currName.startsWith("atd"))
				{
					currValue = currProperty.getPropertyValue();
					if (currValue == null || currValue.length() == 0)
					{
						this.log
								.error("You must set a value for global property: "
										+ currName);
					}
				}
			}
		} catch (Exception e)
		{
			this.log.error("Error checking global properties for atd module");
			this.log.error(e.getMessage());
			this.log.error(Util.getStackTrace(e));

		}
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		this.log.info("Shutting down ATD Module");
	}
	
}
