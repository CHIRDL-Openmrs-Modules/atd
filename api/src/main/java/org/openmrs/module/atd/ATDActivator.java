package org.openmrs.module.atd;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.chirdlutil.util.Util;

/**
 * Checks that all global properties for this module have been
 * initialized
 * 
 * @author Tammy Dugan
 */
public class ATDActivator extends BaseModuleActivator implements DaemonTokenAware {

	private static final Logger log = LoggerFactory.getLogger(ATDActivator.class);
	
	/**
	 * @see org.openmrs.module.BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		try
		{
			this.log.info("Starting ATD Module");
			// check that all global properties are set
			AdministrationService adminService = Context
					.getAdministrationService();
			
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
	 * @see org.openmrs.module.BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		this.log.info("Shutting down ATD Module");
	}

	/**
	 * @see org.openmrs.module.DaemonTokenAware#setDaemonToken(org.openmrs.module.DaemonToken)
	 */
	@Override
	public void setDaemonToken(DaemonToken token) {
		org.openmrs.module.atd.util.Util.setDaemonToken(token);
	}
	
}
