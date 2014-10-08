package org.openmrs.module.atd.util;

/**
 * 
 * @author wang417
 * A view of form attribute value information
 */
public class FormAttributeValueDescriptor {
	private String formName;
	private String locationName;
	private String locationTagName;
	private String attributeName;
	private String attributeValue;
	
	public FormAttributeValueDescriptor(String formName, String locationName, String locationTagName, String attributeName, String attributeValue) {
		this.formName = formName;
		this.locationName = locationName;
		this.locationTagName = locationTagName;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}
	
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public String getLocationTagName() {
		return locationTagName;
	}
	public void setLocationTagName(String locationTagName) {
		this.locationTagName = locationTagName;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + ((attributeValue == null) ? 0 : attributeValue.hashCode());
		result = prime * result + ((formName == null) ? 0 : formName.hashCode());
		result = prime * result + ((locationName == null) ? 0 : locationName.hashCode());
		result = prime * result + ((locationTagName == null) ? 0 : locationTagName.hashCode());
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
		FormAttributeValueDescriptor other = (FormAttributeValueDescriptor) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (attributeValue == null) {
			if (other.attributeValue != null)
				return false;
		} else if (!attributeValue.equals(other.attributeValue))
			return false;
		if (formName == null) {
			if (other.formName != null)
				return false;
		} else if (!formName.equals(other.formName))
			return false;
		if (locationName == null) {
			if (other.locationName != null)
				return false;
		} else if (!locationName.equals(other.locationName))
			return false;
		if (locationTagName == null) {
			if (other.locationTagName != null)
				return false;
		} else if (!locationTagName.equals(other.locationTagName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FormAttributeValueDescriptor [formName=" + formName + ", locationName=" + locationName + ", locationTagName=" + locationTagName + ", attributeName=" + attributeName + ", attributeValue=" + attributeValue + "]";
	}
	
	

	
	
}
