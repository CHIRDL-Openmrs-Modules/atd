package org.openmrs.module.atd.hibernateBeans;


/**
 * Holds information to store in the state table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class LocationTagMap implements java.io.Serializable {

	// Fields
	private Integer locationId = null;
	private Integer locationTagId = null;
	
	// Constructors

	/** default constructor */
	public LocationTagMap() {
	}

	public Integer getLocationId()
	{
		return this.locationId;
	}

	public void setLocationId(Integer locationId)
	{
		this.locationId = locationId;
	}

	public Integer getLocationTagId()
	{
		return this.locationTagId;
	}

	public void setLocationTagId(Integer locationTagId)
	{
		this.locationTagId = locationTagId;
	}
}