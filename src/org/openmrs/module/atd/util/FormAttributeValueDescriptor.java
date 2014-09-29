package org.openmrs.module.atd.util;

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
	
	

	
	
}
