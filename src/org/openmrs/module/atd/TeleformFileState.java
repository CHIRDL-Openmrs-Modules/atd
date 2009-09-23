/**
 * 
 */
package org.openmrs.module.atd;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vibha Anand
 * 
 */
public class TeleformFileState 
{
	private Map<String,Object> parameters;
	private String directoryName;
	private String fileExtension;
	private long sentinelDate;    // in File.lastModified format
	private String criteria;
	private String filename; 	
	private String fullFilePath;
	private Integer formId;
	private Integer formInstanceId;
	
	public TeleformFileState()
	{
		this.sentinelDate = 0;
		this.criteria = "EXISTS";
		this.filename = "";
		this.parameters = new HashMap<String,Object>();
	}
	
	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters()
	{
		return this.parameters;
	}
	
	public String getFileExtension()
	{
		return this.fileExtension;
	}

	public void setFileExtension(String fileExtension)
	{
		this.fileExtension = fileExtension;
	}
	
	public String getDirectoryName()
	{
		return this.directoryName;
	}
	
	public void setDirectoryName(String state)
	{
		this.directoryName = state;
	}

	public String getFilename()
	{
		return this.filename;
	}
	
	public void setFilename(String name)
	{
		this.filename = name;
	}

	public long getSentinelDate()
	{
		return this.sentinelDate;
	}
	
	public void setSentinelDate(long thisDate)
	{
		this.sentinelDate = thisDate;
	}
	
	public String getCriteria()
	{
		return this.criteria;
	}
	
	public void setCriteria(String thisCriteria)
	{
		this.criteria = thisCriteria;
	}

	public int getFormInstanceId()
	{
		return this.formInstanceId;
	}
	
	public void setFormInstanceId(int id)
	{
		this.formInstanceId = id;
	}
	
	public int getFormId()
	{
		return this.formId;
	}
	
	public void setFormId(int id)
	{
		this.formId = id;
	}
	
	public Object getParameter(String paramName){
		if(this.parameters != null){
			return this.parameters.get(paramName);
		}
		return null;
	}
	
	public void addParameter(String paramName, Object paramVal){
		if(this.parameters == null){
			this.parameters = new HashMap<String,Object>();
		}
		this.parameters.put(paramName, paramVal);
	}

	public String getFullFilePath()
	{
		return this.fullFilePath;
	}

	public void setFullFilePath(String fullFilePath)
	{
		this.fullFilePath = fullFilePath;
	}
}
