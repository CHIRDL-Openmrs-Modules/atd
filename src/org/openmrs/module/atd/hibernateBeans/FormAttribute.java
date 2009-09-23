package org.openmrs.module.atd.hibernateBeans;

/**
 * Holds information to store in the atd_form_attribute table
 * 
 * @author Tammy Dugan
 */
public class FormAttribute implements java.io.Serializable {

	// Fields
	private Integer formAttributeId = null;
	private String name = null;
	private String description = null;


	// Constructors

	/** default constructor */
	public FormAttribute() {
	}

	

	/**
	 * @return the formAttributeId
	 */
	public Integer getFormAttributeId()
	{
		return this.formAttributeId;
	}



	/**
	 * @param formAttributeId the formAttributeId to set
	 */
	public void setFormAttributeId(Integer formAttributeId)
	{
		this.formAttributeId = formAttributeId;
	}



	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return this.description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
}