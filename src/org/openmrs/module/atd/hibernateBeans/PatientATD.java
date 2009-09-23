package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;

import org.openmrs.module.dss.hibernateBeans.Rule;

/**
 * Holds information to store in the atd_patient_atd table
 * 
 * @author Tammy Dugan
 */
public class PatientATD implements java.io.Serializable {

	// Fields
	private Integer atdId = null;
	private Integer patientId = null;
	private Integer formId = null;
	private Integer fieldId = null;
	private String text = null;
	private Integer formInstanceId = null;
	private Date creationTime = null;
	private Rule rule = null;
	private Integer encounterId = null;
	
	// Constructors

	/** default constructor */
	public PatientATD() {
	}

	/**
	 * @return the atdId
	 */
	public Integer getAtdId()
	{
		return this.atdId;
	}

	/**
	 * @param atdId the atdId to set
	 */
	public void setAtdId(Integer atdId)
	{
		this.atdId = atdId;
	}

	/**
	 * @return the patientId
	 */
	public Integer getPatientId()
	{
		return this.patientId;
	}

	/**
	 * @param patientId the patientId to set
	 */
	public void setPatientId(Integer patientId)
	{
		this.patientId = patientId;
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
	 * @return the fieldId
	 */
	public Integer getFieldId()
	{
		return this.fieldId;
	}

	/**
	 * @param fieldId the fieldId to set
	 */
	public void setFieldId(Integer fieldId)
	{
		this.fieldId = fieldId;
	}

	/**
	 * @return the text
	 */
	public String getText()
	{
		return this.text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * @return the rule
	 */
	public Rule getRule()
	{
		return this.rule;
	}

	/**
	 * @param rule the rule to set
	 */
	public void setRule(Rule rule)
	{
		this.rule = rule;
	}

	/**
	 * @return the formInstanceId
	 */
	public Integer getFormInstanceId()
	{
		return this.formInstanceId;
	}

	/**
	 * @param formInstanceId the formInstanceId to set
	 */
	public void setFormInstanceId(Integer formInstanceId)
	{
		this.formInstanceId = formInstanceId;
	}

	/**
	 * @return the creationTime
	 */
	public Date getCreationTime()
	{
		return this.creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(Date creationTime)
	{
		this.creationTime = creationTime;
	}

	public Integer getEncounterId()
	{
		return this.encounterId;
	}

	public void setEncounterId(Integer encounterId)
	{
		this.encounterId = encounterId;
	}
}