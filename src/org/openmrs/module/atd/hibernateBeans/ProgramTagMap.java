package org.openmrs.module.atd.hibernateBeans;


/**
 * Holds information to store in the state table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class ProgramTagMap implements java.io.Serializable {

	// Fields
	private Integer programTagMapId = null;
	private Integer programId = null;
	private Program program = null;
	private Integer locationId = null;
	private Integer locationTagId = null;
	
	// Constructors

	/** default constructor */
	public ProgramTagMap() {
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

	public Integer getProgramTagMapId()
	{
		return this.programTagMapId;
	}

	public void setProgramTagMapId(Integer programTagMapId)
	{
		this.programTagMapId = programTagMapId;
	}

	public Integer getProgramId()
	{
		return this.programId;
	}

	public void setProgramId(Integer programId)
	{
		this.programId = programId;
	}

	public Program getProgram()
	{
		return this.program;
	}

	public void setProgram(Program program)
	{
		this.program = program;
	}
}