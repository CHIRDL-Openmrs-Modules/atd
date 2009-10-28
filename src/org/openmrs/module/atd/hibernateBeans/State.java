package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;

import org.openmrs.User;

/**
 * Holds information to store in the state table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class State implements java.io.Serializable {

	// Fields
	private Integer stateId=null;
	private String formName=null;
	private String name = null;
	private String description = null;
	private StateAction action = null;
	private Date dateChanged = null;
	private User changedBy=null;
	private Date dateCreated=null;
	private User creator = null;
	
	// Constructors

	/** default constructor */
	public State() {
	}

	public Integer getStateId()
	{
		return this.stateId;
	}

	public void setStateId(Integer stateId)
	{
		this.stateId = stateId;
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

	public StateAction getAction()
	{
		return this.action;
	}

	public void setAction(StateAction action)
	{
		this.action = action;
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

	public String getFormName()
	{
		return this.formName;
	}

	public void setFormName(String formName)
	{
		this.formName = formName;
	}

}