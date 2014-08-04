/**
 * 
 */
package org.openmrs.module.atd;

import java.util.Map;

import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.dss.hibernateBeans.Rule;


/**
 * @author tmdugan
 *
 */
public interface ParameterHandler
{
	public void addParameters(Map<String, Object> parameters, Rule rule);
	
	public void addParameters(Map<String, Object> parameters, Rule rule, Map<String, Field> fieldMap);
}
