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
import org.openmrs.module.atd.hibernateBeans.FormInstance;
import org.openmrs.module.atd.xmlBeans.Field;
import org.openmrs.module.atd.xmlBeans.Records;
import org.openmrs.logic.util.Util;

/**
 * @author Tammy Dugan
 *
 */
public class TeleformExportXMLDatasource implements LogicDataSource
{
	private LogicTeleformExportDAO logicTeleformExportDAO;
	
	public void setLogicTeleformExportDAO(LogicTeleformExportDAO logicTeleformExportXMLDAO) {
		this.logicTeleformExportDAO = logicTeleformExportXMLDAO;
	}

	public LogicTeleformExportDAO getLogicTeleformExportDAO() {
		return this.logicTeleformExportDAO;
	}
	
	public FormInstance parse(InputStream input,Integer formInstanceId, Integer formId) throws Exception
	{
		return this.logicTeleformExportDAO.parse(input, formInstanceId, formId);
	}
	
	public Records parse(InputStream input) throws Exception
	{
		return this.logicTeleformExportDAO.parse(input);
	}
	
	public HashMap<String,Field> getParsedFile(Integer formInstanceId, Integer formId)
	{
		return this.logicTeleformExportDAO.getParsedFile(formInstanceId,formId);
	}
	
	public void deleteParsedFile(Integer formInstanceId, Integer formId)
	{
		this.logicTeleformExportDAO.deleteParsedFile(formInstanceId,formId);
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
		List<XMLResult> xmlResults = getLogicTeleformExportDAO()
				.getXMLResults(patients, criteria);

		for (XMLResult xmlResult : xmlResults)
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
		Util.applyAggregators(finalResult, criteria,patients);
		return finalResult;
	}

}
