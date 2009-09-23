/**
 * 
 */
package org.openmrs.module.atd.datasource;

import java.util.HashMap;

import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.xmlBeans.Field;

/**
 * Contains metadata for a result from the Teleform Export XML Datasource
 * 
 * @author Tammy Dugan
 *
 */
public class XMLResult
{	
	private Integer personId = null;
	private Result result = Result.emptyResult();
	private HashMap<String,Field> parsedFile = null;
	
	/**
	 * @return the personId
	 */
	public Integer getPersonId()
	{
		return this.personId;
	}
	/**
	 * @param personId the personId to set
	 */
	public void setPersonId(Integer personId)
	{
		this.personId = personId;
	}
	/**
	 * @return the result
	 */
	public Result getResult()
	{
		return this.result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(Result result)
	{
		this.result = result;
	}
	/**
	 * @return the parsedFile
	 */
	public HashMap<String,Field> getParsedFile()
	{
		return this.parsedFile;
	}
	/**
	 * @param parsedFile the parsedFile to set
	 */
	public void setParsedFile(HashMap<String,Field> parsedFile)
	{
		this.parsedFile = parsedFile;
	}

}
