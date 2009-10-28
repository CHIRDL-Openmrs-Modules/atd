package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;

import org.openmrs.api.context.Context;
import org.openmrs.module.atd.service.ATDService;


public class ATDError implements java.io.Serializable {
	
	// Fields
	private Integer atdErrorID = null;
	private Integer errorCategory = null;
	private Integer sessionId = null;
	private String message = null;
	private String details = null;
	private Date dateTime = null;
	private String level = null;
	
	/**
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	//constructor
	public ATDError(){}
	
	public ATDError( String errorLevel, String catString, String desc
			, String trace, Date date, Integer sessionId){
		ATDService cs = (ATDService) Context.getService(ATDService.class);
		errorCategory = cs.getErrorCategoryIdByName(catString);
		if (errorCategory == null){
			errorCategory = cs.getErrorCategoryIdByName("General Error");
		}
		this.sessionId = sessionId;
		message = desc;
		details = trace;
		dateTime = date;
		level = errorLevel;
	}

	
	public Integer getAtdErrorID()
	{
		return this.atdErrorID;
	}

	public void setAtdErrorID(Integer atdErrorID)
	{
		this.atdErrorID = atdErrorID;
	}

	/**
	 * @return the errorCategory
	 */
	public Integer getErrorCategory() {
		return errorCategory;
	}

	/**
	 * @param errorCategory the errorCategory to set
	 */
	public void setErrorCategory(Integer errorCategory) {
		this.errorCategory = errorCategory;
	}

	public Integer getSessionId()
	{
		return this.sessionId;
	}

	public void setSessionId(Integer sessionId)
	{
		this.sessionId = sessionId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getDetails()
	{
		return this.details;
	}

	public void setDetails(String details)
	{
		this.details = details;
	}

	/**
	 * @return the dateTime
	 */
	public Date getDateTime() {
		return dateTime;
	}

	/**
	 * @param now the dateTime to set
	 */
	public void setDateTime(Date now) {
		this.dateTime = now;
	}
}