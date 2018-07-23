/**
 * 
 */
package org.openmrs.module.atd;

import java.util.Map;

import org.openmrs.module.atd.xmlBeans.Field;


/**
 * @author tmdugan
 *
 */
public interface ParameterHandler
{
	public void addParameters(Map<String, Object> parameters, String formType); // CHICA-1234 Added formType parameter
	
	public void addParameters(Map<String, Object> parameters, Map<String, Field> fieldMap, String formType); // CHICA-1234 Added formType parameter
}
