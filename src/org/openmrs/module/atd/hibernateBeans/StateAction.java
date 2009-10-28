package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;

import org.openmrs.User;

/**
 * Holds information to store in the atd_state_action table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class StateAction implements java.io.Serializable {

	// Fields

	private Integer stateActionId=null;
	private String actionName = null;
	private String actionDescription = null;
	private Date dateChanged = null;
	private User changedBy=null;
	private Date dateCreated=null;
	private User creator = null;
	private String actionClass = null;

	// Constructors

	/** default constructor */
	public StateAction() {
	}

	/**
	 * @return the stateActionId
	 */
	public Integer getStateActionId()
	{
		return this.stateActionId;
	}

	/**
	 * @param stateActionId the stateActionId to set
	 */
	public void setStateActionId(Integer stateActionId)
	{
		this.stateActionId = stateActionId;
	}

	/**
	 * @return the actionName
	 */
	public String getActionName()
	{
		return this.actionName;
	}

	/**
	 * @param actionName the actionName to set
	 */
	public void setActionName(String actionName)
	{
		this.actionName = actionName;
	}

	/**
	 * @return the actionDescription
	 */
	public String getActionDescription()
	{
		return this.actionDescription;
	}

	/**
	 * @param actionDescription the actionDescription to set
	 */
	public void setActionDescription(String actionDescription)
	{
		this.actionDescription = actionDescription;
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

	public String getActionClass() {
		return actionClass;
	}

	public void setActionClass(String actionClass) {
		this.actionClass = actionClass;
	}
}