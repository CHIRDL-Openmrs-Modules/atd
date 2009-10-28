package org.openmrs.module.atd.hibernateBeans;

import java.util.Date;
import java.util.HashMap;

import org.openmrs.Patient;

/**
 * Holds information to store in the atd_patient_state table
 * 
 * @author Tammy Dugan
 * @version 1.0
 */
public class PatientState implements java.io.Serializable
{
	// Fields
	private Integer patientStateId=null;
    private Integer patientId=null;
    private State state = null;
	private Date startTime = null;
	private Date endTime = null;
	private Integer sessionId = null;
	private Integer formInstanceId = null;
	private Integer formId = null;
	private Patient patient = null;
	private HashMap<String,Object> parameters = null;
	private Boolean retired = false;
	private Integer locationTagId = null;
	private Integer locationId = null;
	private Date dateRetired = null;
	
	/**
	 * @return the patientStateId
	 */
	public Integer getPatientStateId()
	{
		return this.patientStateId;
	}
	/**
	 * @param patientStateId the patientStateId to set
	 */
	public void setPatientStateId(Integer patientStateId)
	{
		this.patientStateId = patientStateId;
	}

	public State getState()
	{
		return this.state;
	}
	public void setState(State state)
	{
		this.state = state;
	}
	/**
	 * @return the startTime
	 */
	public Date getStartTime()
	{
		return this.startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}
	/**
	 * @return the endTime
	 */
	public Date getEndTime()
	{
		return this.endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime)
	{
		this.endTime = endTime;
	}
	/**
	 * @return the sessionId
	 */
	public Integer getSessionId()
	{
		return this.sessionId;
	}
	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(Integer sessionId)
	{
		this.sessionId = sessionId;
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
	 * @return the patient
	 */
	public Patient getPatient()
	{
		return this.patient;
	}
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient)
	{
		this.patient = patient;
	}
	/**
	 * @return the patientId
	 */
	public Integer getPatientId()
	{
		return this.patientId;
	}
	
	public FormInstance getFormInstance(){
		
		if (this.formId != null && this.formInstanceId != null)
		{
			return new FormInstance(this.locationId,this.formId,this.formInstanceId);
		}
		return null;
	}
	/**
	 * @param patientId the patientId to set
	 */
	public void setPatientId(Integer patientId)
	{
		this.patientId = patientId;
	}
	public HashMap<String, Object> getParameters()
	{
		return this.parameters;
	}
	public void setParameters(HashMap<String, Object> parameters)
	{
		this.parameters = parameters;
	}
	public Boolean getRetired()
	{
		return this.retired;
	}
	public void setRetired(Boolean retired)
	{
		this.retired = retired;
	}
	public Date getDateRetired()
	{
		return this.dateRetired;
	}
	public void setDateRetired(Date dateRetired)
	{
		this.dateRetired = dateRetired;
	}
	public Integer getLocationTagId()
	{
		return this.locationTagId;
	}
	public void setLocationTagId(Integer locationTagId)
	{
		this.locationTagId = locationTagId;
	}
	
	public void setFormInstance(FormInstance formInstance){
		this.formId = formInstance.getFormId();
		this.formInstanceId = formInstance.getFormInstanceId();
		this.locationId = formInstance.getLocationId();
	}
	public Integer getLocationId()
	{
		return this.locationId;
	}
	public void setLocationId(Integer locationId)
	{
		this.locationId = locationId;
	}
}