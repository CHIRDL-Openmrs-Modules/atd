package org.openmrs.module.atd;

import org.openmrs.module.chirdlutilbackports.hibernateBeans.FormAttributeValue;


public class LocationTagPrinterConfig {
	
	String locationTagName;
	Integer locationTagId;
	FormAttributeValue defaultPrinter;
	FormAttributeValue alternatePrinter;
	FormAttributeValue useAlternatePrinter;
	
	public LocationTagPrinterConfig(String locationTagName, Integer locationTagId) {
		this.locationTagName = locationTagName;
		this.locationTagId = locationTagId;
	}
    
    /**
     * @return the locationTagName
     */
    public String getLocationTagName() {
    	return locationTagName;
    }
	
    /**
     * @param locationTagName the locationTagName to set
     */
    public void setLocationTagName(String locationTagName) {
    	this.locationTagName = locationTagName;
    }
	
    /**
     * @return the locationTagId
     */
    public Integer getLocationTagId() {
    	return locationTagId;
    }
	
    /**
     * @param locationTagId the locationTagId to set
     */
    public void setLocationTagId(Integer locationTagId) {
    	this.locationTagId = locationTagId;
    }

	/**
     * @return the defaultPrinter
     */
    public FormAttributeValue getDefaultPrinter() {
    	return defaultPrinter;
    }
	
    /**
     * @param defaultPrinter the defaultPrinter to set
     */
    public void setDefaultPrinter(FormAttributeValue defaultPrinter) {
    	this.defaultPrinter = defaultPrinter;
    }
	
    /**
     * @return the alternatePrinter
     */
    public FormAttributeValue getAlternatePrinter() {
    	return alternatePrinter;
    }
	
    /**
     * @param alternatePrinter the alternatePrinter to set
     */
    public void setAlternatePrinter(FormAttributeValue alternatePrinter) {
    	this.alternatePrinter = alternatePrinter;
    }
	
    /**
     * @return the useAlternatePrinter
     */
    public FormAttributeValue getUseAlternatePrinter() {
    	return useAlternatePrinter;
    }
	
    /**
     * @param useAlternatePrinter the useAlternatePrinter to set
     */
    public void setUseAlternatePrinter(FormAttributeValue useAlternatePrinter) {
    	this.useAlternatePrinter = useAlternatePrinter;
    }
}
