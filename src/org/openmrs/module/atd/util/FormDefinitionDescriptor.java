package org.openmrs.module.atd.util;

/**
 * 
 * @author wang417
 * A view of the form definition information
 */
public class FormDefinitionDescriptor {
	private String formName;
	private String formDescription;
	private String fieldName;
	private String fieldType;
	private String conceptName;
	private String defaultValue;
	private int fieldNumber;
	private String parentFieldName;
	
	
	
	
	public FormDefinitionDescriptor(String formName, String formDescription, String fieldName, String fieldType, String conceptName, String defaultValue, int fieldNumber, String parentFieldName) {
		super();
		this.formName = formName;
		this.formDescription = formDescription;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.conceptName = conceptName;
		this.defaultValue = defaultValue;
		this.fieldNumber = fieldNumber;
		this.parentFieldName = parentFieldName;
	}
	
	
	public FormDefinitionDescriptor() {
		
	}


	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public String getConceptName() {
		return conceptName;
	}
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public int getFieldNumber() {
		return fieldNumber;
	}
	public void setFieldNumber(int fieldNumber) {
		this.fieldNumber = fieldNumber;
	}
	public String getParentFieldName() {
		return parentFieldName;
	}
	public void setParentFieldName(String parentFieldName) {
		this.parentFieldName = parentFieldName;
	}
	public String getFormDescription() {
		return formDescription;
	}
	public void setFormDescription(String formDescription) {
		this.formDescription = formDescription;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptName == null) ? 0 : conceptName.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + fieldNumber;
		result = prime * result + ((fieldType == null) ? 0 : fieldType.hashCode());
		result = prime * result + ((formDescription == null) ? 0 : formDescription.hashCode());
		result = prime * result + ((formName == null) ? 0 : formName.hashCode());
		result = prime * result + ((parentFieldName == null) ? 0 : parentFieldName.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormDefinitionDescriptor other = (FormDefinitionDescriptor) obj;
		if (conceptName == null) {
			if (other.conceptName != null)
				return false;
		} else if (!conceptName.equals(other.conceptName))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		if (fieldNumber != other.fieldNumber)
			return false;
		if (fieldType == null) {
			if (other.fieldType != null)
				return false;
		} else if (!fieldType.equals(other.fieldType))
			return false;
		if (formDescription == null) {
			if (other.formDescription != null)
				return false;
		} else if (!formDescription.equals(other.formDescription))
			return false;
		if (formName == null) {
			if (other.formName != null)
				return false;
		} else if (!formName.equals(other.formName))
			return false;
		if (parentFieldName == null) {
			if (other.parentFieldName != null)
				return false;
		} else if (!parentFieldName.equals(other.parentFieldName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "FormDefinitionDescriptor [formName=" + formName + ", formDescription=" + formDescription + ", fieldName=" + fieldName + ", fieldType=" + fieldType + ", conceptName=" + conceptName + ", defaultValue=" + defaultValue + ", fieldNumber=" + fieldNumber + ", parentFieldName=" + parentFieldName + "]";
	}
	
	
	
}
