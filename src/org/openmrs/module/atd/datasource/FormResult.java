/**
 * 
 */
package org.openmrs.module.atd.datasource;

import java.util.HashMap;

import org.openmrs.logic.result.Result;
import org.openmrs.module.atd.xmlBeans.Field;

/**
 * Contains metadata for a result from the Form Datasource
 * 
 * @author Tammy Dugan
 *
 */
public class FormResult
{	
	private Integer personId = null;
	private Result result = Result.emptyResult();
	private HashMap<String,Field> formFields = null;
	
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
	 * @return the formFields
	 */
	public HashMap<String,Field> getFormFields()
	{
		return this.formFields;
	}
	/**
	 * @param formFields the formFields to set
	 */
	public void setFormFields(HashMap<String,Field> formFields)
	{
		this.formFields = formFields;
	}

}
