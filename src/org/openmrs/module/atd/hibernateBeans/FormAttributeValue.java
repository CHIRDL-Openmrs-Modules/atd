package org.openmrs.module.atd.hibernateBeans;

/**
 * Holds information to store in the atd_form_attribute_value table
 * 
 * @author Tammy Dugan
 */
public class FormAttributeValue implements java.io.Serializable {

	// Fields
	private Integer formAttributeValueId = null;
	private Integer formId = null;
	private Integer formAttributeId = null;
	private String value = null;

	// Constructors

	/** default constructor */
	public FormAttributeValue() {
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
	 * @return the formId
	 */
	public Integer getFormId()
	{
		return this.formId;
	}

	/**
	 * @param formId the formId to set
	 */
	public void setFormId(Integer formId)
	{
		this.formId = formId;
	}

	

	/**
	 * @return the formAttributeValueId
	 */
	public Integer getFormAttributeValueId()
	{
		return this.formAttributeValueId;
	}

	/**
	 * @param formAttributeValueId the formAttributeValueId to set
	 */
	public void setFormAttributeValueId(Integer formAttributeValueId)
	{
		this.formAttributeValueId = formAttributeValueId;
	}

	/**
	 * @return the value
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

}