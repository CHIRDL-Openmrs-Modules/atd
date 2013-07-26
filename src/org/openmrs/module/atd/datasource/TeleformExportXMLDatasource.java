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
public class TeleformExportXMLDatasource implements LogicDataSource
{
	public static final String NAME = "xml";
	
	private LogicTeleformExportDAO logicTeleformExportDAO;
	
	public void setLogicTeleformExportDAO(LogicTeleformExportDAO logicTeleformExportXMLDAO) {
		this.logicTeleformExportDAO = logicTeleformExportXMLDAO;
	}

	public LogicTeleformExportDAO getLogicTeleformExportDAO() {
		return this.logicTeleformExportDAO;
	}
	
	public FormInstance parse(InputStream input,FormInstance formInstance, 
			Integer locationTagId)
	{
		return this.logicTeleformExportDAO.parse(input, formInstance, locationTagId);
	}
	
	public Records parse(InputStream input)
	{
		return this.logicTeleformExportDAO.parse(input);
	}
	
	public HashMap<String,Field> getParsedFile(FormInstance formInstance)
	{
		return this.logicTeleformExportDAO.getParsedFile(formInstance);
	}
	
	public void deleteParsedFile(FormInstance formInstance)
	{
		this.logicTeleformExportDAO.deleteParsedFile(formInstance);
	}
	
	public void clearParsedFile()
	{
	    this.logicTeleformExportDAO.clearParsedFiles();
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
		LogicUtil.applyAggregators(finalResult, criteria,patients);
		return finalResult;
	}

}
