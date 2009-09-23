package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;

import org.openmrs.User;

/**
 * Holds information to store in the state table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class StateMapping implements java.io.Serializable {

	// Fields
	private Integer programId = null;
	private Integer stateMappingId=null;
	private State initialState=null;
	private State nextState=null;
	private Date dateChanged = null;
	private User changedBy=null;
	private Date dateCreated=null;
	private User creator = null;
	
	// Constructors

	/** default constructor */
	public StateMapping() {
	}

	/**
	 * @return the stateMappingId
	 */
	public Integer getStateMappingId()
	{
		return this.stateMappingId;
	}

	/**
	 * @param stateMappingId the stateMappingId to set
	 */
	public void setStateMappingId(Integer stateMappingId)
	{
		this.stateMappingId = stateMappingId;
	}

	public State getInitialState()
	{
		return this.initialState;
	}

	public void setInitialState(State initialState)
	{
		this.initialState = initialState;
	}

	public State getNextState()
	{
		return this.nextState;
	}

	public void setNextState(State nextState)
	{
		this.nextState = nextState;
	}

	public Integer getProgramId()
	{
		return this.programId;
	}

	public void setProgramId(Integer programId)
	{
		this.programId = programId;
	}
	public Date getDateChanged()
	{
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged)
	{
		this.dateChanged = dateChanged;
	}

	public User getChangedBy()
	{
		return this.changedBy;
	}

	public void setChangedBy(User changedBy)
	{
		this.changedBy = changedBy;
	}

	public Date getDateCreated()
	{
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public User getCreator()
	{
		return this.creator;
	}

	public void setCreator(User creator)
	{
		this.creator = creator;
	}
}