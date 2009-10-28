package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;

import org.openmrs.User;

/**
 * Holds information to store in the state table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class Program implements java.io.Serializable {

	// Fields
	private Integer programId=null;
	private String name = null;
	private String description = null;
	private String version = null;
	private Date dateChanged = null;
	private User changedBy=null;
	private Date dateCreated=null;
	private User creator = null;
	private State startState = null;
	private State endState = null;
	
	// Constructors

	/** default constructor */
	public Program() {
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Integer getProgramId()
	{
		return this.programId;
	}

	public void setProgramId(Integer programId)
	{
		this.programId = programId;
	}

	public String getVersion()
	{
		return this.version;
	}

	public void setVersion(String version)
	{
		this.version = version;
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

	public State getStartState()
	{
		return this.startState;
	}

	public void setStartState(State startState)
	{
		this.startState = startState;
	}

	public State getEndState()
	{
		return this.endState;
	}

	public void setEndState(State endState)
	{
		this.endState = endState;
	}
}