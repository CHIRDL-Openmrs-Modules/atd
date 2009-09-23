package org.openmrs.module.atd.hibernateBeans;


/**
 * Holds information to store in the form_instance table
 * 
 * @author Tammy Dugan
 */
public class FormInstance implements java.io.Serializable {

	// Fields
	private Integer formInstanceId = null;
	private Integer formId = null;

	// Constructors

	/** default constructor */
	public FormInstance() {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obs)
	{
		if (obs == null || !(obs instanceof FormInstance))
		{
			return false;
		}

		FormInstance formInstance = (FormInstance) obs;

		if (this.formInstanceId.equals(formInstance.getFormInstanceId()))
		{
			if (this.formId.equals(formInstance.getFormId()))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		Integer formInstanceHashValue = this.formInstanceId;
		Integer formHashValue = this.formId;
		int hash = 7;
		
		if(this.formInstanceId == null)
		{
			formInstanceHashValue = 0;
		}
		
		if(this.formId == null)
		{
			formHashValue = 0;
		}
		
		hash = 31 * hash + formInstanceHashValue;
		hash = 31 * hash + formHashValue;
		return hash;
	}
}