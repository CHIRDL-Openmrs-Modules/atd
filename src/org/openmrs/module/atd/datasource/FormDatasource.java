/**
 * 
 */
package org.openmrs.module.atd.datasource;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.EmptyResult;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.util.LogicUtil;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormInstance;

/**
 * @author Tammy Dugan
 *
 */
public class FormDatasource implements LogicDataSource
{
	public static final String NAME = "form";
	
	private LogicFormDAO logicFormDAO;
	
	public void setLogicFormDAO(LogicFormDAO logicFormDAO) {
		this.logicFormDAO = logicFormDAO;
	}

	public LogicFormDAO getLogicFormDAO() {
		return this.logicFormDAO;
	}
	
	public FormInstance parseTeleformXmlFormat(InputStream input,FormInstance formInstance, 
			Integer locationTagId)
	{
		return this.logicFormDAO.parseTeleformXmlFormat(input, formInstance, locationTagId);
	}
	
	public Records parseTeleformXmlFormat(InputStream input)
	{
		return this.logicFormDAO.parseTeleformXmlFormat(input);
	}
	
	public FormInstance setFormFields(HashMap<String, Field> fieldMap,FormInstance formInstance,Integer locationTagId) {
		return this.logicFormDAO.setFormFields(fieldMap, formInstance, locationTagId);
	}
	
	public HashMap<String,Field> getFormFields(FormInstance formInstance)
	{
		return this.logicFormDAO.getFormFields(formInstance);
	}
	
	public void deleteForm(FormInstance formInstance)
	{
		this.logicFormDAO.deleteForm(formInstance);
	}
	
	public void clearForms()
	{
	    this.logicFormDAO.clearForms();
	}

	/* (non-Javadoc)
	 * @see org.openmrs.logic.datasource.LogicDataSource#getDefaultTTL()
	 */
	public int getDefaultTTL()
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.logic.datasource.LogicDataSource#getKeys()
	 */
	public Collection<String> getKeys()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.logic.datasource.LogicDataSource#hasKey(java.lang.String)
	 */
	public boolean hasKey(String arg0)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.logic.datasource.LogicDataSource#read(org.openmrs.logic.LogicContext, org.openmrs.reporting.Cohort, org.openmrs.logic.LogicCriteria)
	 */
	public Map<Integer, Result> read(LogicContext context,
			Cohort patients, LogicCriteria criteria)
	{
		Map<Integer, Result> finalResult = new HashMap<Integer, Result>();
		List<FormResult> xmlResults = getLogicFormDAO()
				.getFormResults(patients, criteria);

		for (FormResult xmlResult : xmlResults)
		{
			Integer personId = xmlResult.getPersonId();
			Result result = finalResult.get(personId);
			if (xmlResult.getResult() != null
					&& !(xmlResult.getResult() instanceof EmptyResult))
			{
				if (result == null)
				{
					finalResult.put(personId, xmlResult.getResult());
				} else
				{
					result.add(xmlResult.getResult());
				}
			}
		}
		LogicUtil.applyAggregators(finalResult, criteria,patients);
		return finalResult;
	}

}
