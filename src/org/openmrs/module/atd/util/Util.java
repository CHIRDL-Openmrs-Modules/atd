/**
 * 
 */
package org.openmrs.module.atd.util;

import org.openmrs.api.context.Context;
import org.openmrs.module.atd.hibernateBeans.FormAttributeValue;
import org.openmrs.module.atd.service.ATDService;

/**
 * @author Tammy Dugan
 *
 */
public class Util
{
	public static String getFormAttributeValue(Integer formId,String attribute)
	{
		ATDService atdService = Context.getService(ATDService.class);
		FormAttributeValue formAttributeValue =  atdService.getFormAttributeValue(formId, attribute);
		
		if(formAttributeValue != null)
		{
			return formAttributeValue.getValue();
		}
		return null;
	}
}
