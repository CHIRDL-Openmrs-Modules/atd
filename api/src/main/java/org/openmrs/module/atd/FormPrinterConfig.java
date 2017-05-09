package org.openmrs.module.atd;

import java.util.ArrayList;
import java.util.List;


public class FormPrinterConfig {

	Integer formId;
	List<LocationTagPrinterConfig> locationTagPrinterConfigs = new ArrayList<LocationTagPrinterConfig>();
	
	public FormPrinterConfig(Integer formId) {
		this.formId = formId;
	}
	
    /**
     * @return the formId
     */
    public Integer getFormId() {
    	return formId;
    }

    /**
     * @param formId the formId to set
     */
    public void setFormId(Integer formId) {
    	this.formId = formId;
    }

    /**
     * @return the locationTagPrinterConfigs
     */
    public List<LocationTagPrinterConfig> getLocationTagPrinterConfigs() {
    	return locationTagPrinterConfigs;
    }
	
    /**
     * @param locationTagPrinterConfigs the locationTagPrinterConfigs to set
     */
    public void setLocationTagPrinterConfigs(List<LocationTagPrinterConfig> locationTagPrinterConfigs) {
    	this.locationTagPrinterConfigs = locationTagPrinterConfigs;
    }
}
