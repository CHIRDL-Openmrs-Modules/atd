package org.openmrs.module.atd.hibernateBeans;


/**
 * Holds information to store in the form_instance table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class Session implements java.io.Serializable {

	// Fields
	private Integer sessionId = null;
	private Integer encounterId = null;

	// Constructors

	/**
	 * @return the encounterId
	 */
	public Integer getEncounterId()
	{
		return this.encounterId;
	}

	/**
	 * @param encounterId the encounterId to set
	 */
	public void setEncounterId(Integer encounterId)
	{
		this.encounterId = encounterId;
	}

	/** default constructor */
	public Session() {
	}

	/**
	 * @return the sessionId
	 */
	public Integer getSessionId()
	{
		return this.sessionId;
	}

	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(Integer sessionId)
	{
		this.sessionId = sessionId;
	}
	
}